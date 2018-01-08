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
package delfos.rs.trustbased;

import delfos.ERROR_CODES;
import delfos.common.Global;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.BooleanParameter;
import delfos.common.parameters.restriction.IntegerParameter;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.rs.collaborativefiltering.CollaborativeRecommender;
import delfos.rs.collaborativefiltering.knn.RecommendationEntity;
import delfos.rs.collaborativefiltering.profile.Neighbor;
import delfos.rs.recommendation.Recommendation;
import delfos.rs.trustbased.implicittrustcomputation.ShambourLu_ItemBasedImplicitTrustComputation;
import delfos.rs.trustbased.implicittrustcomputation.ShambourLu_UserBasedImplicitTrustComputation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Sistema de recomendación que utiliza dos módulos basados en confianza:
 * confianza por usuarios y por productos. Se encuentra completamente descrito
 * en el paper
 *
 * <p>
 * <p>
 * Qusai Shambour, Jie Liu: An effective Recommender System by Unifying User and
 * Item Trust Information for B2B Applications. Unknow journal.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @version 1.0 24-Apr-2013
 */
public class HybridUserItemTrustBased extends CollaborativeRecommender<HybridUserItemTrustBasedModel> {

    static final long serialVersionUID = -3387516993124229948L;
    public static final Parameter USER_NEIGHBOUR_SIZE = new Parameter("USER_NEIGHBOUR_SIZE", new IntegerParameter(1, Integer.MAX_VALUE, 3));
    public static final Parameter ITEM_NEIGHBOUR_SIZE = new Parameter("ITEM_NEIGHBOUR_SIZE", new IntegerParameter(1, Integer.MAX_VALUE, 4));
    public static final Parameter PROPAGETE_USER_TRUST = new Parameter("PROPAGETE_USER_TRUST", new BooleanParameter(Boolean.TRUE));

    public HybridUserItemTrustBased() {
        super();
        addParameter(USER_NEIGHBOUR_SIZE);
        addParameter(ITEM_NEIGHBOUR_SIZE);
        addParameter(PROPAGETE_USER_TRUST);
    }

    @Override
    public HybridUserItemTrustBasedModel buildRecommendationModel(DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadRatingsDataset {
        //User trust module computations
        HybridUserItemTrustBasedModel.UserBasedTrustModuleModel userModel = buildUserModel(datasetLoader);
        //Item trust module computations
        HybridUserItemTrustBasedModel.ItemBasedTrustModuleModel itemModel = buildItemModel(datasetLoader);
        return new HybridUserItemTrustBasedModel(userModel, itemModel);

    }

    @Override
    public Collection<Recommendation> recommendToUser(
            DatasetLoader<? extends Rating> datasetLoader,
            HybridUserItemTrustBasedModel model, long idUser,
            Set<Long> candidateItems)
            throws UserNotFound, CannotLoadRatingsDataset {

        Collection<Recommendation> ret = new ArrayList<>(candidateItems.size());

        for (long idItem : candidateItems) {
            try {
                Number userPrediction = userPrediction(datasetLoader.getRatingsDataset(), model.getUserBasedTrustModuleModel(), idUser, idItem);
                Number itemPrediction = itemPrediction(datasetLoader.getRatingsDataset(), model.getItemBasedTrustModuleModel(), idUser, idItem);

                if (userPrediction.equals(0) && itemPrediction.equals(0)) {
                    Number prediction = 0;
                    ret.add(new Recommendation(idItem, prediction));
                    if (Global.isVerboseAnnoying()) {
                        Global.showInfoMessage("==========================================================\n");
                        Global.showInfoMessage("User prediction for user " + idUser + ",\t item " + idItem + "\t  ---->  \t" + userPrediction + "\n");
                        Global.showInfoMessage("Item prediction for user " + idUser + ",\t item " + idItem + "\t  ---->  \t" + itemPrediction + "\n");
                        Global.showInfoMessage("Final prediction for user " + idUser + ",\t item " + idItem + "\t  ---->  \t" + prediction + "\n");
                        Global.showInfoMessage("==========================================================\n");
                    }
                    continue;
                }
                if (!userPrediction.equals(0) && itemPrediction.equals(0)) {
                    Number prediction = userPrediction;
                    ret.add(new Recommendation(idItem, prediction));
                    if (Global.isVerboseAnnoying()) {
                        Global.showInfoMessage("==========================================================\n");
                        Global.showInfoMessage("User prediction for user " + idUser + ",\t item " + idItem + "\t  ---->  \t" + userPrediction + "\n");
                        Global.showInfoMessage("Item prediction for user " + idUser + ",\t item " + idItem + "\t  ---->  \t" + itemPrediction + "\n");
                        Global.showInfoMessage("Final prediction for user " + idUser + ",\t item " + idItem + "\t  ---->  \t" + prediction + "\n");
                        Global.showInfoMessage("==========================================================\n");
                    }
                    continue;
                }
                if (userPrediction.equals(0) && !itemPrediction.equals(0)) {
                    Number prediction = itemPrediction;
                    ret.add(new Recommendation(idItem, prediction));
                    if (Global.isVerboseAnnoying()) {
                        Global.showInfoMessage("==========================================================\n");
                        Global.showInfoMessage("User prediction for user " + idUser + ",\t item " + idItem + "\t  ---->  \t" + userPrediction + "\n");
                        Global.showInfoMessage("Item prediction for user " + idUser + ",\t item " + idItem + "\t  ---->  \t" + itemPrediction + "\n");
                        Global.showInfoMessage("Final prediction for user " + idUser + ",\t item " + idItem + "\t  ---->  \t" + prediction + "\n");
                        Global.showInfoMessage("==========================================================\n");
                    }
                    continue;
                }
                if (!userPrediction.equals(0) && !itemPrediction.equals(0)) {
                    Number prediction = (2 * userPrediction.doubleValue() * itemPrediction.doubleValue()) / (userPrediction.doubleValue() + itemPrediction.doubleValue());
                    if (Global.isVerboseAnnoying()) {
                        Global.showInfoMessage("==========================================================\n");
                        Global.showInfoMessage("User prediction for user " + idUser + ",\t item " + idItem + "\t  ---->  \t" + userPrediction + "\n");
                        Global.showInfoMessage("Item prediction for user " + idUser + ",\t item " + idItem + "\t  ---->  \t" + itemPrediction + "\n");
                        Global.showInfoMessage("Final prediction for user " + idUser + ",\t item " + idItem + "\t  ---->  \t" + prediction + "\n");
                        Global.showInfoMessage("==========================================================\n");
                    }
                    ret.add(new Recommendation(idItem, prediction));
                }
            } catch (ItemNotFound ex) {
                ERROR_CODES.ITEM_NOT_FOUND.exit(ex);
            }
        }

        return ret;
    }

    private HybridUserItemTrustBasedModel.UserBasedTrustModuleModel buildUserModel(DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadRatingsDataset {

        ShambourLu_UserBasedImplicitTrustComputation implicitTrustComputation = new ShambourLu_UserBasedImplicitTrustComputation(getPropageteUsersTrustValue());

        implicitTrustComputation.addProgressListener((event) -> {
            HybridUserItemTrustBased.this.fireBuildingProgressChangedEvent(
                    event.getTask(),
                    event.getPercent(),
                    event.getRemainingTime());
        });

        WeightedGraph<Long> usersTrust = implicitTrustComputation.computeTrustValues(datasetLoader, datasetLoader.getRatingsDataset().allUsers());

        RatingsDataset<? extends Rating> ratingsDataset = datasetLoader.getRatingsDataset();

        List<Long> users = new ArrayList<>(ratingsDataset.allUsers());
        List<Long> items = new ArrayList<>(ratingsDataset.allRatedItems());

        Map<Long, Map<Long, Number>> MSD = new TreeMap<>();
        Map<Long, Map<Long, Number>> UJaccard = new TreeMap<>();

        Global.showInfoMessage("============================================================================= \n");
        Global.showInfoMessage("UT-Step 2: User reputation computation.\n");
        Global.showInfoMessage("============================================================================= \n");

        TreeMap<Long, Number> usersReputation = new TreeMap<>();

        {
            int i = 0;

            for (long idUser : users) {
                double sumaConfianza = 0;
                int numAdjacentes = 0;
                for (long idUserAdyacente : users) {
                    if (idUser == idUserAdyacente) {
                        continue;
                    }

                    if (usersTrust.allNodes().contains(idUser) && usersTrust.connectionWeight(idUser, idUserAdyacente).orElse(0.0) > 0) {
                        sumaConfianza += usersTrust.connectionWeight(idUser, idUserAdyacente).orElse(0.0);
                        numAdjacentes++;
                    }
                }
                double userReputation = sumaConfianza / numAdjacentes;

                usersReputation.put(idUser, userReputation);

                fireBuildingProgressChangedEvent("(2/6) User reputation calculation", (int) ((i * 100f) / users.size()), -1);
                i++;
            }
        }

        Global.showInfoMessage("============================================================================= \n");
        Global.showInfoMessage("UT-Step 3: Neighbour selection.\n");
        Global.showInfoMessage("============================================================================= \n");

        TreeMap<Long, Set<Neighbor>> usersNeighbours = new TreeMap<>();
        {
            int i = 0;

            for (Long idUser : users) {
                List<Neighbor> neighborsOfUser = new ArrayList<>(usersTrust.allNodes().size());

                for (Long idUserNeighbour : usersTrust.allNodes()) {

                    //Un usuario no es su propio vecino.
                    if (idUser == idUserNeighbour) {
                        continue;
                    }

                    double trustBetweenUsers = usersTrust.connectionWeight(idUser, idUserNeighbour).orElse(0.0);
                    if (trustBetweenUsers > 0) {
                        neighborsOfUser.add(new Neighbor(RecommendationEntity.USER, idUserNeighbour, trustBetweenUsers));
                    }
                }
                Collections.sort(neighborsOfUser);
                usersNeighbours.put(idUser, new TreeSet<>(neighborsOfUser.subList(0, Math.min(neighborsOfUser.size(), getUserNeighborhoodSize()))));

                fireBuildingProgressChangedEvent("(3/6) User neighbors selection", (int) ((i * 100f) / users.size()), -1);
                i++;
            }
        }

        return new HybridUserItemTrustBasedModel.UserBasedTrustModuleModel(usersNeighbours, usersReputation, usersTrust);
    }

    private HybridUserItemTrustBasedModel.ItemBasedTrustModuleModel buildItemModel(DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadRatingsDataset {

        ShambourLu_ItemBasedImplicitTrustComputation implicitTrustComputation = new ShambourLu_ItemBasedImplicitTrustComputation();

        implicitTrustComputation.addProgressListener((event) -> {
            HybridUserItemTrustBased.this.fireBuildingProgressChangedEvent(event.getTask(), event.getPercent(), event.getRemainingTime());
        });

        WeightedGraph<Long> itemBasedTrust = implicitTrustComputation.computeTrustValues(datasetLoader, datasetLoader.getRatingsDataset().allRatedItems());
        RatingsDataset<? extends Rating> ratingsDataset = datasetLoader.getRatingsDataset();

        List<Long> users = new ArrayList<>(ratingsDataset.allUsers());
        List<Long> items = new ArrayList<>(ratingsDataset.allRatedItems());

        Global.showInfoMessage("============================================================================= \n");
        Global.showInfoMessage("IT-Step 2: Item reputation computation.\n");
        Global.showInfoMessage("============================================================================= \n");

        Map<Long, Map<Long, Number>> itemReputation = new TreeMap<>();

        {
            int i = 0;

            for (long idUser : users) {
                itemReputation.put(idUser, new TreeMap<>());

                try {
                    Map<Long, ? extends Rating> userRatings = ratingsDataset.getUserRatingsRated(idUser);

                    for (long idItem : userRatings.keySet()) {
                        double numerador = 0;
                        double denominador = 0;

                        for (long idItemAdjacenteOrigen : userRatings.keySet()) {
                            if (idItem == idItemAdjacenteOrigen) {
                                continue;
                            }

                            double connection = itemBasedTrust.connectionWeight(idItemAdjacenteOrigen, idItem).orElse(0.0);

                            //Se pasa a calcular la reputación del producto.
                            numerador += connection;
                            denominador++;
                        }

                        double thisItemReputationForThisUser;
                        thisItemReputationForThisUser = numerador / denominador;

                        itemReputation.get(idUser).put(idItem, thisItemReputationForThisUser);
                    }
                } catch (UserNotFound ex) {
                    ERROR_CODES.USER_NOT_FOUND.exit(ex);
                }

                fireBuildingProgressChangedEvent("(5/6) Item reputation calculation", (int) ((i * 100f) / users.size()), -1);
                i++;
            }
        }

        Global.showInfoMessage("============================================================================= \n");
        Global.showInfoMessage("IT-Step 3: Item neighbour selection.\n");
        Global.showInfoMessage("============================================================================= \n");

        TreeMap<Long, Collection<Neighbor>> itemsNeighbours = new TreeMap<>();

        {
            int i = 0;
            for (long idItem : itemBasedTrust.allNodes()) {
                List<Neighbor> neighborsOfItem = new ArrayList<>(itemBasedTrust.allNodes().size());

                for (long idItemNeighbour : itemBasedTrust.allNodes()) {

                    //Un producto no es su propio vecino.
                    if (idItem == idItemNeighbour) {
                        continue;
                    }

                    double trustBetweenItems = itemBasedTrust.connectionWeight(idItem, idItemNeighbour).orElse(0.0);
                    neighborsOfItem.add(new Neighbor(RecommendationEntity.ITEM, idItemNeighbour, trustBetweenItems));

                }
                Collections.sort(neighborsOfItem);
                List<Neighbor> listaRecortada = neighborsOfItem.subList(0, Math.min(neighborsOfItem.size(), getItemNeighborhoodSize()));

                itemsNeighbours.put(idItem, new ArrayList<>(listaRecortada));

                fireBuildingProgressChangedEvent("(6/6 Item neighbors selection", (int) ((i * 100f) / itemBasedTrust.allNodes().size()), -1);
                i++;
            }
        }

        return new HybridUserItemTrustBasedModel.ItemBasedTrustModuleModel(itemBasedTrust, itemReputation, itemsNeighbours);
    }

    public Number itemPrediction(
            RatingsDataset<? extends Rating> ratingsDataset,
            HybridUserItemTrustBasedModel.ItemBasedTrustModuleModel itemBasedTrustModuleModel,
            long idUser,
            long idItem) throws UserNotFound, ItemNotFound {

        Map<Long, ? extends Rating> userRatings = ratingsDataset.getUserRatingsRated(idUser);
        double mediaItem = ratingsDataset.getMeanRatingItem(idItem);

        Global.showln("USER " + idUser + " ITEM " + idItem + " (Item prediction)");

        double numerador = 0;
        double denominador = 0;
        int numVecinosUsados = 0;

        Collection<Neighbor> thisItemNeighbors = itemBasedTrustModuleModel.getItemsNeighbours().get(idItem);
        for (Long idItemNeighbor : ratingsDataset.allRatedItems()) {

            if (numVecinosUsados > getItemNeighborhoodSize()) {
                if (Global.isVerboseAnnoying()) {
                    Global.showInfoMessage("Ya se han usado el número máximo de vecinos: " + getItemNeighborhoodSize());
                }
                break;
            }

            double connection = itemBasedTrustModuleModel.getItemsTrust().connectionWeight(idItem, idItemNeighbor).orElse(0.0);
            if (connection > 0) {
                //Predecir con la confianza.

                if (!userRatings.containsKey(idItemNeighbor)) {
                    continue;
                }

                double rating = userRatings.get(idItemNeighbor).getRatingValue().doubleValue();
                double mediaItemVecino = ratingsDataset.getMeanRatingItem(idItemNeighbor);

                double connectionItemVecino = itemBasedTrustModuleModel.getItemsTrust().connectionWeight(idItem, idItemNeighbor).orElse(0.0);
                numerador += connectionItemVecino * (rating - mediaItemVecino);
                denominador += connectionItemVecino;
                numVecinosUsados++;

            } else {
                //Predecir con la reputación
                if (!userRatings.containsKey(idItemNeighbor)) {
                    continue;
                }

                double rating = userRatings.get(idItemNeighbor).getRatingValue().doubleValue();
                double mediaItemVecino = ratingsDataset.getMeanRatingItem(idItemNeighbor);

                double reputacionItemVecino_paraUsuarioActual;
                if (itemBasedTrustModuleModel.getItemsReputation().containsKey(idUser) && itemBasedTrustModuleModel.getItemsReputation().get(idUser).containsKey(idItemNeighbor)) {
                    reputacionItemVecino_paraUsuarioActual = itemBasedTrustModuleModel.getItemsReputation().get(idUser).get(idItemNeighbor).doubleValue();
                } else {
                    reputacionItemVecino_paraUsuarioActual = 0;
                }

                numerador += reputacionItemVecino_paraUsuarioActual * (rating - mediaItemVecino);
                denominador += reputacionItemVecino_paraUsuarioActual;
                numVecinosUsados++;
            }

        }
        if (denominador > 0) {
            double prediccion = numerador / denominador;
            double prediccionConMedia = prediccion + mediaItem;
            Global.showln("USER " + idUser + " ITEM " + idItem + " (Item prediction) --> NumVecinosUsados: " + numVecinosUsados + "  \tpredicción: " + prediccion + "  \tpredicción ajustada: " + prediccionConMedia);
            return prediccionConMedia;
        } else {
            return 0;
        }
    }

    public Number userPrediction(
            RatingsDataset<? extends Rating> ratingsDataset,
            HybridUserItemTrustBasedModel.UserBasedTrustModuleModel userBasedTrustModuleModel,
            long idUser,
            long idItem)
            throws UserNotFound {

        double userMeanRating = ratingsDataset.getMeanRatingUser(idUser);

        double numerador = 0;
        double denominador = 0;
        TreeMap<Long, Set<Neighbor>> usersNeighbours = userBasedTrustModuleModel.getUsersNeighbours();
        TreeMap<Long, Number> usersReputation = userBasedTrustModuleModel.getUsersReputation();
        WeightedGraph<Long> usersTrust = userBasedTrustModuleModel.getUsersTrust();

        for (Neighbor userNeighbor : usersNeighbours.get(idUser)) {
            double neighbourMeanRating;
            long idUserNeighbour = userNeighbor.getIdNeighbor();
            Map<Long, ? extends Rating> neighbourRatings;
            try {
                neighbourRatings = ratingsDataset.getUserRatingsRated(idUserNeighbour);

                if (!neighbourRatings.containsKey(idItem)) {
                    //Salir, el vecino no ha valorado el producto.
                    continue;
                }

                neighbourMeanRating = ratingsDataset.getMeanRatingUser(userNeighbor.getIdNeighbor());

            } catch (UserNotFound ex) {
                ERROR_CODES.USER_NOT_FOUND.exit(ex);
                continue;
            }

            if (usersTrust.connectionWeight(idUser, idUserNeighbour).orElse(0.0) > 0) {
                numerador += usersTrust.connectionWeight(idUser, idUserNeighbour).orElse(0.0) * (neighbourRatings.get(idItem).getRatingValue().doubleValue() - neighbourMeanRating);
                denominador += usersTrust.connectionWeight(idUser, idUserNeighbour).orElse(0.0);
            } else {
                //No hay definida confianza directa, usando reputación.
                numerador += usersReputation.get(idUserNeighbour).doubleValue() * (neighbourRatings.get(idItem).getRatingValue().doubleValue() - neighbourMeanRating);
                denominador += usersReputation.get(idUserNeighbour).doubleValue();
            }
        }
        if (denominador == 0) {
            return 0;
        } else {
            return (numerador / denominador) + userMeanRating;
        }

    }

    public int getUserNeighborhoodSize() {
        return (Integer) getParameterValue(USER_NEIGHBOUR_SIZE);
    }

    public int getItemNeighborhoodSize() {
        return (Integer) getParameterValue(ITEM_NEIGHBOUR_SIZE);
    }

    public boolean getPropageteUsersTrustValue() {
        return (Boolean) getParameterValue(PROPAGETE_USER_TRUST);
    }
}
