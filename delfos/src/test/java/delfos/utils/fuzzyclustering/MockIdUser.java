package delfos.utils.fuzzyclustering;

/**
 *
 * @version 16-sep-2014
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 */
public class MockIdUser implements Comparable {

    final int idUser;

    public MockIdUser(int idUser) {
        this.idUser = idUser;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MockIdUser) {
            MockIdUser id = (MockIdUser) obj;
            return idUser == id.idUser;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + this.idUser;
        return hash;
    }

    @Override
    public int compareTo(Object obj) {
        if (obj instanceof MockIdUser) {
            MockIdUser id = (MockIdUser) obj;

            return Integer.compare(idUser, id.idUser);
        }
        throw new IllegalStateException("Not comparable: " + obj);
    }

    @Override
    public String toString() {
        return "u_" + idUser;
    }

}
