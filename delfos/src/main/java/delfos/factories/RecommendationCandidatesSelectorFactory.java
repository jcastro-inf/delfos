package delfos.factories;

import delfos.recommendationcandidates.AllCatalogItems;
import delfos.recommendationcandidates.OnlyNewItems;
import delfos.recommendationcandidates.RecommendationCandidatesSelector;

/**
 *
 * @version 03-jun-2014
* @author Jorge Castro Gallardo
 */
public class RecommendationCandidatesSelectorFactory extends Factory<RecommendationCandidatesSelector> {

    private static final RecommendationCandidatesSelectorFactory instance;

    public static RecommendationCandidatesSelectorFactory getInstance() {
        return instance;
    }

    static {
        instance = new RecommendationCandidatesSelectorFactory();

        instance.addClass(OnlyNewItems.class);
        instance.addClass(AllCatalogItems.class);
    }

    private RecommendationCandidatesSelectorFactory() {
    }
}
