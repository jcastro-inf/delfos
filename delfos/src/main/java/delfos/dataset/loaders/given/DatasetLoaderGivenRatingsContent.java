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
package delfos.dataset.loaders.given;

import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.CannotLoadUsersDataset;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.loader.types.DatasetLoaderAbstract;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.dataset.basic.user.UsersDataset;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 26-nov-2013
 * @param <RatingType>
 */
public class DatasetLoaderGivenRatingsContent<RatingType extends Rating> extends DatasetLoaderAbstract<RatingType> {

    private final ContentDataset contentDataset;
    private final RatingsDataset<RatingType> ratingsDataset;
    private final UsersDataset usersDataset;

    private static final long serialVersionUID = 1L;

    public DatasetLoaderGivenRatingsContent(RatingsDataset<RatingType> ratingsDataset, ContentDataset contentDataset, UsersDataset usersDataset) {
        this.ratingsDataset = ratingsDataset;
        this.contentDataset = contentDataset;
        this.usersDataset = usersDataset;
    }

    @Override
    public RatingsDataset<RatingType> getRatingsDataset() throws CannotLoadRatingsDataset {
        return ratingsDataset;
    }

    @Override
    public ContentDataset getContentDataset() throws CannotLoadContentDataset {
        return contentDataset;
    }

    @Override
    public UsersDataset getUsersDataset() throws CannotLoadUsersDataset {
        return usersDataset;
    }

    @Override
    public RelevanceCriteria getDefaultRelevanceCriteria() {
        return new RelevanceCriteria(4);
    }
}
