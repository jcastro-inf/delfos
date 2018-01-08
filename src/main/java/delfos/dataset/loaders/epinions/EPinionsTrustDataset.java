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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import delfos.dataset.basic.trust.TrustDataset;
import delfos.dataset.basic.trust.TrustDatasetAbstract;
import delfos.common.Chronometer;
import delfos.common.datastructures.DoubleMapping;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.Global;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 12-Diciembre-2013
 */
public class EPinionsTrustDataset implements TrustDataset<EPinionsTrustStatement> {

    private final TrustDatasetAbstract<EPinionsTrustStatement> trustDataset;

    private final DoubleMapping<Long, Long> usersIndex;

    public EPinionsTrustDataset(File trustFile) throws IOException {
        this(trustFile, new DoubleMapping<Long, Long>());
    }

    public EPinionsTrustDataset(File trustFile, DoubleMapping<Long, Long> usersIndex) throws IOException {

        this.usersIndex = usersIndex;

        Collection<EPinionsTrustStatement> trustStatements = new LinkedList<EPinionsTrustStatement>();

        BufferedReader br = new BufferedReader(new FileReader(trustFile));
        String linea = br.readLine();

        Chronometer c = new Chronometer();
        int i = 1;

        while (linea != null) {
            String[] columns = linea.split("\t");

            long MY_ID = new Long(columns[0]);
            if (!usersIndex.containsType1Value(MY_ID)) {
                usersIndex.add(MY_ID, (long) (usersIndex.size() + 1));
            }
            long OTHER_ID = new Long(columns[1]);
            if (!usersIndex.containsType1Value(OTHER_ID)) {
                usersIndex.add(OTHER_ID, (long) (usersIndex.size() + 1));
            }

            double VALUE = new Double(columns[2]);
            long CREATION;

            SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd");
            try {
                Date date = df.parse(columns[3]);
                CREATION = date.getTime();
            } catch (ParseException ex) {
                throw new IOException(ex);
            }

            long idUserSource = usersIndex.typeOneToTypeTwo(MY_ID);
            long idUserDestiny = usersIndex.typeOneToTypeTwo(OTHER_ID);

            trustStatements.add(new EPinionsTrustStatement(idUserSource, idUserDestiny, VALUE, CREATION));

            linea = br.readLine();
            if (i % 100000 == 0) {
                Global.showInfoMessage("Loading EPinions trust --> " + i + " trust statements " + c.printPartialElapsed() + " / " + c.printTotalElapsed() + "\n");
                c.setPartialEllapsedCheckpoint();
            }
            i++;
        }

        trustDataset = new TrustDatasetAbstract<EPinionsTrustStatement>(trustStatements);
    }

    @Override
    public Collection<? extends Long> allUsers() {
        return trustDataset.allUsers();
    }

    @Override
    public Collection<EPinionsTrustStatement> getUserTrustStatements(long idUser) throws UserNotFound {
        return trustDataset.getUserTrustStatements(idUser);
    }
}
