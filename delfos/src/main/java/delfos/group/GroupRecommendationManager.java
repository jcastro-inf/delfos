package delfos.group;

import delfos.ConsoleParameters;
import delfos.Constants;
import delfos.ERROR_CODES;
import delfos.UndefinedParameterException;
import delfos.common.Global;
import delfos.common.parallelwork.MultiThreadExecutionManager;
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
import java.util.TreeMap;
import java.util.TreeSet;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;

/**
 *
 * @author Jorge Castro Gallardo
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
        if (consoleParameters.isDefined(TOP_N_VALUE)) {
            topN = Integer.parseInt(consoleParameters.getValue(TOP_N_VALUE));
        } else {
            topN = 4;
            Global.showMessage("Using default value for topN: '" + topN + "'\n");
        }

        if (consoleParameters.isDefined(GRS_CONFIG_FILE_PARAMETER)) {
            String grsConfigFileString = consoleParameters.getValue(GRS_CONFIG_FILE_PARAMETER);
            configFile_grs = new File(grsConfigFileString);
        } else {
            Global.showWarning("Using default grs configFile: '" + configFile_grs.getAbsolutePath() + "'");
        }

        if (consoleParameters.isDefined(RS_CONFIG_FILE_PARAMETER)) {
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
        if (consoleParameters.isDefined(BUILD_MODEL_PARAMETER)) {
            Global.showMessage("Building model for grs described in file '" + configFile_grs.getAbsolutePath() + "'\n");

            Object recommendationModel = groupRecommenderSystem.buildRecommendationModel(datasetLoader);

            groupRecommenderSystem.saveRecommendationModel(grsFilePersistence, recommendationModel);

            Global.showMessage("Building model for rs described in file '" + configFile_rs.getAbsolutePath() + "'\n");
            Object recommendationModel_singleUser = recommenderSystem.buildRecommendationModel(datasetLoader);

            recommenderSystem.saveRecommendationModel(rsFilePersistence, recommendationModel_singleUser);

            correctOption = true;
        }

        if (consoleParameters.isDefined(GROUP_MEMBERS_PARAMETER)) {
            Global.showMessage("Recommending for grs described in file '" + configFile_grs.getAbsolutePath() + "'\n");

            List<Recommendation> groupRecommendations;
            Map<Integer, Collection<Recommendation>> singleUserRecommendations = new TreeMap<>();

            GroupOfUsers group;
            {
                List<String> list;
                try {
                    list = consoleParameters.getValues(GROUP_MEMBERS_PARAMETER);
                } catch (UndefinedParameterException ex) {
                    ERROR_CODES.UNDEFINED_ERROR.exit(ex);
                    return false;
                }

                Global.showMessage("Group of users " + list + "\n");

                List<Integer> groupMembers = new ArrayList<>();
                for (String idUser : list) {
                    groupMembers.add(Integer.parseInt(idUser));
                }

                group = new GroupOfUsers(groupMembers);
            }

            Collection<Integer> users;
            Collection<Integer> items;

            if (grsc.datasetLoader instanceof ContentDatasetLoader) {
                ContentDatasetLoader contentDatasetLoader = (ContentDatasetLoader) datasetLoader;
                items = contentDatasetLoader.getContentDataset().allID();
            } else {
                items = grsc.datasetLoader.getRatingsDataset().allRatedItems();
            }

            if (grsc.datasetLoader instanceof UsersDatasetLoader) {
                UsersDatasetLoader usersDatasetLoader = (UsersDatasetLoader) datasetLoader;
                users = usersDatasetLoader.getUsersDataset().getAllID();
            } else {
                users = datasetLoader.getRatingsDataset().allUsers();
            }

            Global.showMessage("Dataset:\n");
            Global.showMessage("\tUsers:   " + users.size() + "\n");
            Global.showMessage("\tItems:   " + items.size() + "\n");
            Global.showMessage("\tRatings: " + grsc.datasetLoader.getRatingsDataset().getNumRatings() + "\n");
            Set<Integer> candidateItems = new TreeSet<>();
            candidateItems.addAll(items);

            for (int idMember : group) {
                candidateItems.removeAll(datasetLoader.getRatingsDataset().getUserRated(idMember));
            }
            candidateItems = Collections.unmodifiableSet(candidateItems);

            Object recommendationModel_grs = groupRecommenderSystem.loadRecommendationModel(
                    grsFilePersistence,
                    users,
                    items);

            Object groupModel = groupRecommenderSystem.buildGroupModel(datasetLoader, recommendationModel_grs, group);

            groupRecommendations = new ArrayList<>(groupRecommenderSystem.recommendOnly(datasetLoader, recommendationModel_grs, groupModel, group, candidateItems));
            Collections.sort(groupRecommendations);

            Object recommendationModel_singleUser = recommenderSystem.loadRecommendationModel(rsFilePersistence, users, items);

            List<SingleUserRecommendationTask> tareas = new LinkedList<>();

            for (int idUser : group) {
                tareas.add(new SingleUserRecommendationTask(
                        recommenderSystem,
                        datasetLoader,
                        recommendationModel_singleUser,
                        idUser,
                        candidateItems));
            }

            MultiThreadExecutionManager<SingleUserRecommendationTask> multiThreadExecutionManager = new MultiThreadExecutionManager<>(
                    recommenderSystem.getName() + ":memberRecommendation",
                    tareas,
                    SingleUserRecommendationTaskExecutor.class);
            multiThreadExecutionManager.run();

            multiThreadExecutionManager.getAllFinishedTasks().stream().forEach((task) -> {
                singleUserRecommendations.put(task.getIdUser(), task.getRecommendationList());
            });

            {
                //Miro las predichas para todos.
                Set<Integer> idItem_recommended = new TreeSet<>();
                for (Recommendation r : groupRecommendations) {
                    idItem_recommended.add(r.getIdItem());
                }

                for (int idMember : singleUserRecommendations.keySet()) {
                    Set<Integer> thisUserIdItem_recommended = new TreeSet<>();
                    singleUserRecommendations.get(idMember).stream().forEach((r) -> {
                        thisUserIdItem_recommended.add(r.getIdItem());
                    });
                    idItem_recommended.retainAll(thisUserIdItem_recommended);
                }

                idItem_recommended = Collections.unmodifiableSet(idItem_recommended);

                for (Iterator<Recommendation> it = groupRecommendations.iterator(); it.hasNext();) {
                    Recommendation r = it.next();
                    if (!idItem_recommended.contains(r.getIdItem())) {
                        it.remove();
                    }
                }

                for (int idMember : singleUserRecommendations.keySet()) {
                    singleUserRecommendations.put(idMember, new LinkedList<>(singleUserRecommendations.get(idMember)));

                    for (Iterator<Recommendation> it = singleUserRecommendations.get(idMember).iterator(); it.hasNext();) {
                        Recommendation r = it.next();
                        if (!idItem_recommended.contains(r.getIdItem())) {
                            it.remove();
                        }
                    }
                }

                Global.showMessage("Finished intersection of recommendations.\n");
            }

            {
                //Selection of top-N recommended items
                groupRecommendations = groupRecommendations.subList(0, Math.min(groupRecommendations.size(), topN));

                //Miro las predichas para el grupo.
                Set<Integer> topNforGroup = new TreeSet<>();
                for (Recommendation r : groupRecommendations) {
                    topNforGroup.add(r.getIdItem());
                }

                topNforGroup = Collections.unmodifiableSet(topNforGroup);

                for (int idMember : singleUserRecommendations.keySet()) {
                    singleUserRecommendations.put(idMember, new LinkedList<>(singleUserRecommendations.get(idMember)));

                    for (Iterator<Recommendation> it = singleUserRecommendations.get(idMember).iterator(); it.hasNext();) {
                        Recommendation r = it.next();
                        if (!topNforGroup.contains(r.getIdItem())) {
                            it.remove();
                        }
                    }
                }

                Global.showMessage("Finished topN selection of recommendations.\n");

            }
            File outputFile = new File("consensus_" + group.toString() + ".xml");

            if (consoleParameters.isDefined("-outputFile")) {
                String outputFileString = consoleParameters.getValue("-outputFile");
                outputFile = new File(outputFileString);
            } else {
                Global.showWarning("Using default output file: '" + configFile_grs.getAbsolutePath() + "'");
            }

            writeXML(groupRecommendations, singleUserRecommendations, outputFile);

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
            itemsIntersection.add(r.getIdItem());
        }

        //Elimino los que no aparecen recomendades para los miembros
        for (int idMember : singleUserRecommendations.keySet()) {
            Set<Integer> thisMemberItems = new TreeSet<>();
            singleUserRecommendations.get(idMember).stream().forEach((r) -> {
                thisMemberItems.add(r.getIdItem());
            });
            itemsIntersection.retainAll(thisMemberItems);
        }

        itemsIntersection = Collections.unmodifiableSet(itemsIntersection);

        for (int idMember : singleUserRecommendations.keySet()) {
            Element thisMemberElement = new Element(MEMBER_ELEMENT_NAME);
            thisMemberElement.setAttribute(MEMBER_ELEMENT_NAMEID_ATTRIBUTE_NAME, Integer.toString(idMember));
            for (Recommendation r : singleUserRecommendations.get(idMember)) {
                if (itemsIntersection.contains(r.getIdItem())) {
                    Element recommendation = new Element(RECOMMENDATION_ELEMENT_NAME);
                    recommendation.setAttribute(RECOMMENDATION_ELEMENT_ID_ITEM_ATTRIBUTE_NAME, Integer.toString(r.getIdItem()));
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
            if (itemsIntersection.contains(r.getIdItem())) {
                Element recommendation = new Element(RECOMMENDATION_ELEMENT_NAME);
                recommendation.setAttribute(RECOMMENDATION_ELEMENT_ID_ITEM_ATTRIBUTE_NAME, Integer.toString(r.getIdItem()));
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
