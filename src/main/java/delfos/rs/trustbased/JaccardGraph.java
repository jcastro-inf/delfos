/*
 * Copyright (C) 2016 jcastro
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package delfos.rs.trustbased;

import delfos.common.Chronometer;
import delfos.common.Global;
import delfos.common.exceptions.CouldNotComputeSimilarity;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.statisticalfuncions.MeanIterative;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.similaritymeasures.JaccardForUsers;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Calcula el grafo entre usuarios utilizando la medida Jaccard, con el número
 * de valoraciones en común de los usuarios.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @version 1.0 19-Julio-2013
 */
public class JaccardGraph extends WeightedGraphCalculation<Long> {

    private static final long serialVersionUID = -3387516993124229948L;

    public JaccardGraph() {
    }

    @Override
    public WeightedGraph<Long> computeTrustValues(DatasetLoader<? extends Rating> datasetLoader, Collection<Long> users) throws CannotLoadRatingsDataset {
        boolean printPartialResults;

        final RatingsDataset<? extends Rating> ratingsDataset = datasetLoader.getRatingsDataset();
        printPartialResults = ratingsDataset.allRatedItems().size() <= 100 && ratingsDataset.allUsers().size() <= 28;

        if (!Global.isVerboseAnnoying()) {
            printPartialResults = false;
        }

        Map<Long, Map<Long, Number>> UJaccard = users.parallelStream().collect(Collectors.toMap(user -> user, user -> new TreeMap<>()));

        JaccardForUsers jaccardForUsers = new JaccardForUsers();

        int numLoops = users.size() * users.size();
        int thisLoop = 1;

        Chronometer c = new Chronometer();
        MeanIterative meanTimePerLoop = new MeanIterative();

        for (Long idUser1 : users) {
            for (Long idUser2 : users) {
                if (!UJaccard.get(idUser1).containsKey(idUser2)) {
                    c.reset();
                    double similarity;
                    try {

                        similarity = jaccardForUsers.similarity(ratingsDataset, idUser1, idUser2);
                        UJaccard.get(idUser1).put(idUser2, similarity);
                        UJaccard.get(idUser2).put(idUser1, similarity);
                    } catch (CouldNotComputeSimilarity ex) {
                    }
                    meanTimePerLoop.addValue(c.getTotalElapsed());

                    long remainingTime = (long) ((numLoops - thisLoop) * meanTimePerLoop.getMean());

                    fireProgressChanged("Building JaccardGraph", (thisLoop * 100) / numLoops, remainingTime);
                }
                thisLoop++;
            }
        }
        WeightedGraph<Long> jaccardGraph = new WeightedGraph<>(UJaccard);

        return jaccardGraph;
    }
}
