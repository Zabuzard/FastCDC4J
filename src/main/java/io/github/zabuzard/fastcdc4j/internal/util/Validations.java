package io.github.zabuzard.fastcdc4j.internal.util;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Utility class providing validation methods.
 *
 * @author Daniel Tischner {@literal <zabuza.dev@gmail.com>}
 */
public final class Validations {
	/**
	 * Throws {@link IllegalArgumentException} if the given value is not positive, i.e. when it is negative.
	 *
	 * @param value     The value to test
	 * @param valueName The name of the value, used to describe it in the exception reason, not null
	 *
	 * @return The given value for convenient chaining
	 */
	@SuppressWarnings("OverloadedMethodsWithSameNumberOfParameters")
	public static long requirePositive(final long value, final String valueName) {
		Objects.requireNonNull(valueName);
		Validations.require(value >= 0, valueName + " must be positive.");
		return value;
	}

	/**
	 * Throws {@link IllegalArgumentException} if the given value is not positive or zero, i.e. when it is negative or
	 * zero.
	 *
	 * @param value     The value to test
	 * @param valueName The name of the value, used to describe it in the exception reason, not null
	 *
	 * @return The given value for convenient chaining
	 */
	@SuppressWarnings("OverloadedMethodsWithSameNumberOfParameters")
	public static long requirePositiveNonZero(final long value, final String valueName) {
		Objects.requireNonNull(valueName);
		Validations.require(value > 0, valueName + " must be positive and not zero.");
		return value;
	}

	/**
	 * Throws {@link IllegalArgumentException} if the given value is not positive, i.e. when it is negative.
	 *
	 * @param value     The value to test
	 * @param valueName The name of the value, used to describe it in the exception reason, not null
	 *
	 * @return The given value for convenient chaining
	 */
	@SuppressWarnings("OverloadedMethodsWithSameNumberOfParameters")
	public static int requirePositive(final int value, final String valueName) {
		Objects.requireNonNull(valueName);
		Validations.require(value >= 0, valueName + " must be positive.");
		return value;
	}

	/**
	 * Throws {@link IllegalArgumentException} if the given value is not positive or zero, i.e. when it is negative or
	 * zero.
	 *
	 * @param value     The value to test
	 * @param valueName The name of the value, used to describe it in the exception reason, not null
	 *
	 * @return The given value for convenient chaining
	 */
	@SuppressWarnings("OverloadedMethodsWithSameNumberOfParameters")
	public static int requirePositiveNonZero(final int value, final String valueName) {
		Objects.requireNonNull(valueName);
		Validations.require(value > 0, valueName + " must be positive and not zero.");
		return value;
	}

	/**
	 * Throws {@link IllegalArgumentException} if the runnable throws the given expected exception.
	 *
	 * @param expectedException The exception for which to throw if it happens, not null
	 * @param runnable          The runnable to execute, not null
	 * @param message           The message to provide in the exception as reason, not null
	 * @param <E>               The type of the exception to expect
	 */
	public static <E extends RuntimeException> void requireNotThrow(final Class<E> expectedException,
			final Runnable runnable, final String message) {
		Objects.requireNonNull(expectedException);
		Objects.requireNonNull(runnable);
		Objects.requireNonNull(message);
		try {
			runnable.run();
		} catch (final RuntimeException e) {
			if (expectedException.isInstance(e)) {
				throw new IllegalArgumentException(message, e);
			}
		}
	}

	/**
	 * Throws {@link IllegalArgumentException} if the given predicate resolves to true.
	 *
	 * @param predicate The predicate to test, not null
	 * @param object    The object to test the predicate against
	 * @param message   The message to provide in the exception, not null
	 * @param <T>       The type of the object to test
	 *
	 * @return The given value for convenient chaining
	 */
	public static <T> T require(final Predicate<? super T> predicate, final T object, final String message) {
		Objects.requireNonNull(predicate);
		Objects.requireNonNull(message);
		Validations.require(predicate.test(object), message);
		return object;
	}

	/**
	 * Throws {@link IllegalArgumentException} if the given predicate resolves to false.
	 *
	 * @param predicate The predicate to test
	 * @param message   The message to provide in the exception, not null
	 */
	@SuppressWarnings("BooleanParameter")
	public static void require(final boolean predicate, final String message) {
		Objects.requireNonNull(message);
		Validations.require(predicate, IllegalArgumentException::new, message);
	}

	/**
	 * Throws the given exception if the predicate resolves to false.
	 *
	 * @param predicate         The predicate to test, not null
	 * @param object            The object to test the predicate against
	 * @param exceptionSupplier The supplier to use for getting the exception to throw, not null
	 * @param message           The message to provide in the exception, not null
	 * @param <T>               The type of the object to test
	 * @param <E>               The type of the exception to throw
	 *
	 * @return The given value for convenient chaining
	 */
	public static <T, E extends RuntimeException> T require(final Predicate<? super T> predicate, final T object,
			final Function<? super String, E> exceptionSupplier, final String message) {
		Objects.requireNonNull(predicate);
		Objects.requireNonNull(exceptionSupplier);
		Objects.requireNonNull(message);
		Validations.require(predicate.test(object), exceptionSupplier, message);
		return object;
	}

	/**
	 * Throws the given exception if the predicate resolves to false.
	 *
	 * @param predicate         The predicate to test
	 * @param exceptionSupplier The supplier to use for getting the exception to throw, not null
	 * @param message           The message to provide in the exception, not null
	 * @param <E>               The type of the exception to throw
	 */
	@SuppressWarnings({ "BooleanParameter", "WeakerAccess" })
	public static <E extends RuntimeException> void require(final boolean predicate,
			final Function<? super String, E> exceptionSupplier, final String message) {
		Objects.requireNonNull(exceptionSupplier);
		Objects.requireNonNull(message);
		if (predicate) {
			return;
		}
		throw exceptionSupplier.apply(message);
	}

	/**
	 * Utility class. No implementation.
	 */
	private Validations() {
		throw new UnsupportedOperationException("Utility class, no implementation");
	}
}
