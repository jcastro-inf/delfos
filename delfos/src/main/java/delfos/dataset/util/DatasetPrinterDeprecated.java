package delfos.dataset.util;

import delfos.common.Global;
import delfos.common.decimalnumbers.NumberRounder;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.storage.memory.BothIndexRatingsDataset;
import delfos.rs.collaborativefiltering.profile.Neighbor;
import delfos.rs.trustbased.WeightedGraph;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Clase para escribir datasets al estilo linea de comandos.
 *
 * @author Jorge Castro Gallardo
 * @version 1.0 24-Apr-2013
 *
 * @deprecated Ahora se debe usar la clase {@link delfos.util.DatasetPrinter}
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
            Global.showInfoMessage("|\t|");
            for (int idUser : users) {
                Global.showInfoMessage("U_" + idUser + "\t|");
            }
            Global.showInfoMessage("\n");

            Global.showInfoMessage("+-------+");
            for (int idUsers : users) {
                Global.showInfoMessage("-------+");
            }
            Global.showInfoMessage("\n");
        }

        //Escribo cada línea
        for (int idUser : users) {
            Global.showInfoMessage("|U_" + idUser + "\t|");
            for (int idUser2 : users) {
                try {
                    if (!values.containsKey(idUser)) {
                        throw new UserNotFound(idUser);
                    } else {
                        if (!values.get(idUser).containsKey(idUser2)) {
                            throw new UserNotFound(idUser);
                        }
                        Number value = values.get(idUser).get(idUser2);
                        Global.showInfoMessage("" + NumberRounder.round(value, numDecimals) + "\t|");
                    }
                } catch (UserNotFound ex) {
                    Global.showInfoMessage(" - \t|");
                }
            }
            Global.showInfoMessage("\n");
        }

        //Cierro la tabla
        Global.showInfoMessage("+-------+");
        for (int idItem : users) {
            Global.showInfoMessage("-------+");
        }
        Global.showInfoMessage("\n");
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
            Global.showInfoMessage("|\t|Value\t|\n");
            Global.showInfoMessage("+-------+-------+\n");
        }

        //Escribo cada línea
        for (int idUser : users) {
            Global.showInfoMessage("|U_" + idUser + "\t|");

            Number value = values.get(idUser);
            Global.showInfoMessage("" + NumberRounder.round(value, numDecimals) + "\t|\n");
        }

        //Cierro la tabla
        {
            Global.showInfoMessage("+-------+-------+\n");
        }
    }

    /**
     * @param neighbors
     * @deprecated Ahora se debe usar la clase
     * {@link delfos.util.DatasetPrinter}
     */
    public static void printNeighborsUsers(Map<Integer, Set<Neighbor>> neighbors) {

        Global.showInfoMessage("User\t|\tNeighbors\n");
        Global.showInfoMessage("+-------+-------+\n");

        for (int idUser : neighbors.keySet()) {

            Global.showInfoMessage("|U_" + idUser + "\t|");
            for (Neighbor n : neighbors.get(idUser)) {
                Global.showInfoMessage("U_" + n.getIdNeighbor() + " -> [" + NumberRounder.round(n.getSimilarity(), numDecimals) + "]\t|");
            }
            Global.showInfoMessage("\n");
        }

        Global.showInfoMessage("+-------+-------+\n");
    }

    /**
     * @param neighbors
     * @deprecated Ahora se debe usar la clase
     * {@link delfos.util.DatasetPrinter}
     */
    public static void printNeighborsItems(Map<Integer, Collection<Neighbor>> neighbors) {

        Global.showInfoMessage("Item\t|\tNeighbors\n");
        Global.showInfoMessage("+-------+-------+\n");

        for (int idItem : neighbors.keySet()) {

            Global.showInfoMessage("|I_" + idItem + "\t|");
            for (Neighbor n : neighbors.get(idItem)) {
                Global.showInfoMessage("I_" + n.getIdNeighbor() + " -> [" + NumberRounder.round(n.getSimilarity(), numDecimals) + "]\t|");
            }
            Global.showInfoMessage("\n");
        }

        Global.showInfoMessage("+-------+-------+\n");
    }

    /**
     * @param ratingsDataset
     * @deprecated Ahora se debe usar la clase
     * {@link delfos.util.DatasetPrinter}
     */
    public static void printGeneralInformation(RatingsDataset<? extends Rating> ratingsDataset) {
        Global.showInfoMessage("El dataset tiene " + ratingsDataset.allUsers().size() + " usuarios\n");
        Global.showInfoMessage("El dataset tiene " + ratingsDataset.allRatedItems().size() + " productos valorados\n");
        Global.showInfoMessage("El dataset tiene " + ratingsDataset.getNumRatings() + " registros\n");
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
            Global.showInfoMessage("|\t|");
            for (Object node : users) {
                Global.showInfoMessage("Node_" + node.toString() + "\t|");
            }
            Global.showInfoMessage("\n");

            Global.showInfoMessage("+-------+");
            for (Object node : users) {
                Global.showInfoMessage("-------+");
            }
            Global.showInfoMessage("\n");
        }

        //Escribo cada línea
        for (Integer idUser : users) {
            Global.showInfoMessage("|Node_" + idUser.toString() + "\t|");
            for (Integer idUser2 : users) {

                Number value = weightedGraph.connection(idUser, idUser2);
                Global.showInfoMessage("" + NumberRounder.round(value, numDecimals) + "\t|");
            }
            Global.showInfoMessage("\n");
        }

        //Cierro la tabla
        Global.showInfoMessage("+-------+");
        for (Object node : users) {
            Global.showInfoMessage("-------+");
        }
        Global.showInfoMessage("\n");
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
            Global.showInfoMessage("|\t\t|");
            for (int idItem : items) {
                Global.showInfoMessage("\tI_" + idItem + "\t|");
            }
            Global.showInfoMessage("\n");

            Global.showInfoMessage("+---------------+");
            for (int idItem : items) {
                Global.showInfoMessage("---------------+");
            }
            Global.showInfoMessage("\n");
        }

        //Escribo cada línea
        for (int idUser : users) {
            Global.showInfoMessage("|\tU_" + idUser + "\t|");
            for (int idItem : items) {
                try {
                    Rating rating = ratingsDataset.getRating(idUser, idItem);
                    if (rating == null) {
                        Global.showInfoMessage("\t - \t|");
                    } else {

                        Global.showInfoMessage("\t" + NumberRounder.round(rating.ratingValue, numDecimals) + "\t|");
                    }
                } catch (UserNotFound ex) {
                    Global.showInfoMessage("\t - \t|");
                } catch (ItemNotFound ex) {
                    Global.showInfoMessage("\t - \t|");
                }
            }
            Global.showInfoMessage("\n");
        }

        //Cierro la tabla
        Global.showInfoMessage("+---------------+");
        for (int idItem : items) {
            Global.showInfoMessage("---------------+");
        }
        Global.showInfoMessage("\n");
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
            Global.showInfoMessage("|\t|");
            for (int idItem : items) {
                Global.showInfoMessage("I_" + idItem + "\t|");
            }
            Global.showInfoMessage("\n");

            Global.showInfoMessage("+-------+");
            for (int idItem : items) {
                Global.showInfoMessage("-------+");
            }
            Global.showInfoMessage("\n");
        }

        //Escribo cada línea
        for (int idUser : users) {
            Global.showInfoMessage("|U_" + idUser + "\t|");
            for (int idItem : items) {
                try {
                    Rating rating = ratingsDataset.getRating(idUser, idItem);
                    if (rating == null) {
                        Global.showInfoMessage(" - \t|");
                    } else {

                        Global.showInfoMessage("" + NumberRounder.round(rating.ratingValue, numDecimals) + "\t|");
                    }
                } catch (UserNotFound ex) {
                    Global.showInfoMessage(" - \t|");
                } catch (ItemNotFound ex) {
                    Global.showInfoMessage(" - \t|");
                }

            }
            Global.showInfoMessage("\n");
        }

        //Cierro la tabla
        Global.showInfoMessage("+-------+");
        for (int idItem : items) {
            Global.showInfoMessage("-------+");
        }
        Global.showInfoMessage("\n");
    }

}
