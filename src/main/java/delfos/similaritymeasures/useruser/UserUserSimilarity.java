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
package delfos.similaritymeasures.useruser;

import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.user.User;
import delfos.similaritymeasures.SimilarityMeasure;

/**
 *
 * @version 08-may-2014
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public interface UserUserSimilarity extends SimilarityMeasure {

    /**
     * Similitud entre dos usuarios, utilizando los datos del datasetLoader.
     *
     * @param datasetLoader
     * @param idUser1
     * @param idUser2
     * @return
     */
    @Deprecated
    public double similarity(DatasetLoader<? extends Rating> datasetLoader, long idUser1, long idUser2);

    public double similarity(DatasetLoader<? extends Rating> datasetLoader, User user1, User user2);
}
