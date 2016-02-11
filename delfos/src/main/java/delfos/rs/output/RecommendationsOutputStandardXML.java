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
package delfos.rs.output;

import delfos.ERROR_CODES;
import delfos.io.xml.recommendations.RecommendationsToXML;
import delfos.rs.recommendation.Recommendation;
import delfos.rs.recommendation.Recommendations;
import delfos.rs.recommendation.RecommendationsFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

/**
 * Escribe las recomendaciones en formato XML en la salida estándar.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 28-oct-2013
 */
public class RecommendationsOutputStandardXML extends RecommendationsOutputMethod {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor por defecto, que añade los parámetros de este método de
     * salida de recomendaciones.
     */
    public RecommendationsOutputStandardXML() {
        super();
    }

    @Override
    public void writeRecommendations(Recommendations recommendations) {
        List<Recommendation> topNrecommendations = new ArrayList<>(recommendations.getRecommendations());

        if (getNumberOfRecommendations() > 0) {
            Collections.sort(topNrecommendations);
            topNrecommendations = topNrecommendations.subList(0, Math.min(topNrecommendations.size(), getNumberOfRecommendations()));
        }

        Recommendations recommendationsWithNewRanking = RecommendationsFactory.copyRecommendationsWithNewRanking(recommendations, topNrecommendations);

        Element recommendationsElement = RecommendationsToXML.getRecommendationsElement(recommendationsWithNewRanking);

        Document doc = new Document(recommendationsElement);
        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat().setEncoding("ISO-8859-1"));
        try {
            outputter.output(doc, System.out);
        } catch (IOException ex) {
            ERROR_CODES.CANNOT_WRITE_RECOMMENDATIONS.exit(ex);
        }
    }
}
