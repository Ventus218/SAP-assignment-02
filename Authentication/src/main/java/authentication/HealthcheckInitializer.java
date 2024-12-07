package authentication;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.*;
import jakarta.annotation.PostConstruct;
import authentication.ports.MetricsService;

@Component
public class HealthcheckInitializer {

	@Autowired
	MetricsService metricsService;

	@Value("${metrics.service.address}")
	private String metricsServiceAddress;

	@Value("${authentication.service.address}")
	private String selfServiceAddress;

	@PostConstruct
	public void startMonitoring() {
		if (metricsServiceAddress.isBlank()) {
			throw new IllegalArgumentException("metrics.service.address can't be blank");
		}
		if (selfServiceAddress.isBlank()) {
			throw new IllegalArgumentException("authentication.service.address can't be blank");
		}
		metricsService.registerForHealthcheckMonitoring(metricsServiceAddress, selfServiceAddress);
	}
}
