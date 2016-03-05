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
import delfos.common.parameters.restriction.DoubleParameter;
import delfos.common.parameters.restriction.ParameterOwnerRestriction;
import delfos.common.parameters.restriction.RecommenderSystemParameterRestriction;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.util.DatasetOperations;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.grs.GroupRecommenderSystemAdapter;
import delfos.group.grs.SingleRecommendationModel;
import delfos.group.grs.aggregation.AggregationOfIndividualRatings;
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
import delfos.rs.recommendation.RecommendationsToUser;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Collectors;
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
 * @author jcastro-inf ( https://github.com/jcastro-inf )
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
            new DoubleParameter(0.01f, 1, 0.8f)
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
    public <RatingType extends Rating> GroupModelPseudoUser buildGroupModel(
            DatasetLoader<RatingType> datasetLoader,
            SingleRecommendationModel recommendationModel,
            GroupOfUsers groupOfUsers)
            throws UserNotFound, CannotLoadRatingsDataset {

        AggregationOperator aggregationOperator = getAggregationOperator();
        Map<Item, RatingType> groupAggregatedProfile = AggregationOfIndividualRatings.getGroupProfile(datasetLoader, aggregationOperator, groupOfUsers);
        return new GroupModelPseudoUser(groupOfUsers, groupAggregatedProfile);
    }

    @Override
    public <RatingType extends Rating> GroupRecommendationsWithMembersRecommendations recommendOnly(
            DatasetLoader<RatingType> datasetLoader, SingleRecommendationModel recommendationModel, GroupModelPseudoUser groupModel, GroupOfUsers groupOfUsers, Set<Item> candidateItems)
            throws UserNotFound, ItemNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset, NotEnoughtUserInformation {

        final GroupRecommendationsSelector itemSelector = (GroupRecommendationsSelector) getParameterValue(ITEM_SELECTOR);
        final AggregationOperator aggregationOperator = getAggregationOperator();
        final RecommenderSystem<? extends Object> singleUserRecommenderSystem = getSingleUserRecommender();

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
        Collection<RecommendationsToUser> membersRecommendations
                = AggregationOfIndividualRecommendations.performSingleUserRecommendations(
                        groupOfUsers.getIdMembers(),
                        singleUserRecommenderSystem,
                        datasetLoader,
                        recommendationModel,
                        candidateItems);

        GroupRecommendations groupRecommendations = new GroupRecommendations(
                groupOfUsers,
                AggregationOfIndividualRecommendations.aggregateLists(
                        aggregationOperator,
                        membersRecommendations
                )
        );

        Set<Item> itemsIntersection = AggregationOfIndividualRecommendations.intersectionOfRecommendations(
                groupRecommendations,
                membersRecommendations);

        groupRecommendations = AggregationOfIndividualRecommendations
                .applyItemIntersection(groupRecommendations, itemsIntersection);

        membersRecommendations = membersRecommendations.stream()
                .map(memberRecommendations -> AggregationOfIndividualRecommendations
                        .applyItemIntersection(memberRecommendations, itemsIntersection))
                .collect(Collectors.toList());

        final Set<Item> topNSet = itemSelector.getRecommendationSelection(
                membersRecommendations);

        groupRecommendations = AggregationOfIndividualRecommendations.applyItemIntersection(
                groupRecommendations,
                topNSet);

        membersRecommendations = membersRecommendations.stream()
                .map(memberRecommendations -> AggregationOfIndividualRecommendations
                        .applyItemIntersection(memberRecommendations, topNSet))
                .collect(Collectors.toList());

        File consensusIntputXML = getConsensusInputXML(groupRecommendations, membersRecommendations);

        ConsensusOfIndividualRecommendationsToXML.writeConsensusInputXML(
                datasetLoader,
                groupRecommendations,
                membersRecommendations,
                consensusIntputXML);

        GroupRecommendationsWithMembersRecommendations groupRecommendationsWithMembersRecommendations
                = new GroupRecommendationsWithMembersRecommendations(
                        groupRecommendations,
                        membersRecommendations.toArray(new RecommendationsToUser[0])
                );

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
                GroupRecommendations newGroupRecommendations = ConsensusOfIndividualRecommendationsToXML.readConsensusOutputXML(consensusOutputXML).groupRecommendation;

                return new GroupRecommendationsWithMembersRecommendations(newGroupRecommendations, membersRecommendations.toArray(new RecommendationsToUser[0]));
            } catch (JDOMException | IOException ex) {
                ERROR_CODES.UNRECOGNIZED_XML_ELEMENT.exit(ex);
                throw new IllegalStateException("Error at writting XML: " + ex.toString());
            }
        }
    }

    public File getConsensusInputXML(
            GroupRecommendations groupRecommendations,
            Collection<RecommendationsToUser> membersRecommendations) {

        final File consensusInputFilesDirectory = (File) getParameterValue(CONSENSUS_INPUT_FILES_DIRECTORY);
        File consensusIntputXML = new File(consensusInputFilesDirectory.getAbsolutePath()
                + File.separator + groupRecommendations.getGroupOfUsers().toString() + "_consensusInput.xml");
        return consensusIntputXML;
    }

    public <RatingType extends Rating> void saveGroupInputDataAndRequests(
            Map<Integer, Map<Integer, RatingType>> membersRatings,
            Collection<Item> candidateItems) {

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

        ConsensusOfIndividualRecommendationsToXML.writeRecommendationMembersRatingsXML(
                membersRatings,
                candidateItems.stream().map(item -> item.getId()).collect(Collectors.toSet()),
                groupPredictionRequestsFile);
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
