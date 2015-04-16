package delfos.dataset.basic.features;

import delfos.dataset.basic.features.FeatureGenerator;
import delfos.dataset.basic.features.Feature;
import delfos.dataset.basic.features.FeatureType;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;
import org.junit.BeforeClass;
import static delfos.Assert.assertStringArrayEquals;
import delfos.constants.TestConstants;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.item.ContentDatasetDefault;
import delfos.dataset.basic.item.Item;
import delfos.io.csv.dataset.item.ContentDatasetToCSV;
import delfos.io.csv.dataset.item.DefaultContentDatasetToCSV;
import delfos.common.FileUtilities;

/**
 * Testeo el comportamiento de los datasets de contenido con múltiples valores
 * para un atributo. Ej: Una película tiene los dos géneros drama y horror;
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 12-mar-2014
 */
public class MultiNominalFeatureTypeTest {

    protected static final File directory = new File(TestConstants.TEST_DATA_DIRECTORY + MultiNominalFeatureTypeTest.class.getSimpleName());

    protected static final File contentCSVFile = new File(directory.getAbsolutePath() + File.separator + "contentCSV.csv");
    protected static final File contentDatasetGeneratedCSVFile = new File(directory.getAbsolutePath() + File.separator + "contentCSVgenerated.csv");

    @BeforeClass
    public static void beforeClass() {
        {
            //Creo el archivo csv
            StringBuilder str = new StringBuilder();
            str.append("idItem,name,\"generos_nominal*\"").append("\n");
            str.append("1,\"Titanic\",drama&&love").append("\n");
            str.append("2,\"Back To The Future 1\",sci-fi").append("\n");
            str.append("3,\"Back To The Future 3\",sci-fi&&western").append("\n");

            if (directory.exists()) {
                FileUtilities.deleteDirectoryRecursive(directory);
            }
            directory.mkdir();

            FileWriter writer;
            try {
                writer = new FileWriter(contentCSVFile);
                writer.write(str.toString());
                writer.flush();
                writer.close();
            } catch (IOException ex) {
                Logger.getLogger(MultiNominalFeatureTypeTest.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    @Test
    public void testCSVReadMultipleNominalFeatureType() throws Throwable {

        // 1.- Data preparation
        ContentDatasetToCSV contentDatasetToCSV = new DefaultContentDatasetToCSV();
        ContentDataset contentDataset = contentDatasetToCSV.readContentDataset(contentCSVFile);
        Feature generos = contentDataset.searchFeature("generos");

        // 2.- Execution
        String[] generosItem1 = ((List<String>) contentDataset.get(1).getFeatureValue(generos)).toArray(new String[0]);
        String[] generosItem2 = ((List<String>) contentDataset.get(2).getFeatureValue(generos)).toArray(new String[0]);
        String[] generosItem3 = ((List<String>) contentDataset.get(3).getFeatureValue(generos)).toArray(new String[0]);

        // 3.- Check results
        String[] generosItem1_expected = {"drama", "love"};
        String[] generosItem2_expected = {"sci-fi"};
        String[] generosItem3_expected = {"sci-fi", "western"};

        assertStringArrayEquals(generosItem1_expected, generosItem1);
        assertStringArrayEquals(generosItem2_expected, generosItem2);
        assertStringArrayEquals(generosItem3_expected, generosItem3);
    }

    @Test
    public void testCSVwriteMultipleNominalFeatureType() throws Throwable {

        // 1.- Data preparation
        ContentDataset contentDataset;
        {
            FeatureGenerator generator = new FeatureGenerator();
            generator.createFeature("generos", FeatureType.MultiNominal);
            Feature generos = generator.searchFeature("generos");
            List<Item> items = new ArrayList<Item>();
            // Item 1
            {
                Map<Feature, Object> featureValues = new TreeMap<Feature, Object>();

                List<String> generosValues = new LinkedList<String>();
                generosValues.add("drama");
                generosValues.add("love");

                featureValues.put(generos, generosValues);
                Item item1 = new Item(1, "Titanic", featureValues);

                items.add(item1);
            }

            // Item 2
            {
                Map<Feature, Object> featureValues = new TreeMap<Feature, Object>();

                List<String> generosValues = new LinkedList<String>();
                generosValues.add("sci-fi");

                featureValues.put(generos, generosValues);
                Item item2 = new Item(2, "Back To The Future 1", featureValues);

                items.add(item2);
            }

            // Item 3
            {
                Map<Feature, Object> featureValues = new TreeMap<Feature, Object>();

                List<String> generosValues = new LinkedList<String>();
                generosValues.add("sci-fi");
                generosValues.add("western");

                featureValues.put(generos, generosValues);
                Item item3 = new Item(3, "Back To The Future 3", featureValues);

                items.add(item3);
            }
            contentDataset = new ContentDatasetDefault(items);
        }

        // 2.- Execution
        ContentDatasetToCSV contentDatasetToCSV = new DefaultContentDatasetToCSV();
        contentDatasetToCSV.writeDataset(contentDataset, contentDatasetGeneratedCSVFile.getAbsolutePath());

        // 3.- Check results
    }

}
