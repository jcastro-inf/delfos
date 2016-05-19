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
package delfos.main.managers.recommendation.group;

import delfos.ConsoleParameters;
import delfos.ERROR_CODES;
import delfos.UndefinedParameterException;
import delfos.common.Global;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.exceptions.ratings.NotEnoughtUserInformation;
import delfos.configfile.rs.single.RecommenderSystemConfiguration;
import delfos.configfile.rs.single.RecommenderSystemConfigurationFileParser;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.user.User;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.grs.GroupRecommenderSystem;
import delfos.group.grs.recommendations.GroupRecommendations;
import delfos.main.managers.CaseUseSubManager;
import delfos.main.managers.recommendation.ArgumentsRecommendation;
import static delfos.main.managers.recommendation.group.GroupRecommendation.GROUP_MODE;
import static delfos.main.managers.recommendation.group.GroupRecommendation.TARGET_GROUP;
import delfos.recommendationcandidates.RecommendationCandidatesSelector;
import delfos.rs.persistence.FailureInPersistence;
import delfos.rs.persistence.PersistenceMethod;
import delfos.rs.persistence.PersistenceMethodStrategy;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @version 20-oct-2014
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class Recommend extends CaseUseSubManager {

    public static Recommend getInstance() {
        return Holder.INSTANCE;
    }

    public static class Holder {

        public static final Recommend INSTANCE = new Recommend();
    }

    public Recommend() {
        super(GroupRecommendation.getInstance());
    }

    @Override
    public boolean isRightManager(ConsoleParameters consoleParameters) {
        return consoleParameters.isFlagDefined(GROUP_MODE)
                && consoleParameters.isParameterDefined(TARGET_GROUP);
    }

    @Override
    public void manageCaseUse(ConsoleParameters consoleParameters) {
        manageGroupRecommendation(consoleParameters);
    }

    public static void manageGroupRecommendation(ConsoleParameters consoleParameters) {

        String configurationFile = ArgumentsRecommendation.extractConfigurationFile(consoleParameters);
        if (!new File(configurationFile).exists()) {
            ERROR_CODES.CONFIG_FILE_NOT_EXISTS.exit(new FileNotFoundException("Configuration file '" + configurationFile + "' not found"));
        }

        RecommenderSystemConfiguration rsc = RecommenderSystemConfigurationFileParser.loadConfigFile(configurationFile);

        GroupRecommenderSystem<Object, Object> groupRecommenderSystem = getGroupRecommenderSystem(rsc);
        DatasetLoader<? extends Rating> datasetLoader = rsc.datasetLoader;
        RecommendationCandidatesSelector candidatesSelector = rsc.recommendationCandidatesSelector;
        PersistenceMethod persistenceMethod = rsc.persistenceMethod;

        GroupOfUsers targetGroup = extractTargetGroup(consoleParameters, datasetLoader);

        GroupRecommendations recommendToGroup = recommendToGroup(groupRecommenderSystem, persistenceMethod, targetGroup, datasetLoader, candidatesSelector);

        rsc.recommdendationsOutputMethod.writeRecommendations(recommendToGroup);
    }

    public static GroupRecommendations recommendToGroup(
            GroupRecommenderSystem<Object, Object> groupRecommenderSystem,
            PersistenceMethod persistenceMethod,
            GroupOfUsers targetGroup,
            DatasetLoader<? extends Rating> datasetLoader,
            RecommendationCandidatesSelector candidatesSelector) {

        GroupRecommendations recommendations = null;

        Set<Item> candidateItems;
        try {
            candidateItems = candidatesSelector.candidateItems(datasetLoader, targetGroup);
        } catch (UserNotFound ex) {
            ERROR_CODES.USER_NOT_FOUND.exit(ex);
            throw new IllegalArgumentException(ex);
        }

        if (Global.isVerboseAnnoying()) {
            Global.showInfoMessage("List of candidate items for group " + targetGroup.getIdMembers() + " size: " + candidateItems.size() + "\n");
            Global.showInfoMessage("\t" + candidateItems + "\n");
        }

        Object recommendationModel;
        try {
            Global.showMessageTimestamped("Loading recommendation model");
            recommendationModel = PersistenceMethodStrategy.loadModel(groupRecommenderSystem, persistenceMethod, targetGroup.getIdMembers(),
                    candidateItems.stream().map(item -> item.getId()).collect(Collectors.toSet()), datasetLoader);
            Global.showMessageTimestamped("Loaded recommendation model");
        } catch (FailureInPersistence ex) {
            ERROR_CODES.FAILURE_IN_PERSISTENCE.exit(ex);
            throw new IllegalArgumentException(ex);
        }

        Object groupModel;
        try {
            groupModel = groupRecommenderSystem.buildGroupModel(datasetLoader, recommendationModel, targetGroup);
            recommendations = groupRecommenderSystem.recommendOnly(
                    datasetLoader,
                    recommendationModel,
                    groupModel,
                    targetGroup,
                    candidateItems);
        } catch (UserNotFound ex) {
            ERROR_CODES.USER_NOT_FOUND.exit(ex);
            throw new IllegalArgumentException(ex);
        } catch (ItemNotFound ex) {
            ERROR_CODES.ITEM_NOT_FOUND.exit(ex);
            throw new IllegalArgumentException(ex);
        } catch (NotEnoughtUserInformation ex) {
            Global.showWarning("Recommender system '" + groupRecommenderSystem.getName() + "' reported: Not enought user information (group=" + targetGroup + ").");
            //ERROR_CODES.USER_NOT_ENOUGHT_INFORMATION.exit(ex);
            recommendations = new GroupRecommendations(targetGroup, Collections.EMPTY_LIST);
        }

        if (Global.isVerboseAnnoying()) {
            if (recommendations.getRecommendations().isEmpty()) {
                Global.showWarning("Recommendation list for group '" + targetGroup + "' is empty, check for causes.");
            } else {
                Global.showInfoMessage("Recommendation list for group '" + targetGroup + "' of size " + recommendations.getRecommendations().size() + "\n");
                Global.showInfoMessage("\t" + recommendations.toString() + "\n");
            }
        }
        return recommendations;
    }

    public static GroupRecommenderSystem getGroupRecommenderSystem(RecommenderSystemConfiguration rsc) throws RuntimeException {
        if (rsc.recommenderSystem instanceof GroupRecommenderSystem) {
            GroupRecommenderSystem groupRecommenderSystem = (GroupRecommenderSystem) rsc.recommenderSystem;
            return groupRecommenderSystem;
        } else {
            IllegalStateException ise = new IllegalStateException("Recommender '" + rsc.recommenderSystem.getAlias() + "' (class '" + rsc.recommenderSystem.getClass() + "') is not a group recomender system)");
            ERROR_CODES.NOT_A_GROUP_RECOMMENDER_SYSTEM.exit(ise);
            throw ise;
        }
    }

    public static GroupOfUsers extractTargetGroup(ConsoleParameters consoleParameters, DatasetLoader<? extends Rating> datasetLoader) throws UserNotFound, NumberFormatException {

        List<String> groupMembers;

        try {
            groupMembers = consoleParameters.getValues(GroupRecommendation.TARGET_GROUP);
        } catch (UndefinedParameterException ex) {
            Global.showWarning("Target group members must be specified through parameter '" + GroupRecommendation.TARGET_GROUP + "' values\n");
            ERROR_CODES.GROUP_NOT_DEFINED.exit(ex);
            throw ex;
        }

        List<User> members = groupMembers.stream()
                .map(idUserString -> {
                    int idUser;
                    try {
                        idUser = new Integer(idUserString);
                    } catch (NumberFormatException ex) {
                        throw ex;
                    }
                    return idUser;
                })
                .map(idUser -> datasetLoader.getUsersDataset().getUser(idUser))
                .collect(Collectors.toList());

        return new GroupOfUsers(members);
    }
}
