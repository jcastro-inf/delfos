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
package delfos.common.datastructures.queue;

/**
 * The higher the priority, the better.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @param <KeyType>
 */
public class PriorityItem<KeyType> implements Comparable<PriorityItem<KeyType>> {

    KeyType key;

    double priority;

    public PriorityItem(KeyType key, double priority) {
        this.key = key;
        this.priority = priority;
    }

    @Override
    public int compareTo(PriorityItem<KeyType> o) {
        return -Double.compare(this.priority, o.priority);
    }

    @Override
    public String toString() {
        return key.toString() + " -> " + priority;

    }

    public KeyType getKey() {
        return key;
    }

    public double getPriority() {
        return priority;
    }

}
