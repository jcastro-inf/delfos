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
package delfos.group.view.grouprecommendation;

import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.loader.types.ContentDatasetLoader;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.loaders.csv.CSVfileDatasetLoader;
import delfos.group.experiment.validation.recommendableitems.NeverRatedItems;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.grs.SingleRecommendationModel;
import delfos.group.grs.benchmark.polylens.PolyLens;
import delfos.group.grs.recommendations.GroupRecommendations;
import delfos.group.view.InitialFrame;
import delfos.rs.output.RecommendationsOutputStandardRaw;
import delfos.rs.recommendation.Recommendation;
import delfos.rs.recommendation.RecommendationComputationDetails;
import java.io.File;
import java.util.Collection;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.JFrame;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class GroupRecommendationWindow extends JFrame {

    private final InitialFrame initialFrame;
    private final InitialFrame aThis;

    public GroupRecommendationWindow(InitialFrame aThis) {
        if (1 == 1) {
            throw new UnsupportedOperationException("Not yet implemented");
        }
        this.aThis = aThis;
    }

    public void recomendar() throws CannotLoadRatingsDataset, CannotLoadContentDataset, UserNotFound, ItemNotFound {
        //Recomendar

        PolyLens groupRecommenderSystem = new PolyLens();
        DatasetLoader<? extends Rating> datasetLoader = new CSVfileDatasetLoader();
        datasetLoader.setParameterValue(CSVfileDatasetLoader.RATINGS_FILE, new File("datasets" + File.separator + "dummyRatings.csv"));
        datasetLoader.setParameterValue(CSVfileDatasetLoader.CONTENT_FILE, new File("datasets" + File.separator + "dummyMovies.csv"));
        datasetLoader.setParameterValue(CSVfileDatasetLoader.INDEXATION, CSVfileDatasetLoader.INDEX_BOTH);

        GroupOfUsers groupOfUsers = new GroupOfUsers();

        Random r = new Random(System.currentTimeMillis());
        RatingsDataset<? extends Rating> ratingsDataset = datasetLoader.getRatingsDataset();
        Integer[] allUsers = ratingsDataset.allUsers().toArray(new Integer[0]);
        while (groupOfUsers.size() < 4) {
            groupOfUsers.addUser(allUsers[r.nextInt(allUsers.length)]);
        }
        SingleRecommendationModel build = groupRecommenderSystem.buildRecommendationModel(datasetLoader);
        GroupOfUsers buildGroupModel = groupRecommenderSystem.buildGroupModel(datasetLoader, build, groupOfUsers);

        NeverRatedItems nri = new NeverRatedItems();

        Set<Integer> allItems;
        if (datasetLoader instanceof ContentDatasetLoader) {
            ContentDatasetLoader contentDatasetLoader = (ContentDatasetLoader) datasetLoader;
            allItems = new TreeSet<>(contentDatasetLoader.getContentDataset().allIDs());
        } else {
            allItems = new TreeSet<>(datasetLoader.getRatingsDataset().allRatedItems());
        }

        Collection<Recommendation> recomm = groupRecommenderSystem.recommendOnly(datasetLoader, build, buildGroupModel, groupOfUsers, allItems);

        RecommendationsOutputStandardRaw output = new RecommendationsOutputStandardRaw();
        output.writeRecommendations(new GroupRecommendations(groupOfUsers, recomm, RecommendationComputationDetails.EMPTY_DETAILS));

    }
}
