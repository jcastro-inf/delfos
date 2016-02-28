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
package delfos.group.grs.mean;

import delfos.common.parallelwork.Task;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.rs.nonpersonalised.meanrating.arithmeticmean.MeanRating;

/**
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 9-Junio-2013
 */
public class MeanRatingTask extends Task {

    private final DatasetLoader<? extends Rating> datasetLoader;
    private final Item item;
    private MeanRating meanRating;

    /**
     *
     * @param datasetLoader
     * @param item
     */
    public MeanRatingTask(DatasetLoader<? extends Rating> datasetLoader, Item item) {
        this.datasetLoader = datasetLoader;
        this.item = item;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();

        str.append("task-----> ").append(this.getClass().getName()).append("\n");
        str.append("idItem --> ")
                .append("(").append(item.getId()).append(") ")
                .append(item.getName()).append("\n");

        return str.toString();
    }

    public void setMeanRating(MeanRating meanRating) {
        this.meanRating = meanRating;
    }

    public MeanRating getMeanRating() {
        return meanRating;
    }

    public DatasetLoader<? extends Rating> getDatasetLoader() {
        return datasetLoader;
    }

    public Item getItem() {
        return item;
    }

}
