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
package delfos.group.grs.cww;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import delfos.common.Global;
import delfos.common.aggregationoperators.weighted.WeightedAggregationOperator;
import delfos.common.aggregationoperators.weighted.WeightedSumAggregation;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.exceptions.ratings.NotEnoughtUserInformation;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.BooleanParameter;
import delfos.common.parameters.restriction.FloatParameter;
import delfos.common.parameters.restriction.ParameterOwnerRestriction;
import delfos.common.parameters.restriction.RecommenderSystemParameterRestriction;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.loaders.given.DatasetLoaderGivenRatingsDataset;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.generated.modifieddatasets.PseudoUserRatingsDataset;
import delfos.dataset.util.DatasetPrinterDeprecated;
import delfos.dataset.util.DatasetUtilities;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.grs.GroupRecommenderSystemAdapter;
import delfos.group.grs.SingleRecommendationModel;
import delfos.group.grs.aggregation.GroupModelPseudoUser;
import delfos.group.grs.cww.centrality.CentralityConceptDefinition;
import delfos.group.grs.cww.centrality.definitions.AritmethicMeanConnectionWeightCentrality;
import delfos.rs.RecommenderSystem;
import delfos.rs.RecommendationModelBuildingProgressListener;
import delfos.rs.collaborativefiltering.knn.memorybased.nwr.KnnMemoryBasedNWR;
import delfos.rs.recommendation.Recommendation;
import delfos.rs.trustbased.StrongTermOverConnections;
import delfos.rs.trustbased.WeightedGraphAdapter;
import delfos.rs.trustbased.WeightedGraphCalculation;
import delfos.rs.trustbased.WeightedGraphNormaliser;
import delfos.rs.trustbased.implicittrustcomputation.ShambourLu_UserBasedImplicitTrustComputation;

/**
 * Sistema de recomendación a grupos de usuarios que agrega las valoraciones de
 * los miembros teniendo en cuenta la centralidad de los mismos.
 *
* @author Jorge Castro Gallardo
 *
 * @version 12-Enero-2014
 */
public class CentralityWeightedAggregationGRS extends GroupRecommenderSystemAdapter<SingleRecommendationModel, GroupModelPseudoUser> {

    private static final long serialVersionUID = 1L;

    public static final Parameter SINGLE_USER_RECOMMENDER = new Parameter(
            "SINGLE_USER_RECOMMENDER",
            new RecommenderSystemParameterRestriction(new KnnMemoryBasedNWR(), RecommenderSystem.class),
            "Especifica el sistema de recomendación single user que se extiende "
            + "para ser usaso en recomendación a grupos.");

    /**
     * Especifica el método de cálculo de la red social del grupo. Debe ser un
     * objeto de tipo {@link WeightedGraphCalculation}.
     */
    public static final Parameter SOCIAL_NETWORK_CALCULATOR = new Parameter(
            "SOCIAL_NETWORK_CALCULATOR",
            new ParameterOwnerRestriction(WeightedGraphCalculation.class, new ShambourLu_UserBasedImplicitTrustComputation(false)));

    public static final Parameter STRONG_APPLY = new Parameter("STRONG_APPLY", new BooleanParameter(Boolean.TRUE));
    public static final Parameter STRONG_MIN = new Parameter("STRONG_MIN", new FloatParameter(0, 1.0f, 0.6f));
    public static final Parameter STRONG_MAX = new Parameter("STRONG_MAX", new FloatParameter(0, 1.0f, 0.8f));

    public static final Parameter CENTRALITY_CONCEPT = new Parameter("CENTRALITY_CONCEPT", new ParameterOwnerRestriction(CentralityConceptDefinition.class, new AritmethicMeanConnectionWeightCentrality()));

    public static final Parameter NORMALISE_SOCIAL_NETWORK_CONNECTIONS = new Parameter("NORMALISE_SOCIAL_NETWORK_CONNECTIONS", new BooleanParameter(Boolean.FALSE));

    /**
     * "Especifica el sistema de recomendación single user que se extiende para
     * ser usado en recomendación a grupos.
     */
    public CentralityWeightedAggregationGRS() {
        super();
        addParameter(SINGLE_USER_RECOMMENDER);
        addParameter(SOCIAL_NETWORK_CALCULATOR);

        addParameter(STRONG_APPLY);
        addParameter(STRONG_MIN);
        addParameter(STRONG_MAX);

        addParameter(NORMALISE_SOCIAL_NETWORK_CONNECTIONS);

        addParameter(CENTRALITY_CONCEPT);
        setAlias("CentralityGRS");
    }

    public CentralityWeightedAggregationGRS(RecommenderSystem<? extends Object> singleUserRecommender) {
        this();
        setParameterValue(SINGLE_USER_RECOMMENDER, singleUserRecommender);
    }

    public CentralityWeightedAggregationGRS(RecommenderSystem<? extends Object> singleUserRecommender, boolean strongApply, double aStrong, double bStrong, boolean normalise) {
        this();
        setParameterValue(SINGLE_USER_RECOMMENDER, singleUserRecommender);
        setParameterValue(STRONG_APPLY, strongApply);
        setParameterValue(STRONG_MIN, aStrong);
        setParameterValue(STRONG_MAX, bStrong);
        setParameterValue(NORMALISE_SOCIAL_NETWORK_CONNECTIONS, normalise);
    }

    public CentralityWeightedAggregationGRS(
            RecommenderSystem<? extends Object> singleUserRecommender,
            WeightedGraphCalculation<? extends Object> weightedGraphCalculation,
            CentralityConceptDefinition centralityConceptDefinition,
            boolean normalise,
            boolean strongApply,
            double aStrong,
            double bStrong) {
        this();
        setParameterValue(SINGLE_USER_RECOMMENDER, singleUserRecommender);
        setParameterValue(SOCIAL_NETWORK_CALCULATOR, weightedGraphCalculation);
        setParameterValue(STRONG_APPLY, strongApply);
        setParameterValue(STRONG_MIN, aStrong);
        setParameterValue(STRONG_MAX, bStrong);
        setParameterValue(NORMALISE_SOCIAL_NETWORK_CONNECTIONS, normalise);
    }

    @Override
    public SingleRecommendationModel buildRecommendationModel(DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadRatingsDataset, CannotLoadContentDataset {

        RecommendationModelBuildingProgressListener buildListener = this::fireBuildingProgressChangedEvent;
        getSingleUserRecommender().addRecommendationModelBuildingProgressListener(buildListener);
        Object build = getSingleUserRecommender().buildRecommendationModel(datasetLoader);
        getSingleUserRecommender().removeRecommendationModelBuildingProgressListener(buildListener);
        return new SingleRecommendationModel(build);
    }

    @Override
    public GroupModelPseudoUser buildGroupModel(DatasetLoader<? extends Rating> datasetLoader, SingleRecommendationModel RecommendationModel, GroupOfUsers groupOfUsers) throws UserNotFound, CannotLoadRatingsDataset {
        Map<Integer, Number> groupRatings = getGroupRatings(datasetLoader, groupOfUsers, getSocialNetworkCalculator());

        return new GroupModelPseudoUser(groupOfUsers, groupRatings);
    }

    @Override
    public Collection<Recommendation> recommendOnly(
            DatasetLoader<? extends Rating> datasetLoader, SingleRecommendationModel RecommendationModel, GroupModelPseudoUser groupModel, GroupOfUsers groupOfUsers, java.util.Set<Integer> candidateItems)
            throws UserNotFound, ItemNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset, NotEnoughtUserInformation {

        //Recojo los parámetros en variables
        RecommenderSystem recommenderSystem = getSingleUserRecommender();

        PseudoUserRatingsDataset<Rating> ratingsDataset_withPseudoUser = new PseudoUserRatingsDataset<>(
                datasetLoader.getRatingsDataset(),
                DatasetUtilities.getUserMap_Rating(-1, groupModel.getRatings()),
                groupOfUsers.getIdMembers());
        int idGroup = ratingsDataset_withPseudoUser.getIdPseudoUser();

        if (Global.isVerboseAnnoying()) {
            DatasetPrinterDeprecated.printCompactRatingTable(ratingsDataset_withPseudoUser, Arrays.asList(idGroup), ratingsDataset_withPseudoUser.getUserRated(idGroup));
        }

        Collection<Recommendation> groupRecom;

        groupRecom = recommenderSystem.recommendToUser(new DatasetLoaderGivenRatingsDataset(datasetLoader, ratingsDataset_withPseudoUser),
                RecommendationModel.getRecommendationModel(),
                idGroup,
                candidateItems);

        return groupRecom;
    }

    @Override
    public boolean isRatingPredictorRS() {
        return true;
    }

    private RecommenderSystem<Object> getSingleUserRecommender() {
        return (RecommenderSystem<Object>) getParameterValue(SINGLE_USER_RECOMMENDER);
    }

    private WeightedGraphCalculation getSocialNetworkCalculator() {
        return (WeightedGraphCalculation) getParameterValue(SOCIAL_NETWORK_CALCULATOR);
    }

    public boolean isStrongApply() {
        return (Boolean) getParameterValue(STRONG_APPLY);
    }

    public boolean isNormaliseSocialNetworkConnections() {
        return (Boolean) getParameterValue(NORMALISE_SOCIAL_NETWORK_CONNECTIONS);
    }

    public CentralityConceptDefinition<Integer> getCentralityConceptDefinition() {
        return (CentralityConceptDefinition<Integer>) getParameterValue(CENTRALITY_CONCEPT);
    }

    public Map<Integer, Number> getGroupRatings(DatasetLoader<? extends Rating> datasetLoader, GroupOfUsers groupOfUsers, WeightedGraphCalculation userTrustGenerator) throws UserNotFound, CannotLoadRatingsDataset {
        // Generate group social network.
        WeightedGraphAdapter<Integer> userTrust = userTrustGenerator.computeTrustValues(datasetLoader, groupOfUsers.getIdMembers());

        if (Global.isVerboseAnnoying()) {
            DatasetPrinterDeprecated.printWeightedGraph(userTrust);
        }

        if (isNormaliseSocialNetworkConnections()) {
            userTrust = new WeightedGraphNormaliser<Integer>(userTrust);
            if (Global.isVerboseAnnoying()) {
                Global.showInfoMessage("Normalised graph\n");
                DatasetPrinterDeprecated.printWeightedGraph(userTrust);
            }
        }

        if (isStrongApply()) {
            userTrust = new StrongTermOverConnections(userTrust, getSTRONG_MIN(), getSTRONG_MAX());
            if (Global.isVerboseAnnoying()) {
                Global.showInfoMessage("Graph modified by Strong(" + getSTRONG_MIN() + "," + getSTRONG_MAX() + ")\n");
                DatasetPrinterDeprecated.printWeightedGraph(userTrust);
            }
        }

        // Compute centrality of each member
        Map<Integer, Double> centrality = new TreeMap<>();
        CentralityConceptDefinition<Integer> centralityConceptDefinition = getCentralityConceptDefinition();

        for (int idMember : groupOfUsers) {
            double centralityOfUser = centralityConceptDefinition.centrality(userTrust, idMember);
            centrality.put(idMember, centralityOfUser);
        }

        // Generate groupProfile.
        Map<Integer, Number> groupRatings = new TreeMap<Integer, Number>();
        {
            WeightedAggregationOperator aggregationOperator = new WeightedSumAggregation();
            Map<Integer, Map<Integer, ? extends Rating>> groupMembersRatings = new TreeMap<Integer, Map<Integer, ? extends Rating>>();
            Set<Integer> itemsRatedByGroup = new TreeSet<Integer>();
            for (int idUser : groupOfUsers.getIdMembers()) {
                groupMembersRatings.put(idUser, datasetLoader.getRatingsDataset().getUserRatingsRated(idUser));
                itemsRatedByGroup.addAll(groupMembersRatings.get(idUser).keySet());
            }
            for (int idItem : itemsRatedByGroup) {
                List<Double> ratingsValues = new ArrayList<Double>(groupOfUsers.size());
                List<Double> memberWeights = new ArrayList<Double>(groupOfUsers.size());
                for (int idMember : groupOfUsers) {
                    Rating rating = groupMembersRatings.get(idMember).get(idItem);
                    if (rating != null) {
                        ratingsValues.add(rating.getRatingValue().doubleValue());
                        memberWeights.add(centrality.get(idMember));
                    }
                }

                //Normalización de los pesos
                double norm = 0;
                for (double weight : memberWeights) {
                    norm += weight;
                }
                if (norm != 0) {
                    for (ListIterator<Double> it = memberWeights.listIterator(); it.hasNext();) {
                        double weight = it.next();
                        it.set(weight / norm);
                    }
                } else {
                    double equiWeight = 1.0 / memberWeights.size();
                    memberWeights.clear();
                    for (double rating : ratingsValues) {
                        memberWeights.add(equiWeight);
                    }
                }

                double groupRating = aggregationOperator.aggregateValues(ratingsValues, memberWeights);
                groupRatings.put(idItem, groupRating);
            }
        }
        return groupRatings;
    }

    private double getSTRONG_MAX() {
        return ((Number) getParameterValue(STRONG_MAX)).doubleValue();
    }

    private double getSTRONG_MIN() {
        return ((Number) getParameterValue(STRONG_MIN)).doubleValue();
    }
}
