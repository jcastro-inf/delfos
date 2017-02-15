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
package delfos.view.results;

import delfos.dataset.basic.rating.Rating;
import delfos.experiment.casestudy.CaseStudy;
import delfos.results.MeasureResult;
import delfos.results.evaluationmeasures.EvaluationMeasure;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 * Clase utilizada para generar cuadros de diálogo con un resumen de los resultados de un algoritmo sobre un dataset
 * concreto.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class ResultsDialog {

    /**
     * Crea un cuadro de diálogo con los resultados del estudio de casos indicado.
     *
     * @param <RecommendationModel>
     * @param <RatingType>
     * @param f Ventana a la que se asocia el cuadro de diálogo.
     * @param c Caso de estudio cuyos resultados se desea mostrar
     * @return Diálogo con los resultados.
     */
    public static <RecommendationModel extends Object, RatingType extends Rating>
            JDialog showResultsDialog(JFrame f, CaseStudy<RecommendationModel, RatingType> c) {

        JDialog dialog;
        Date d = new Date();
        DateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        dialog = new JDialog(f, "Results of " + c.getAlias() + " at " + df.format(d));

        dialog.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();

        Map<String, Number> mediaResultados = new TreeMap<>();

        for (EvaluationMeasure em : c.getEvaluationMeasures()) {
            MeasureResult agregateResults = c.getMeasureResult(em);
            mediaResultados.put(em.toString(), agregateResults.getValue());
        }

        //Matriz de
        int i = 0;
        for (Map.Entry<String, Number> entry : mediaResultados.entrySet()) {
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.weightx = 1.0;
            constraints.weighty = 0.0;
            constraints.gridx = 0;
            constraints.gridy = i;
            constraints.gridwidth = 1;
            constraints.gridheight = 1;
            constraints.insets = new Insets(3, 4, 3, 4);
            dialog.add(new JLabel(entry.getKey()), constraints);

            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.weightx = 1.0;
            constraints.weighty = 0.0;
            constraints.gridx = 1;
            constraints.gridy = i;
            constraints.gridwidth = 1;
            constraints.gridheight = 1;
            constraints.insets = new Insets(3, 4, 3, 4);
            dialog.add(new JLabel(entry.getValue().toString()), constraints);
            i++;
        }
        dialog.pack();
        dialog.setVisible(true);
        return dialog;
    }
}
