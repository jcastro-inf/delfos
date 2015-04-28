package delfos.group.groupsofusers.measuresovergroups;

import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.util.DatasetPrinterDeprecated;
import delfos.rs.trustbased.implicittrustcomputation.ShambourLu_UserBasedImplicitTrustComputation;
import delfos.rs.trustbased.WeightedGraphAdapter;
import delfos.rs.trustbased.WeightedGraphCalculation;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.Global;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.FloatParameter;
import delfos.common.parameters.restriction.IntegerParameter;
import delfos.common.parameters.restriction.ParameterOwnerRestriction;
import delfos.group.groupsofusers.GroupOfUsers;

/**
 * Clase para calcular el índice con el que un grupo es un clique a partir de la
 * red de confianza entre los miembros.
 *
* @author Jorge Castro Gallardo
 * @version 1.0 03-May-2013
 */
public class FuzzyCliqueMeasure extends GroupMeasureAdapter {

    public static final Parameter weightedGraphCalculation = new Parameter(
            "weightedGraphCalculation",
            new ParameterOwnerRestriction(WeightedGraphCalculation.class, new ShambourLu_UserBasedImplicitTrustComputation()));
    public static final Parameter N1_forShort = new Parameter(
            "N1_forShort",
            new FloatParameter(0, 1, 0.25f));
    public static final Parameter N2_forShort = new Parameter(
            "N2_forShort",
            new FloatParameter(0, 1, 0.5f));
    public static final Parameter N4_forStrong = new Parameter(
            "N4_forStrong",
            new FloatParameter(0, 1, 0.3f));
    public static final Parameter N5_forStrong = new Parameter(
            "N5_forStrong",
            new FloatParameter(0, 1, 0.8f));
    public static final Parameter N6_forLong = new Parameter(
            "N6_forLong",
            new IntegerParameter(1, Integer.MAX_VALUE, 2));
    public static final Parameter N7_forLong = new Parameter(
            "N7_forLong",
            new IntegerParameter(1, Integer.MAX_VALUE, 4));

    public FuzzyCliqueMeasure() {
        super();
        addParameter(weightedGraphCalculation);
        addParameter(N1_forShort);
        addParameter(N2_forShort);
        addParameter(N4_forStrong);
        addParameter(N5_forStrong);
        addParameter(N6_forLong);
        addParameter(N7_forLong);
    }

    public FuzzyCliqueMeasure(WeightedGraphCalculation weightedGraphCalculation) {
        this();
        setParameterValue(FuzzyCliqueMeasure.weightedGraphCalculation, weightedGraphCalculation);
    }

    public FuzzyCliqueMeasure(
            WeightedGraphCalculation weightedGraphAlgorithmTechnique,
            float n1_for_short,
            float n2_for_short,
            float n4_for_strong,
            float n5_for_strong) {

        this(weightedGraphAlgorithmTechnique);

        checkRestrictions(n1_for_short, n2_for_short, n4_for_strong, n5_for_strong);

        setParameterValue(FuzzyCliqueMeasure.N1_forShort, n1_for_short);
        setParameterValue(FuzzyCliqueMeasure.N2_forShort, n2_for_short);
        setParameterValue(FuzzyCliqueMeasure.N4_forStrong, n4_for_strong);
        setParameterValue(FuzzyCliqueMeasure.N5_forStrong, n5_for_strong);

        checkRestrictions();
    }

    public FuzzyCliqueMeasure(
            WeightedGraphCalculation weightedGraphAlgorithmTechnique,
            double n1_for_short,
            double n2_for_short,
            double n4_for_strong,
            double n5_for_strong) {

        this(weightedGraphAlgorithmTechnique, (float) n1_for_short, (float) n2_for_short, (float) n4_for_strong, (float) n5_for_strong);
    }

    @Override
    public String getNameWithParameters() {
        return "FClq_" + getWeightedGraphCalculation().getShortName()
                + "_SH(" + getN1_forShort() + "," + getN2_forShort() + ")"
                + "_ST(" + getN4_forStrong() + "," + getN5_forStrong() + ")";
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

        WeightedGraphAdapter<Integer> trustNetwork = getWeightedGraphCalculation().computeTrustValues(datasetLoader);

        if (Global.isVerboseAnnoying()) {
            DatasetPrinterDeprecated.printWeightedGraph(trustNetwork);
        }

        double valueOfC1 = 1;
        double numNodos = trustNetwork.allNodes().size();
        for (int x_i : group) {
            for (int x_j : group) {

                //No se tiene en cuenta la conexión consigo mismo.
                if (x_i == x_j) {
                    continue;
                }

                //Calculo cuánto satisface la clausula 1.
                double maximoValorEstePar = 0;
                for (int k = 0; k < trustNetwork.maxK(); k++) {
                    double valor = conjunctionOperator(wordShort_relative(k / numNodos), wordStrong(trustNetwork.composition(x_i, x_j, k)));

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
        for (int x : group) {
            for (int z : trustNetwork.allNodes()) {
                if (group.contains(z)) {
                    continue;
                }

                double maxOfAllK = 0;

                for (int k = 1; k < trustNetwork.maxK(); k++) {

                    double valueOfThisK = conjunctionOperator(wordNotLong(k), wordStrong(trustNetwork.composition(x, z, k)));

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
        if (relativeLength <= getN1_forShort()) {
            ret = 1;
        } else {
            if (relativeLength >= getN2_forShort()) {
                ret = 0;
            } else {
                ret = 1 - (relativeLength - getN1_forShort()) / (getN2_forShort() - getN1_forShort());
            }
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
        final float n4 = getN4_forStrong();
        final float n5 = getN5_forStrong();

        if (connection <= n4) {
            return 0;
        } else {
            if (connection >= n5) {
                return 1;
            } else {
                return (connection - n4) / (n5 - n4);
            }
        }
    }

    public float getN1_forShort() {
        return (Float) getParameterValue(N1_forShort);
    }

    public float getN2_forShort() {
        return (Float) getParameterValue(N2_forShort);
    }

    public float getN4_forStrong() {
        return (Float) getParameterValue(N4_forStrong);
    }

    public float getN5_forStrong() {
        return (Float) getParameterValue(N5_forStrong);
    }

    public WeightedGraphCalculation getWeightedGraphCalculation() {
        return (WeightedGraphCalculation) getParameterValue(weightedGraphCalculation);
    }

    private void checkRestrictions(float n1_for_short, float n2_for_short, float n4_for_strong, float n5_for_strong) {
        if (n1_for_short >= n2_for_short) {
            throw new IllegalArgumentException("The short label definition is not valid (n1 must be less than n2).");
        }
        if (n4_for_strong >= n5_for_strong) {
            throw new IllegalArgumentException("The strong label definition is not valid (n4 must be less than n5).");
        }
    }

    private void checkRestrictions() throws IllegalArgumentException {
        if (getN1_forShort() >= getN2_forShort()) {
            throw new IllegalArgumentException("The short label definition is not valid (n1 must be less than n2).");
        }
        if (getN4_forStrong() >= getN5_forStrong()) {
            throw new IllegalArgumentException("The strong label definition is not valid (n4 must be less than n5).");
        }

        if (getN6_forLong() >= getN7_forLong()) {
            throw new IllegalArgumentException("The long label definition is not valid (n6 must be less than n7).");
        }
    }

    private double wordLong(int numConnections) {
        final int n6 = getN6_forLong();
        final int n7 = getN7_forLong();

        if (numConnections <= n6) {
            return 0;
        } else {
            if (numConnections >= n7) {
                return 1;
            } else {
                return (numConnections - n6) / (n7 - n6);
            }
        }
    }

    private double wordNotLong(int numConnections) {
        return 1 - wordLong(numConnections);
    }

    private int getN6_forLong() {
        return (Integer) getParameterValue(N6_forLong);
    }

    private int getN7_forLong() {
        return (Integer) getParameterValue(N7_forLong);
    }
}
