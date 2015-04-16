package delfos.group.groupsofusers.measuresovergroups;

import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.rs.trustbased.implicittrustcomputation.ShambourLu_UserBasedImplicitTrustComputation;
import delfos.rs.trustbased.WeightedGraphAdapter;
import delfos.rs.trustbased.WeightedGraphCalculation;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.ParameterOwnerRestriction;
import delfos.group.groupsofusers.GroupOfUsers;

/**
 * Clase para la suma de todas las distancias entre todos los pares de usuarios.
 *
* @author Jorge Castro Gallardo
 * @version 1.0 03-May-2013
 */
public class SumDistanceInGraph extends GroupMeasureAdapter {

    public static final Parameter weightedGraphCalculation = new Parameter(
            "weightedGraphCalculation",
            new ParameterOwnerRestriction(WeightedGraphCalculation.class, new ShambourLu_UserBasedImplicitTrustComputation()));

    public SumDistanceInGraph() {
        super();
        addParameter(weightedGraphCalculation);
    }

    public SumDistanceInGraph(WeightedGraphCalculation weightedGraphCalculation) {
        this();
        setParameterValue(SumDistanceInGraph.weightedGraphCalculation, weightedGraphCalculation);
    }
    
    @Override
    public String getNameWithParameters() {
        return "SumD_" + getWeightedGraphCalculation().getShortName();
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

        WeightedGraphAdapter<Integer> trustNetwork = getWeightedGraphCalculation().computeTrustValues(datasetLoader, group.getGroupMembers());

        double sumDistance = 0;

        for (int idMember1 : group.getGroupMembers()) {
            for (int idMember2 : group.getGroupMembers()) {
                double distance;
                distance = trustNetwork.geodesicDistance(idMember1, idMember2);
                sumDistance += distance;
            }
        }
        return sumDistance;

    }

    public WeightedGraphCalculation getWeightedGraphCalculation() {
        return (WeightedGraphCalculation) getParameterValue(weightedGraphCalculation);
    }

    
}
