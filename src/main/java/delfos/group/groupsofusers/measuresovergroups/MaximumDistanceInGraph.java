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

import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.rs.trustbased.implicittrustcomputation.ShambourLu_UserBasedImplicitTrustComputation;
import delfos.rs.trustbased.WeightedGraph;
import delfos.rs.trustbased.WeightedGraphCalculation;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.ParameterOwnerRestriction;
import delfos.group.groupsofusers.GroupOfUsers;

/**
 * Clase para calcular la distancia máxima entre dos miembros del grupo.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @version 1.0 03-May-2013
 */
public class MaximumDistanceInGraph extends GroupMeasureAdapter {

    public static final Parameter weightedGraphCalculation = new Parameter(
            "weightedGraphCalculation",
            new ParameterOwnerRestriction(WeightedGraphCalculation.class, new ShambourLu_UserBasedImplicitTrustComputation()));

    public MaximumDistanceInGraph() {
        super();
        addParameter(weightedGraphCalculation);
    }

    public MaximumDistanceInGraph(WeightedGraphCalculation weightedGraphCalculation) {
        this();
        setParameterValue(MaximumDistanceInGraph.weightedGraphCalculation, weightedGraphCalculation);
    }

    @Override
    public String getNameWithParameters() {
        return "MaxD_" + getWeightedGraphCalculation().getShortName();
    }

    /**
     * Devuelve el grado con el que el grupo indicado es un clique.
     *
     * @param trustNetwork Red de confianza.
     * @param group Grupo a comprobar.
     * @return Valor difuso con el que un grupo es un clique.
     */
    @Override
    public double getMeasure(DatasetLoader<? extends Rating> datasetLoader, GroupOfUsers group) throws CannotLoadRatingsDataset {


        WeightedGraph<Integer> trustNetwork = getWeightedGraphCalculation().computeTrustValues(datasetLoader, group.getIdMembers());

        double maxDistance = 0;

        for (int idMember1 : group.getIdMembers()) {
            for (int idMember2 : group.getIdMembers()) {
                double distance;
                distance = trustNetwork.geodesicDistance(idMember1, idMember2);

                if (maxDistance < distance) {
                    maxDistance = distance;
                }
            }
        }
        return maxDistance;

    }

    public WeightedGraphCalculation getWeightedGraphCalculation() {
        return (WeightedGraphCalculation) getParameterValue(weightedGraphCalculation);
    }
}
