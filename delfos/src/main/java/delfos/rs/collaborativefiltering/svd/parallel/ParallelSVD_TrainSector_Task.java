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
package delfos.rs.collaborativefiltering.svd.parallel;

import java.util.Collection;
import delfos.common.parallelwork.Task;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;

/**
 *
 * @version 24-jul-2014
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 */
public class ParallelSVD_TrainSector_Task extends Task {

    private final RatingsDataset<? extends Rating> ratingsDataset;
    private final ParallelSVD_AlgorithmParameters algorithmParameters;
    private final ParallelSVDModel parallelSVDModel;

    private final int feature;
    private final Collection<Integer> usersSet;
    private final Collection<Integer> itemsSet;
    private final long seed;

    public ParallelSVD_TrainSector_Task(
            RatingsDataset<? extends Rating> ratingsDataset,
            ParallelSVD_AlgorithmParameters algorithmParameters,
            ParallelSVDModel parallelSVDModel,
            int feature, Collection<Integer> usersSet, Collection<Integer> itemsSet, long seed) {
        this.ratingsDataset = ratingsDataset;
        this.algorithmParameters = algorithmParameters;
        this.parallelSVDModel = parallelSVDModel;
        this.feature = feature;
        this.usersSet = usersSet;
        this.itemsSet = itemsSet;
        this.seed = seed;
    }

    @Override
    public String toString() {
        return "ParallelSVDTask: " + usersSet + " && " + itemsSet;
    }

    public RatingsDataset<? extends Rating> getRatingsDataset() {
        return ratingsDataset;
    }

    public ParallelSVD_AlgorithmParameters getAlgorithmParameters() {
        return algorithmParameters;
    }

    public ParallelSVDModel getParallelSVDModel() {
        return parallelSVDModel;
    }

    public int getFeature() {
        return feature;
    }

    public Collection<Integer> getUsersSet() {
        return usersSet;
    }

    public Collection<Integer> getItemsSet() {
        return itemsSet;
    }

    public long getSeed() {
        return seed;
    }

}
