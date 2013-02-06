package me.stendec.abyss.util;

import java.util.Iterator;

public class IteratorChain<T> implements Iterator<T> {
    private final Iterator<T>[] iters;
    private int current;

    public IteratorChain(final Iterator<T>... iterators) {
        iters = iterators;
        current = 0;
    }

    public boolean hasNext() {
        while (current < iters.length && !iters[current].hasNext() )
            current++;

        return current < iters.length;
    }

    public T next() {
        while (current < iters.length && !iters[current].hasNext() )
            current++;

        return iters[current].next();
    }

    public void remove() {
        // Not Implemented
    }
}