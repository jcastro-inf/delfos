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
package delfos.rs.recommendation;

import delfos.dataset.basic.item.Item;
import delfos.rs.collaborativefiltering.profile.Neighbor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RecommendationWithNeighbors extends Recommendation {

    private final List<Neighbor> neighbors;

    @Deprecated
    public RecommendationWithNeighbors(Integer idItem, Number preference, List<Neighbor> neighbors) {
        super(idItem, preference);
        this.neighbors = Collections.unmodifiableList(new ArrayList<Neighbor>(neighbors));
    }

    public RecommendationWithNeighbors(Item item, Number preference, List<Neighbor> neighbors) {
        super(item, preference);
        this.neighbors = Collections.unmodifiableList(new ArrayList<Neighbor>(neighbors));
    }

    public List<Neighbor> getNeighbors() {
        return neighbors;
    }

}
