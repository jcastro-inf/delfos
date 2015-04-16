package delfos.group.experiment.groupformation;

import delfos.group.experiment.validation.groupformation.FixedGroupSize_OnlyNGroups;
import java.util.Collection;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.generated.random.RandomDatasetLoader;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.group.groupsofusers.GroupOfUsers;

/**
 * Clase para testear la implemnetación de {@link FixedGroupSize_OnlyNGroups}.
 * Evalúa que funciona correctamente el método {@link FixedGroupSize_OnlyNGroups#shuffle()
 * }, si genera grupos de tamaño fijo, si genera el número de grupos
 * especificado.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 1.0 (09-01-2013)
 */
public class FixedGroupSize_OnlyNGroupsTest {

    /**
     * Dataset aleatorio que se utiliza en los test.
     */
    private static DatasetLoader<? extends Rating> datasetLoader;

    public FixedGroupSize_OnlyNGroupsTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        //Creo un dataset de valoraciones aleatorio.

        datasetLoader = new RandomDatasetLoader(1000, 1000, 0.01);

    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Testea si la clase está implementada de manera que sean replicables los
     * resultados aleatorios.
     */
    @Test
    public void testRandomReplicability() throws CannotLoadRatingsDataset {

        FixedGroupSize_OnlyNGroups fixedGroupSize_OnlyNGroups = new FixedGroupSize_OnlyNGroups(200, 5);

        long seedValue = System.currentTimeMillis();
        fixedGroupSize_OnlyNGroups.setSeedValue(seedValue);
        Collection<GroupOfUsers> groupsGenerated1 = fixedGroupSize_OnlyNGroups.shuffle(datasetLoader);

        fixedGroupSize_OnlyNGroups.setSeedValue(seedValue);
        Collection<GroupOfUsers> groupsGenerated2 = fixedGroupSize_OnlyNGroups.shuffle(datasetLoader);

        Assert.assertEquals("Groups generated are not equal:\n" + groupsGenerated1.toString() + "\n" + groupsGenerated2 + "\n",
                groupsGenerated1,
                groupsGenerated2);

    }

    /**
     * Evalúa que la clase genera correctamente los grupos de usuarios, todos
     * con el mismo tamaño.
     */
    @Test
    public void testGroupsSizes() throws CannotLoadRatingsDataset {

        int[] groupTams = {1, 5, 10};
        int numGroups = 100;

        for (int groupSize : groupTams) {
            System.out.println("testGroupsSizes --> size = " + groupSize);
            FixedGroupSize_OnlyNGroups fixedGroupSize_OnlyNGroups = new FixedGroupSize_OnlyNGroups(numGroups, groupSize);

            Collection<GroupOfUsers> groups = fixedGroupSize_OnlyNGroups.shuffle(datasetLoader);
            assert groups.size() == numGroups;

            groups.stream().forEach((g) -> {
                assert g.size() == groupSize;
            });
        }
    }

    /**
     * Test para comprobar que la clase falla cuando se asignan valores fuera
     * del al número de grupos generados.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testIllegalNumGroups() throws CannotLoadRatingsDataset {
        System.out.println("testIllegalNumGroups");

        FixedGroupSize_OnlyNGroups fixedGroupSize_OnlyNGroups = new FixedGroupSize_OnlyNGroups(0, 5);
        fixedGroupSize_OnlyNGroups.shuffle(datasetLoader);
    }

    /**
     * Test para comprobar que la clase falla cuando se asignan valores fuera
     * del al número de grupos generados.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testIllegalGroupSize() throws CannotLoadRatingsDataset {
        System.out.println("testIllegalGroupSize");

        FixedGroupSize_OnlyNGroups fixedGroupSize_OnlyNGroups = new FixedGroupSize_OnlyNGroups(500, 0);
        fixedGroupSize_OnlyNGroups.shuffle(datasetLoader);
    }
}
