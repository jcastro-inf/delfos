package delfos.view.neighborhood.components.uknn;

import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.ContentDatasetLoader;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.loader.types.UsersDatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.user.User;
import delfos.rs.collaborativefiltering.knn.RecommendationEntity;
import delfos.rs.collaborativefiltering.profile.Neighbor;
import delfos.rs.recommendation.Recommendation;
import delfos.rs.recommendation.Recommendations;
import delfos.rs.recommendation.RecommendationsWithNeighbors;
import delfos.view.neighborhood.components.recommendations.RecommendationsTable;
import delfos.view.neighborhood.results.RecommendationsGUI;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Collections;
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
    private static final String RATINGS_BORDER_TITLE = "Ratings neighbor vs target";
    private static final String NEIGHBORS_BORDER_TITLE = "User neighbors";

    private RecommendationsTable recommendationsTable;
    private UserNeighborsTable neighborsTable;
    private RatingsUserNeighborTable ratingsTable;
    private final Component resultsComponent;
    private JPanel ratingsPanel;
    private JPanel neighborsPanel;
    private JPanel recommendationsPanel;
    private DatasetLoader datasetLoader;

    public KnnMemoryCFRSRecommendationsGUI() {
        this.resultsComponent = panelResults();

        plugListeners();
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
        ratingsTable.setRatings(Collections.emptyList(), null);

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
    }

    private void addNeighborTableListener() {

        neighborsTable.addNeighborSelectorListener((ListSelectionEvent e) -> {
            ContentDataset contentDataset = ((ContentDatasetLoader) datasetLoader).getContentDataset();
            RatingsDataset<? extends Rating> ratingsDataset = datasetLoader.getRatingsDataset();

            if (e.getFirstIndex() == -1) {
                ratingsTable.setRatings(Collections.emptyList(), contentDataset);
            }
            Neighbor neighbor = neighborsTable.getSelected();
            if (neighbor == null) {
                return;
            }
            ratingsTable.setRatings(ratingsDataset.getUserRatingsRated(neighbor.getIdNeighbor()).values(), contentDataset);
        });
    }

    private void plugListeners() {
        addNeighborTableListener();
    }
}
