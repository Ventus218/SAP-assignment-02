package authentication.adapters;

import java.util.concurrent.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.boot.web.client.RestTemplateBuilder;
import authentication.ports.MetricsService;

@Service
public class MetricsServiceAdapter implements MetricsService {

	private final RestClient restClient;
	private final Executor executor;

	private static final long RESCHEDULE_DELAY_MILLIS = 5000;

	public MetricsServiceAdapter(RestClient.Builder restClientBuilder) {
		this.restClient = restClientBuilder.build();
		this.executor = Executors.newVirtualThreadPerTaskExecutor();
	}

	public CompletableFuture<Void> registerForHealthcheckMonitoring(String metricsServiceAddress, String selfAddress) {
		return CompletableFuture.runAsync(() -> {
			var res = restClient.post().uri("http://" + metricsServiceAddress + "/metrics/endpoints")
					.body(new RegisterDTO(selfAddress)).retrieve().toBodilessEntity();
			if (!res.getStatusCode().is2xxSuccessful()) {
				throw new RuntimeException();
			}
		}, executor)
				.handleAsync((s, t) -> (t != null) ? rescheduleAfterMillis(metricsServiceAddress, selfAddress) : null);
	}

	private Void rescheduleAfterMillis(String metricsServiceAddress, String selfAddress) {
		try {
			Thread.sleep(RESCHEDULE_DELAY_MILLIS);
		} catch (InterruptedException ignored) {
		}
		registerForHealthcheckMonitoring(metricsServiceAddress, selfAddress);
		return null;
	}

	public record RegisterDTO(String value) {
	}
}
