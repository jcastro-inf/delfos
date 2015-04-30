package delfos.main.managers.recommendation.nonpersonalised;

import delfos.main.managers.CaseUseModeWithSubManagers;
import delfos.main.managers.CaseUseSubManager;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @version 22-oct-2014
 * @author Jorge Castro Gallardo
 */
public class NonPersonalisedRecommendation extends CaseUseModeWithSubManagers {

    /**
     * Parámetro de la linea de comandos para usar el modo non-personalised.
     */
    public static final String NON_PERSONALISED_MODE = "--non-personalised";

    private static final NonPersonalisedRecommendation instance = new NonPersonalisedRecommendation();

    public static NonPersonalisedRecommendation getInstance() {
        return instance;
    }

    @Override
    public String getModeParameter() {
        return NON_PERSONALISED_MODE;
    }

    @Override
    public Collection<CaseUseSubManager> getAllCaseUseSubManagers() {
        ArrayList<CaseUseSubManager> allCaseUseModeSubManagers = new ArrayList<>();

        allCaseUseModeSubManagers.add(BuildRecommendationModel.getInstance());
        allCaseUseModeSubManagers.add(Recommend.getInstance());

        return allCaseUseModeSubManagers;
    }

}
