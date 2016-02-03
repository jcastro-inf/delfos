package delfos.utils.fuzzyclustering;

/**
 *
 * @version 16-sep-2014
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 */
public class MockIdItem implements Comparable {

    final int idItem;

    public MockIdItem(int idUser) {
        this.idItem = idUser;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MockIdItem) {
            MockIdItem id = (MockIdItem) obj;
            return idItem == id.idItem;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + this.idItem;
        return hash;
    }

    @Override
    public int compareTo(Object obj) {
        if (obj instanceof MockIdItem) {
            MockIdItem id = (MockIdItem) obj;

            return Integer.compare(idItem, id.idItem);
        }
        throw new IllegalStateException("Not comparable: " + obj);
    }

    @Override
    public String toString() {
        return "i_" + idItem;
    }

}
