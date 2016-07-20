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
package delfos.dataset.loaders.epinions.trustlet;

import delfos.common.Chronometer;
import delfos.common.Global;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.CannotLoadTrustDataset;
import delfos.common.exceptions.dataset.CannotLoadUsersDataset;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.DirectoryParameter;
import delfos.dataset.basic.features.Feature;
import delfos.dataset.basic.features.FeatureGenerator;
import delfos.dataset.basic.features.FeatureType;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.item.ContentDatasetDefault;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.ContentDatasetLoader;
import delfos.dataset.basic.loader.types.DatasetLoaderAbstract;
import delfos.dataset.basic.loader.types.TrustDatasetLoader;
import delfos.dataset.basic.loader.types.UsersDatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.trust.TrustDataset;
import delfos.dataset.basic.trust.TrustDatasetAbstract;
import delfos.dataset.basic.trust.TrustStatement;
import delfos.dataset.basic.user.User;
import delfos.dataset.basic.user.UsersDataset;
import delfos.dataset.basic.user.UsersDatasetAdapter;
import delfos.dataset.storage.memory.BothIndexRatingsDataset;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 10-dic-2013
 */
public class EPinionsTrustlet extends DatasetLoaderAbstract<Rating> implements UsersDatasetLoader, ContentDatasetLoader, TrustDatasetLoader {

    public static final Parameter EPINIONS_TRUSTLET_DIRECTORY;

    private RatingsDataset<Rating> ratingsDataset = null;
    private ContentDataset contentDataset = null;
    private TrustDataset<TrustStatement> trustDataset = null;
    private UsersDataset usersDataset = null;

    static {
        File epinionsDatasetDirectory = new File("." + File.separator + "datasets" + File.separator + "epinions" + File.separator);
        EPINIONS_TRUSTLET_DIRECTORY = new Parameter("EPINIONS_TRUSTLET_DIRECTORY", new DirectoryParameter(epinionsDatasetDirectory));
    }

    public EPinionsTrustlet() {
        addParameter(EPINIONS_TRUSTLET_DIRECTORY);

        addParammeterListener(() -> {
            ratingsDataset = null;
            contentDataset = null;
            usersDataset = null;
            trustDataset = null;
        });
    }

    private File getDirectory() {
        return (File) getParameterValue(EPINIONS_TRUSTLET_DIRECTORY);
    }

    private void loadDataset() {
        if (ratingsDataset == null) {
            String directory = getDirectory().getAbsolutePath();

            Map<Long, Integer> idUsers = new HashMap<>();
            Map<Long, Integer> idItems = new HashMap<>();
            Map<Long, Integer> idAuthors = new HashMap<>();
            Map<Long, Integer> idSubjects = new HashMap<>();

            try {
                //graphBuilder.addFriendshipRelations(trustFile);

                String trustFile = directory + File.separator + "user_rating.txt";
                Collection<TrustStatement> trustStatements = new LinkedList<>();
                BufferedReader lineReader = new BufferedReader(new FileReader(trustFile));

                String line = lineReader.readLine();
                int lineNumber = 0;

                while (line != null) {
                    try {
                        StringTokenizer tokenizer = new StringTokenizer(line);
                        long sourceUser = Long.parseLong(tokenizer.nextToken());
                        long destinyUser = Long.parseLong(tokenizer.nextToken());
                        double arcValue = Double.parseDouble(tokenizer.nextToken());

                        if (!idUsers.containsKey(sourceUser)) {
                            idUsers.put(sourceUser, idUsers.size() + 1);
                        }
                        if (!idUsers.containsKey(destinyUser)) {
                            idUsers.put(destinyUser, idUsers.size() + 1);
                        }

                        int idUser = idUsers.get(sourceUser);
                        int idNeighbor = idUsers.get(destinyUser);

                        //Los trust statements tienen fecha, pero la ignoro para ahorrar tamaño en memoria.
                        //String date = tokenizer.nextToken();
                        trustStatements.add(new TrustStatement(idUser, idNeighbor, arcValue));
                    } catch (Throwable ex) {
                        Global.showWarning("Error in line " + lineNumber + " of file " + trustFile);
                        throw ex;
                    }
                    line = lineReader.readLine();
                    lineNumber++;
                }

                Global.showMessageTimestamped("Loaded " + trustStatements.size() + " trust statements");
                trustDataset = new TrustDatasetAbstract<>(trustStatements);
            } catch (IOException ex) {
                throw new CannotLoadTrustDataset(ex);
            }

            try {
                String contentFile = directory + File.separator + "mc.txt";
                //graphBuilder.addAuthorRelations(contentFile);
                FeatureGenerator featureGenerator = new FeatureGenerator();
                Feature authorFeature = featureGenerator.createFeature("author", FeatureType.Nominal);
                Feature subjectFeature = featureGenerator.createFeature("subject", FeatureType.Nominal);

                BufferedReader lineReader = new BufferedReader(new FileReader(contentFile));
                String line = lineReader.readLine();

                int i = 1;

                Chronometer c = new Chronometer();

                Set<Item> items = new TreeSet<>();

                Feature[] features = new Feature[2];
                features[0] = authorFeature;
                features[1] = subjectFeature;

                Feature[] onlyAuthorFeature = new Feature[1];
                onlyAuthorFeature[0] = authorFeature;

                int lineNumber = 1;

                while (line != null) {

                    try {

                        Item item;

                        StringTokenizer tokenizer = new StringTokenizer(line, "\\|");

                        Long itemKey = new Long(tokenizer.nextToken());
                        Long author = new Long(tokenizer.nextToken());
                        Long subject;
                        if (tokenizer.hasMoreTokens()) {
                            subject = new Long(tokenizer.nextToken());

                            if (!idItems.containsKey(itemKey)) {
                                idItems.put(itemKey, idItems.size() + 1);
                            }

                            if (!idAuthors.containsKey(author)) {
                                idAuthors.put(author, idAuthors.size() + 1);
                            }

                            if (!idSubjects.containsKey(subject)) {
                                idSubjects.put(subject, idSubjects.size() + 1);
                            }

                            int idItem = idItems.get(itemKey);
                            int idSubject = idSubjects.get(subject);
                            int idAuthor = idAuthors.get(author);

                            Object[] values = new Object[2];
                            values[0] = idAuthor;
                            values[1] = idSubject;

                            item = new Item(idItem, "Item_" + idItem, features, values);
                        } else {

                            if (!idItems.containsKey(itemKey)) {
                                idItems.put(itemKey, idItems.size() + 1);
                            }

                            if (!idAuthors.containsKey(author)) {
                                idAuthors.put(author, idAuthors.size() + 1);
                            }

                            Integer idItem = idItems.get(itemKey);
                            int idAuthor = idAuthors.get(author);

                            Object[] values = new Object[1];
                            values[0] = idAuthor;

                            item = new Item(idItem, "Item_" + idItem, onlyAuthorFeature, values);
                        }
                        items.add(item);
                    } catch (Throwable ex) {
                        Global.showWarning("Error in line " + lineNumber + " of file " + contentFile);
                        throw ex;
                    }

                    if (i % 100000 == 0) {
                        Global.showInfoMessage("Loading EPinions content --> " + i + " items " + c.printPartialElapsed() + " / " + c.printTotalElapsed() + "\n");
                        c.setPartialEllapsedCheckpoint();
                    }

                    line = lineReader.readLine();
                    lineNumber++;
                }

                Global.showMessageTimestamped("Loaded " + items.size() + " items");
                contentDataset = new ContentDatasetDefault(items);
            } catch (IOException ex) {
                throw new CannotLoadContentDataset(ex);
            }

            try {
                String ratingsFile = directory + File.separator + "rating.txt";
                //graphBuilder.addRatingRelations(ratingPath);
                Collection<Rating> ratings = new LinkedList<>();

                FileReader friendshipDataFileReader = new FileReader(ratingsFile);

                BufferedReader lineReader = new BufferedReader(friendshipDataFileReader);
                int lineNumber = 1;
                String line = lineReader.readLine();

                while (line != null) {
                    try {
                        StringTokenizer tokenizer = new StringTokenizer(line);

                        Long userKey = Long.parseLong(tokenizer.nextToken());
                        Long itemKey = Long.parseLong(tokenizer.nextToken());
                        Integer rating = Integer.parseInt(tokenizer.nextToken());

                        if (!idUsers.containsKey(userKey)) {
                            idUsers.put(userKey, idUsers.size() + 1);
                        }
                        if (!idItems.containsKey(itemKey)) {
                            idItems.put(itemKey, idItems.size() + 1);
                        }
                        int idUser = idUsers.get(userKey);
                        int idItem = idItems.get(itemKey);

                        ratings.add(new Rating(idUser, idItem, rating));

                    } catch (Throwable ex) {
                        Global.showWarning("Error in line " + lineNumber + " of file " + ratingsFile);
                        throw ex;
                    }
                    lineNumber++;

                    line = lineReader.readLine();
                }
                Global.showMessageTimestamped("Loaded " + ratings.size() + " ratings.");

                ratingsDataset = new BothIndexRatingsDataset<>(ratings);
            } catch (IOException ex) {
                throw new CannotLoadRatingsDataset(ex);
            }

            Set<User> users = idUsers.values().stream()
                    .map((idUser) -> new User(idUser))
                    .collect(Collectors.toSet());

            usersDataset = new UsersDatasetAdapter(users);

        }
    }

    @Override
    public RatingsDataset getRatingsDataset() throws CannotLoadRatingsDataset {
        loadDataset();
        return ratingsDataset;
    }

    public File getDatasetDirectory() {
        return (File) getParameterValue(EPINIONS_TRUSTLET_DIRECTORY);
    }

    @Override
    public ContentDataset getContentDataset() throws CannotLoadContentDataset {
        loadDataset();
        return contentDataset;
    }

    @Override
    public TrustDataset getTrustDataset() throws CannotLoadTrustDataset {
        loadDataset();
        return trustDataset;
    }

    @Override
    public UsersDataset getUsersDataset() throws CannotLoadUsersDataset {
        loadDataset();
        return usersDataset;
    }
}
