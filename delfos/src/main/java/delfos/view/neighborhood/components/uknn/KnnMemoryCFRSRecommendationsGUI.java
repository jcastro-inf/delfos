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
package delfos.view.neighborhood.components.uknn;

import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.loader.types.UsersDatasetLoader;
import delfos.dataset.basic.user.User;
import delfos.dataset.basic.user.UsersDataset;
import delfos.rs.collaborativefiltering.knn.RecommendationEntity;
import delfos.rs.collaborativefiltering.profile.Neighbor;
import delfos.rs.recommendation.Recommendation;
import delfos.rs.recommendation.Recommendations;
import delfos.rs.recommendation.RecommendationsWithNeighbors;
import delfos.view.neighborhood.RecommendationsExplainedWindow;
import delfos.view.neighborhood.components.recommendations.RecommendationsTable;
import delfos.view.neighborhood.results.RecommendationsGUI;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;

/**
 *
 * @author jcastro
 */
public class KnnMemoryCFRSRecommendationsGUI implements RecommendationsGUI {

    private static final String RECOMMENDATIONS_BORDER_TITLE = "Recommendations";
    private static final String RATINGS_BORDER_TITLE = "Ratings target vs neighbor";
    private static final String NEIGHBORS_BORDER_TITLE = "User neighbors";

    private final RecommendationsExplainedWindow recommendationsExplainedWindow;

    private RecommendationsTable recommendationsTable;
    private UserNeighborsTable neighborsTable;
    private RatingsUserNeighborTable ratingsTable;
    private final Component resultsComponent;
    private JPanel ratingsPanel;
    private JPanel neighborsPanel;
    private JPanel recommendationsPanel;
    private DatasetLoader datasetLoader;

    public KnnMemoryCFRSRecommendationsGUI(RecommendationsExplainedWindow recommendationsExplainedWindow) {
        this.resultsComponent = panelResults();

        plugListeners();
        this.recommendationsExplainedWindow = recommendationsExplainedWindow;
    }

    public final Component panelResults() {

        JPanel results = new JPanel(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();

        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        results.add(recommendationsPanel(), constraints);

        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        results.add(neighborsPanel(), constraints);

        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.gridx = 2;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        results.add(ratingsPanel(), constraints);

        return results;
    }

    private Component recommendationsPanel() {
        recommendationsPanel = new JPanel(new GridBagLayout());

        recommendationsPanel.setBorder(BorderFactory.createTitledBorder(RECOMMENDATIONS_BORDER_TITLE));

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);

        this.recommendationsTable = new RecommendationsTable();
        recommendationsPanel.add(recommendationsTable.getComponent(), constraints);
        return recommendationsPanel;
    }

    private Component neighborsPanel() {
        neighborsPanel = new JPanel(new GridBagLayout());
        neighborsPanel.setBorder(BorderFactory.createTitledBorder(NEIGHBORS_BORDER_TITLE));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);

        this.neighborsTable = new UserNeighborsTable();
        neighborsPanel.add(neighborsTable.getComponent(), constraints);
        return neighborsPanel;
    }

    private Component ratingsPanel() {
        ratingsPanel = new JPanel(new GridBagLayout());
        ratingsPanel.setBorder(BorderFactory.createTitledBorder(RATINGS_BORDER_TITLE));

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);

        this.ratingsTable = new RatingsUserNeighborTable();
        ratingsPanel.add(ratingsTable.getComponent(), constraints);
        return ratingsPanel;
    }

    @Override
    public Component getComponent() {
        return resultsComponent;
    }

    @Override
    public void clearData() {
        recommendationsTable.setRecomendaciones(RecommendationsWithNeighbors.EMPTY_LIST);
        neighborsTable.setNeighbors(RecommendationsWithNeighbors.EMPTY_LIST);
        ratingsTable.setRatings(recommendationsExplainedWindow.configuredDatasetSelected().getDatasetLoader(), null, null);

    }

    @Override
    public void updateResult(DatasetLoader datasetLoader, Object recommendationModel, User user, Recommendations recommendations, Set<Item> candidateItems) {

        this.datasetLoader = datasetLoader;

        Map<Integer, Number> recommendationsByItem = Recommendation.convertToMapOfNumbers(recommendations.getRecommendations());
        List<Recommendation> recommendationsComplete = candidateItems.stream()
                .map((item -> {
                    if (recommendationsByItem.containsKey(item.getId())) {
                        return new Recommendation(item, recommendationsByItem.get(item.getId()));
                    } else {
                        return new Recommendation(item, Double.NaN);
                    }
                })).collect(Collectors.toList());
        recommendationsComplete.sort(Recommendation.BY_PREFERENCE_DESC);

        recommendationsTable.setRecomendaciones(new Recommendations(recommendations.getTarget(), recommendationsComplete));

        if (recommendations instanceof RecommendationsWithNeighbors) {
            RecommendationsWithNeighbors recommendationsWithNeighbors = (RecommendationsWithNeighbors) recommendations;
            Map<Integer, Neighbor> neighbors = recommendationsWithNeighbors.getNeighbors().stream()
                    .collect(Collectors.toMap(
                                    (neighbor -> neighbor.getIdNeighbor()),
                                    Function.identity()));

            List<Neighbor> neighborsComplete = ((UsersDatasetLoader) datasetLoader).getUsersDataset().stream()
                    .filter((neighbor) -> !Objects.equals(neighbor.getId(), user.getId()))
                    .map((neighbor -> {
                        if (neighbors.containsKey(neighbor.getId())) {
                            return neighbors.get(neighbor.getId());
                        } else {
                            return new Neighbor(RecommendationEntity.USER, neighbor.getId(), Double.NaN);
                        }
                    }))
                    .collect(Collectors.toList());

            neighborsComplete.sort(Neighbor.BY_SIMILARITY_DESC);
            neighborsTable.setNeighbors(new RecommendationsWithNeighbors(
                    user.getName(),
                    recommendations.getRecommendations(),
                    neighborsComplete)
            );
        }

        ratingsTable.setRatings(datasetLoader, user, null);
    }

    private void addNeighborTableListener() {

        neighborsTable.addNeighborSelectorListener((ListSelectionEvent e) -> {

            User user = recommendationsExplainedWindow.userSelected();
            UsersDataset usersDataset = ((UsersDatasetLoader) datasetLoader).getUsersDataset();

            int indexSelected = e.getFirstIndex();
            if (indexSelected == -1) {
                ratingsTable.setRatings(datasetLoader, user, null);
            } else if (neighborsTable.getSelected() == null) {
                ratingsTable.setRatings(datasetLoader, user, null);
            } else {
                User neighbor = usersDataset.get(neighborsTable.getSelected().getIdNeighbor());
                ratingsTable.setRatings(datasetLoader, user, neighbor);
            }
        });
    }

    private void plugListeners() {
        addNeighborTableListener();
    }
}
