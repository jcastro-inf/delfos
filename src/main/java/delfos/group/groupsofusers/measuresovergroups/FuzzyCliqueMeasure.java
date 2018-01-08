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
package delfos.group.groupsofusers.measuresovergroups;

import delfos.common.Global;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.DoubleParameter;
import delfos.common.parameters.restriction.IntegerParameter;
import delfos.common.parameters.restriction.ParameterOwnerRestriction;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.rs.trustbased.WeightedGraph;
import delfos.rs.trustbased.WeightedGraphCalculation;
import delfos.rs.trustbased.implicittrustcomputation.ShambourLu_UserBasedImplicitTrustComputation;

/**
 * Clase para calcular el índice con el que un grupo es un clique a partir de la
 * red de confianza entre los miembros.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @version 1.0 03-May-2013
 */
public class FuzzyCliqueMeasure extends GroupMeasureAdapter {

    public static final Parameter WEIGHTED_GRAPH_CALCULATION = new Parameter(
            "weightedGraphCalculation",
            new ParameterOwnerRestriction(WeightedGraphCalculation.class, new ShambourLu_UserBasedImplicitTrustComputation()));
    public static final Parameter N1_FOR_SHORT = new Parameter(
            "N1_forShort",
            new DoubleParameter(0, 1, 0.25f));
    public static final Parameter N2_FOR_SHORT = new Parameter(
            "N2_forShort",
            new DoubleParameter(0, 1, 0.5f));
    public static final Parameter N4_FOR_STRONG = new Parameter(
            "N4_forStrong",
            new DoubleParameter(0, 1, 0.3f));
    public static final Parameter N5_FOR_STRONG = new Parameter(
            "N5_forStrong",
            new DoubleParameter(0, 1, 0.8f));
    public static final Parameter N6_FOR_LONG = new Parameter(
            "N6_forLong",
            new IntegerParameter(1, Integer.MAX_VALUE, 2));
    public static final Parameter N7_FOR_LONG = new Parameter(
            "N7_forLong",
            new IntegerParameter(1, Integer.MAX_VALUE, 4));

    public FuzzyCliqueMeasure() {
        super();
        addParameter(WEIGHTED_GRAPH_CALCULATION);
        addParameter(N1_FOR_SHORT);
        addParameter(N2_FOR_SHORT);
        addParameter(N4_FOR_STRONG);
        addParameter(N5_FOR_STRONG);
        addParameter(N6_FOR_LONG);
        addParameter(N7_FOR_LONG);
    }

    public FuzzyCliqueMeasure(WeightedGraphCalculation weightedGraphCalculation) {
        this();
        setParameterValue(FuzzyCliqueMeasure.WEIGHTED_GRAPH_CALCULATION, weightedGraphCalculation);
    }

    public FuzzyCliqueMeasure(
            WeightedGraphCalculation weightedGraphAlgorithmTechnique,
            double n1_for_short,
            double n2_for_short,
            double n4_for_strong,
            double n5_for_strong) {

        this(weightedGraphAlgorithmTechnique);

        checkRestrictions(n1_for_short, n2_for_short, n4_for_strong, n5_for_strong);

        setParameterValue(FuzzyCliqueMeasure.N1_FOR_SHORT, n1_for_short);
        setParameterValue(FuzzyCliqueMeasure.N2_FOR_SHORT, n2_for_short);
        setParameterValue(FuzzyCliqueMeasure.N4_FOR_STRONG, n4_for_strong);
        setParameterValue(FuzzyCliqueMeasure.N5_FOR_STRONG, n5_for_strong);

        checkRestrictions();
    }

    @Override
    public String getNameWithParameters() {
        return "FClq_" + getWeightedGraphCalculation().getShortName()
                + "_SH(" + getN1_FOR_SHORT() + "," + getN2_FOR_SHORT() + ")"
                + "_ST(" + getN4_FOR_STRONG() + "," + getN5_FOR_STRONG() + ")";
    }

    /**
     * Devuelve el grado con el que el grupo indicado es un clique.
     *
     * @param datasetLoader
     * @param group Grupo a comprobar.
     * @return Valor difuso con el que un grupo es un clique.
     */
    @Override
    public double getMeasure(DatasetLoader<? extends Rating> datasetLoader, GroupOfUsers group) throws CannotLoadRatingsDataset {
        checkRestrictions();

        WeightedGraph<Long> trustNetwork = getWeightedGraphCalculation().computeTrustValues(datasetLoader);

        if (Global.isVerboseAnnoying()) {
            trustNetwork.printTable(System.out);
        }

        double valueOfC1 = 1;
        double numNodos = trustNetwork.allNodes().size();
        for (long x_i : group) {
            for (long x_j : group) {

                //No se tiene en cuenta la conexión consigo mismo.
                if (x_i == x_j) {
                    continue;
                }

                //Calculo cuánto satisface la clausula 1.
                double maximoValorEstePar = 0;
                for (int k = 1; k < trustNetwork.maxK(); k++) {
                    double valor = conjunctionOperator(wordShort_relative(k / numNodos), wordStrong(trustNetwork.distanceJumpLimited(x_i, x_j, k)));

                    if (maximoValorEstePar < valor) {
                        maximoValorEstePar = valor;
                    }
                }

                if (maximoValorEstePar < valueOfC1) {
                    valueOfC1 = maximoValorEstePar;
                }

            }
        }

        double maxInnerValueOfC2 = 0;
        for (long x : group) {
            for (long z : trustNetwork.allNodes()) {
                if (group.contains(z)) {
                    continue;
                }

                double maxOfAllK = 0;

                for (int k = 1; k < trustNetwork.maxK(); k++) {

                    double valueOfThisK = conjunctionOperator(wordNotLong(k), wordStrong(trustNetwork.distanceJumpLimited(x, z, k)));

                    if (maxOfAllK < valueOfThisK) {
                        maxOfAllK = valueOfThisK;
                    }
                }

                if (maxInnerValueOfC2 < maxOfAllK) {
                    maxInnerValueOfC2 = maxOfAllK;
                }
            }
        }

        double valueOfC2 = 1 - maxInnerValueOfC2;

        Global.showInfoMessage("C1 = " + valueOfC1 + "\tC2 = " + valueOfC2 + "\n");

        double degreeToWhichGroupIsAClique = conjunctionOperator(valueOfC1, valueOfC2);

        return degreeToWhichGroupIsAClique;
    }

    private double conjunctionOperator(double a, double b) {
        return a * b;
    }

    /**
     * Calcula el grado de pertenencia de la longitud relativa de un camino con
     * la etiqueta corto [SH(k/n)], donde k es la distancia y n es el número
     * total de nodos.
     *
     * @param relativeLength Longitud relativa, siendo 0 si es el mismo nodo y 1
     * si hay que pasar por todos los posibles nodos para llegar al objetivo.
     * k/n donde n es el número total de nodos.
     *
     * @return grado de pertenencia con la etiqueta corto.
     */
    private double wordShort_relative(double relativeLength) {
        double ret;
        if (relativeLength <= getN1_FOR_SHORT()) {
            ret = 1;
        } else if (relativeLength >= getN2_FOR_SHORT()) {
            ret = 0;
        } else {
            ret = 1 - (relativeLength - getN1_FOR_SHORT()) / (getN2_FOR_SHORT() - getN1_FOR_SHORT());
        }

        // Global.showInfoMessage("Distancia " + relativeLength + " --> " + ret);
        return ret;
    }

    /**
     * Calcula el grado de pertenencia de la intensidad de una conexión entre
     * dos nodos con la etiqueta fuerte [ST{[R(x,y)}].
     *
     * @param connection
     * @return
     */
    private double wordStrong(double connection) {
        final double n4 = getN4_FOR_STRONG();
        final double n5 = getN5_FOR_STRONG();

        if (connection <= n4) {
            return 0;
        } else if (connection >= n5) {
            return 1;
        } else {
            return (connection - n4) / (n5 - n4);
        }
    }

    public double getN1_FOR_SHORT() {
        return (Double) getParameterValue(N1_FOR_SHORT);
    }

    public double getN2_FOR_SHORT() {
        return (Double) getParameterValue(N2_FOR_SHORT);
    }

    public double getN4_FOR_STRONG() {
        return (Double) getParameterValue(N4_FOR_STRONG);
    }

    public double getN5_FOR_STRONG() {
        return (Double) getParameterValue(N5_FOR_STRONG);
    }

    public WeightedGraphCalculation getWeightedGraphCalculation() {
        return (WeightedGraphCalculation) getParameterValue(WEIGHTED_GRAPH_CALCULATION);
    }

    private void checkRestrictions(double n1_for_short, double n2_for_short, double n4_for_strong, double n5_for_strong) {
        if (n1_for_short >= n2_for_short) {
            throw new IllegalArgumentException("The short label definition is not valid (n1 must be less than n2).");
        }
        if (n4_for_strong >= n5_for_strong) {
            throw new IllegalArgumentException("The strong label definition is not valid (n4 must be less than n5).");
        }
    }

    private void checkRestrictions() throws IllegalArgumentException {
        if (getN1_FOR_SHORT() >= getN2_FOR_SHORT()) {
            throw new IllegalArgumentException("The short label definition is not valid (n1 must be less than n2).");
        }
        if (getN4_FOR_STRONG() >= getN5_FOR_STRONG()) {
            throw new IllegalArgumentException("The strong label definition is not valid (n4 must be less than n5).");
        }

        if (getN6_FOR_LONG() >= getN7_FOR_LONG()) {
            throw new IllegalArgumentException("The long label definition is not valid (n6 must be less than n7).");
        }
    }

    private double wordLong(int numConnections) {
        final int n6 = getN6_FOR_LONG();
        final int n7 = getN7_FOR_LONG();

        if (numConnections <= n6) {
            return 0;
        } else if (numConnections >= n7) {
            return 1;
        } else {
            return (numConnections - n6) / (n7 - n6);
        }
    }

    private double wordNotLong(int numConnections) {
        return 1 - wordLong(numConnections);
    }

    private int getN6_FOR_LONG() {
        return (Integer) getParameterValue(N6_FOR_LONG);
    }

    private int getN7_FOR_LONG() {
        return (Integer) getParameterValue(N7_FOR_LONG);
    }
}
