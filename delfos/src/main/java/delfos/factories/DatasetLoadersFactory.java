package delfos.factories;

import delfos.configureddatasets.ConfiguredDatasetLoader;
import delfos.dataset.generated.random.RandomDatasetLoader;
import delfos.dataset.loaders.csv.CSVfileDatasetLoader;
import delfos.dataset.loaders.csv.changeable.ChangeableCSVFileDatasetLoader;
import delfos.dataset.loaders.database.mysql.DatabaseDatasetLoader;
import delfos.dataset.loaders.database.mysql.MySQLDatabaseDatasetLoader_Default;
import delfos.dataset.loaders.database.mysql.changeable.ChangeableMySQLDatasetLoader;
import delfos.dataset.loaders.epinions.EPinionsDatasetLoader;
import delfos.dataset.loaders.epinions.trustlet.EPinionsTrustlet;
import delfos.dataset.loaders.jester.Jester;
import delfos.dataset.loaders.movilens.ml100k.MovieLens100k;
import delfos.dataset.loaders.movilens.ml1m.MovieLens1Million;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.papertestdatasets.ImplicitTrustDataset;

/**
 * Factoría que conoce todos los dataset de los que dispone la biblioteca de
 * recomendación.
 *
* @author Jorge Castro Gallardo
 *
 * @version 1.1 24-Jan-2013
 * @version 1.0 Unknow date
 * @version 2.0 9-Mayo-2013 Ahora la clase hereda de {@link Factory}.
 */
public class DatasetLoadersFactory extends Factory<DatasetLoader> {

    private static final DatasetLoadersFactory instance;

    static {
        instance = new DatasetLoadersFactory();
        instance.addClass(CSVfileDatasetLoader.class);
        instance.addClass(DatabaseDatasetLoader.class);
        instance.addClass(Jester.class);
        instance.addClass(RandomDatasetLoader.class);
        instance.addClass(MySQLDatabaseDatasetLoader_Default.class);
        instance.addClass(ImplicitTrustDataset.class);

        //Datasets modificables
        instance.addClass(ChangeableCSVFileDatasetLoader.class);
        instance.addClass(ChangeableMySQLDatasetLoader.class);

        //Datasets de MovieLens
        instance.addClass(MovieLens100k.class);
        instance.addClass(MovieLens1Million.class);

        //Datasets de EPinions
        instance.addClass(EPinionsDatasetLoader.class);
        instance.addClass(EPinionsTrustlet.class);

        //Configured datasets
        instance.addClass(ConfiguredDatasetLoader.class);

    }

    private DatasetLoadersFactory() {
    }

    public static DatasetLoadersFactory getInstance() {
        return instance;
    }
}
