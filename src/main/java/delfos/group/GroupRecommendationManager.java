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
package delfos.group;

import delfos.ConsoleParameters;
import delfos.Constants;
import delfos.ERROR_CODES;
import delfos.UndefinedParameterException;
import delfos.common.Global;
import delfos.configfile.rs.single.RecommenderSystemConfiguration;
import delfos.configfile.rs.single.RecommenderSystemConfigurationFileParser;
import delfos.dataset.basic.loader.types.ContentDatasetLoader;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.loader.types.UsersDatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.experiment.casestudy.parallel.SingleUserRecommendationTask;
import delfos.experiment.casestudy.parallel.SingleUserRecommendationTaskExecutor;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.grs.GroupRecommenderSystem;
import delfos.group.grs.recommendations.GroupRecommendations;
import delfos.rs.RecommenderSystem;
import delfos.rs.persistence.FilePersistence;
import delfos.rs.recommendation.Recommendation;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class GroupRecommendationManager {

    public static final String TOP_N_VALUE = "-topN";

    public static final String GRS_CONFIG_FILE_PARAMETER = "-grsConfigFile";
    public static final String RS_CONFIG_FILE_PARAMETER = "-rsConfigFile";
    public static final String BUILD_MODEL_PARAMETER = "--build";
    public static final String GROUP_MEMBERS_PARAMETER = "-g";
    public static final String GRS_RECOMMENDATION_PARAMMETER = "-grs";

    public static final int NUM_FEATURES = 20;
    public static final int NUM_ITERATIONS = 20;

    static boolean execute(ConsoleParameters consoleParameters) throws Exception {

        File configFile_grs = new File("grsConfiguration.xml");
        File configFile_rs = new File("rsConfiguration.xml");

        int topN;
        if (consoleParameters.isParameterDefined(TOP_N_VALUE)) {
            topN = Integer.parseInt(consoleParameters.getValue(TOP_N_VALUE));
        } else {
            topN = 4;
            Global.showInfoMessage("Using default value for topN: '" + topN + "'\n");
        }

        if (consoleParameters.isParameterDefined(GRS_CONFIG_FILE_PARAMETER)) {
            String grsConfigFileString = consoleParameters.getValue(GRS_CONFIG_FILE_PARAMETER);
            configFile_grs = new File(grsConfigFileString);
        } else {
            Global.showWarning("Using default grs configFile: '" + configFile_grs.getAbsolutePath() + "'");
        }

        if (consoleParameters.isParameterDefined(RS_CONFIG_FILE_PARAMETER)) {
            String rsConfigFileString = consoleParameters.getValue(RS_CONFIG_FILE_PARAMETER);
            configFile_rs = new File(rsConfigFileString);
        } else {
            Global.showWarning("Using default rs configFile: '" + configFile_rs.getAbsolutePath() + "'");
        }

        RecommenderSystemConfiguration grsc = RecommenderSystemConfigurationFileParser.loadConfigFile(configFile_grs.getAbsolutePath());
        GroupRecommenderSystem<Object, Object> groupRecommenderSystem;
        if (grsc.recommenderSystem instanceof GroupRecommenderSystem) {
            groupRecommenderSystem = (GroupRecommenderSystem) grsc.recommenderSystem;
        } else {
            ERROR_CODES.NOT_A_GROUP_RECOMMENDER_SYSTEM.exit(new IllegalStateException(configFile_grs.getAbsolutePath() + " does not contains a group recommender system."));
            return false;
        }

        FilePersistence grsFilePersistence = (FilePersistence) grsc.persistenceMethod;
        DatasetLoader<? extends Rating> datasetLoader = grsc.datasetLoader;

        RecommenderSystemConfiguration rsc = RecommenderSystemConfigurationFileParser.loadConfigFile(configFile_rs.getAbsolutePath());
        RecommenderSystem<Object> recommenderSystem;

        if (rsc.recommenderSystem instanceof RecommenderSystem) {
            recommenderSystem = (RecommenderSystem) rsc.recommenderSystem;
        } else {
            ERROR_CODES.NOT_A_RECOMMENDER_SYSTEM.exit(new IllegalStateException(configFile_rs.getAbsolutePath() + " does not contains a recommender system."));
            return false;
        }
        FilePersistence rsFilePersistence = (FilePersistence) rsc.persistenceMethod;

        boolean correctOption = false;
        if (consoleParameters.isParameterDefined(BUILD_MODEL_PARAMETER)) {
            Global.showInfoMessage("Building model for grs described in file '" + configFile_grs.getAbsolutePath() + "'\n");

            Object recommendationModel = groupRecommenderSystem.buildRecommendationModel(datasetLoader);

            groupRecommenderSystem.saveRecommendationModel(grsFilePersistence, recommendationModel);

            Global.showInfoMessage("Building model for rs described in file '" + configFile_rs.getAbsolutePath() + "'\n");
            Object recommendationModel_singleUser = recommenderSystem.buildRecommendationModel(datasetLoader);

            recommenderSystem.saveRecommendationModel(rsFilePersistence, recommendationModel_singleUser);

            correctOption = true;
        }

        if (consoleParameters.isParameterDefined(GROUP_MEMBERS_PARAMETER)) {
            Global.showInfoMessage("Recommending for grs described in file '" + configFile_grs.getAbsolutePath() + "'\n");

            GroupRecommendations groupRecommendations;

            GroupOfUsers group;
            {
                List<String> list;
                try {
                    list = consoleParameters.getValues(GROUP_MEMBERS_PARAMETER);
                } catch (UndefinedParameterException ex) {
                    ERROR_CODES.UNDEFINED_ERROR.exit(ex);
                    return false;
                }

                Global.showInfoMessage("Group of users " + list + "\n");

                List<Integer> groupMembers = new ArrayList<>();
                for (String idUser : list) {
                    groupMembers.add(Integer.parseInt(idUser));
                }

                group = new GroupOfUsers(groupMembers.toArray(new Integer[0]));
            }

            Collection<Integer> users;
            Collection<Integer> items;

            if (grsc.datasetLoader instanceof ContentDatasetLoader) {
                ContentDatasetLoader contentDatasetLoader = (ContentDatasetLoader) datasetLoader;
                items = contentDatasetLoader.getContentDataset().allIDs();
            } else {
                items = grsc.datasetLoader.getRatingsDataset().allRatedItems();
            }

            if (grsc.datasetLoader instanceof UsersDatasetLoader) {
                UsersDatasetLoader usersDatasetLoader = (UsersDatasetLoader) datasetLoader;
                users = usersDatasetLoader.getUsersDataset().allIDs();
            } else {
                users = datasetLoader.getRatingsDataset().allUsers();
            }

            Global.showInfoMessage("Dataset:\n");
            Global.showInfoMessage("\tUsers:   " + users.size() + "\n");
            Global.showInfoMessage("\tItems:   " + items.size() + "\n");
            Global.showInfoMessage("\tRatings: " + grsc.datasetLoader.getRatingsDataset().getNumRatings() + "\n");
            Set<Integer> _candidateItems = new TreeSet<>();
            _candidateItems.addAll(items);

            for (int idMember : group) {
                _candidateItems.removeAll(datasetLoader.getRatingsDataset().getUserRated(idMember));
            }
            Set<Integer> candidateItems = Collections.unmodifiableSet(_candidateItems);

            Object recommendationModel_grs = groupRecommenderSystem.loadRecommendationModel(
                    grsFilePersistence,
                    users,
                    items);

            Object groupModel = groupRecommenderSystem.buildGroupModel(datasetLoader, recommendationModel_grs, group);

            groupRecommendations = groupRecommenderSystem.recommendOnly(
                    datasetLoader,
                    recommendationModel_grs,
                    groupModel,
                    group,
                    candidateItems.stream().map(idItem -> datasetLoader.getContentDataset().get(idItem)).collect(Collectors.toSet()));

            Object recommendationModel_singleUser = recommenderSystem.loadRecommendationModel(rsFilePersistence, users, items);

            List<SingleUserRecommendationTask> tareas = new LinkedList<>();

            for (int idUser : group) {
                tareas.add(new SingleUserRecommendationTask(
                        recommenderSystem,
                        datasetLoader,
                        recommendationModel_singleUser,
                        idUser,
                        candidateItems.stream().map(idItem -> datasetLoader.getContentDataset()
                                .get(idItem)).collect(Collectors.toSet())));
            }

            Map<Integer, Collection<Recommendation>> singleUserRecommendations
                    = group.getIdMembers().parallelStream().map(idUser -> new SingleUserRecommendationTask(
                                    recommenderSystem,
                                    datasetLoader,
                                    recommendationModel_singleUser,
                                    idUser,
                                    candidateItems.stream().map(idItem -> datasetLoader.getContentDataset().get(idItem)).collect(Collectors.toSet())))
                    .map(new SingleUserRecommendationTaskExecutor())
                    .collect(
                            Collectors.toMap(
                                    recommendationsToUser -> recommendationsToUser.getUser().getId(),
                                    recommendationsToUser -> recommendationsToUser.getRecommendations())
                    );

            {
                //Miro las predichas para todos.
                Set<Integer> idItem_recommended = new TreeSet<>();
                for (Recommendation r : groupRecommendations.getRecommendations()) {
                    idItem_recommended.add(r.getItem().getId());
                }

                for (int idMember : singleUserRecommendations.keySet()) {
                    Set<Integer> thisUserIdItem_recommended = new TreeSet<>();
                    singleUserRecommendations.get(idMember).stream().forEach((r) -> {
                        thisUserIdItem_recommended.add(r.getItem().getId());
                    });
                    idItem_recommended.retainAll(thisUserIdItem_recommended);
                }

                idItem_recommended = Collections.unmodifiableSet(idItem_recommended);

                for (Iterator<Recommendation> it = groupRecommendations.getRecommendations().iterator(); it.hasNext();) {
                    Recommendation r = it.next();
                    if (!idItem_recommended.contains(r.getItem().getId())) {
                        it.remove();
                    }
                }

                for (int idMember : singleUserRecommendations.keySet()) {
                    singleUserRecommendations.put(idMember, new LinkedList<>(singleUserRecommendations.get(idMember)));

                    for (Iterator<Recommendation> it = singleUserRecommendations.get(idMember).iterator(); it.hasNext();) {
                        Recommendation r = it.next();
                        if (!idItem_recommended.contains(r.getItem().getId())) {
                            it.remove();
                        }
                    }
                }

                Global.showInfoMessage("Finished intersection of recommendations.\n");
            }

            {

                List<Recommendation> selection = new ArrayList<>(groupRecommendations.getRecommendations())
                        .subList(0, Math.min(groupRecommendations.getRecommendations().size(), topN));
                //Selection of top-N recommended items
                groupRecommendations = new GroupRecommendations(group, selection, groupRecommendations.getRecommendationComputationDetails());

                //Miro las predichas para el grupo.
                Set<Integer> topNforGroup = new TreeSet<>();
                for (Recommendation r : groupRecommendations.getRecommendations()) {
                    topNforGroup.add(r.getItem().getId());
                }

                topNforGroup = Collections.unmodifiableSet(topNforGroup);

                for (int idMember : singleUserRecommendations.keySet()) {
                    singleUserRecommendations.put(idMember, new LinkedList<>(singleUserRecommendations.get(idMember)));

                    for (Iterator<Recommendation> it = singleUserRecommendations.get(idMember).iterator(); it.hasNext();) {
                        Recommendation r = it.next();
                        if (!topNforGroup.contains(r.getItem().getId())) {
                            it.remove();
                        }
                    }
                }

                Global.showInfoMessage("Finished topN selection of recommendations.\n");

            }
            File outputFile = new File("consensus_" + group.toString() + ".xml");

            if (consoleParameters.isParameterDefined("-outputFile")) {
                String outputFileString = consoleParameters.getValue("-outputFile");
                outputFile = new File(outputFileString);
            } else {
                Global.showWarning("Using default output file: '" + configFile_grs.getAbsolutePath() + "'");
            }

            writeXML(groupRecommendations.getRecommendations(), singleUserRecommendations, outputFile);

            correctOption = true;
        }

        return correctOption;
    }

    public static final String CASE_ROOT_ELEMENT_NAME = "Recommendations";
    public static final String MEMBER_ELEMENT_NAME = "Member";
    public static final String MEMBER_ELEMENT_NAMEID_ATTRIBUTE_NAME = "id";
    public static final String GROUP_ELEMENT_NAME = "Group";
    public static final String GROUP_ELEMENT_MEMBERS_ATTRIBUTE_NAME = "members";
    public static final String RECOMMENDATION_ELEMENT_NAME = "Recommendation";
    public static final String RECOMMENDATION_ELEMENT_ID_ITEM_ATTRIBUTE_NAME = "idItem";
    public static final String RECOMMENDATION_ELEMENT_PREFERENCE_ATTRIBUTE_NAME = "preference";

    private static void writeXML(Collection<Recommendation> groupRecommendations, Map<Integer, Collection<Recommendation>> singleUserRecommendations, File outputFile) {

        Element root = new Element(CASE_ROOT_ELEMENT_NAME);

        Set<Integer> itemsIntersection = new TreeSet<>();

        //Miro los items recomendados para el grupo
        for (Recommendation r : groupRecommendations) {
            itemsIntersection.add(r.getItem().getId());
        }

        //Elimino los que no aparecen recomendades para los miembros
        for (int idMember : singleUserRecommendations.keySet()) {
            Set<Integer> thisMemberItems = new TreeSet<>();
            singleUserRecommendations.get(idMember).stream().forEach((r) -> {
                thisMemberItems.add(r.getItem().getId());
            });
            itemsIntersection.retainAll(thisMemberItems);
        }

        itemsIntersection = Collections.unmodifiableSet(itemsIntersection);

        for (int idMember : singleUserRecommendations.keySet()) {
            Element thisMemberElement = new Element(MEMBER_ELEMENT_NAME);
            thisMemberElement.setAttribute(MEMBER_ELEMENT_NAMEID_ATTRIBUTE_NAME, Integer.toString(idMember));
            for (Recommendation r : singleUserRecommendations.get(idMember)) {
                if (itemsIntersection.contains(r.getItem().getId())) {
                    Element recommendation = new Element(RECOMMENDATION_ELEMENT_NAME);
                    recommendation.setAttribute(RECOMMENDATION_ELEMENT_ID_ITEM_ATTRIBUTE_NAME, Integer.toString(r.getItem().getId()));
                    recommendation.setAttribute(RECOMMENDATION_ELEMENT_PREFERENCE_ATTRIBUTE_NAME, Double.toString(r.getPreference().doubleValue()));
                    thisMemberElement.addContent(recommendation);
                }
            }
            root.addContent(thisMemberElement);
        }

        Element groupElement = new Element(GROUP_ELEMENT_NAME);

        StringBuilder str = new StringBuilder();

        Integer[] idMembers = singleUserRecommendations.keySet().toArray(new Integer[0]);

        str.append(idMembers[0]);
        for (int i = 1; i < idMembers.length; i++) {
            str.append(",").append(idMembers[i]);

        }

        groupElement.setAttribute(GROUP_ELEMENT_MEMBERS_ATTRIBUTE_NAME, str.toString());
        for (Recommendation r : groupRecommendations) {
            if (itemsIntersection.contains(r.getItem().getId())) {
                Element recommendation = new Element(RECOMMENDATION_ELEMENT_NAME);
                recommendation.setAttribute(RECOMMENDATION_ELEMENT_ID_ITEM_ATTRIBUTE_NAME, Integer.toString(r.getItem().getId()));
                recommendation.setAttribute(RECOMMENDATION_ELEMENT_PREFERENCE_ATTRIBUTE_NAME, Double.toString(r.getPreference().doubleValue()));
                groupElement.addContent(recommendation);
            }
        }
        root.addContent(groupElement);

        Document doc = new Document();
        doc.addContent(root);
        XMLOutputter outputter = new XMLOutputter(Constants.getXMLFormat());
        try (FileWriter fileWriter = new FileWriter(outputFile)) {
            outputter.output(doc, fileWriter);
        } catch (IOException ex) {
            ERROR_CODES.CANNOT_WRITE_RESULTS_FILE.exit(ex);
        }
    }
}
