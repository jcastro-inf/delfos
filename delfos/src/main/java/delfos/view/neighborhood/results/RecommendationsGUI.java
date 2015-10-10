package delfos.view.neighborhood.results;

import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.user.User;
import delfos.rs.recommendation.Recommendations;
import java.awt.Component;
import java.util.Set;

/**
 *
 * @author jcastro
 */
public interface RecommendationsGUI {

    public Component getComponent();

    public void updateResult(
            DatasetLoader datasetLoader,
            Object recommendationModel,
            User user,
            Recommendations recommendations,
            Set<Item> candidateItems
    );

    public void clearData();
}
