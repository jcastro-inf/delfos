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
import delfos.group.grs.filtered.filters.GroupRatingsFilter;
import delfos.group.grs.filtered.filters.NoFilter;
import delfos.group.grs.filtered.filters.OutliersItemsStandardDeviationThresholdFilter;
import delfos.group.grs.filtered.filters.OutliersItemsStandardDeviationTopPercentFilter;
import delfos.group.grs.filtered.filters.OutliersRatingsFilter;
import delfos.group.grs.filtered.filters.OutliersRatingsStandardDeviationFilter;

/**
 * Conoce las técnicas de filtrado de valoraciones para la recomendación a
 * grupos y las proveee.
 *
* @author Jorge Castro Gallardo
 *
 * @version 1.0 09-Mayo-2013
 */
public class GroupRatingsFilterFactory extends Factory<GroupRatingsFilter> {

    private static final GroupRatingsFilterFactory instance;

    static {
        instance = new GroupRatingsFilterFactory();

        instance.addClass(NoFilter.class);

        instance.addClass(OutliersRatingsFilter.class);
        instance.addClass(OutliersRatingsStandardDeviationFilter.class);

        instance.addClass(OutliersItemsStandardDeviationThresholdFilter.class);
        instance.addClass(OutliersItemsStandardDeviationTopPercentFilter.class);
    }

    public static GroupRatingsFilterFactory getInstance() {
        return instance;
    }
}
