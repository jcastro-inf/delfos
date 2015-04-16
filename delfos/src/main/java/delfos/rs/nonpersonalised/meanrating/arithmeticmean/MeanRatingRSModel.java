package delfos.rs.nonpersonalised.meanrating.arithmeticmean;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * Modelo de recomendación del sistema {@link MeanRatingRS}, que almacena para
 * cada producto su valoración media.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2).
 *
 * @version 1.0 9-Junio-2013
 */
public class MeanRatingRSModel implements Serializable {

    private static final long serialVersionUID = 301;
    private final List<MeanRating> rangedMeanRatings;

    public MeanRatingRSModel(List<MeanRating> rangedMeanRatings) {
        Collections.sort(rangedMeanRatings);
        this.rangedMeanRatings = rangedMeanRatings;
    }

    /**
     * Devuelve la lista de productos, ordenada por su valoración media.
     *
     * @return Modelo de recomendación.
     */
    public List<MeanRating> getRangedMeanRatings() {
        return Collections.unmodifiableList(rangedMeanRatings);
    }
}
