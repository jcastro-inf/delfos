package delfos.dataset.util;

import java.util.*;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.storage.memory.BothIndexRatingsDataset;
import delfos.ERROR_CODES;
import delfos.rs.collaborativefiltering.profile.Neighbor;
import delfos.rs.recommendation.Recommendation;
import delfos.rs.trustbased.WeightedGraph;
import delfos.common.decimalnumbers.NumberRounder;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.Global;

/**
 * Clase para escribir datasets al estilo linea de comandos.
 *
* @author Jorge Castro Gallardo
 * @version 1.0 24-Apr-2013
 *
 * @deprecated Ahora se debe usar la clase
 * {@link delfos.util.DatasetPrinter}
 */
public class DatasetPrinterDeprecated {

    private static final int numDecimals = 4;

    /**
     * @param values
     * @param users
     * @deprecated Ahora se debe usar la clase
     * {@link delfos.util.DatasetPrinter}
     */
    public static void printCompactUserUserTable(Map<Integer, Map<Integer, Number>> values, Collection<Integer> users) {

        //Escribo la cabecera
        {
            Global.showMessage("|\t|");
            for (int idUser : users) {
                Global.showMessage("U_" + idUser + "\t|");
            }
            Global.showMessage("\n");

            Global.showMessage("+-------+");
            for (int idUsers : users) {
                Global.showMessage("-------+");
            }
            Global.showMessage("\n");
        }

        //Escribo cada línea
        for (int idUser : users) {
            Global.showMessage("|U_" + idUser + "\t|");
            for (int idUser2 : users) {
                try {
                    if (!values.containsKey(idUser)) {
                        throw new UserNotFound(idUser);
                    } else {
                        if (!values.get(idUser).containsKey(idUser2)) {
                            throw new UserNotFound(idUser);
                        }
                        Number value = values.get(idUser).get(idUser2);
                        Global.showMessage("" + NumberRounder.round(value, numDecimals) + "\t|");
                    }
                } catch (UserNotFound ex) {
                    Global.showMessage(" - \t|");
                }
            }
            Global.showMessage("\n");
        }

        //Cierro la tabla
        Global.showMessage("+-------+");
        for (int idItem : users) {
            Global.showMessage("-------+");
        }
        Global.showMessage("\n");
    }

    /**
     * @param values
     * @deprecated Ahora se debe usar la clase
     * {@link delfos.util.DatasetPrinter}
     */
    public static void printOneColumnUserTable(Map<Integer, Number> values) {

        Set<Integer> users = values.keySet();

        //Escribo la cabecera
        {
            Global.showMessage("|\t|Value\t|\n");
            Global.showMessage("+-------+-------+\n");
        }

        //Escribo cada línea
        for (int idUser : users) {
            Global.showMessage("|U_" + idUser + "\t|");

            Number value = values.get(idUser);
            Global.showMessage("" + NumberRounder.round(value, numDecimals) + "\t|\n");
        }

        //Cierro la tabla
        {
            Global.showMessage("+-------+-------+\n");
        }
    }

    /**
     * @param neighbors
     * @deprecated Ahora se debe usar la clase
     * {@link delfos.util.DatasetPrinter}
     */
    public static void printNeighborsUsers(Map<Integer, Set<Neighbor>> neighbors) {

        Global.showMessage("User\t|\tNeighbors\n");
        Global.showMessage("+-------+-------+\n");

        for (int idUser : neighbors.keySet()) {

            Global.showMessage("|U_" + idUser + "\t|");
            for (Neighbor n : neighbors.get(idUser)) {
                Global.showMessage("U_" + n.getIdNeighbor() + " -> [" + NumberRounder.round(n.getSimilarity(), numDecimals) + "]\t|");
            }
            Global.showMessage("\n");
        }

        Global.showMessage("+-------+-------+\n");
    }

    /**
     * @param neighbors
     * @deprecated Ahora se debe usar la clase
     * {@link delfos.util.DatasetPrinter}
     */
    public static void printNeighborsItems(Map<Integer, Collection<Neighbor>> neighbors) {

        Global.showMessage("Item\t|\tNeighbors\n");
        Global.showMessage("+-------+-------+\n");

        for (int idItem : neighbors.keySet()) {

            Global.showMessage("|I_" + idItem + "\t|");
            for (Neighbor n : neighbors.get(idItem)) {
                Global.showMessage("I_" + n.getIdNeighbor() + " -> [" + NumberRounder.round(n.getSimilarity(), numDecimals) + "]\t|");
            }
            Global.showMessage("\n");
        }

        Global.showMessage("+-------+-------+\n");
    }

    /**
     * @param ratingsDataset
     * @deprecated Ahora se debe usar la clase
     * {@link delfos.util.DatasetPrinter}
     */
    public static void printGeneralInformation(RatingsDataset<? extends Rating> ratingsDataset) {
        Global.showMessage("El dataset tiene " + ratingsDataset.allUsers().size() + " usuarios\n");
        Global.showMessage("El dataset tiene " + ratingsDataset.allRatedItems().size() + " productos valorados\n");
        Global.showMessage("El dataset tiene " + ratingsDataset.getNumRatings() + " registros\n");
    }

    /**
     * @param weightedGraph
     * @deprecated Ahora se debe usar la clase
     * {@link delfos.util.DatasetPrinter}
     */
    public static void printWeightedGraph(WeightedGraph<Integer> weightedGraph) {
        printWeightedGraph(weightedGraph, weightedGraph.allNodes());
    }

    /**
     * @param weightedGraph
     * @param users
     * @deprecated Ahora se debe usar la clase
     * {@link delfos.util.DatasetPrinter}
     */
    public static void printWeightedGraph(WeightedGraph<Integer> weightedGraph, Collection<Integer> users) {

        //Escribo la cabecera
        {
            Global.showMessage("|\t|");
            for (Object node : users) {
                Global.showMessage("Node_" + node.toString() + "\t|");
            }
            Global.showMessage("\n");

            Global.showMessage("+-------+");
            for (Object node : users) {
                Global.showMessage("-------+");
            }
            Global.showMessage("\n");
        }

        //Escribo cada línea
        for (Integer idUser : users) {
            Global.showMessage("|Node_" + idUser.toString() + "\t|");
            for (Integer idUser2 : users) {

                Number value = weightedGraph.connection(idUser, idUser2);
                Global.showMessage("" + NumberRounder.round(value, numDecimals) + "\t|");
            }
            Global.showMessage("\n");
        }

        //Cierro la tabla
        Global.showMessage("+-------+");
        for (Object node : users) {
            Global.showMessage("-------+");
        }
        Global.showMessage("\n");
    }

    private DatasetPrinterDeprecated() {
    }

    /**
     * @param ratings
     * @param users
     * @param items
     * @deprecated Ahora se debe usar la clase
     * {@link delfos.util.DatasetPrinter}
     */
    public static void printFancyRatingTable(Map<Integer, Map<Integer, Number>> ratings, Collection<Integer> users, Collection<Integer> items) {
        printFancyRatingTable(new BothIndexRatingsDataset(ratings), users, items);
    }

    /**
     * @param ratingsDataset
     * @param users
     * @param items
     * @deprecated Ahora se debe usar la clase
     * {@link delfos.util.DatasetPrinter}
     */
    public static void printFancyRatingTable(RatingsDataset<? extends Rating> ratingsDataset, Collection<Integer> users, Collection<Integer> items) {
        //Escribo la cabecera
        {
            Global.showMessage("|\t\t|");
            for (int idItem : items) {
                Global.showMessage("\tI_" + idItem + "\t|");
            }
            Global.showMessage("\n");

            Global.showMessage("+---------------+");
            for (int idItem : items) {
                Global.showMessage("---------------+");
            }
            Global.showMessage("\n");
        }

        //Escribo cada línea
        for (int idUser : users) {
            Global.showMessage("|\tU_" + idUser + "\t|");
            for (int idItem : items) {
                try {
                    Rating rating = ratingsDataset.getRating(idUser, idItem);
                    if (rating == null) {
                        Global.showMessage("\t - \t|");
                    } else {

                        Global.showMessage("\t" + NumberRounder.round(rating.ratingValue, numDecimals) + "\t|");
                    }
                } catch (UserNotFound ex) {
                    Global.showMessage("\t - \t|");
                } catch (ItemNotFound ex) {
                    Global.showMessage("\t - \t|");
                }
            }
            Global.showMessage("\n");
        }

        //Cierro la tabla
        Global.showMessage("+---------------+");
        for (int idItem : items) {
            Global.showMessage("---------------+");
        }
        Global.showMessage("\n");
    }

    /**
     * @param ratings
     * @deprecated Ahora se debe usar la clase
     * {@link delfos.util.DatasetPrinter}
     */
    public static void printCompactRatingTable(Map<Integer, Map<Integer, Number>> ratings) {
        Set<Integer> users = new TreeSet<Integer>(ratings.keySet());
        Set<Integer> items = new TreeSet<Integer>();

        Map<Integer, Map<Integer, Rating>> ratings_r = new TreeMap<Integer, Map<Integer, Rating>>();
        for (int idUser : ratings.keySet()) {
            items.addAll(ratings.get(idUser).keySet());
            ratings_r.put(idUser, new TreeMap<Integer, Rating>());
            for (Map.Entry<Integer, Number> entry : ratings.get(idUser).entrySet()) {
                int idItem = entry.getKey();
                Number ratingValue = entry.getValue();
                ratings_r.get(idUser).put(idItem, new Rating(idUser, idItem, ratingValue));
            }

        }

        printCompactRatingTable(new BothIndexRatingsDataset<Rating>(ratings_r), new ArrayList<Integer>(users), new ArrayList<Integer>(items));
    }

    /**
     * @param <RatingType>
     * @param ratings
     * @deprecated Ahora se debe usar la clase
     * {@link delfos.util.DatasetPrinter}
     */
    public static <RatingType extends Rating> void printCompactRatingTable(RatingsDataset<RatingType> ratings) {
        Set<Integer> users = new TreeSet<Integer>(ratings.allUsers());
        Set<Integer> items = new TreeSet<Integer>(ratings.allRatedItems());
        printCompactRatingTable(new BothIndexRatingsDataset<RatingType>(ratings), new ArrayList<Integer>(users), new ArrayList<Integer>(items));
    }

    /**
     * @param ratings
     * @deprecated Ahora se debe usar la clase
     * {@link delfos.util.DatasetPrinter}
     */
    public static void printCompactRatingTable(Collection<Rating> ratings) {
        printCompactRatingTable(new BothIndexRatingsDataset<Rating>(ratings));

    }

    /**
     * @param ratings
     * @param users
     * @param items
     * @deprecated Ahora se debe usar la clase
     * {@link delfos.util.DatasetPrinter}
     */
    public static void printCompactRatingTable(Map<Integer, Map<Integer, Number>> ratings, Collection<Integer> users, Collection<Integer> items) {
        Map<Integer, Map<Integer, Rating>> ratings_r = new TreeMap<Integer, Map<Integer, Rating>>();
        for (int idUser : ratings.keySet()) {
            items.addAll(ratings.get(idUser).keySet());
            ratings_r.put(idUser, new TreeMap<Integer, Rating>());
            for (Map.Entry<Integer, Number> entry : ratings.get(idUser).entrySet()) {
                int idItem = entry.getKey();
                Number ratingValue = entry.getValue();
                ratings_r.get(idUser).put(idItem, new Rating(idUser, idItem, ratingValue));
            }

        }

        printCompactRatingTable(new BothIndexRatingsDataset<Rating>(ratings_r), users, items);
    }

    /**
     * @param ratingsDataset
     * @param users
     * @param items
     * @deprecated Ahora se debe usar la clase
     * {@link delfos.util.DatasetPrinter}
     */
    public static void printCompactRatingTable(RatingsDataset<? extends Rating> ratingsDataset, Collection<Integer> users, Collection<Integer> items) {
        //Escribo la cabecera
        {
            Global.showMessage("|\t|");
            for (int idItem : items) {
                Global.showMessage("I_" + idItem + "\t|");
            }
            Global.showMessage("\n");

            Global.showMessage("+-------+");
            for (int idItem : items) {
                Global.showMessage("-------+");
            }
            Global.showMessage("\n");
        }

        //Escribo cada línea
        for (int idUser : users) {
            Global.showMessage("|U_" + idUser + "\t|");
            for (int idItem : items) {
                try {
                    Rating rating = ratingsDataset.getRating(idUser, idItem);
                    if (rating == null) {
                        Global.showMessage(" - \t|");
                    } else {

                        Global.showMessage("" + NumberRounder.round(rating.ratingValue, numDecimals) + "\t|");
                    }
                } catch (UserNotFound ex) {
                    Global.showMessage(" - \t|");
                } catch (ItemNotFound ex) {
                    Global.showMessage(" - \t|");
                }

            }
            Global.showMessage("\n");
        }

        //Cierro la tabla
        Global.showMessage("+-------+");
        for (int idItem : items) {
            Global.showMessage("-------+");
        }
        Global.showMessage("\n");
    }

    /**
     * @param rd
     * @param users
     * @param recommendations
     * @deprecated Ahora se debe usar la clase
     * {@link delfos.util.DatasetPrinter}
     */
    public static void printCompactRatingTable(RatingsDataset<? extends Rating> rd, Collection<Integer> users, List<Recommendation> recommendations) {
        Map<Integer, Map<Integer, ? extends Rating>> ratings = new TreeMap<Integer, Map<Integer, ? extends Rating>>();

        for (int idUser : users) {
            try {
                ratings.put(idUser, rd.getUserRatingsRated(idUser));
            } catch (UserNotFound ex) {
                ERROR_CODES.USER_NOT_FOUND.exit(ex);
            }
        }

        int idUserRecommendacion = -1;
        Map<Integer, Rating> recommendationsMap = new TreeMap<Integer, Rating>();
        for (Recommendation r : recommendations) {
            recommendationsMap.put(r.getIdItem(), new Rating(-1, r.getIdItem(), r.getPreference()));
        }

        ratings.put(idUserRecommendacion, recommendationsMap);

        printCompactRatingTable(DatasetOperations.convertRatingsToNumber(ratings));
    }
}
