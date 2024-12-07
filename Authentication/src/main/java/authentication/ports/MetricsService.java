package authentication.ports;

import java.util.concurrent.CompletableFuture;

public interface MetricsService {
	public CompletableFuture<Void> incrementCounter(String counterId, long amount);

	public CompletableFuture<Void> registerForHealthcheckMonitoring(String selfAddress);
}
