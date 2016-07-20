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
package delfos.rs.nonpersonalised.meanrating.arithmeticmean;

import delfos.rs.recommendation.Recommendation;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Modelo de recomendación del sistema {@link MeanRatingRS}, que almacena para cada producto su valoración media.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 9-Junio-2013
 */
public class MeanRatingRSModel implements Serializable {

    private static final long serialVersionUID = 301;

    public static MeanRatingRSModel create(Collection<Recommendation> recommendationModel1) {
        List<MeanRating> meanRatings = recommendationModel1.stream()
                .sorted(Recommendation.BY_PREFERENCE_DESC)
                .map(recommendation -> new MeanRating(recommendation.getItem(), recommendation.getPreference().doubleValue()))
                .collect(Collectors.toList());
        MeanRatingRSModel meanRatingRSModel = new MeanRatingRSModel(meanRatings);
        return meanRatingRSModel;

    }
    private final List<MeanRating> rangedMeanRatings;

    public MeanRatingRSModel(List<MeanRating> rangedMeanRatings) {
        this.rangedMeanRatings = Collections.unmodifiableList(
                rangedMeanRatings.parallelStream()
                .sorted(MeanRating.BY_PREFERENCE_DESC)
                .collect(Collectors.toList())
        );
    }

    /**
     * Devuelve la lista de productos, ordenada por su valoración media.
     *
     * @return Modelo de recomendación.
     */
    public List<MeanRating> getSortedMeanRatings() {
        return rangedMeanRatings;
    }
}
