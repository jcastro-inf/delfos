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
package delfos.utils.streams;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jcastro
 */
public class IteratorToList {

    public static <T> List<T> collectInList(Iterable<T> elements) {
        ArrayList<T> list = new ArrayList<>();
        for (T t : elements) {
            list.add(t);
        }
        return list;
    }
}
