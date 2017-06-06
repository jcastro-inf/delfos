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

import delfos.common.Chronometer;
import delfos.common.Global;
import delfos.common.datastructures.DoubleMapping;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.domain.Domain;
import delfos.dataset.storage.memory.BothIndexRatingsDataset;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 10-dic-2013
 */
public class EPinionsRatingsDataset implements RatingsDataset<EPinionsRating> {

    private final BothIndexRatingsDataset<EPinionsRating> ratingsDataset_overItems;
    private final BothIndexRatingsDataset<EPinionsRating> ratingsDataset_overAuthors;

    private final DoubleMapping<Long, Long> usersIndex = new DoubleMapping<>();

    /**
     * Carga el fichero de valoraciones del dataset EPinions indicado.
     *
     * @param ratingsFile
     * @param epinionsContentDataset
     * @throws java.io.FileNotFoundException
     */
    public EPinionsRatingsDataset(File ratingsFile, EPinionsContentDataset epinionsContentDataset) throws FileNotFoundException, IOException {

        BufferedReader br = new BufferedReader(new FileReader(ratingsFile));
        String linea = br.readLine();

        List<EPinionsRating> ratings_overAuthors = new ArrayList<>();
        List<EPinionsRating> ratings_overItems = new ArrayList<>();

        Chronometer c = new Chronometer();
        int i = 1;

        while (linea != null) {

            String[] columns = linea.split("\t");

            final long OBJECT_ID;
            final long MEMBER_ID;
            final byte RATING;
            final boolean STATUS;
            final long CREATION;
            final long LAST_MODIFIED;
            final String TYPE;
            final long VERTICAL_ID;

            OBJECT_ID = new Long(columns[0]);

            String typeString = columns[6];
            if (typeString.isEmpty() || typeString.equals("1")) {
                TYPE = "Item";
            } else if (typeString.equals("2")) {

                //Se supone que el dataset tiene la opción de valorar otros objetos distintos de los productos.
                //TYPE = "Author";
                TYPE = "Item";
            } else {
                throw new IllegalArgumentException("Unrecognized rating type at line " + i + " of file " + ratingsFile);
            }

            MEMBER_ID = new Long(columns[1]);
            if (!usersIndex.containsType1Value(MEMBER_ID)) {
                usersIndex.add(MEMBER_ID, (long) usersIndex.size());
            }

            {
                Byte ratingValue = new Byte(columns[2]);
                //Convierto los ratings con un valor de 6 a 5, como indica la wiki del dataset.
                if (ratingValue == 6) {
                    ratingValue = 5;
                }
                RATING = ratingValue;
            }

            String statusString = columns[3];
            if (statusString.isEmpty()) {
                STATUS = false;
            } else if (statusString.equals("1")) {
                STATUS = true;
            } else if (statusString.equals("0")) {
                STATUS = false;
            } else {
                throw new IllegalStateException("Status value of '" + statusString + "' is not allowed.");
            }

            SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd");
            try {
                Date date = df.parse(columns[4]);
                CREATION = date.getTime();
            } catch (ParseException ex) {
                throw new IOException(ex);
            }

            String lastModifiedString = columns[5];
            if (lastModifiedString.isEmpty()) {
                LAST_MODIFIED = CREATION;
            } else {
                try {
                    Date date = df.parse(lastModifiedString);
                    LAST_MODIFIED = date.getTime();
                } catch (ParseException ex) {
                    throw new IOException(ex);
                }
            }

            // TODO (¿Qué es esta columna?) 10-06-2014 --> Es el idRating, es decir su identificador único.
            if (columns.length >= 8) {
                VERTICAL_ID = new Long(columns[7]);
            } else {
                VERTICAL_ID = -1;
            }

            if (TYPE.equals("Item")) {
                long idUser = usersIndex.typeOneToTypeTwo(MEMBER_ID);

                if (!epinionsContentDataset.getProductsIndex().containsType1Value(OBJECT_ID)) {
                    Global.showWarning("The item " + OBJECT_ID + " is not defined in the items set. ¿is in authors? " + epinionsContentDataset.getProductsIndex().containsType1Value(OBJECT_ID) + " ¿is in subjects? " + epinionsContentDataset.getSubjectsIndex().containsType1Value(OBJECT_ID) + " ¿Is in users? " + usersIndex.containsType1Value(OBJECT_ID));

                } else {
                    long idItem = epinionsContentDataset.getProductsIndex().typeOneToTypeTwo(OBJECT_ID);
                    EPinionsRating ePinionsRating = new EPinionsRating(idUser, idItem, RATING, STATUS, CREATION, LAST_MODIFIED, VERTICAL_ID);
                    ratings_overItems.add(ePinionsRating);
                }
            } else if (TYPE.equals("Author")) {

                if (!epinionsContentDataset.getAuthorsIndex().containsType1Value(OBJECT_ID)) {
                    Global.showWarning("The author " + OBJECT_ID + " is not defined in the authors index. ¿is in items? " + epinionsContentDataset.getProductsIndex().containsType1Value(OBJECT_ID) + " ¿is in subjects? " + epinionsContentDataset.getSubjectsIndex().containsType1Value(OBJECT_ID));
                } else {
                    long idUser = usersIndex.typeOneToTypeTwo(MEMBER_ID);
                    long idAuthor = epinionsContentDataset.getAuthorsIndex().typeOneToTypeTwo(OBJECT_ID);
                    EPinionsRating ePinionsRating = new EPinionsRating(idUser, idAuthor, RATING, STATUS, CREATION, LAST_MODIFIED, VERTICAL_ID);
                    ratings_overAuthors.add(ePinionsRating);
                }
            } else {
                throw new IllegalArgumentException("Unrecognized rating type at line " + i + " of file " + ratingsFile);
            }

            linea = br.readLine();

            if (i % 100000 == 0) {
                Global.showInfoMessage("Loading EPinions ratings --> " + i + " ratings " + c.printPartialElapsed() + " / " + c.printTotalElapsed() + "\n");
                c.setPartialEllapsedCheckpoint();
            }

            i++;
        }
        br.close();

        ratingsDataset_overItems = new BothIndexRatingsDataset<>(ratings_overItems);
        ratingsDataset_overAuthors = new BothIndexRatingsDataset<>(ratings_overAuthors);
    }

    @Override
    public EPinionsRating getRating(long idUser, long idItem) throws UserNotFound, ItemNotFound {
        return ratingsDataset_overItems.getRating(idUser, idItem);
    }

    @Override
    public Set<Long> allUsers() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Set<Long> allRatedItems() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Set<Long> getUserRated(long idUser) throws UserNotFound {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Set<Long> getItemRated(long idItem) throws ItemNotFound {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Map<Long, EPinionsRating> getUserRatingsRated(long idUser) throws UserNotFound {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Map<Long, EPinionsRating> getItemRatingsRated(long idItem) throws ItemNotFound {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double getMeanRatingItem(long idItem) throws ItemNotFound {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double getMeanRatingUser(long idUser) throws UserNotFound {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Domain getRatingsDomain() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public long getNumRatings() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public long sizeOfUserRatings(long idUser) throws UserNotFound {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public long sizeOfItemRatings(long idItem) throws ItemNotFound {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isRatedUser(long idUser) throws UserNotFound {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isRatedItem(long idItem) throws ItemNotFound {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double getMeanRating() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Iterator<EPinionsRating> iterator() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public DoubleMapping<Long, Long> getUsersIndex() {
        return usersIndex;
    }
}
