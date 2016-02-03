/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package delfos.group.casestudy.definedcases.hesitant.experiment2allGroups;

import delfos.configureddatasets.ConfiguredDatasetLoader;
import delfos.group.experiment.validation.groupformation.GroupFormationTechnique;
import delfos.group.experiment.validation.groupformation.SimilarMembers;
import delfos.group.grs.GroupRecommenderSystem;
import delfos.group.grs.hesitant.HesitantKnnGroupUser;
import delfos.utils.hesitant.similarity.HesitantPearson;
import delfos.utils.hesitant.similarity.factory.HesitantSimilarityFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author jcastro
 */
public class HesitantGRS_configuration {

    public static final Collection<GroupFormationTechnique> getGroupFormationTechnique() {
        return Arrays.asList(
                1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
                15, 20, 25, 50,
                100, 150, 200, 250, 300, 350, 400, 450, 500, 550, 600, 650, 700, 750, 800, 850, 900)
                .stream()
                .map((groupSize) -> {
                    GroupFormationTechnique gft = new SimilarMembers(groupSize);
                    return gft;
                }).collect(Collectors.toList());

    }

    public static final List<GroupRecommenderSystem> getGRSs() {
        int neighborhoodSize = 100;

        List<GroupRecommenderSystem> ret = new ArrayList<>();

        ret.addAll(HesitantSimilarityFactory.getAll()
                .stream()
                .map((hesitantSimilarity) -> {
                    HesitantKnnGroupUser grs = new HesitantKnnGroupUser();
                    grs.setParameterValue(HesitantKnnGroupUser.NEIGHBORHOOD_SIZE, neighborhoodSize);
                    grs.setParameterValue(HesitantKnnGroupUser.HESITANT_SIMILARITY_MEASURE, hesitantSimilarity);
                    return grs;
                }).collect(Collectors.toList()));

        {

            HesitantPearson hesitantSimilarity = new HesitantPearson();
            HesitantKnnGroupUser hesitantGRS = new HesitantKnnGroupUser();

            hesitantGRS.setParameterValue(HesitantKnnGroupUser.NEIGHBORHOOD_SIZE, neighborhoodSize);
            hesitantGRS.setParameterValue(HesitantKnnGroupUser.HESITANT_SIMILARITY_MEASURE, hesitantSimilarity);
            hesitantGRS.setParameterValue(HesitantKnnGroupUser.DELETE_REPEATED, true);

            ret.add(hesitantGRS);
        }
        return ret;
    }

    public static final Collection<ConfiguredDatasetLoader> getDatasetLoader() {
        return Arrays.asList(
                new ConfiguredDatasetLoader("ml-100k"),
                new ConfiguredDatasetLoader("ml-1m")
        );
    }
}
