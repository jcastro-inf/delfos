package delfos.rs.persistence;

import delfos.common.Global;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.user.User;
import delfos.factories.RecommenderSystemsFactory;
import delfos.io.xml.recommendations.RecommendationsToXML;
import delfos.rs.RecommenderSystem;
import delfos.rs.collaborativefiltering.Recommender_DatasetProperties;
import delfos.rs.output.RecommendationsOutputFileXML;
import delfos.rs.recommendation.Recommendation;
import delfos.rs.recommendation.Recommendations;
import java.io.File;
import java.util.Iterator;
import org.jdom2.input.SAXBuilder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * Clase para, una vez que se han ejecutado los test
 * {@link RecommenderSystemWithDatabasePersistence} y
 * {@link RecommenderSystemWithFilePersitence} comprueba que los resultados son
 * iguales.
 *
 * @author Jorge Castro Gallardo (Sinbad2,Universidad de Jaén)
 *
 * @version 1.0 22-Mar-2013
 */
public class Persistence_Equality {

    public Persistence_Equality() {
    }

    /**
     * Comprueba si los resultados de recomendación con persistencia en base de
     * datos y en fichero son los mismos.
     *
     * @throws java.lang.Exception
     */
    @Test

    public void testPersistenceRecommendationsAreEqual() throws Exception {

        for (RecommenderSystem recommenderSystem : RecommenderSystemsFactory.getInstance().getAllClasses(RecommenderSystem.class)) {

            if (recommenderSystem instanceof Recommender_DatasetProperties) {
                continue;
            }

            RecommendationsOutputFileXML recommendationsOutput_database = new RecommendationsOutputFileXML(recommenderSystem.getName() + "_recommendations_database");
            RecommendationsOutputFileXML recommendationsOutput_file = new RecommendationsOutputFileXML(recommenderSystem.getName() + "_recommendations_f");
            RatingsDataset<? extends Rating> fileRatingsDataset = FilePersistenceTest.datasetLoader.getRatingsDataset();
            for (int idUser : FilePersistenceTest.datasetLoader.getRatingsDataset().allUsers()) {
                User user = new User(idUser);

                File fileRecommendations_File = FilePersistenceTest.getRecommendationFile(recommenderSystem, user);
                File databaseRecommendations_File = DatabasePersistenceTest.getRecommendationFile(recommenderSystem, user);

                Global.showln("====================================================");
                Global.showln(fileRecommendations_File.getAbsolutePath());
                Global.showln(databaseRecommendations_File.getAbsolutePath());
                Global.showln("");
                Global.showln("");
                assertTrue(recommenderSystem.getName() + ": " + "Recommendations for filePersistence, user " + user + " and rs: " + recommenderSystem.getName() + " doesn't exist", fileRecommendations_File.exists());
                assertTrue(recommenderSystem.getName() + ": " + "Recommendations for databasePersistence, user " + user + " and rs: " + recommenderSystem.getName() + " doesn't exist", databaseRecommendations_File.exists());

                SAXBuilder saxBuilder = new SAXBuilder();
                Recommendations fileRecommendations = RecommendationsToXML.getRecommendations(saxBuilder.build(fileRecommendations_File).getRootElement());
                Recommendations databaseRecommendations = RecommendationsToXML.getRecommendations(saxBuilder.build(databaseRecommendations_File).getRootElement());

                assertEquals(recommenderSystem.getName() + ": " + "Recommendations are refered to different users", fileRecommendations.getTargetIdentifier(), databaseRecommendations.getTargetIdentifier());
                assertEquals(recommenderSystem.getName() + ": " + "Recommendations sizes are different", fileRecommendations.getRecommendations().size(), databaseRecommendations.getRecommendations().size());

                assertRecommendationsAreEqual(recommenderSystem, fileRecommendations_File, fileRecommendations, databaseRecommendations_File, databaseRecommendations);
            }
        }
        assertTrue(true);
    }

    private void assertRecommendationsAreEqual(RecommenderSystem recommenderSystem, File fileRecommendations_File, Recommendations fileRecommendations, File databaseRecommendations_File, Recommendations databaseRecommendations) {
        Iterator<Recommendation> fileRecommendationsIterator = fileRecommendations.sortByPreference().getRecommendations().listIterator();
        Iterator<Recommendation> databaseRecommendationsIterator = databaseRecommendations.sortByPreference().getRecommendations().listIterator();

        for (; fileRecommendationsIterator.hasNext();) {
            Recommendation fileRecommendation = fileRecommendationsIterator.next();
            Recommendation databaseRecommendation = databaseRecommendationsIterator.next();

            if (!fileRecommendation.relaxedEquals(databaseRecommendation)) {
                assertEquals(recommenderSystem.getName() + ": " + "Recommendation lists are different\n"
                        + fileRecommendations_File.getAbsolutePath() + "\n"
                        + databaseRecommendations_File.getAbsolutePath(),
                        fileRecommendations.getRecommendations(),
                        databaseRecommendations.getRecommendations());
            }
        }
    }
}
