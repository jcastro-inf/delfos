package delfos.common;

import java.util.Iterator;

/**
 * Iterador que bloquea el método para eliminar elementos.
 *
* @author Jorge Castro Gallardo
 *
 * @version 17-Septiembre-2013
 */
public class LockedIterator<Element> implements Iterator<Element> {

    private final Iterator<Element> iterator;

    public LockedIterator(Iterator<Element> iterator) {
        this.iterator = iterator;
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public Element next() {
        return iterator.next();
    }

    @Override
    public void remove() {
        throw new IllegalStateException("Cannot remove elements from a locked iterator.");
    }
}
