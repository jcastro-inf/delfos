package delfos.factories;

import delfos.rs.output.RecommendationsOutputDatabase;
import delfos.rs.output.RecommendationsOutputFileXML;
import delfos.rs.output.RecommendationsOutputMethod;
import delfos.rs.output.RecommendationsOutputStandardRaw;
import delfos.rs.output.RecommendationsOutputStandardXML;

/**
 * Factoría que conoce los métodos de escritura de recomendaciones
 * ({@link RecommendationsOutputMethod}).
 *
* @author Jorge Castro Gallardo
 *
 * @version 28-oct-2013
 */
public class RecommendationsOutputMethodFactory extends Factory<RecommendationsOutputMethod> {

    private static final RecommendationsOutputMethodFactory instance;

    static {
        instance = new RecommendationsOutputMethodFactory();
        instance.addClass(RecommendationsOutputDatabase.class);
        instance.addClass(RecommendationsOutputFileXML.class);
        instance.addClass(RecommendationsOutputStandardRaw.class);
        instance.addClass(RecommendationsOutputStandardXML.class);
    }

    private RecommendationsOutputMethodFactory() {
    }

    public static RecommendationsOutputMethodFactory getInstance() {
        return instance;
    }

}
