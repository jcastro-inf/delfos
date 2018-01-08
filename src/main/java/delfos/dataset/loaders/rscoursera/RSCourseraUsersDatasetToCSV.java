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
package delfos.dataset.loaders.rscoursera;

import com.csvreader.CsvReader;
import delfos.common.Global;
import delfos.common.exceptions.dataset.CannotLoadUsersDataset;
import delfos.dataset.basic.user.User;
import delfos.dataset.basic.user.UsersDataset;
import delfos.dataset.basic.user.UsersDatasetAdapter;
import delfos.io.csv.dataset.user.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;

/**
 * Clase para leer/escribir un dataset de contenido a fichero csv.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 04-Mar-2013
 */
public class RSCourseraUsersDatasetToCSV implements UsersDatasetToCSV {

    private final int userIdColumn = 0;
    private final int userNameColumn = 1;

    public RSCourseraUsersDatasetToCSV() {
    }

    @Override
    public void writeDataset(UsersDataset usersDataset, String fileName) throws IOException {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public UsersDataset readUsersDataset(File usersFile) throws CannotLoadUsersDataset, FileNotFoundException {

        try {

            CsvReader reader = new CsvReader(
                    new FileInputStream(usersFile.getAbsolutePath()),
                    Charset.forName("UTF-8"));
            reader.setRecordDelimiter('\n');
            reader.setDelimiter(',');

            Set<User> users = new HashSet<>();

            int line = 1;
            while (reader.readRecord()) {
                try {
                    int idUser = Integer.parseInt(reader.get(userIdColumn));
                    String name = reader.get(userNameColumn);
                    users.add(new User(idUser, name));
                } catch (NumberFormatException ex) {
                    Global.showWarning("Cannot read user in line " + line + " of file " + usersFile.getAbsolutePath() + "\n");
                    Global.showWarning(ex);
                }
                line++;

            }

            reader.close();
            UsersDataset cd = new UsersDatasetAdapter(users);
            return cd;
        } catch (IOException ex) {
            throw new CannotLoadUsersDataset(ex);
        }
    }
}
