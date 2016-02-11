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
package delfos.view.neighborhood.results;

import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.user.User;
import delfos.rs.recommendation.Recommendations;
import java.awt.Component;
import java.util.Set;

/**
 *
 * @author jcastro
 */
public interface RecommendationsGUI {

    public Component getComponent();

    public void updateResult(
            DatasetLoader datasetLoader,
            Object recommendationModel,
            User user,
            Recommendations recommendations,
            Set<Item> candidateItems
    );

    public void clearData();
}
