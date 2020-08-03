package de.zabuza.fastcdc4j.internal.util;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;

/**
 * Iterator that flattens an iterator over other iterators. The given iterator is consumed on-the-fly.
 * <p>
 * Use {@code new FlatIterator(outer, Function::identity)} if {@link X} is already the inner iterator.
 *
 * @param <X> The type contained in the outer iterator, either already an iterator or used to produce iterators using a
 *            given function
 * @param <Y> The type contained in the inner iterators, also the final type contained in this iterator
 */
public final class FlatIterator<X, Y> implements Iterator<Y> {
	/**
	 * The source iterator used to generate inner iterators from.
	 */
	private final Iterator<X> outerIterator;
	/**
	 * Function that provides the final inner iterators based on the outer iterators data.
	 */
	private final Function<X, Iterator<Y>> provider;
	/**
	 * The current inner iterator to iterate over.
	 */
	private Iterator<Y> currentInnerIter;

	/**
	 * Creates a new flat iterator that flattens the given iterator on-the-fly.
	 *
	 * @param outerIterator The source iterator used to generate inner iterators from
	 * @param provider      Function that provides the final inner iterators based on the outer iterators data
	 */
	public FlatIterator(Iterator<X> outerIterator, Function<X, Iterator<Y>> provider) {
		this.outerIterator = outerIterator;
		this.provider = provider;
	}

	@Override
	public boolean hasNext() {
		while (true) {
			boolean hasNext = currentInnerIter != null && currentInnerIter.hasNext();
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
			throw new NoSuchElementException();
		}

		return currentInnerIter.next();
	}
}
