package authentication.ports;

import java.util.concurrent.CompletableFuture;

public interface MetricsService {
	public CompletableFuture<Void> registerForHealthcheckMonitoring(String metricsServiceAddress, String selfAddress);
}
