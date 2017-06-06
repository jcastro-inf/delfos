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
        private TreeMap<Long, Set<Neighbor>> usersNeighbours;
        private TreeMap<Long, Number> usersReputation;
        private WeightedGraph<Long> usersTrust;

        public UserBasedTrustModuleModel(TreeMap<Long, Set<Neighbor>> usersNeighbours, TreeMap<Long, Number> usersReputation, TreeMap<Long, Map<Long, Number>> usersTrust) {
            this.usersNeighbours = usersNeighbours;
            this.usersReputation = usersReputation;
            this.usersTrust = new WeightedGraph<Long>(usersTrust);
        }

        UserBasedTrustModuleModel(TreeMap<Long, Set<Neighbor>> usersNeighbours, TreeMap<Long, Number> usersReputation, WeightedGraph<Long> usersTrust) {
            this.usersNeighbours = usersNeighbours;
            this.usersReputation = usersReputation;
            this.usersTrust = usersTrust;
        }

        public TreeMap<Long, Set<Neighbor>> getUsersNeighbours() {
            return usersNeighbours;
        }

        public TreeMap<Long, Number> getUsersReputation() {
            return usersReputation;
        }

        public WeightedGraph<Long> getUsersTrust() {
            return usersTrust;
        }
    }

    public static class ItemBasedTrustModuleModel implements Serializable {

        static final long serialVersionUID = -3387516993124229948L;
        private Map<Long, Map<Long, Number>> itemsReputation;
        private Map<Long, Collection<Neighbor>> itemsNeighbours;
        private WeightedGraph<Long> itemsTrust;

        public ItemBasedTrustModuleModel(TreeMap<Long, Map<Long, Number>> itemsTrust, TreeMap<Long, Map<Long, Number>> itemsReputation, TreeMap<Long, Collection<Neighbor>> itemsNeighbours) {
            this.itemsReputation = itemsReputation;
            this.itemsNeighbours = itemsNeighbours;
            this.itemsTrust = new WeightedGraph<Long>(itemsTrust);
        }

        public ItemBasedTrustModuleModel(WeightedGraph<Long> itemBasedTrust, Map<Long, Map<Long, Number>> itemsReputation, TreeMap<Long, Collection<Neighbor>> itemsNeighbours) {
            this.itemsReputation = itemsReputation;
            this.itemsNeighbours = itemsNeighbours;
            this.itemsTrust = itemBasedTrust;
        }

        public Map<Long, Collection<Neighbor>> getItemsNeighbours() {
            return Collections.unmodifiableMap(itemsNeighbours);
        }

        public Map<Long, Map<Long, Number>> getItemsReputation() {
            return Collections.unmodifiableMap(itemsReputation);
        }

        public WeightedGraph<Long> getItemsTrust() {
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
