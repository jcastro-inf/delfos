package delfos.utils.hesitant.similarity.factory;

import delfos.utils.hesitant.similarity.HesitantPearson;
import delfos.utils.hesitant.similarity.HesitantSimilarity;
import delfos.utils.hesitant.similarity.basic.HesitantMeanAggregation;
import delfos.utils.hesitant.similarity.basic.HesitantMinAggregation;
import delfos.utils.hesitant.similarity.basic.HesitantRMSMeanAggregation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 *
 * @author jcastro
 */
public class HesitantSimilarityFactory {

    private static final Collection< HesitantSimilarity> hesitantSimilarities;

    static {
        ArrayList<HesitantSimilarity> similarities = new ArrayList<>(1);

        similarities.add(new HesitantPearson());
        similarities.add(new HesitantMeanAggregation());
        similarities.add(new HesitantMinAggregation());
        similarities.add(new HesitantRMSMeanAggregation());

        hesitantSimilarities = Collections.unmodifiableCollection(similarities);
    }

    public static Collection<HesitantSimilarity> getAll() {
        return hesitantSimilarities;
    }

    public static HesitantSimilarity getHesitantSimilarity(String name) {
        Optional<HesitantSimilarity> hesitantSimilarity
                = hesitantSimilarities.stream()
                .filter(hesitantSimilarityIterated
                        -> hesitantSimilarityIterated.getClass().getSimpleName().equalsIgnoreCase(name))
                .findFirst();

        if (hesitantSimilarity.isPresent()) {
            return hesitantSimilarity.get();
        } else {
            throw new IllegalArgumentException("Hesitant similarity '" + name + "' not found");
        }

    }
}
