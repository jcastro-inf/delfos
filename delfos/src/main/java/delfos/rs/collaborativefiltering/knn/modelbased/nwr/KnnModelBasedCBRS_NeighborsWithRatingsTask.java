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
package delfos.rs.collaborativefiltering.knn.modelbased.nwr;

import java.util.Collections;
import java.util.List;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.rs.collaborativefiltering.profile.Neighbor;

/**
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 19-Noviembre-2013
 */
public class KnnModelBasedCBRS_NeighborsWithRatingsTask {

    public final int idItem;
    private KnnModelBased_NWR rs;
    private RatingsDataset<? extends Rating> ratingsDataset;
    private List<Neighbor> neighbors = null;

    public KnnModelBasedCBRS_NeighborsWithRatingsTask(int idItem, KnnModelBased_NWR rs, RatingsDataset<? extends Rating> ratingsDataset) {
        this.idItem = idItem;
        this.rs = rs;
        this.ratingsDataset = ratingsDataset;
    }

    public int getIdItem() {
        return idItem;
    }

    public KnnModelBased_NWR getRecommenderSystem() {
        return rs;
    }

    public RatingsDataset<? extends Rating> getRatingsDataset() {
        return ratingsDataset;
    }

    public void setNeighbors(List<Neighbor> neighbors) {
        rs = null;
        ratingsDataset = null;
        this.neighbors = neighbors;
    }

    public List<Neighbor> getNeighbors() {
        return Collections.unmodifiableList(neighbors);
    }
}
