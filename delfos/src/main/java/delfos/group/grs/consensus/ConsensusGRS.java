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
package delfos.group.grs.consensus;

import delfos.ERROR_CODES;
import delfos.common.Global;
import delfos.common.aggregationoperators.AggregationOperator;
import delfos.common.aggregationoperators.MinimumValue;
import delfos.common.datastructures.queue.PriorityItem;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.exceptions.ratings.NotEnoughtUserInformation;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.BooleanParameter;
import delfos.common.parameters.restriction.DirectoryParameter;
import delfos.common.parameters.restriction.FloatParameter;
import delfos.common.parameters.restriction.ParameterOwnerRestriction;
import delfos.common.parameters.restriction.RecommenderSystemParameterRestriction;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.user.User;
import delfos.dataset.util.DatasetOperations;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.grs.GroupRecommenderSystemAdapter;
import delfos.group.grs.SingleRecommendationModel;
import delfos.group.grs.aggregation.AggregationOfIndividualRecommendations;
import delfos.group.grs.aggregation.GroupModelPseudoUser;
import delfos.group.grs.consensus.itemselector.BordaCount;
import delfos.group.grs.consensus.itemselector.GroupRecommendationsSelector;
import delfos.group.grs.recommendations.GroupRecommendations;
import delfos.group.grs.recommendations.GroupRecommendationsWithMembersRecommendations;
import delfos.io.csv.dataset.DatasetToCSV;
import delfos.rs.RecommendationModelBuildingProgressListener;
import delfos.rs.RecommenderSystem;
import delfos.rs.collaborativefiltering.svd.SVDFoldingIn;
import delfos.rs.recommendation.Recommendation;
import delfos.rs.recommendation.RecommendationComputationDetails;
import delfos.rs.recommendation.Recommendations;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.jdom2.JDOMException;

/**
 * Group recommender system proposed in the paper.
 * <p>
 * <p>
 * Castro, Jorge, Francisco J. Quesada, Iván Palomares, and Luis Martínez. "A
 * Consensus‐Driven Group Recommender System." International Journal of
 * Intelligent Systems 30, no. 8 (2015): 887-906.
 * <p>
 * <p>
 * This recommender system first computes each member individual
 * recommendations. After that selects a reduced subset of items using Borda's
 * count. The individual preferences over this reduced set are fed into a
 * consensus model, which bring the members' opinions closer and finally
 * computes the collective preference and returns the final ordering, which
 * composes the group recommendation.
 *
 * @author Jorge Castro Gallardo
 */
public class ConsensusGRS extends GroupRecommenderSystemAdapter<SingleRecommendationModel, GroupModelPseudoUser> {

    private static final long serialVersionUID = 1L;
    /**
     * Specifies the single user recommender system that this group recommender
     * system uses in the individual recommendation phase.
     */
    public static final Parameter SINGLE_USER_RECOMMENDER = new Parameter(
            "SINGLE_USER_RECOMMENDER",
            new RecommenderSystemParameterRestriction(new SVDFoldingIn(19, 10), RecommenderSystem.class),
            "Specifies the single user recommender system that this group recommender system uses in the individual recommendation phase..");
    /**
     * Specifies the aggregatoin technique used to aggregate the members'
     * individual recommendation and build the group profile.
     */
    public static final Parameter AGGREGATION_OPERATOR = new Parameter(
            "AGGREGATION_METHOD",
            new ParameterOwnerRestriction(AggregationOperator.class, new MinimumValue()),
            "Especifica la técnica de agregación para agregar los ratings de "
            + "los usuarios y formar el perfil del grupo."
    );

    public static final Parameter ITEM_SELECTOR = new Parameter(
            "ITEM_SELECTOR",
            new ParameterOwnerRestriction(GroupRecommendationsSelector.class, new BordaCount())
    );

    public static final Parameter DATASET_PERSISTENCE_DIRECTORY = new Parameter(
            "DATASETS_PERSISTENCE_DIRECTORY",
            new DirectoryParameter(new File("./experiments/consensus-grs-datasets/"))
    );

    public static final Parameter CONSENSUS_OUTPUT_FILES_DIRECTORY = new Parameter(
            "CONSENSUS_OUTPUT_FILES_DIRECTORY",
            new DirectoryParameter(new File("./experiments/consensus-grs-output"))
    );

    public static final Parameter CONSENSUS_INPUT_FILES_DIRECTORY = new Parameter(
            "CONSENSUS_INPUT_FILES_DIRECTORY",
            new DirectoryParameter(new File("./experiments/consensus-grs-input"))
    );

    public static final Parameter APPLY_CONSENSUS = new Parameter(
            "APPLY_CONSENSUS",
            new BooleanParameter(true)
    );

    public static final Parameter CONSENSUS_DEGREE = new Parameter(
            "CONSENSUS_DEGREE",
            new FloatParameter(0.01f, 1, 0.8f)
    );

    public ConsensusGRS() {
        super();
        addParameter(SINGLE_USER_RECOMMENDER);
        addParameter(AGGREGATION_OPERATOR);
        addParameter(ITEM_SELECTOR);
        addParameter(APPLY_CONSENSUS);
        addParameter(CONSENSUS_DEGREE);

        addParameter(DATASET_PERSISTENCE_DIRECTORY);
        addParameter(CONSENSUS_INPUT_FILES_DIRECTORY);
        addParameter(CONSENSUS_OUTPUT_FILES_DIRECTORY);
    }

    public ConsensusGRS(
            RecommenderSystem singleUserRecommender, AggregationOperator aggregationOperator, GroupRecommendationsSelector itemSelector, boolean isApplyConsensus, double consensusDegree) {

        this();
        setParameterValue(SINGLE_USER_RECOMMENDER, singleUserRecommender);
        setParameterValue(AGGREGATION_OPERATOR, aggregationOperator);
        setParameterValue(ITEM_SELECTOR, itemSelector);

        setParameterValue(APPLY_CONSENSUS, isApplyConsensus);
        setParameterValue(CONSENSUS_DEGREE, consensusDegree);
    }

    @Override
    public SingleRecommendationModel buildRecommendationModel(DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadRatingsDataset, CannotLoadContentDataset {

        saveDataset(datasetLoader);

        RecommendationModelBuildingProgressListener buildListener = this::fireBuildingProgressChangedEvent;
        RecommenderSystem singleUserRecommender = getSingleUserRecommender();
        singleUserRecommender.addRecommendationModelBuildingProgressListener(buildListener);
        Object innerRecommendationModel = singleUserRecommender.buildRecommendationModel(datasetLoader);
        singleUserRecommender.removeRecommendationModelBuildingProgressListener(buildListener);

        return new SingleRecommendationModel(innerRecommendationModel);
    }

    private void saveDataset(DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadRatingsDataset {
        File datasetPersistenceDirectory = (File) getParameterValue(DATASET_PERSISTENCE_DIRECTORY);
        if (!datasetPersistenceDirectory.exists()) {
            datasetPersistenceDirectory.mkdirs();
        }

        DatasetToCSV datasetToCSV = new DatasetToCSV(
                new File(datasetPersistenceDirectory.getAbsolutePath() + File.separator + "ratings-training-recommendation-model.csv"),
                new File(datasetPersistenceDirectory.getAbsolutePath() + File.separator + "content-training-recommendation-model.csv"),
                new File(datasetPersistenceDirectory.getAbsolutePath() + File.separator + "users-training-recommendation-model.csv"));
        datasetToCSV.saveRatingsDataset(datasetLoader.getRatingsDataset());
    }

    @Override
    public GroupModelPseudoUser buildGroupModel(DatasetLoader<? extends Rating> datasetLoader, SingleRecommendationModel RecommendationModel, GroupOfUsers groupOfUsers) throws UserNotFound, CannotLoadRatingsDataset {
        AggregationOperator aggregationOperator = getAggregationOperator();
        Map<Integer, Number> groupAggregatedProfile = getGroupProfile(datasetLoader, aggregationOperator, groupOfUsers);
        return new GroupModelPseudoUser(groupOfUsers, groupAggregatedProfile);
    }

    @Override
    public Collection<Recommendation> recommendOnly(
            DatasetLoader<? extends Rating> datasetLoader, SingleRecommendationModel RecommendationModel, GroupModelPseudoUser groupModel, GroupOfUsers groupOfUsers, java.util.Set<Integer> candidateItems)
            throws UserNotFound, ItemNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset, NotEnoughtUserInformation {

        GroupRecommendationsWithMembersRecommendations groupRecommendationsWithMembersRecommendations = recommendOnlyWithMembersRecommendations(datasetLoader, RecommendationModel, groupModel, groupOfUsers, candidateItems);

        return groupRecommendationsWithMembersRecommendations.getRecommendations();

    }

    public GroupRecommendationsWithMembersRecommendations recommendOnlyWithMembersRecommendations(
            DatasetLoader<? extends Rating> datasetLoader,
            SingleRecommendationModel RecommendationModel,
            GroupModelPseudoUser groupModel,
            GroupOfUsers groupOfUsers,
            Set<Integer> candidateItems)
            throws UserNotFound, ItemNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset, NotEnoughtUserInformation {

        final GroupRecommendationsSelector itemSelector = (GroupRecommendationsSelector) getParameterValue(ITEM_SELECTOR);
        final AggregationOperator aggregationOperator = getAggregationOperator();
        final RecommenderSystem singleUserRecommenderSystem = getSingleUserRecommender();

        final boolean isConsensusApplied = (Boolean) getParameterValue(APPLY_CONSENSUS);
        final double consensusDegree = ((Number) getParameterValue(CONSENSUS_DEGREE)).doubleValue();

        RatingsDataset<? extends Rating> ratingsDataset = datasetLoader.getRatingsDataset();

        Map<Integer, Map<Integer, Rating>> membersRatings = DatasetOperations.selectRatings((RatingsDataset<Rating>) ratingsDataset,
                groupOfUsers.getIdMembers()
        );

        saveGroupInputDataAndRequests(
                membersRatings,
                candidateItems
        );

        Map<Integer, Collection<Recommendation>> membersRecommendationsList
                = AggregationOfIndividualRecommendations.performSingleUserRecommendations(
                        groupOfUsers.getIdMembers(),
                        singleUserRecommenderSystem,
                        datasetLoader,
                        RecommendationModel,
                        candidateItems);

        Collection<Recommendation> groupRecommendationsList = AggregationOfIndividualRecommendations.aggregateLists(aggregationOperator, membersRecommendationsList);

        Set<Integer> itemsIntersection = intersectionOfRecommendations(groupRecommendationsList, membersRecommendationsList);
        groupRecommendationsList = applyItemIntersection(groupRecommendationsList, itemsIntersection);
        membersRecommendationsList = applyItemIntersection(membersRecommendationsList, itemsIntersection);

        final Set<Integer> topNSet = itemSelector.getRecommendationSelection(
                membersRecommendationsList);
        groupRecommendationsList = applyItemIntersection(groupRecommendationsList, topNSet);
        membersRecommendationsList = applyItemIntersection(membersRecommendationsList, topNSet);

        File consensusIntputXML = getConsensusInputXML(membersRecommendationsList, groupRecommendationsList, groupOfUsers);

        ConsensusOfIndividualRecommendationsToXML.writeConsensusInputXML(
                datasetLoader,
                groupRecommendationsList,
                membersRecommendationsList,
                consensusIntputXML);

        GroupRecommendations groupRecommendations = new GroupRecommendations(groupOfUsers, groupRecommendationsList, RecommendationComputationDetails.EMPTY_DETAILS);
        Recommendations[] membersRecommendations = new Recommendations[membersRecommendationsList.size()];
        {
            int i = 0;
            for (Map.Entry<Integer, Collection<Recommendation>> entry : membersRecommendationsList.entrySet()) {
                Integer idMember = entry.getKey();
                membersRecommendations[i]
                        = new Recommendations(User.getTargetId(idMember), membersRecommendationsList.get(idMember), RecommendationComputationDetails.EMPTY_DETAILS);

                i++;
            }
        }

        GroupRecommendationsWithMembersRecommendations groupRecommendationsWithMembersRecommendations
                = new GroupRecommendationsWithMembersRecommendations(groupRecommendations, membersRecommendations);

        if (!isConsensusApplied) {
            return groupRecommendationsWithMembersRecommendations;
        }

        File consensusOutputXML = getConsensusOutputXMLwithDesiredConsensusDegree(consensusIntputXML, consensusDegree);

        File consensusOutputDirectory = (File) getParameterValue(CONSENSUS_OUTPUT_FILES_DIRECTORY);
        if (!consensusOutputDirectory.exists()) {
            consensusOutputDirectory.mkdir();
        }

        if (consensusOutputXML == null) {
            System.err.println("There is no consensus for input '" + consensusIntputXML.getAbsolutePath() + "' with consensus degree " + consensusDegree);
            return groupRecommendationsWithMembersRecommendations;
        } else {
            try {
                groupRecommendationsList = ConsensusOfIndividualRecommendationsToXML.readConsensusOutputXML(consensusOutputXML).consensusRecommendations;
                GroupRecommendations newGroupRecommendations = new GroupRecommendations(groupOfUsers, groupRecommendationsList, RecommendationComputationDetails.EMPTY_DETAILS);
                return new GroupRecommendationsWithMembersRecommendations(newGroupRecommendations, membersRecommendations);
            } catch (JDOMException | IOException ex) {
                ERROR_CODES.UNRECOGNIZED_XML_ELEMENT.exit(ex);
                throw new IllegalStateException("Error at writting XML: " + ex.toString());
            }

        }
    }

    public File getConsensusInputXML(Map<Integer, Collection<Recommendation>> membersRecommendations, Collection<Recommendation> groupRecommendations, GroupOfUsers groupOfUsers) {
        final File consensusInputFilesDirectory = (File) getParameterValue(CONSENSUS_INPUT_FILES_DIRECTORY);
        File consensusIntputXML = new File(consensusInputFilesDirectory.getAbsolutePath() + File.separator + groupOfUsers.toString() + "_consensusInput.xml");
        return consensusIntputXML;
    }

    public static Set<Integer> intersectionOfRecommendations(
            Collection<Recommendation> groupRecommendations,
            Map<Integer, Collection<Recommendation>> membersRecommendations) {

        Set<Integer> itemsIntersection = new TreeSet<>();
        for (Recommendation r : groupRecommendations) {
            itemsIntersection.add(r.getIdItem());
        }
        for (int idMember : membersRecommendations.keySet()) {
            Set<Integer> thisUserIdItem_recommended = new TreeSet<>();
            membersRecommendations.get(idMember).stream().forEach((r) -> {
                thisUserIdItem_recommended.add(r.getIdItem());
            });
            itemsIntersection.retainAll(thisUserIdItem_recommended);
        }
        itemsIntersection = Collections.unmodifiableSet(itemsIntersection);

        return itemsIntersection;
    }

    public static Collection<Recommendation> applyItemIntersection(
            Collection<Recommendation> recommendations, Set<Integer> items) {
        Collection<Recommendation> recommendationsIntersected = new ArrayList<>(items.size());

        recommendations.stream()
                .filter(
                        (recommendation) -> (items.contains(recommendation.getIdItem())))
                .forEach(
                        (recommendation) -> {
                            recommendationsIntersected.add(recommendation);
                        });

        return recommendationsIntersected;
    }

    public static Map<Integer, Collection<Recommendation>> applyItemIntersection(
            Map<Integer, Collection<Recommendation>> usersRecommendations, Set<Integer> items) {

        Map<Integer, Collection<Recommendation>> userRecommendationsIntersected
                = new TreeMap<>();

        usersRecommendations.entrySet().stream().forEach((entry) -> {
            int idUser = entry.getKey();
            Collection<Recommendation> userRecommendations = entry.getValue();

            userRecommendationsIntersected.put(idUser, new ArrayList<>(items.size()));

            userRecommendations.stream()
                    .filter((recommendation) -> (items.contains(recommendation.getIdItem())))
                    .forEach((recommendation) -> {
                        userRecommendationsIntersected.get(idUser).add(recommendation);
                    });
        });
        return userRecommendationsIntersected;
    }

    public <RatingType extends Rating> void saveGroupInputDataAndRequests(
            Map<Integer, Map<Integer, RatingType>> membersRatings,
            Collection<Integer> candidateItems) {

        File consensusInputFilesDirectory = (File) getParameterValue(CONSENSUS_INPUT_FILES_DIRECTORY);
        if (!consensusInputFilesDirectory.exists()) {
            boolean mkdirs = consensusInputFilesDirectory.mkdirs();
            if (!mkdirs) {
                throw new IllegalStateException("Could not create '" + consensusInputFilesDirectory.getAbsolutePath() + "' dir");
            }
        }

        HashCodeBuilder hashBuilder = new HashCodeBuilder(37, 11);
        hashBuilder.append(membersRatings);
        hashBuilder.append(candidateItems);
        File groupPredictionRequestsFile = new File(
                consensusInputFilesDirectory.getAbsolutePath() + File.separator
                + membersRatings.keySet() + "_groupDataAndRequests.xml");

        ConsensusOfIndividualRecommendationsToXML.writeRecommendationMembersRatingsXML(membersRatings, candidateItems, groupPredictionRequestsFile);
    }

    @Override
    public boolean isRatingPredictorRS() {
        return true;
    }

    public AggregationOperator getAggregationOperator() {
        return (AggregationOperator) getParameterValue(AGGREGATION_OPERATOR);
    }

    public RecommenderSystem getSingleUserRecommender() {
        return (RecommenderSystem) getParameterValue(SINGLE_USER_RECOMMENDER);
    }

    public static Map<Integer, Number> getGroupProfile(DatasetLoader<? extends Rating> datasetLoader, AggregationOperator aggregationOperator, GroupOfUsers groupOfUsers) throws UserNotFound, CannotLoadRatingsDataset {

        //Generate groupProfile:
        Map<Integer, List<Number>> groupRatingsList = new TreeMap<>();

        for (int idUser : groupOfUsers.getIdMembers()) {
            Map<Integer, ? extends Rating> userRatingsRated = datasetLoader.getRatingsDataset().getUserRatingsRated(idUser);
            for (int idItem : userRatingsRated.keySet()) {
                if (!groupRatingsList.containsKey(idItem)) {
                    groupRatingsList.put(idItem, new LinkedList<>());
                }
                groupRatingsList.get(idItem).add(userRatingsRated.get(idItem).getRatingValue());
            }
        }

        //Aggregate profiles
        Map<Integer, Number> groupRatings = new TreeMap<>();
        groupRatingsList.keySet().stream().forEach((idItem) -> {
            List<Number> lista = groupRatingsList.get(idItem);
            float aggregateValue = aggregationOperator.aggregateValues(lista);
            groupRatings.put(idItem, aggregateValue);
        });

        return groupRatings;
    }

    public File getConsensusOutputXMLwithDesiredConsensusDegree(File consensusInputXML, double consensusDegree) {
        File consensusOutputDirectory = (File) getParameterValue(CONSENSUS_OUTPUT_FILES_DIRECTORY);

        String consensusInputXMLFileNameNoExtension = consensusInputXML.getName().substring(0, consensusInputXML.getName().lastIndexOf("."));

        String consensusInputXMLInOutputDirectoryAbsolutePath = consensusOutputDirectory.getAbsolutePath() + File.separator + consensusInputXMLFileNameNoExtension;

        File consensusInputXMLInOutputDirectory = new File(consensusInputXMLInOutputDirectoryAbsolutePath);

        if (!consensusInputXML.exists()) {
            Global.showWarning("The input XML '" + consensusInputXMLInOutputDirectory + "' does not exists in the output directory");
            return null;
        }

        if (!consensusOutputDirectory.exists()) {
            Global.showWarning("'" + consensusOutputDirectory.getAbsolutePath() + "' not exists");
            return null;
        }

        if (!consensusOutputDirectory.isDirectory()) {
            Global.showWarning("'" + consensusOutputDirectory.getAbsolutePath() + "' is not a directory");
            return null;
        }

        List<File> childrenFiles = new ArrayList<>(Arrays.asList(consensusOutputDirectory.listFiles()));
        PriorityQueue<PriorityItem<File>> queue = new PriorityQueue<>(Collections.reverseOrder());

        for (File consensusOutputFile : childrenFiles) {
            final String outputFileNameNoExtension = consensusOutputFile.getName().substring(0, consensusOutputFile.getName().lastIndexOf("."));
            if (outputFileNameNoExtension.startsWith(consensusInputXMLFileNameNoExtension) && outputFileNameNoExtension.contains("Consenso")) {
                try {
                    Global.showln(consensusOutputFile.getAbsolutePath());
                    double thisFileConsensusDegree = ConsensusOfIndividualRecommendationsToXML.readConsensusOutputXML(consensusOutputFile).consensusDegree;

                    queue.add(new PriorityItem<>(consensusOutputFile, thisFileConsensusDegree));
                } catch (JDOMException | IOException ex) {
                    Global.showWarning(ex);
                }
            }
        }

        if (queue.isEmpty()) {
            return null;
        }

        if (Global.isVerboseAnnoying()) {
            Global.showInfoMessage("Found " + queue.size() + " consensus files");
        }

        while (!queue.isEmpty()) {
            PriorityItem<File> priorityItem = queue.poll();

            double consensusDegreeThisFile = priorityItem.getPriority();

            if (consensusDegreeThisFile >= consensusDegree) {
                return priorityItem.getKey();
            }
        }

        throw new IllegalStateException("Consensus degree not reached for '" + consensusInputXMLFileNameNoExtension + "'");
    }

}
