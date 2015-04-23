package delfos.rs.collaborativefiltering;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import delfos.common.Global;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.storage.memory.BothIndexRatingsDataset;
import delfos.rs.recommendation.Recommendation;

/**
 * Sistema de recomendación para comprobar que las particiones de
 * entrenamiento/evaluación se realizan correctamente. Comprueba que no se
 * solicitan ratings que se usaron en la fase de construcción.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 1.0 21-02-2013
 */
public class Recommender_DatasetProperties extends CollaborativeRecommender<Number> {

    private static final long serialVersionUID = 1L;
    private BothIndexRatingsDataset copyOfTrainingDataset;

    @Override
    public Number buildRecommendationModel(DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadRatingsDataset {
        this.copyOfTrainingDataset = new BothIndexRatingsDataset(datasetLoader.getRatingsDataset());

        Global.showMessage("Showing statistics about the dataset\n");
        Global.showMessage("Num ratings " + datasetLoader.getRatingsDataset().getNumRatings() + "\n");
        Global.showMessage("Num users   " + datasetLoader.getRatingsDataset().allUsers().size() + "\n");
        Global.showMessage("Num items   " + datasetLoader.getRatingsDataset().allRatedItems().size() + "\n");

        Map<Integer, Integer> usersRatingsNum = new TreeMap<>();
        Map<Integer, Integer> itemsRatingsNum = new TreeMap<>();
        RatingsDataset<? extends Rating> ratingsDataset = datasetLoader.getRatingsDataset();
        for (Rating rating : ratingsDataset) {
            int idUser = rating.idUser;
            int idItem = rating.idItem;

            if (usersRatingsNum.containsKey(idUser)) {
                int numUserRatings = usersRatingsNum.get(idUser);
                usersRatingsNum.put(idUser, numUserRatings + 1);
            } else {
                usersRatingsNum.put(idUser, 1);
            }

            if (itemsRatingsNum.containsKey(idItem)) {
                int numItemRatings = itemsRatingsNum.get(idItem);
                itemsRatingsNum.put(idItem, numItemRatings + 1);
            } else {
                itemsRatingsNum.put(idItem, 1);
            }
        }

        TreeMap<Integer, Integer> userHistogram = new TreeMap<>();
        for (Map.Entry<Integer, Integer> entry : usersRatingsNum.entrySet()) {
            Integer idUser = entry.getKey();
            Integer numRatings = entry.getValue();
            if (userHistogram.containsKey(numRatings)) {
                userHistogram.put(numRatings, userHistogram.get(numRatings) + 1);
            } else {
                userHistogram.put(numRatings, 1);
            }
        }

        TreeMap<Integer, Integer> itemHistogram = new TreeMap<>();
        for (Map.Entry<Integer, Integer> entry : itemsRatingsNum.entrySet()) {
            Integer idItem = entry.getKey();
            Integer numRatings = entry.getValue();
            if (itemHistogram.containsKey(numRatings)) {
                itemHistogram.put(numRatings, itemHistogram.get(numRatings) + 1);
            } else {
                itemHistogram.put(numRatings, 1);
            }

        }

        Global.showMessage("\n\nhistograma usuarios\n");
        for (int numRatings : userHistogram.keySet()) {
            Global.showMessage("\tUsers with " + numRatings + " ratings\t----->" + userHistogram.get(numRatings) + "\n");
        }
        Global.showMessage("\n\n");
        Global.showMessage("\n\nhistograma productos\n");
        for (int numRatings : itemHistogram.keySet()) {
            Global.showMessage("\tItems with " + numRatings + " ratings\t----->" + itemHistogram.get(numRatings) + "\n");
        }

        if (Global.isVerboseAnnoying()) {
            Global.showMessage("Histograms in tab separated values format: \n\n");
            Global.showMessage("NumRatings\tNumUsers\n");
            for (int numRatings : userHistogram.keySet()) {
                Global.showMessage(numRatings + "\t" + userHistogram.get(numRatings) + "\n");
            }
            Global.showMessage("\n");

            Global.showMessage("NumRatings\tNumItems\n");
            for (int numRatings : itemHistogram.keySet()) {
                Global.showMessage(numRatings + "\t" + itemHistogram.get(numRatings) + "\n");
            }
            Global.showMessage("\n");
        }

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
