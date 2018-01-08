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
package delfos.group.groupsofusers.measuresovergroups;

import java.util.Map;
import java.util.TreeMap;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.ERROR_CODES;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.IntegerParameter;
import delfos.group.groupsofusers.GroupOfUsers;

/**
 * Clase para calcular número de ratings del grupo.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @version 1.0 03-Junio-2013
 */
public class CommonRatings extends GroupMeasureAdapter {

    public static final Parameter numCommon = new Parameter("numCommon", new IntegerParameter(1, Integer.MAX_VALUE, 2));

    public CommonRatings() {
        super();

        addParameter(numCommon);
    }

    public CommonRatings(int numCommon) {
        this();
        setParameterValue(CommonRatings.numCommon, numCommon);
    }

    public int getNumCommon() {
        return (Integer) getParameterValue(numCommon);
    }

    @Override
    public String getNameWithParameters() {
        return getName() + "_" + getNumCommon() + "_members";
    }

    /**
     * Devuelve el grado con el que el grupo indicado es un clique.
     *
     * @param datasetLoader
     * @param group Grupo a comprobar.
     * @return Valor difuso con el que un grupo es un clique.
     */
    @Override
    public double getMeasure(DatasetLoader<? extends Rating> datasetLoader, GroupOfUsers group) throws CannotLoadRatingsDataset {

        RatingsDataset<? extends Rating> ratingsDataset = datasetLoader.getRatingsDataset();
        Map<Long, Long> numMembersRated = new TreeMap<Long, Long>();
        for (long idUser : group) {
            try {
                for (long idItem : ratingsDataset.getUserRated(idUser)) {
                    if (!numMembersRated.containsKey(idItem)) {
                        numMembersRated.put(idItem, 0l);
                    }
                    numMembersRated.put(idItem, numMembersRated.get(idItem) + 1);
                }
            } catch (UserNotFound ex) {
                ERROR_CODES.USER_NOT_FOUND.exit(ex);
            }
        }

        double value = 0;
        for (long idItem : numMembersRated.keySet()) {
            long numMembers = numMembersRated.get(idItem);
            if (numMembers >= getNumCommon()) {
                value++;
            }
        }

        value = value / numMembersRated.size();

        return value;
    }
}
