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
package delfos.group.factories;

import delfos.factories.Factory;
import delfos.group.grs.consensus.itemselector.BordaCount;
import delfos.group.grs.consensus.itemselector.GroupRecommendationsSelector;
import delfos.group.grs.consensus.itemselector.NoSelection;
import delfos.group.grs.consensus.itemselector.RandomSelection;
import delfos.group.grs.consensus.itemselector.TopNOfEach;

/**
 *
 * @author Jorge Castro Gallardo
 *
 * @version 27-sept-2014
 */
public class GroupRecommendationsSelectorFactory extends Factory<GroupRecommendationsSelector> {

    private final static GroupRecommendationsSelectorFactory instance;

    static {
        instance = new GroupRecommendationsSelectorFactory();

        instance.addClass(RandomSelection.class);
        instance.addClass(BordaCount.class);
        instance.addClass(TopNOfEach.class);
        instance.addClass(NoSelection.class);

    }

    private GroupRecommendationsSelectorFactory() {
    }

    public static GroupRecommendationsSelectorFactory getInstance() {
        return instance;
    }
}
