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
package delfos.group.experiment.validation.groupformation;

import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.parameters.Parameter;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.similaritymeasures.useruser.UserUserSimilarity;
import java.util.Collection;
import java.util.Random;

/**
 * Crea grupos buscando similitudes entre las preferencias de los miembros. Los
 * grupos no tienen usuarios en común, es decir, la intersección entre cualquier
 * par de grupos generados (de una vez) es siempre el conjunto vacío.
 *
 * @version 10-abr-2014
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class DissimilarMembers extends GroupFormationTechnique {

    public static final Parameter GROUP_SIZE_PARAMETER = DissimilarMembers_OnlyNGroups.GROUP_SIZE_PARAMETER;
    public static final Parameter SIMILARITY_MEASURE = DissimilarMembers_OnlyNGroups.SIMILARITY_MEASURE;

    /**
     * Genera una validación de usuarios que genera grupos de tamaño fijo. Por
     * defecto, el tamaño de los grupos es de cuatro miembros.
     */
    public DissimilarMembers() {
        super();
        addParameter(GROUP_SIZE_PARAMETER);
        addParameter(SIMILARITY_MEASURE);

        addParammeterListener(() -> {
            int groupSizeValue = (Integer) getParameterValue(GROUP_SIZE_PARAMETER);

            String oldAlias = getAlias();
            String newAlias = this.getClass().getSimpleName() + "(size=" + groupSizeValue + ")";

            if (!oldAlias.equals(newAlias)) {
                setAlias(newAlias);
            }
        });
    }

    /**
     * Genera una validación de usuarios que genera grupos de tamaño fijo. Por
     * defecto, el tamaño de los grupos es de cuatro miembros.
     *
     * @param groupSizeValue Tamaño de los grupos generados
     */
    public DissimilarMembers(int groupSizeValue) {
        this();
        setParameterValue(GROUP_SIZE_PARAMETER, groupSizeValue);
    }

    @Override
    public Collection<GroupOfUsers> generateGroups(DatasetLoader<? extends Rating> datasetLoader, Collection<User> usersAllowed) throws CannotLoadRatingsDataset {
        if (datasetLoader == null) {
            throw new IllegalStateException("The datasetLoader is null.");
        }
        Random random = new Random(getSeedValue());

        final int groupSize = (Integer) getParameterValue(GROUP_SIZE_PARAMETER);
        final int maximumGroups = datasetLoader.getRatingsDataset().allUsers().size() / groupSize;
        UserUserSimilarity similarityMeasure = (UserUserSimilarity) getParameterValue(SIMILARITY_MEASURE);

        DissimilarMembers_OnlyNGroups dissimilarMembers_OnlyNGroups = new DissimilarMembers_OnlyNGroups(maximumGroups, groupSize);
        dissimilarMembers_OnlyNGroups.setParameterValue(DissimilarMembers_OnlyNGroups.SIMILARITY_MEASURE, similarityMeasure);

        dissimilarMembers_OnlyNGroups.addListener(this::progressChanged);

        Collection<GroupOfUsers> result = dissimilarMembers_OnlyNGroups.generateGroups(datasetLoader);

        return result;
    }
}
