package delfos.view.neighborhood.components.iknn;

import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.ContentDatasetLoader;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.user.User;
import delfos.rs.collaborativefiltering.knn.RecommendationEntity;
import delfos.rs.collaborativefiltering.knn.modelbased.KnnModelBasedCFRSModel;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;

/**
 *
 * @author jcastro
 */
public class KnnModelCFRSRecommendationsGUI implements RecommendationsGUI {

    private RecommendationsTable recommendationsTable;
    private ItemNeighborsTable neighborsTable;
    private RatingsItemNeighborTable ratingsTable;
    private final Component resultsComponent;
    private JPanel ratingsPanel;
    private JPanel neighborsPanel;
    private JPanel recommendationsPanel;
    private DatasetLoader datasetLoader;
    private final RecommendationsExplainedWindow recommendationsExplainedWindow;

    public KnnModelCFRSRecommendationsGUI(RecommendationsExplainedWindow recommendationsExplainedWindow) {
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

        recommendationsPanel.setBorder(BorderFactory.createTitledBorder("Recommendations"));

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
        neighborsPanel.setBorder(BorderFactory.createTitledBorder("Neighbors"));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);

        this.neighborsTable = new ItemNeighborsTable();
        neighborsPanel.add(neighborsTable.getComponent(), constraints);
        return neighborsPanel;
    }

    private Component ratingsPanel() {
        ratingsPanel = new JPanel(new GridBagLayout());
        ratingsPanel.setBorder(BorderFactory.createTitledBorder("Ratings"));

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);

        this.ratingsTable = new RatingsItemNeighborTable();
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
        neighborsTable.setNeighbors(null, Collections.EMPTY_LIST, null);
        ratingsTable.setRatings(Collections.emptyList(), null);

    }

    @Override
    public void updateResult(DatasetLoader datasetLoader, Object recommendationModel, User user, Recommendations recommendations, Set<Item> candidateItems) {

        this.datasetLoader = datasetLoader;
        KnnModelBasedCFRSModel knnModelBasedCFRSModel;
        if (!(recommendationModel instanceof KnnModelBasedCFRSModel)) {
            return;
        }
        knnModelBasedCFRSModel = (KnnModelBasedCFRSModel) recommendationModel;
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
    }

    private void plugListeners() {
        addRecommendationTableListener();
    }

    private void addRecommendationTableListener() {
        recommendationsTable.addRecommendationSelectorListener((ListSelectionEvent e) -> {

            ContentDataset contentDataset = ((ContentDatasetLoader) datasetLoader).getContentDataset();
            RatingsDataset<? extends Rating> ratingsDataset = datasetLoader.getRatingsDataset();

            if (e.getFirstIndex() == -1) {
                neighborsTable.setNeighbors(null, Collections.EMPTY_LIST, null);
            } else {

                Recommendation recommendation = recommendationsTable.getSelectedRecommendation();
                if (recommendation == null) {
                    neighborsTable.setNeighbors(null, Collections.EMPTY_LIST, null);
                    return;
                }
                KnnModelBasedCFRSModel itemModel = (KnnModelBasedCFRSModel) recommendationsExplainedWindow.getRecommendationModelHolder().getRecommendationModel();

                Map<Integer, Neighbor> neighborsByItem = contentDataset.stream().
                        collect(
                                Collectors.toMap(
                                        item -> item.getId(),
                                        item -> new Neighbor(RecommendationEntity.ITEM, item, Double.NaN)));

                itemModel
                        .getItemProfile(recommendation.getItem().getId())
                        .getAllNeighbors().stream()
                        .forEach(neighbor
                                -> neighborsByItem.put(neighbor.getIdNeighbor(), neighbor));

                List<Neighbor> itemNeighbors = new ArrayList<>(neighborsByItem.values());

                itemNeighbors.sort(Neighbor.BY_SIMILARITY_DESC);

                neighborsTable.setNeighbors(
                        recommendationsExplainedWindow.userSelected(),
                        itemNeighbors,
                        recommendationsExplainedWindow.configuredDatasetSelected().getDatasetLoader()
                );
            }
        });
    }
}
