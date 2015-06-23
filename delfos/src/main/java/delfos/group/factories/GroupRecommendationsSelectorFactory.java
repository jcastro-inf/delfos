package delfos.group.factories;

import delfos.factories.Factory;
import delfos.group.grs.consensus.itemselector.BordaCount;
import delfos.group.grs.consensus.itemselector.GroupRecommendationsSelector;
import delfos.group.grs.consensus.itemselector.NoSelection;
import delfos.group.grs.consensus.itemselector.RandomSelection;
import delfos.group.grs.consensus.itemselector.TopNOfEach;

/**
 *
 * @author Jorge Castro Gallardo
 *
 * @version 27-sept-2014
 */
public class GroupRecommendationsSelectorFactory extends Factory<GroupRecommendationsSelector> {

    private final static GroupRecommendationsSelectorFactory instance;

    static {
        instance = new GroupRecommendationsSelectorFactory();

        instance.addClass(RandomSelection.class);
        instance.addClass(BordaCount.class);
        instance.addClass(TopNOfEach.class);
        instance.addClass(NoSelection.class);

    }

    private GroupRecommendationsSelectorFactory() {
    }

    public static GroupRecommendationsSelectorFactory getInstance() {
        return instance;
    }
}
