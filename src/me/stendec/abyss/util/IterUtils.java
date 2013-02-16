package me.stendec.abyss.util;

import me.stendec.abyss.AbyssPlugin;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class IterUtils {

    public static class Size {
        public final short x, z;

        public Size(final short x, final short z) {
            this.x = x;
            this.z = z;
        }

        public String toString() {
            return String.format("%dx%d", x, z);
        }

    }

    public static class SizeIterator implements Iterator<Size> {

        private final short start_x, end_x, start_z, end_z;
        private short x, z;
        private boolean has_next;
        private final boolean square;

        public SizeIterator(final AbyssPlugin plugin) {
            square = plugin.squareOnly;

            if ( square ) {
                final short min = (short) Math.max(plugin.minimumSizeX, plugin.minimumSizeZ);
                final short max = (short) Math.min(plugin.maximumSizeX, plugin.maximumSizeZ);

                start_x = min; end_x = max;
                start_z = min; end_z = max;

            } else {
                start_x = plugin.minimumSizeX;
                end_x = plugin.maximumSizeX;

                start_z = plugin.minimumSizeZ;
                end_z = plugin.maximumSizeZ;
            }

            x = start_x;
            z = start_z;

            has_next = true;
        }

        public boolean hasNext() { return has_next; }
        public void remove() { throw new UnsupportedOperationException(); }

        public Size next() throws NoSuchElementException {
            if ( ! has_next )
                throw new NoSuchElementException();

            Size out = new Size(x, z);

            // Prepare for the next one.
            if ( square ) {
                x++;
                z++;

            } else {
                while ( x <= end_x ) {
                    z++;

                    if ( z > end_z ) {
                        z = start_z;
                        x++;
                    }

                    if ( z < start_x || z >= x )
                        break;
                }
            }

            has_next = ( x <= end_x );
            return out;
        }

    }


    public static class IteratorChain<T> implements Iterator<T> {
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

}