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
package delfos.dataset.loaders.database.mysql;

import delfos.ERROR_CODES;
import delfos.common.exceptions.DatabaseNotReady;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadUsersDataset;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.BooleanParameter;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.loader.types.ContentDatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.dataset.basic.user.User;
import delfos.dataset.basic.user.UsersDataset;
import delfos.dataset.basic.user.UsersDatasetAdapter;
import delfos.dataset.loaders.database.DatabaseContentDataset;
import delfos.dataset.loaders.database.DatabaseRatingsDataset;
import delfos.dataset.storage.memory.BothIndexRatingsDataset;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.stream.Collectors;

/**
 * Interfaz que define los métodos que un constructor de datasets para
 * recomendación basada en contenido debe implementar. Carga el dataset en
 * memoria y sólo accede a la base de datos la primera vez que se le solicitan
 * los datasets. Las sucesivas veces que se solicite, el
 * <code>MovilensDatasetConstructor</code> devolverá la referencia al objeto en
 * memoria.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version UnknownDate
 * @version 26-Noviembre-2013
 */
public class DatabaseDatasetLoader extends MySQLDatabaseDatasetLoaderAbstract implements ContentDatasetLoader {

    private static final long serialVersionUID = 1L;
    private ContentDataset mcd;
    private RatingsDataset<? extends Rating> mrd;
    private UsersDataset mud;
    public static final Parameter cache = new Parameter("cache", new BooleanParameter(Boolean.TRUE));

    public DatabaseDatasetLoader() throws DatabaseNotReady {
        super();

        addParammeterListener(() -> {
            mcd = null;
            mrd = null;
        });
    }

    /**
     * Construye el datasets de rating de una bbdd, archivo, etc.
     *
     * @return el datasets de ratings completo
     */
    @Override
    public RatingsDataset<? extends Rating> getRatingsDataset() {

        if (mrd == null) {
            if ((Boolean) getParameterValue(cache)) {
                DatabaseRatingsDataset databaseRatingsDataset = new DatabaseRatingsDataset(getConnection());
                mrd = new BothIndexRatingsDataset(databaseRatingsDataset);
            } else {
                mrd = new DatabaseRatingsDataset(getConnection());
            }
        }
        return mrd;
    }

    @Override
    public ContentDataset getContentDataset() throws CannotLoadContentDataset {
        if (mcd == null) {
            try {
                mcd = new DatabaseContentDataset(getConnection());
            } catch (ClassNotFoundException ex) {
                ERROR_CODES.DEPENDENCY_NOT_FOUND.exit(ex);
            } catch (SQLException ex) {
                ERROR_CODES.DATABASE_NOT_READY.exit(ex);
            } catch (FileNotFoundException ex) {
                throw new CannotLoadContentDataset(ex);
            } catch (IOException ex) {
                throw new CannotLoadContentDataset(ex);
            }
        }
        return mcd;
    }

    @Override
    public UsersDataset getUsersDataset() throws CannotLoadUsersDataset {
        if (mud == null) {
            mud = new UsersDatasetAdapter(mrd
                    .allUsers().stream()
                    .map(idUser -> new User(idUser))
                    .collect(Collectors.toSet()));

        }
        return mud;
    }

    @Override
    public RelevanceCriteria getDefaultRelevanceCriteria() {
        return new RelevanceCriteria(4);
    }
}
