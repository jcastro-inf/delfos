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
package delfos.dataset.basic.loader.types;

import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.CannotLoadUsersDataset;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.dataset.basic.trust.TrustDataset;
import delfos.dataset.basic.user.UsersDataset;

/**
 * DatasetLoader<? extends Rating> con todos los métodos posibles, dejando que
 * ocurran en tiempo de ejecución los fallos derivados de la no implementación
 * de alguna de las interfaces de los dataset loader.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 26-Noviembre-2013
 * @param <RatingType>
 */
public class CompleteDatasetLoaderAbstract_withTrust<RatingType extends Rating>
        extends DatasetLoaderAbstract<RatingType>
        implements CompleteDatasetLoader<RatingType> {

    private static final long serialVersionUID = 1L;

    @Override
    public RelevanceCriteria getDefaultRelevanceCriteria() {
        throw new IllegalStateException("Not implemented yet.");
    }

    @Override
    public ContentDataset getContentDataset() throws CannotLoadContentDataset {
        throw new CannotLoadUsersDataset("Not implemented yet.");
    }

    @Override
    public UsersDataset getUsersDataset() throws CannotLoadUsersDataset {
        throw new CannotLoadUsersDataset("Not implemented yet.");
    }

    @Override
    public RatingsDataset<RatingType> getRatingsDataset() throws CannotLoadRatingsDataset {
        throw new CannotLoadRatingsDataset("Not implemented yet.");
    }

    @Override
    public TrustDataset getTrustDataset() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
