package delfos.rs.collaborativefiltering.knn.modelbased;

import delfos.rs.collaborativefiltering.knn.RecommendationEntity;
import delfos.rs.collaborativefiltering.profile.CollaborativeFilteringItemProfile;
import delfos.rs.collaborativefiltering.profile.Neighbor;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase para almacenar el perfil de un producto de un sistema de reocomendación
 * Knn ItemItem.
 *
 * @see KnnModelBasedCFRS
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 1.0 Unknow date
 * @version 1.1 18-Jan-2013
 */
public class KnnModelItemProfile extends CollaborativeFilteringItemProfile implements Serializable {

    private static final long serialVersionUID = 102L;

    /**
     * Lista de vecinos del producto.
     */
    private final List<Neighbor> neighborsSimilarity;

    /**
     * Se implementa el constructor por defecto para que el objeto sea
     * serializable.
     *
     * @deprecated Instead of this, use constructor {@link KnnModelItemProfile#KnnModelItemProfile(int)
     * }
     */
    protected KnnModelItemProfile() {
        super(-1);
        neighborsSimilarity = null;
    }

    /**
     * Constructor por defecto del perfil de producto. Crea la lista de vecinos
     * vacía.
     *
     * @param idItem Producto al que se refiere este perfil.
     */
    public KnnModelItemProfile(int idItem) {
        super(idItem);
        this.neighborsSimilarity = new ArrayList<>();
    }

    /**
     * Constructor de un perfil de producto. Establece la lista de vecinos que
     * tiene.
     *
     * @param idItem Producto al que se refiere el perfil.
     * @param neighbors Vecinos del producto.
     */
    public KnnModelItemProfile(int idItem, List<Neighbor> neighbors) {
        this(idItem);
        neighborsSimilarity.addAll(neighbors);

        neighborsSimilarity.sort(Neighbor.BY_SIMILARITY_DESC);
    }

    /**
     * Añade un nuevo producto como vecino del producto al que se refiere este
     * perfil.
     *
     * @param idItemNeighbor Producto vecino.
     * @param similarity Similitud del vecino.
     */
    public void addItem(int idItemNeighbor, float similarity) {
        neighborsSimilarity.add(new Neighbor(RecommendationEntity.ITEM, idItemNeighbor, similarity));
    }

    /**
     * Devuelve una lista de todos los vecinos de este producto.
     *
     * @return Vecinos de este producto, ordenados por similitud.
     */
    public List<Neighbor> getAllNeighbors() {
        return new ArrayList<>(neighborsSimilarity);
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();

        for (Neighbor n : neighborsSimilarity) {
            str.append(n.getIdNeighbor());
            str.append("->");
            str.append(n.getSimilarity());
            str.append(" ");
        }
        return str.toString();
    }
}
