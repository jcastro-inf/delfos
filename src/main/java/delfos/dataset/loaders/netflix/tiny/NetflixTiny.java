package delfos.dataset.loaders.netflix.tiny;

import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.CannotLoadUsersDataset;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.DirectoryParameter;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.item.ContentDatasetDefault;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.ContentDatasetLoader;
import delfos.dataset.basic.loader.types.DatasetLoaderAbstract;
import delfos.dataset.basic.loader.types.UsersDatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.user.User;
import delfos.dataset.basic.user.UsersDataset;
import delfos.dataset.basic.user.UsersDatasetAdapter;
import delfos.dataset.storage.memory.BothIndexRatingsDataset;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class NetflixTiny extends DatasetLoaderAbstract<Rating> implements ContentDatasetLoader, UsersDatasetLoader {

    public static final Parameter datasetDirectory = new Parameter(
            "directory",
            new DirectoryParameter(new File(
                    File.separator +
                            "home" + File.separator +
                            "jcastro" + File.separator +
                            "Dropbox" +File.separator+
                            "Datasets-new" +File.separator+
                            "netflix-tiny" +File.separator+
                            "netflix_3m1k" +File.separator
            )));

    public static final String arff_fileName = "netflix_3m1k.arff";
    public static final String itemSim = "netflix_3m1k_itemSim.txt";
    public static final String split = "netflix_3m1k_split.txt";
    public static final String userSim = "netflix_3m1k_userSim.txt";

    ContentDataset contentDataset;
    RatingsDataset<Rating> ratingsDataset;
    UsersDataset usersDataset;

    public NetflixTiny(){
        addParameter(datasetDirectory);
    }

    @Override
    public ContentDataset getContentDataset() throws CannotLoadContentDataset{
        if(contentDataset==null) {
            loadDataset(getDirectory());
        }
        return contentDataset;
    }

    @Override
    public RatingsDataset<Rating> getRatingsDataset() throws CannotLoadRatingsDataset {
        if(ratingsDataset==null){
            loadDataset(getDirectory());
        }
        return ratingsDataset;
    }

    @Override
    public UsersDataset getUsersDataset() throws CannotLoadUsersDataset {
        if(usersDataset==null){
            loadDataset(getDirectory());
        }
        return usersDataset;
    }
    public File getDirectory(){
        return (File) getParameterValue(datasetDirectory);
    }

    public void loadDataset(File directory) {
        File arff = new File(directory.getPath()+File.separator+arff_fileName);


        try (BufferedReader br = new BufferedReader(new FileReader(arff))) {

            ArrayList<String> lines = new ArrayList<>();
            {
                String line = br.readLine();
                while (line != null) {
                    lines.add(line);
                    line = br.readLine();
                }
            }


            List<String> linesForItemRatings = lines.stream().sequential()
                    .filter(line -> line.contains("@ATTRIBUTE"))
                    .filter(line -> line.contains("Rate for "))
                    .map(line -> line.replaceAll("\t"," "))
                    .collect(Collectors.toList());

            ContentDataset contentDataset = createContentDataset(linesForItemRatings);
            this.contentDataset = contentDataset;

            String dataMarker = "@DATA";

            List<Integer> dataMarkerIndexes = IntStream.range(0, lines.size()).parallel().boxed().filter(index -> lines.get(index).equals(dataMarker)).collect(Collectors.toList());
            if(dataMarkerIndexes.isEmpty()){
                throw new IllegalArgumentException("No data marker.");
            }else if(dataMarkerIndexes.size() > 1 ){
                throw new IllegalArgumentException("More than one data markers.");
            }

            List<String> dataLines = lines.parallelStream()
                    .filter(line -> line.startsWith("{"))
                    .collect(Collectors.toList());

            Set<User> users = dataLines.parallelStream().filter(line -> !line.isEmpty())
                    .map(line -> {
                        String modifiedLine = line;
                        modifiedLine = modifiedLine.replaceAll("\\{0 ","");
                        modifiedLine = modifiedLine.substring(0, modifiedLine.indexOf(","));
                        return  new User(new Integer(modifiedLine));
                    })
                    .collect(Collectors.toSet());

            UsersDatasetAdapter _usersDataset = new UsersDatasetAdapter(users);
            this.usersDataset = _usersDataset;

            List<Rating> ratings = dataLines.stream().filter(line -> !line.isEmpty())
                    .map(line -> line.replaceAll("\\{", " "))
                    .map(line -> line.replaceAll("\\}", " "))
                    .flatMap(line -> {

                        String[] ratingsFields = line.split(",");
                        String userField = ratingsFields[0].trim();
                        String userMarkerSt = userField.split(" ")[0];
                        String idUserString = userField.split(" ")[1];

                        User user = _usersDataset.getUser(new Integer(idUserString));

                        List<Rating> ratingsThisUser = Arrays.asList(ratingsFields).subList(1,ratingsFields.length).stream()
                                .map(ratingField -> {
                                    String idItemString = ratingField.trim().split(" ")[0];
                                    Integer idItem = new Integer(idItemString);

                                    if(!contentDataset.allIDs().contains(idItem)){
                                        return null;
                                    }
                                    Item item = contentDataset.getItem(idItem);

                                    String ratingValueString = ratingField.trim().split(" ")[1];
                                    Integer ratingValue = new Integer(ratingValueString);
                                    return new Rating(user.getId(), item.getId(), ratingValue);
                                })
                                .filter(rating -> rating!= null)
                                .collect(Collectors.toList());
                        return ratingsThisUser.stream();
                    }).collect(Collectors.toList());

            this.ratingsDataset = new BothIndexRatingsDataset<>(ratings);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ContentDataset createContentDataset(List<String> linesForItemRatings) {
        Set<Item> items = linesForItemRatings.stream()
                .map(line -> {
                    int idItem = extractIdItem(line);
                    String name = extractMovieName(line);
                    System.out.println(idItem + "\t"+name);
                    return new Item(idItem, name);
                }).collect(Collectors.toSet());
        return new ContentDatasetDefault(items);
    }

    private final Pattern patternIdItem = Pattern.compile(".*\\[(.*)\\].*");

    private int extractIdItem(String line) {

        Matcher matcher = patternIdItem.matcher(line);

        boolean matches = matcher.matches();
        if(!matches){
            throw new IllegalArgumentException("Error reading idItem from line: "+line);
        }

        String idItemString = matcher.group(1);
        return new Integer(idItemString);
    }

    private final Pattern patternName = Pattern.compile(".*'Rate for (.*)\\[.*");

    private String extractMovieName(String line) {

        Matcher matcher = patternName.matcher(line);

        boolean matches = matcher.matches();
        if(!matches){
            throw new IllegalArgumentException("Error reading name from line: "+line);
        }

        String name = matcher.group(1);
        return name;
    }
}
