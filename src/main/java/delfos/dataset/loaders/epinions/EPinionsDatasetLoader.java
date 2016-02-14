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
package delfos.dataset.loaders.epinions;

import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.CannotLoadTrustDataset;
import delfos.common.exceptions.dataset.CannotLoadUsersDataset;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.DirectoryParameter;
import delfos.dataset.basic.loader.types.DatasetLoaderAbstract;
import delfos.dataset.basic.loader.types.TrustDatasetLoader;
import delfos.dataset.basic.user.User;
import delfos.dataset.basic.user.UsersDataset;
import delfos.dataset.basic.user.UsersDatasetAdapter;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 10-dic-2013
 */
public class EPinionsDatasetLoader extends DatasetLoaderAbstract implements TrustDatasetLoader {

    public static final Parameter EPINIONS_DATASET_DIRECTORY;

    private EPinionsRatingsDataset ratingsDataset = null;
    private EPinionsContentDataset contentDataset = null;
    private EPinionsTrustDataset trustDataset = null;

    private UsersDataset usersDataset = null;

    static {
        File epinionsDatasetDirectory = new File("." + File.separator + "datasets" + File.separator + "epinions" + File.separator);
        EPINIONS_DATASET_DIRECTORY = new Parameter("EPINIONS_DATASET_DIRECTORY", new DirectoryParameter(epinionsDatasetDirectory));
    }

    public EPinionsDatasetLoader() {
        addParameter(EPINIONS_DATASET_DIRECTORY);

        addParammeterListener(() -> {
            usersDataset = null;
            ratingsDataset = null;
            contentDataset = null;
            trustDataset = null;
        });
    }

    @Override
    public EPinionsRatingsDataset getRatingsDataset() throws CannotLoadRatingsDataset {
        if (ratingsDataset == null) {
            try {
                File ratingsFile = new File(getDatasetDirectory() + File.separator + "rating.txt");
                ratingsDataset = new EPinionsRatingsDataset(ratingsFile, getContentDataset());
            } catch (IOException ex) {
                throw new CannotLoadRatingsDataset(ex);
            }
        }

        return ratingsDataset;
    }

    public File getDatasetDirectory() {
        return (File) getParameterValue(EPINIONS_DATASET_DIRECTORY);
    }

    @Override
    public EPinionsContentDataset getContentDataset() throws CannotLoadContentDataset {

        if (contentDataset == null) {
            try {
                File contentFile = new File(getDatasetDirectory() + File.separator + "mc.txt");
                contentDataset = new EPinionsContentDataset(contentFile);
            } catch (IOException ex) {
                throw new CannotLoadRatingsDataset(ex);
            }
        }

        return contentDataset;
    }

    @Override
    public EPinionsTrustDataset getTrustDataset() throws CannotLoadTrustDataset {
        if (trustDataset == null) {
            try {
                File trustFile = new File(getDatasetDirectory() + File.separator + "user_rating.txt");
                trustDataset = new EPinionsTrustDataset(trustFile, getRatingsDataset().getUsersIndex());
            } catch (IOException ex) {
                throw new CannotLoadTrustDataset(ex);
            }
        }

        return trustDataset;
    }

    @Override
    public UsersDataset getUsersDataset() throws CannotLoadUsersDataset {
        if (usersDataset == null) {
            Set<User> users = getRatingsDataset().allUsers().stream().map(idUser -> new User(idUser)).collect(Collectors.toSet());
            usersDataset = new UsersDatasetAdapter(users);
        }

        return usersDataset;
    }

}
