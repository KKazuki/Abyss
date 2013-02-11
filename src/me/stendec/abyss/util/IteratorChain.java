package me.stendec.abyss.util;

import java.util.Iterator;

public class IteratorChain<T> implements Iterator<T> {
    private final Iterator<T>[] iterators;
    private int current;
    private int last;

    public IteratorChain(final Iterator<T>... iterators) {
        this.iterators = iterators;
        current = 0;
        last = -1;
    }

    public boolean hasNext() {
        while (current < iterators.length && !iterators[current].hasNext() )
            current++;

        return current < iterators.length;
    }

    public T next() {
        while (current < iterators.length && !iterators[current].hasNext() )
            current++;

        last = current;
        return iterators[current].next();
    }

    public void remove() {
        if ( last == -1 )
            throw new IllegalStateException();

        iterators[last].remove();
        last = -1;
    }
}