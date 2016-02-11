/* 
 * Copyright (C) 2016 jcastro
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
