package delfos.group.experiment.validation.groupformation;

import delfos.configureddatasets.ConfiguredDatasetsFactory;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.similaritymeasures.PearsonCorrelationCoefficient;
import delfos.similaritymeasures.useruser.UserUserSimilarity;
import delfos.similaritymeasures.useruser.UserUserSimilarityWrapper;
import delfos.similaritymeasures.useruser.UserUserSimilarityWrapper_relevanceFactor;
import java.util.Collection;
import org.junit.Test;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class DissimilarMembersTest {

    @Test
    public void testShuffle() {
        System.out.println("shuffle");
        DatasetLoader<? extends Rating> datasetLoader = ConfiguredDatasetsFactory.getInstance()
                .getDatasetLoader("ml-100k");

        DissimilarMembers dissimilarMembers = new DissimilarMembers();

        UserUserSimilarity defaultSimilarity = new UserUserSimilarityWrapper_relevanceFactor(
                new UserUserSimilarityWrapper(
                        new PearsonCorrelationCoefficient()),
                20
        );

        dissimilarMembers.setParameterValue(DissimilarMembers.SIMILARITY_MEASURE, defaultSimilarity);
        Collection<GroupOfUsers> result = dissimilarMembers.shuffle(datasetLoader);
    }

}
