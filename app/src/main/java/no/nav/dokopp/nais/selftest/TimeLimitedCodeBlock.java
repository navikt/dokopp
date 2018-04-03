package no.nav.dokopp.nais.selftest;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by T133804 on 15.08.2017.
 */
public final class TimeLimitedCodeBlock {
	
	private TimeLimitedCodeBlock() {
	}
	
	public static <T> T runWithTimeout(Callable<T> callable, long timeout, TimeUnit timeUnit, String methodName) {
		final ExecutorService executor = Executors.newSingleThreadExecutor();
		final Future<T> future = executor.submit(callable);
		executor.shutdown(); // This does not cancel the already-scheduled task.
		try {
			return future.get(timeout, timeUnit);
		} catch (TimeoutException e) {
			future.cancel(true);
			throw new ApplicationNotReadyException(methodName + " timed out", e.getCause());
		} catch (InterruptedException |ExecutionException e) {
			throw new ApplicationNotReadyException(methodName + " failed with message:" + e.getMessage() , e.getCause());
		}
	}
}
