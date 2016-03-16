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
package delfos.dataset.util;

import delfos.common.Global;
import delfos.common.decimalnumbers.NumberRounder;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.user.User;
import delfos.dataset.storage.memory.BothIndexRatingsDataset;
import delfos.rs.collaborativefiltering.profile.Neighbor;
import delfos.rs.trustbased.WeightedGraph;
import dnl.utils.text.table.TextTable;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Clase para transformar datasets a cadenas con formato amigable para el
 * usuario.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @version 1.0 15-Enero-2014
 */
public class DatasetPrinter {

    private static final int numDecimals = 4;

    public static void printCompactUserUserTable(Map<Integer, Map<Integer, Number>> values, Collection<Integer> users) {

        //Escribo la cabecera
        {
            Global.showInfoMessage("|\t|");
            users.stream().forEach((idUser) -> {
                Global.showInfoMessage("U_" + idUser + "\t|");
            });
            Global.showInfoMessage("\n");

            Global.showInfoMessage("+-------+");
            users.stream().forEach((_item) -> {
                Global.showInfoMessage("-------+");
            });
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
                        Global.showInfoMessage("" + NumberRounder.round(value) + "\t|");
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
            Global.showInfoMessage("" + NumberRounder.round(value) + "\t|\n");
        }

        //Cierro la tabla
        {
            Global.showInfoMessage("+-------+-------+\n");
        }
    }

    public static void printNeighborsUsers(Map<Integer, Set<Neighbor>> neighbors) {

        Global.showInfoMessage("User\t|\tNeighbors\n");
        Global.showInfoMessage("+-------+-------+\n");

        for (int idUser : neighbors.keySet()) {

            Global.showInfoMessage("|U_" + idUser + "\t|");
            for (Neighbor n : neighbors.get(idUser)) {
                Global.showInfoMessage("U_" + n.getIdNeighbor() + " -> [" + NumberRounder.round(n.getSimilarity()) + "]\t|");
            }
            Global.showInfoMessage("\n");
        }

        Global.showInfoMessage("+-------+-------+\n");
    }

    public static void printNeighborsItems(Map<Integer, Collection<Neighbor>> neighbors) {

        Global.showInfoMessage("Item\t|\tNeighbors\n");
        Global.showInfoMessage("+-------+-------+\n");

        for (int idItem : neighbors.keySet()) {

            Global.showInfoMessage("|I_" + idItem + "\t|");
            for (Neighbor n : neighbors.get(idItem)) {
                Global.showInfoMessage("I_" + n.getIdNeighbor() + " -> [" + NumberRounder.round(n.getSimilarity()) + "]\t|");
            }
            Global.showInfoMessage("\n");
        }

        Global.showInfoMessage("+-------+-------+\n");
    }

    public static void printGeneralInformation(RatingsDataset<? extends Rating> ratingsDataset) {
        Global.showInfoMessage("El dataset tiene " + ratingsDataset.allUsers().size() + " usuarios\n");
        Global.showInfoMessage("El dataset tiene " + ratingsDataset.allRatedItems().size() + " productos valorados\n");
        Global.showInfoMessage("El dataset tiene " + ratingsDataset.getNumRatings() + " registros\n");
    }

    public static <Node> String printWeightedGraph(WeightedGraph<Node> weightedGraph) {
        return weightedGraph.toStringTable();
    }

    public static <Node> String printWeightedGraph(WeightedGraph<Node> weightedGraph, Collection<Node> nodes) {
        return weightedGraph.toStringTable(nodes.stream().collect(Collectors.toSet()));
    }

    private DatasetPrinter() {
    }

    public static void printFancyRatingTable(Map<Integer, Map<Integer, Number>> ratings, Collection<Integer> users, Collection<Integer> items) {
        printFancyRatingTable(new BothIndexRatingsDataset(ratings), users, items);
    }

    public static void printFancyRatingTable(RatingsDataset<? extends Rating> ratingsDataset, Collection<Integer> users, Collection<Integer> items) {
        //Escribo la cabecera
        {
            Global.showInfoMessage("|\t\t|");
            items.stream().forEach((idItem) -> {
                Global.showInfoMessage("\tI_" + idItem + "\t|");
            });
            Global.showInfoMessage("\n");

            Global.showInfoMessage("+---------------+");
            items.stream().forEach((_item) -> {
                Global.showInfoMessage("---------------+");
            });
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

                        Global.showInfoMessage("\t" + NumberRounder.round(rating.getRatingValue()) + "\t|");
                    }
                } catch (UserNotFound | ItemNotFound ex) {
                    Global.showInfoMessage("\t - \t|");
                }
            }
            Global.showInfoMessage("\n");
        }

        //Cierro la tabla
        Global.showInfoMessage("+---------------+");
        items.stream().forEach((_item) -> {
            Global.showInfoMessage("---------------+");
        });
        Global.showInfoMessage("\n");
    }

    public static String printCompactRatingTable(Map<Integer, Map<Integer, Number>> ratings) {
        Set<Integer> users = new TreeSet<>(ratings.keySet());
        Set<Integer> items = new TreeSet<>();

        Map<Integer, Map<Integer, Rating>> ratings_r = new TreeMap<>();
        for (int idUser : ratings.keySet()) {
            items.addAll(ratings.get(idUser).keySet());
            ratings_r.put(idUser, new TreeMap<>());
            for (Map.Entry<Integer, Number> entry : ratings.get(idUser).entrySet()) {
                int idItem = entry.getKey();
                Number ratingValue = entry.getValue();
                ratings_r.get(idUser).put(idItem, new Rating(idUser, idItem, ratingValue));
            }

        }

        return printCompactRatingTable(new BothIndexRatingsDataset<>(ratings_r), new ArrayList<>(users), new ArrayList<>(items));
    }

    public static <RatingType extends Rating> String printCompactRatingTable(RatingsDataset<RatingType> ratings) {
        Set<Integer> users = new TreeSet<>(ratings.allUsers());
        Set<Integer> items = new TreeSet<>(ratings.allRatedItems());
        return printCompactRatingTable(new BothIndexRatingsDataset<>(ratings), new ArrayList<>(users), new ArrayList<>(items));
    }

    public static String printCompactRatingTable(Collection<Rating> ratings) {
        return printCompactRatingTable(new BothIndexRatingsDataset<Rating>(ratings));
    }

    public static String printCompactRatingTable(Map<Integer, Map<Integer, Number>> ratings, Collection<Integer> users, Collection<Integer> items) {
        Map<Integer, Map<Integer, Rating>> ratings_r = new TreeMap<>();
        for (int idUser : ratings.keySet()) {
            items.addAll(ratings.get(idUser).keySet());
            ratings_r.put(idUser, new TreeMap<>());
            for (Map.Entry<Integer, Number> entry : ratings.get(idUser).entrySet()) {
                int idItem = entry.getKey();
                Number ratingValue = entry.getValue();
                ratings_r.get(idUser).put(idItem, new Rating(idUser, idItem, ratingValue));
            }

        }

        return printCompactRatingTable(new BothIndexRatingsDataset<>(ratings_r), users, items);
    }

    public static String printCompactRatingTable(RatingsDataset<? extends Rating> ratingsDataset, Collection<Integer> users, Collection<Integer> items) {

        StringBuilder str = new StringBuilder();

        //Escribo la cabecera
        {
            str.append("|\t|");
            items.stream().forEach((idItem) -> {
                str.append("I_").append(idItem).append("\t|");
            });
            str.append("\n");

            str.append("+-------+");
            items.stream().forEach((_item) -> {
                str.append("-------+");
            });
            str.append("\n");
        }

        //Escribo cada línea
        for (int idUser : users) {
            str.append("|U_").append(idUser).append("\t|");
            for (int idItem : items) {
                try {
                    Rating rating = ratingsDataset.getRating(idUser, idItem);
                    if (rating == null) {
                        str.append(" - \t|");
                    } else {

                        str.append("").append(NumberRounder.round(rating.getRatingValue())).append("\t|");
                    }
                } catch (UserNotFound | ItemNotFound ex) {
                    str.append(" - \t|");
                }

            }
            str.append("\n");
        }

        //Cierro la tabla
        str.append("+-------+");
        items.stream().forEach((_item) -> {
            str.append("-------+");
        });
        str.append("\n");

        return str.toString();
    }

    public static String printCompactRatingTable(DatasetLoader<? extends Rating> datasetLoader, Collection<User> _users) {

        final List<User> users = _users.stream().sorted(User.BY_ID).collect(Collectors.toList());
        final List<Item> itemsAllUsersRated = users.stream()
                .map(user -> datasetLoader.getRatingsDataset().getUserRated(user.getId()))
                .map(userRated
                        -> userRated.stream()
                        .map(idItem -> datasetLoader.getContentDataset().get(idItem))
                        .collect(Collectors.toSet())
                )
                .flatMap(set -> set.stream())
                .collect(Collectors.toSet())
                .stream()
                .sorted(Item.BY_ID)
                .collect(Collectors.toList());

        List<String> columnNames = new ArrayList<>();
        columnNames.add("user\\items");
        columnNames.addAll(itemsAllUsersRated.stream()
                .map(item -> "Item_" + item.getId())
                .collect(Collectors.toList()));

        Object[][] data = new Object[users.size()][itemsAllUsersRated.size() + 1];

        DecimalFormat format = new DecimalFormat("0.0000");

        IntStream.range(0, users.size()).forEach(indexUser -> {
            User user = users.get(indexUser);

            data[indexUser][0] = user.toString();

            Map<Integer, ? extends Rating> userRatingsRated = datasetLoader.getRatingsDataset().getUserRatingsRated(user.getId());

            IntStream.range(0, itemsAllUsersRated.size()).forEach(indexItem -> {
                Item item = itemsAllUsersRated.get(indexItem);
                if (userRatingsRated.containsKey(item.getId())) {
                    data[indexUser][indexItem + 1] = format.format(userRatingsRated.get(item.getId()).getRatingValue().doubleValue());
                } else {
                    data[indexUser][indexItem + 1] = "";
                }
            });
        });

        TextTable textTable = new TextTable(columnNames.toArray(new String[0]), data);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream recordingStream = new PrintStream(baos);
        textTable.printTable(recordingStream, 0);

        return baos.toString();
    }

    public static String datasetDiff(RatingsDataset<? extends Rating> rd1, RatingsDataset<? extends Rating> rd2) {
        return RatingsDatasetDiff.printDiff(rd1, rd2);
    }

    public static String printCompactMatrix(Map<Object, Map<Object, String>> matrix) {

        StringBuilder str = new StringBuilder();

        Set<Object> rowKeys = matrix.keySet();
        Set<Object> colKeys = new TreeSet<>();
        for (Object rowKey : rowKeys) {
            colKeys.addAll(matrix.get(rowKey).keySet());
        }

        //Escribo la cabecera, para los nombres de las columnas
        {
            str.append("|\t|");
            colKeys.stream().forEach((colKey) -> {
                str.append(colKey).append("\t|");
            });
            str.append("\n");

            str.append("+-------+");
            colKeys.stream().forEach((colKey) -> {
                str.append("-------+");
            });
            str.append("\n");
        }
        //Escribo cada línea
        for (Object rowKey : rowKeys) {
            str.append("|").append(rowKey).append("\t|");
            for (Object colKey : colKeys) {
                if (matrix.get(rowKey).containsKey(colKey)) {
                    String cellValue = matrix.get(rowKey).get(colKey);
                    str.append(" ").append(cellValue).append(" \t|");
                } else {
                    str.append("   \t|");
                }
            }
            str.append("\n");
        }

        //Cierro la tabla
        str.append("+-------+");
        colKeys.stream().forEach((colKey) -> {
            str.append("-------+");
        });
        str.append("\n");

        return str.toString();
    }
}
