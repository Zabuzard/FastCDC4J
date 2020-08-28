package de.zabuza.fastcdc4j.internal.util;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Function;

/**
 * Iterator that flattens an iterator over other iterators. The given iterator is consumed on-the-fly.
 * <p>
 * Use {@code new FlatIterator(outer, Function::identity)} if {@link X} is already the inner iterator.
 *
 * @param <X> The type contained in the outer iterator, either already an iterator or used to produce iterators using a
 *            given function
 * @param <Y> The type contained in the inner iterators, also the final type contained in this iterator
 *
 * @author Daniel Tischner {@literal <zabuza.dev@gmail.com>}
 */
public final class FlatIterator<X, Y> implements Iterator<Y> {
	/**
	 * The source iterator used to generate inner iterators from.
	 */
	private final Iterator<? extends X> outerIterator;
	/**
	 * Function that provides the final inner iterators based on the outer iterators data.
	 */
	private final Function<? super X, ? extends Iterator<Y>> provider;
	/**
	 * The current inner iterator to iterate over.
	 */
	private Iterator<Y> currentInnerIter;

	/**
	 * Creates a new flat iterator that flattens the given iterator on-the-fly.
	 *
	 * @param outerIterator The source iterator used to generate inner iterators from, not null
	 * @param provider      Function that provides the final inner iterators based on the outer iterators data, not
	 *                      null
	 */
	public FlatIterator(final Iterator<? extends X> outerIterator,
			final Function<? super X, ? extends Iterator<Y>> provider) {
		this.outerIterator = Objects.requireNonNull(outerIterator);
		this.provider = Objects.requireNonNull(provider);
	}

	@Override
	public boolean hasNext() {
		while (true) {
			final boolean hasNext = currentInnerIter != null && currentInnerIter.hasNext();
			if (hasNext) {
				return true;
			}

			// Not set yet or exhausted
			if (!outerIterator.hasNext()) {
				return false;
			}

			currentInnerIter = provider.apply(outerIterator.next());
		}
	}

	@Override
	public Y next() {
		// hasNext also prepares currentInnerIter
		if (!hasNext()) {
			throw new NoSuchElementException("Attempting to get the next element but the iterator is out of elements");
		}

		return currentInnerIter.next();
	}
}
