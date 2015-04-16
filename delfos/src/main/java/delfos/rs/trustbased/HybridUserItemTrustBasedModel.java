package delfos.rs.trustbased;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import delfos.rs.collaborativefiltering.profile.Neighbor;

/**
 * Modelo de recomendación del sistema de recomendación
 * {@link HybridUserItemTrustBased_asPaperSays}.
 *
 * @author Jorge
 * @version 1.0 27-Mayo-2013
 */
public class HybridUserItemTrustBasedModel implements Serializable {

    static final long serialVersionUID = -3387516993124229948L;
    private final UserBasedTrustModuleModel userBasedTrustModuleModel;
    private final ItemBasedTrustModuleModel itemBasedTrustModuleModel;

    public static class UserBasedTrustModuleModel implements Serializable {

        private static final long serialVersionUID = -3387516993124229948L;
        private TreeMap<Integer, Set<Neighbor>> usersNeighbours;
        private TreeMap<Integer, Number> usersReputation;
        private WeightedGraph<Integer> usersTrust;

        public UserBasedTrustModuleModel(TreeMap<Integer, Set<Neighbor>> usersNeighbours, TreeMap<Integer, Number> usersReputation, TreeMap<Integer, Map<Integer, Number>> usersTrust) {
            this.usersNeighbours = usersNeighbours;
            this.usersReputation = usersReputation;
            this.usersTrust = new WeightedGraphAdapter<Integer>(usersTrust);
        }

        UserBasedTrustModuleModel(TreeMap<Integer, Set<Neighbor>> usersNeighbours, TreeMap<Integer, Number> usersReputation, WeightedGraphAdapter<Integer> usersTrust) {
            this.usersNeighbours = usersNeighbours;
            this.usersReputation = usersReputation;
            this.usersTrust = usersTrust;
        }

        public TreeMap<Integer, Set<Neighbor>> getUsersNeighbours() {
            return usersNeighbours;
        }

        public TreeMap<Integer, Number> getUsersReputation() {
            return usersReputation;
        }

        public WeightedGraph<Integer> getUsersTrust() {
            return usersTrust;
        }
    }

    public static class ItemBasedTrustModuleModel implements Serializable {

        static final long serialVersionUID = -3387516993124229948L;
        private Map<Integer, Map<Integer, Number>> itemsReputation;
        private Map<Integer, Collection<Neighbor>> itemsNeighbours;
        private WeightedGraph<Integer> itemsTrust;

        public ItemBasedTrustModuleModel(TreeMap<Integer, Map<Integer, Number>> itemsTrust, TreeMap<Integer, Map<Integer, Number>> itemsReputation, TreeMap<Integer, Collection<Neighbor>> itemsNeighbours) {
            this.itemsReputation = itemsReputation;
            this.itemsNeighbours = itemsNeighbours;
            this.itemsTrust = new WeightedGraphAdapter<Integer>(itemsTrust);
        }

        public ItemBasedTrustModuleModel(WeightedGraph<Integer> itemBasedTrust, Map<Integer, Map<Integer, Number>> itemsReputation, TreeMap<Integer, Collection<Neighbor>> itemsNeighbours) {
            this.itemsReputation = itemsReputation;
            this.itemsNeighbours = itemsNeighbours;
            this.itemsTrust = itemBasedTrust;
        }

        public Map<Integer, Collection<Neighbor>> getItemsNeighbours() {
            return Collections.unmodifiableMap(itemsNeighbours);
        }

        public Map<Integer, Map<Integer, Number>> getItemsReputation() {
            return Collections.unmodifiableMap(itemsReputation);
        }

        public WeightedGraph<Integer> getItemsTrust() {
            return itemsTrust;
        }
    }

    public HybridUserItemTrustBasedModel(
            UserBasedTrustModuleModel userBasedTrustModuleModel,
            ItemBasedTrustModuleModel itemBasedTrustModuleModel) {
        this.userBasedTrustModuleModel = userBasedTrustModuleModel;
        this.itemBasedTrustModuleModel = itemBasedTrustModuleModel;
    }

    public ItemBasedTrustModuleModel getItemBasedTrustModuleModel() {
        return itemBasedTrustModuleModel;
    }

    public UserBasedTrustModuleModel getUserBasedTrustModuleModel() {
        return userBasedTrustModuleModel;
    }
}
