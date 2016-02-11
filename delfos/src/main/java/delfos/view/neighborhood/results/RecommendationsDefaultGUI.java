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
package delfos.view.neighborhood.results;

import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.user.User;
import delfos.rs.recommendation.Recommendation;
import delfos.rs.recommendation.Recommendations;
import delfos.view.neighborhood.components.recommendations.RecommendationsTable;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

/**
 *
 * @author jcastro
 */
public class RecommendationsDefaultGUI implements RecommendationsGUI {

    private RecommendationsTable recommendationsTable;
    private final Component resultsComponent;
    private JPanel recommendationsPanel;
    private DatasetLoader datasetLoader;

    public RecommendationsDefaultGUI() {
        this.resultsComponent = panelResults();
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

    @Override
    public Component getComponent() {
        return resultsComponent;
    }

    @Override
    public void clearData() {
        Recommendations emptyRecommendations = new Recommendations(null, Collections.EMPTY_LIST);

        recommendationsTable.setRecomendaciones(emptyRecommendations);

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
    }
}
