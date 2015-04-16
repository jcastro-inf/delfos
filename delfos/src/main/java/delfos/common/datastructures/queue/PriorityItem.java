package delfos.common.datastructures.queue;

/**
 * The higher the priority, the better.
 *
* @author Jorge Castro Gallardo
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
