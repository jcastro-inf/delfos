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
 * @author jcastro-inf ( https://github.com/jcastro-inf )
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
