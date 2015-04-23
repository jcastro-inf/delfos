package delfos.dataset.loaders.epinions.trustlet;

import com.google.common.io.LineReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.StringTokenizer;
import delfos.Path;
import delfos.common.Chronometer;
import delfos.common.Global;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.CannotLoadTrustDataset;
import delfos.common.exceptions.dataset.CannotLoadUsersDataset;
import delfos.common.exceptions.dataset.items.ItemAlreadyExists;
import delfos.common.exceptions.dataset.users.UserAlreadyExists;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.DirectoryParameter;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.item.ContentDatasetDefault;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.features.Feature;
import delfos.dataset.basic.features.FeatureGenerator;
import delfos.dataset.basic.features.FeatureType;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.trust.TrustDataset;
import delfos.dataset.basic.trust.TrustDatasetAbstract;
import delfos.dataset.basic.trust.TrustStatement;
import delfos.dataset.basic.user.User;
import delfos.dataset.basic.user.UsersDataset;
import delfos.dataset.basic.user.UsersDatasetAdapter;
import delfos.dataset.basic.loader.types.DatasetLoaderAbstract;
import delfos.dataset.basic.loader.types.ContentDatasetLoader;
import delfos.dataset.basic.loader.types.TrustDatasetLoader;
import delfos.dataset.basic.loader.types.UsersDatasetLoader;
import delfos.dataset.storage.memory.BothIndexRatingsDataset;

/**
 *
* @author Jorge Castro Gallardo
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
        String directory = Path.getDatasetDirectory();
        File epinionsDatasetDirectory = new File(directory + File.separator + "epinions" + File.separator);
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
                LineReader lineReader = new LineReader(new FileReader(trustFile));

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

                LineReader lineReader = new LineReader(new FileReader(contentFile));
                String line = lineReader.readLine();

                int i = 1;

                Chronometer c = new Chronometer();

                LinkedList<Item> items = new LinkedList<>();

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
                        Global.showMessage("Loading EPinions content --> " + i + " items " + c.printPartialElapsed() + " / " + c.printTotalElapsed() + "\n");
                        c.setPartialEllapsedCheckpoint();
                    }

                    line = lineReader.readLine();
                    lineNumber++;
                }

                Global.showMessageTimestamped("Loaded " + items.size() + " items");
                contentDataset = new ContentDatasetDefault(items);
            } catch (IOException | ItemAlreadyExists ex) {
                throw new CannotLoadContentDataset(ex);
            }

            try {
                String ratingsFile = directory + File.separator + "rating.txt";
                //graphBuilder.addRatingRelations(ratingPath);
                Collection<Rating> ratings = new LinkedList<>();

                FileReader friendshipDataFileReader = new FileReader(ratingsFile);

                LineReader lineReader = new LineReader(friendshipDataFileReader);
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

            try {
                Collection<User> users = new LinkedList<>();
                idUsers.values().stream().forEach((idUser) -> {
                    users.add(new User(idUser));
                });

                usersDataset = new UsersDatasetAdapter(users);
            } catch (UserAlreadyExists ex) {
                throw new CannotLoadUsersDataset(ex);
            }
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
