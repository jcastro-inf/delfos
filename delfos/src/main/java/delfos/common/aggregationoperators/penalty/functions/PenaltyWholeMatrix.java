package delfos.common.aggregationoperators.penalty.functions;

import java.util.Map;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.FloatParameter;

public class PenaltyWholeMatrix extends PenaltyFunction {

    public static final Parameter ABS_EXPONENT = new Parameter("ABS_EXPONENT", new FloatParameter(0.00000001f, 100f, 1));
    public static final Parameter ITEM_EXPONENT = new Parameter("ITEM_EXPONENT", new FloatParameter(0.00000001f, 100f, 3));

    private double oldAbsExponent = -1;
    private double oldItemExponent = -1;

    public PenaltyWholeMatrix() {
        super();
        addParameter(ABS_EXPONENT);
        addParameter(ITEM_EXPONENT);

        addParammeterListener(() -> {

            double newAbsExponent = ((Number) getParameterValue(ABS_EXPONENT)).doubleValue();
            double newItemExponent = ((Number) getParameterValue(ITEM_EXPONENT)).doubleValue();

            String oldAliasParameters = this.getClass().getSimpleName()
                    + "_abs=" + oldAbsExponent
                    + "_item=" + oldItemExponent;

            String newAliasParameters = this.getClass().getSimpleName()
                    + "_abs=" + newAbsExponent
                    + "_item=" + newItemExponent;

            if (!oldAliasParameters.equals(newAliasParameters)) {
                oldAbsExponent = newAbsExponent;
                oldItemExponent = newItemExponent;

                setAlias(newAliasParameters);

            }

        });
    }

    public PenaltyWholeMatrix(double absExponent, double itemExponent) {
        this();

        setParameterValue(ABS_EXPONENT, absExponent);
        setParameterValue(ITEM_EXPONENT, itemExponent);
    }

    @Override
    public double penaltyThisItem(Number aggregatedValue, Iterable<Number> referenceValues) {
        throw new UnsupportedOperationException("This penalty does not support column only aggregation!");
    }

    @Override
    public double penalty(
            Map<Integer, Map<Integer, Number>> referenceValues_byMember,
            Map<Integer, Number> aggregated_byItem) {

        final double absExponent = ((Number) getParameterValue(ABS_EXPONENT)).doubleValue();
        final double itemExponent = ((Number) getParameterValue(ITEM_EXPONENT)).doubleValue();

        double sumPenalty = 0;
        for (int idUser : referenceValues_byMember.keySet()) {
            double sumPenaltyItem = 0;
            for (int idItem : referenceValues_byMember.get(idUser).keySet()) {
                double x_user_item = referenceValues_byMember.get(idUser).get(idItem).doubleValue();
                double y_item = aggregated_byItem.get(idItem).doubleValue();
                double penalty = Math.abs(x_user_item - y_item);

                penalty = Math.pow(penalty, absExponent);
                sumPenaltyItem += penalty;
            }
            sumPenaltyItem = Math.pow(sumPenaltyItem, itemExponent);
            sumPenalty += sumPenaltyItem;
        }

        if (Double.isInfinite(sumPenalty) || Double.isNaN(sumPenalty)) {
            throw new IllegalStateException("Error in computing penalty --> " + sumPenalty);
        }

        return sumPenalty;
    }
}
