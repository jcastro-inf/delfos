package delfos.rs.contentbased.vsm;

import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.ParameterOwnerRestriction;
import delfos.similaritymeasures.CosineCoefficient;
import delfos.similaritymeasures.WeightedSimilarityMeasure;

/**
 *
 * @author jcastro
 *
 * @version 5-marzo-2014
 */
public class ContentBasedVSMRS {

    /**
     * Parámetro que almacena la medida de similitud que el sistema utiliza.
     */
    public static final Parameter SIMILARITY_MEASURE = new Parameter(
            "Similarity_measure",
            new ParameterOwnerRestriction(WeightedSimilarityMeasure.class, new CosineCoefficient()),
            "Parámetro que almacena la medida de similitud que el sistema utiliza."
    );

}
