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
package delfos.rs.collaborativefiltering;

import delfos.common.Global;
import delfos.common.datastructures.histograms.HistogramCategories;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.user.UsersDataset;
import delfos.dataset.storage.memory.BothIndexRatingsDataset;
import delfos.rs.recommendation.Recommendation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

/**
 * Sistema de recomendación para comprobar que las particiones de entrenamiento/evaluación se realizan correctamente.
 * Comprueba que no se solicitan ratings que se usaron en la fase de construcción.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 21-02-2013
 */
public class Recommender_DatasetProperties extends CollaborativeRecommender<Number> {

    private static final long serialVersionUID = 1L;
    private BothIndexRatingsDataset copyOfTrainingDataset;

    @Override
    public Number buildRecommendationModel(DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadRatingsDataset {
        this.copyOfTrainingDataset = new BothIndexRatingsDataset(datasetLoader.getRatingsDataset());

        Global.showInfoMessage("Showing statistics about the dataset\n");
        Global.showInfoMessage("Num ratings " + datasetLoader.getRatingsDataset().getNumRatings() + "\n");
        Global.showInfoMessage("Num users   " + datasetLoader.getRatingsDataset().allUsers().size() + "\n");
        Global.showInfoMessage("Num items   " + datasetLoader.getRatingsDataset().allRatedItems().size() + "\n");

        RatingsDataset<? extends Rating> ratingsDataset = datasetLoader.getRatingsDataset();
        UsersDataset usersDataset = datasetLoader.getUsersDataset();

        ContentDataset contentDataset = datasetLoader.getContentDataset();

        HistogramCategories<Integer> userNumRatingsHistogram = new HistogramCategories<>();
        HistogramCategories<Integer> itemNumRatingsHistogram = new HistogramCategories<>();
        HistogramCategories<String> ratingsHistogram = new HistogramCategories<>();

        usersDataset.stream().forEach(user -> {
            userNumRatingsHistogram.addValue(ratingsDataset.getUserRated(user.getId()).size());
        });
        contentDataset.stream().forEach(item -> {
            itemNumRatingsHistogram.addValue(ratingsDataset.getItemRated(item.getId()).size());
        });

        for (Rating rating : ratingsDataset) {
            ratingsHistogram.addValue(rating.getRatingValue().toString());
        }

        System.out.println("#ratings\t#items");
        itemNumRatingsHistogram.printHistogram(System.out);

        System.out.println("#ratings\t#users");
        userNumRatingsHistogram.printHistogram(System.out);

        System.out.println("Ratings values histogram");
        ratingsHistogram.printHistogram(System.out);

        System.out.println("Users and items with at least one rating:");
        final long usersWithRatings = ratingsDataset.allUsers().parallelStream()
                .filter(idUser -> !ratingsDataset.getUserRated(idUser).isEmpty())
                .count();
        final long itemsWithRatings = ratingsDataset.allRatedItems().parallelStream()
                .filter(idItem -> !ratingsDataset.getItemRated(idItem).isEmpty())
                .count();

        System.out.println("#Users with ratings: " + usersWithRatings);
        System.out.println("#Items with ratings: " + itemsWithRatings);

        return 3;
    }

    @Override
    public Collection<Recommendation> recommendToUser(DatasetLoader<? extends Rating> datasetLoader, Number model, Integer idUser, java.util.Set<Integer> candidateItems) throws UserNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset {

        Map<Integer, Rating> userRatingsInRecommendation = new TreeMap<>();
        Map<Integer, Rating> userRatingsInTraining = new TreeMap<>();

        RatingsDataset<? extends Rating> ratingsDataset = datasetLoader.getRatingsDataset();

        if (datasetLoader.getRatingsDataset().allUsers().contains(idUser)) {
            userRatingsInRecommendation.putAll(ratingsDataset.getUserRatingsRated(idUser));
        }

        if ((copyOfTrainingDataset != null) && copyOfTrainingDataset.allUsers().contains(idUser)) {
            userRatingsInTraining.putAll(copyOfTrainingDataset.getUserRatingsRated(idUser));
        }
        userRatingsInRecommendation.putAll(ratingsDataset.getUserRatingsRated(idUser));

        for (int idItem : candidateItems) {

            boolean errors = false;
            boolean error1 = false;
            String message = "The rating is known in ";
            if (userRatingsInTraining.containsKey(idItem)) {
                message += "training ";
                errors = true;
                error1 = true;
            }

            if (userRatingsInRecommendation.containsKey(idItem)) {

                if (error1) {
                    message += "and ";
                }
                message += "recommendation ";
                errors = true;
            }
            if (errors) {
                Global.showWarning(message + "phase\n");
            }
        }
        return getRecommendationList(model, candidateItems);
    }

    private Collection<Recommendation> getRecommendationList(Number model, Collection<Integer> candidateItems) {
        Collection<Recommendation> ret = new ArrayList<>(candidateItems.size());
        for (int idItem : candidateItems) {
            ret.add(new Recommendation(idItem, model));
        }
        return ret;
    }
}
