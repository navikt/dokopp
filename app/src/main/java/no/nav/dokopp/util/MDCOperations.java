package no.nav.dokopp.util;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.security.SecureRandom;

import static java.lang.System.currentTimeMillis;

@Slf4j
public final class MDCOperations {
	public static final String MDC_CALL_ID = "callId";
	public static final String MDC_USER_ID = "userId";
	public static final String MDC_CONSUMER_ID = "consumerId";
	private static final SecureRandom RANDOM = new SecureRandom();

	private MDCOperations() {
	}

	public static String generateCallId() {
		int randomNr = RANDOM.nextInt(2147483647);
		long systemTime = currentTimeMillis();
		return "CallId_" + systemTime + "_" + randomNr;
	}

	public static String getFromMDC(String key) {
		String value = MDC.get(key);
		log.debug("Getting key: " + key + " from MDC with value: " + value);
		return value;
	}

}
