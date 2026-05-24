package br.com.fiap.ford.pulsoretencao.autenticacao.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class AuthenticationRateLimiter {

	private static final int MAX_FAILURES = 5;
	private static final Duration WINDOW = Duration.ofMinutes(10);

	private final ConcurrentMap<String, AttemptWindow> attempts = new ConcurrentHashMap<>();

	public String key(String clientId, String remoteAddress) {
		String normalizedClientId = StringUtils.hasText(clientId)
				? clientId.trim().toLowerCase(Locale.ROOT)
				: "unknown-client";
		String normalizedRemoteAddress = StringUtils.hasText(remoteAddress)
				? remoteAddress.trim()
				: "unknown-address";
		return normalizedClientId + "@" + normalizedRemoteAddress;
	}

	public boolean isAllowed(String key) {
		AttemptWindow window = attempts.get(key);
		if (window == null) {
			return true;
		}
		if (window.isExpired()) {
			attempts.remove(key, window);
			return true;
		}
		return window.failures() < MAX_FAILURES;
	}

	public void recordFailure(String key) {
		Instant expiresAt = Instant.now().plus(WINDOW);
		attempts.compute(key, (ignored, current) -> {
			if (current == null || current.isExpired()) {
				return new AttemptWindow(1, expiresAt);
			}
			return new AttemptWindow(current.failures() + 1, current.expiresAt());
		});
	}

	public void reset(String key) {
		attempts.remove(key);
	}

	private record AttemptWindow(int failures, Instant expiresAt) {

		private boolean isExpired() {
			return Instant.now().isAfter(expiresAt);
		}
	}
}
