package delfos.rs.recommendation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import org.junit.Test;

/**
 *
 * @version 31-jul-2014
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class RecommendationsTest {

    public RecommendationsTest() {
    }

    @Test
    public void testComparatorAllValuesSameButDifferentItem() {

        Random randomGenerator = new Random(123456789);

        List<Recommendation> recommendations = new ArrayList<>();
        Set<Integer> items = new TreeSet<>();
        while (items.size() < 5000) {
            items.add(randomGenerator.nextInt(10000));
        }

        //Add the all same recommendations.
        {
            double preference = randomGenerator.nextDouble();
            for (int i = 1; i <= 16; i++) {
                long idItem = items.toArray(new Integer[0])[randomGenerator.nextInt(items.size())];
                items.remove(idItem);
                recommendations.add(new Recommendation(idItem, preference));
            }
        }

        //Add some more random recommendations
        {
            for (int i = 1; i <= 17; i++) {
                long idItem = items.toArray(new Integer[0])[randomGenerator.nextInt(items.size())];
                double preference = randomGenerator.nextDouble();
                items.remove(idItem);
                recommendations.add(new Recommendation(idItem, preference));
            }
        }

        Collections.shuffle(recommendations, randomGenerator);

        ArrayList<Recommendation> recommendationsSorted = new ArrayList<>(recommendations);

        Collections.sort(recommendationsSorted);
    }
}
