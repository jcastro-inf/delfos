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
* @author Jorge Castro Gallardo
 *
 * @version 12-Diciembre-2013
 */
public class EPinionsTrustDataset implements TrustDataset<EPinionsTrustStatement> {

    private final TrustDatasetAbstract<EPinionsTrustStatement> trustDataset;

    private final DoubleMapping<Long, Integer> usersIndex;

    public EPinionsTrustDataset(File trustFile) throws IOException {
        this(trustFile, new DoubleMapping<Long, Integer>());
    }

    public EPinionsTrustDataset(File trustFile, DoubleMapping<Long, Integer> usersIndex) throws IOException {

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
                usersIndex.add(MY_ID, usersIndex.size() + 1);
            }
            long OTHER_ID = new Long(columns[1]);
            if (!usersIndex.containsType1Value(OTHER_ID)) {
                usersIndex.add(OTHER_ID, usersIndex.size() + 1);
            }

            float VALUE = new Float(columns[2]);
            long CREATION;

            SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd");
            try {
                Date date = df.parse(columns[3]);
                CREATION = date.getTime();
            } catch (ParseException ex) {
                throw new IOException(ex);
            }

            int idUserSource = usersIndex.typeOneToTypeTwo(MY_ID);
            int idUserDestiny = usersIndex.typeOneToTypeTwo(OTHER_ID);

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
    public Collection<? extends Integer> allUsers() {
        return trustDataset.allUsers();
    }

    @Override
    public Collection<EPinionsTrustStatement> getUserTrustStatements(int idUser) throws UserNotFound {
        return trustDataset.getUserTrustStatements(idUser);
    }
}
