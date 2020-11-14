package hu.gaszabo.product.service.infrastructure.util;

import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public final class RetryUtil {

	private static final List<Class<?>> RETRY_EXCEPTIONS = //
			List.of( //
					"javax.persistence.OptimisticLockException", //
					"org.eclipse.persistence.exceptions.OptimisticLockException", //
					"org.springframework.dao.OptimisticLockingFailureException") //
					.stream() //
					.map(RetryUtil::makeClass) //
					.filter(Optional::isPresent) //
					.map(Optional::get) //
					.collect(toList());

	public RetryUtil() {
		throw new UnsupportedOperationException("Class can't be instatiated");
	}

	private static Optional<Class<?>> makeClass(final String fullyQualifiedClassName) {
		try {
			return Optional.of(Class.forName(fullyQualifiedClassName));
		} catch (ClassNotFoundException e) {
			return Optional.empty();
		}
	}

	public static void retry(final Runnable action, final Runnable logWarningMessage, final int attempts) {
		int attemptsLeft = attempts;
		while (true) {
			try {
				action.run();
				break;
			} catch (RuntimeException e) {
				if (attemptsLeft > 0 && shouldAttempt(e)) {
					logWarningMessage.run();
					attemptsLeft--;
				} else {
					throw e;
				}
			}
		}
	}

	public static <T> T retry(final Supplier<T> action, final Runnable logWarningMessage, final int attempts) {
		T result = null;
		int attemptsLeft = attempts;
		while (true) {
			try {
				result = action.get();
				break;
			} catch (RuntimeException e) {
				if (attemptsLeft > 0 && shouldAttempt(e)) {
					logWarningMessage.run();
					attemptsLeft--;
				} else {
					throw e;
				}
			}
		}
		return result;
	}

	private static boolean shouldAttempt(final Throwable e) {
		return getExceptionChain(e) //
				.stream() //
				.anyMatch(t -> RETRY_EXCEPTIONS.stream().anyMatch(re -> re.isAssignableFrom(t.getClass())));
	}

	private static List<Throwable> getExceptionChain(Throwable t) {
		List<Throwable> exceptions = new ArrayList<>();
		while (t != null) {
			exceptions.add(t);
			t = t.getCause();
		}
		return unmodifiableList(exceptions);
	}

}