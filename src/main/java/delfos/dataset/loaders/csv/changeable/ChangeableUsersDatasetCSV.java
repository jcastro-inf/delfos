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
package delfos.dataset.loaders.csv.changeable;

import delfos.ERROR_CODES;
import delfos.common.exceptions.dataset.CannotSaveUsersDataset;
import delfos.common.exceptions.dataset.entity.EntityNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.parameters.ParameterListener;
import delfos.dataset.basic.features.CollectionOfEntitiesWithFeaturesDefault;
import delfos.dataset.basic.user.User;
import delfos.dataset.changeable.ChangeableUsersDataset;
import delfos.io.csv.dataset.user.DefaultUsersDatasetToCSV;
import delfos.io.csv.dataset.user.UsersDatasetToCSV;
import java.io.File;
import java.io.IOException;
import java.util.Set;

/**
 * Implementa un dataset de contenido con persistencia sobre fichero CSV con la
 * posibilidad de modificar los productos del mismo.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 16-sep-2013
 */
public class ChangeableUsersDatasetCSV extends CollectionOfEntitiesWithFeaturesDefault<User> implements ChangeableUsersDataset {

    private final ChangeableCSVFileDatasetLoader parent;

    public ChangeableUsersDatasetCSV(final ChangeableCSVFileDatasetLoader parent, Set<User> users) {
        super();
        this.parent = parent;
        parent.addParammeterListener(new ParameterListener() {
            private File usersDatasetFile = null;

            @Override
            public void parameterChanged() {
                if (usersDatasetFile == null) {
                    usersDatasetFile = parent.getUsersDatasetFile();
                } else {
                    if (!usersDatasetFile.equals(parent.getUsersDatasetFile())) {
                        try {
                            commitChangesInPersistence();
                        } catch (CannotSaveUsersDataset ex) {
                            ERROR_CODES.CANNOT_WRITE_USERS_DATASET.exit(ex);
                            throw new IllegalStateException(ex);
                        }
                    }
                }
            }
        });

        for (User user : users) {
            addUser(user);
        }
    }

    protected ChangeableUsersDatasetCSV(final ChangeableCSVFileDatasetLoader parent) {
        super();
        this.parent = parent;

        parent.addParammeterListener(new ParameterListener() {
            private File usersDatasetFile = null;

            @Override
            public void parameterChanged() {
                if (usersDatasetFile == null) {
                    usersDatasetFile = parent.getUsersDatasetFile();
                } else {
                    if (!usersDatasetFile.equals(parent.getUsersDatasetFile())) {
                        try {
                            commitChangesInPersistence();
                        } catch (CannotSaveUsersDataset ex) {
                            ERROR_CODES.CANNOT_WRITE_USERS_DATASET.exit(ex);
                            throw new IllegalStateException(ex);
                        }
                    }
                }
            }
        });
    }

    @Override
    public final void addUser(User user) {
        super.add(user);
    }

    @Override
    public User getUser(long idUser) throws UserNotFound {
        try {
            return get(idUser);
        } catch (EntityNotFound ex) {
            throw new UserNotFound(idUser, ex);
        }
    }

    @Override
    public void commitChangesInPersistence() throws CannotSaveUsersDataset {
        try {
            UsersDatasetToCSV usersDatasetToCSV = new DefaultUsersDatasetToCSV();
            usersDatasetToCSV.writeDataset(this, parent.getUsersDatasetFile().getAbsolutePath());
        } catch (IOException ex) {
            throw new CannotSaveUsersDataset(ex);
        }
    }

    @Override
    public User get(long idUser) throws EntityNotFound {
        if (entitiesById.containsKey(idUser)) {
            return entitiesById.get(idUser);
        } else {
            throw new UserNotFound(idUser);
        }
    }
}
