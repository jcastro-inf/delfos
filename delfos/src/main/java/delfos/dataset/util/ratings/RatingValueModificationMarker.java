package delfos.dataset.util.ratings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.domain.Domain;

/**
 * Clase que dado un dataset asigna marcadores de modificación positiva y
 * negativa a los distintos cambios de valor de un rating.
 *
 * @author jcastro
 * @version 2015-feb-18
 */
public class RatingValueModificationMarker {

    private final List<Double> modifications;
    private final List<String> markers;

    public static List<String> generateDefaultMarkers(String up, String stays, String down, int numDivisionsEachPart) {
        List<String> ret = new ArrayList<>();

        //negative modifications
        for (int i = numDivisionsEachPart; i >= 1; i--) {

            String thisMarker = "";
            for (int j = i; j > 0; j--) {
                thisMarker += down;
            }

            ret.add(thisMarker);
        }

        //arround zero
        ret.add(stays);

        //positive modifications
        for (int i = 1; i <= numDivisionsEachPart; i++) {

            String thisMarker = "";
            for (int j = i; j > 0; j--) {
                thisMarker += up;
            }

            ret.add(thisMarker);
        }

        return ret;
    }

    public RatingValueModificationMarker(Domain ratingDomain, List<String> ascSortedMarkers) {

        markers = Collections.unmodifiableList(new ArrayList<String>(ascSortedMarkers));

        double minModification = ratingDomain.min().doubleValue() - ratingDomain.max().doubleValue();
        double maxModification = ratingDomain.max().doubleValue() - ratingDomain.min().doubleValue();

        ArrayList<Double> tempModifications = new ArrayList<>(markers.size());
        for (double i = 0; i < markers.size(); i++) {

            double modificationValue = (i / markers.size()) * (maxModification - minModification) + minModification;
            tempModifications.add(modificationValue);
        }

        modifications = Collections.unmodifiableList(tempModifications);
    }

    public String getRatingModificationMarker(Rating oldRating, Rating newRating) {
        return getRatingModificationMarker(
                oldRating.getRatingValue().doubleValue(),
                newRating.getRatingValue().doubleValue());
    }

    public String getRatingModificationMarker(double oldValue, double newValue) {
        double modification = newValue - oldValue;
        String marker = markers.get(0);
        for (int i = 1; i < markers.size(); i++) {
            if (modification > modifications.get(i)) {
                marker = markers.get(i);
            }
        }
        return marker;
    }

}
