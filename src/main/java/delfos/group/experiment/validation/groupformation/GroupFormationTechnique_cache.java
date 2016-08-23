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

import delfos.Constants;
import delfos.common.FileUtilities;
import delfos.common.Global;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.DirectoryParameter;
import delfos.common.parameters.restriction.ParameterOwnerRestriction;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.user.User;
import delfos.group.groupsofusers.GroupOfUsers;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.stream.Collectors;
import org.apache.commons.collections4.map.LRUMap;

public class GroupFormationTechnique_cache extends GroupFormationTechnique {

    public GroupFormationTechnique_cache() {
        super();
        addParameter(GROUP_FORMATION_TECHNIQUE);
        addParameter(CACHE_DIRECTORY_PARAMETER);
    }

    private static final long serialVersionUID = 1L;

    public static final File DEFAULT_DIRECTORY = new File(
            Constants.getTempDirectory() + File.separator
            + "GroupFormationTechnique_cache" + File.separator);

    public static final String EXTENSION = "groups";
    public static final Parameter CACHE_DIRECTORY_PARAMETER = new Parameter(
            "persistenceFileDirectory",
            new DirectoryParameter(DEFAULT_DIRECTORY));

    public static final Parameter GROUP_FORMATION_TECHNIQUE = new Parameter(
            "groupFormationTechnique",
            new ParameterOwnerRestriction(GroupFormationTechnique.class, new FixedGroupSize_OnlyNGroups(1, 5)));

    private static final Object EXMUT = "GeneralExMut";

    private static final LRUMap<String, Object> CACHE = new LRUMap<>(100);

    @Override
    public Collection<GroupOfUsers> generateGroups(DatasetLoader<? extends Rating> datasetLoader, Collection<User> usersAllowed) throws CannotLoadRatingsDataset {

        final GroupFormationTechnique groupFormationTechnique = getGroupFormationTechnique();
        final int datasetHashCode = datasetLoader.hashCode();
        final int groupFormationTechniqueHashCode = groupFormationTechnique.hashCode();
        String recommendationModelKey = "dl=" + datasetHashCode + "_rs=" + groupFormationTechniqueHashCode;

        recommendationModelKey = recommendationModelKey + "";

        boolean buildModel = false;

        synchronized (EXMUT) {
            if (CACHE.containsKey(recommendationModelKey)
                    && CACHE.get(recommendationModelKey) != null) {

                return (Collection<GroupOfUsers>) CACHE.get(recommendationModelKey);
            }
            if (!CACHE.containsKey(recommendationModelKey)) {
                CACHE.put(recommendationModelKey, null);
                buildModel = true;
            } else {
                try {
                    EXMUT.wait();
                } catch (InterruptedException ex) {

                }
            }

        }

        if (buildModel) {
            Collection<GroupOfUsers> groupsGenerated = actuallyGenerateGroups(datasetLoader, groupFormationTechnique);

            synchronized (EXMUT) {
                CACHE.put(recommendationModelKey, groupsGenerated);
                EXMUT.notifyAll();
            }
            return groupsGenerated;
        } else {
            return generateGroups(datasetLoader);
        }

    }

    public GroupFormationTechnique getGroupFormationTechnique() {
        return (GroupFormationTechnique) getParameterValue(GROUP_FORMATION_TECHNIQUE);
    }

    private Collection<GroupOfUsers> actuallyGenerateGroups(DatasetLoader<? extends Rating> datasetLoader, GroupFormationTechnique groupFormationTechnique) {
        Collection<GroupOfUsers> groupsGenerated;
        int ratingsDatasetHashCode = datasetLoader.getRatingsDataset().hashCode();
        String datasetLoaderAlias = datasetLoader.getAlias();
        String groupFormationString = "_gftHash=" + groupFormationTechnique.hashCode();
        String datasetLoaderString = "_datasetLoader=" + datasetLoaderAlias + "_DLHash=" + ratingsDatasetHashCode;

        File file = new File(getDirectory().getPath() + File.separator + groupFormationTechnique.getName() + groupFormationString + datasetLoaderString);

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Collection<Collection<Integer>> groupsByIdUser = (Collection<Collection<Integer>>) ois.readObject();
            if (groupsByIdUser == null) {
                Global.showWarning("The loaded group formation is null. (GroupFormationTechnique: " + groupFormationTechnique.getClass().getName() + ")");
                throw new IllegalStateException("The loaded group formation is null. (GroupFormationTechnique: " + groupFormationTechnique.getClass().getName() + ")");
            }

            groupsGenerated = groupsByIdUser.stream()
                    .map(groupOfUsers -> groupOfUsers.stream().map(idUser -> datasetLoader.getUsersDataset().get(idUser)).collect(Collectors.toList()))
                    .map(users -> new GroupOfUsers(users))
                    .collect(Collectors.toList());

            return groupsGenerated;
        } catch (NotSerializableException ex) {
            Global.showWarning("The system " + this.getClass() + " has a model not serializable.");
            throw new UnsupportedOperationException(ex);
        } catch (Throwable anyException) {
            Global.showMessageTimestamped("Generating groups: " + file.getAbsolutePath() + "\n");

            final GroupFormationTechniqueProgressListener_default listener = new GroupFormationTechniqueProgressListener_default(System.out, 300000);
            if (Global.isInfoPrinted()) {
                groupFormationTechnique.addListener(listener);
            }
            groupsGenerated = groupFormationTechnique.generateGroups(datasetLoader);
            if (Global.isInfoPrinted()) {
                groupFormationTechnique.removeListener(listener);
            }

            //Save groups
            if (FileUtilities.createDirectoriesForFileIfNotExist(file)) {
                Global.showWarning("Created directory path " + file.getAbsoluteFile().getParentFile() + " for recommendation model");
            }

            Collection<Collection<Integer>> groupsGeneratedIDs = groupsGenerated.stream().map(group -> group.getIdMembers()).collect(Collectors.toList());

            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
                oos.writeObject(groupsGeneratedIDs);
            } catch (NotSerializableException ex) {
                Global.showWarning("The system " + this.getClass() + " has a model not serializable.");
                throw new UnsupportedOperationException(ex);
            } catch (Throwable ex) {
                throw new IllegalStateException(ex);
            }
        }
        return groupsGenerated;
    }

    public GroupFormationTechnique_cache setGroupFormationTechnique(GroupFormationTechnique groupFormationTechnique) {
        setParameterValue(GROUP_FORMATION_TECHNIQUE, groupFormationTechnique);
        return this;
    }

    public File getDirectory() {
        return (File) getParameterValue(CACHE_DIRECTORY_PARAMETER);
    }

    @Override
    public int getGroupSize() {
        return getGroupFormationTechnique().getGroupSize();
    }

}
