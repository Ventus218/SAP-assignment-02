package authentication.adapters;

import java.util.concurrent.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.web.client.RestTemplateBuilder;
import jakarta.annotation.PostConstruct;
import authentication.ports.MetricsService;

@Service
public class MetricsServiceAdapter implements MetricsService {

	@Value("${metrics.service.address}")
	private String metricsServiceAddress;

	private final RestClient restClient;
	private final Executor executor;

	private static final long RESCHEDULE_DELAY_MILLIS = 5000;

	@PostConstruct
	public void validateInjectedValues() {
		if (metricsServiceAddress.isBlank()) {
			throw new IllegalArgumentException("metrics.service.address can't be blank");
		}
	}

	public MetricsServiceAdapter(RestClient.Builder restClientBuilder) {
		this.restClient = restClientBuilder.build();
		this.executor = Executors.newVirtualThreadPerTaskExecutor();
	}

	public CompletableFuture<Void> incrementCounter(String counterId, long amount) {
		return CompletableFuture.runAsync(() -> {
			try {
				var res = restClient.post().uri(baseUrl() + "/metrics/counters/" + counterId)
						.body(new IncrementCounterDTO(amount)).retrieve().toBodilessEntity();
			} catch (Exception e) {
				System.out.println("Something went wrong while updating metrics counter: " + e);
			}
		}, executor);
	}

	public CompletableFuture<Void> registerForHealthcheckMonitoring(String selfAddress) {
		return CompletableFuture.runAsync(() -> {
			var res = restClient.post().uri(baseUrl() + "/metrics/endpoints").body(new RegisterDTO(selfAddress))
					.retrieve().toBodilessEntity();
			if (!res.getStatusCode().is2xxSuccessful()) {
				throw new RuntimeException();
			}
		}, executor).handleAsync((s, t) -> (t != null) ? rescheduleAfterMillis(selfAddress) : null);
	}

	private Void rescheduleAfterMillis(String selfAddress) {
		try {
			Thread.sleep(RESCHEDULE_DELAY_MILLIS);
		} catch (InterruptedException ignored) {
		}
		registerForHealthcheckMonitoring(selfAddress);
		return null;
	}

	private String baseUrl() {
		return "http://" + metricsServiceAddress;
	}

	public record IncrementCounterDTO(long value) {
	}

	public record RegisterDTO(String value) {
	}
}
