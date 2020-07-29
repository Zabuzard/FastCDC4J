package de.zabuza.fastcdc4j.internal.util;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;

public final class FlatIterator<X, Y> implements Iterator<Y> {
	private final Iterator<X> outerIterator;
	private final Function<X, Iterator<Y>> provider;
	private Iterator<Y> currentInnerIter;


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
