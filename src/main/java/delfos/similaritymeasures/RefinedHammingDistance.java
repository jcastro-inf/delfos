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
package delfos.similaritymeasures;

import java.util.Iterator;
import java.util.List;
import delfos.common.exceptions.CouldNotComputeSimilarity;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * 
 * @version 1.0 Unknown date
 * @version 1.1 20-Mar-2013
 */
public class RefinedHammingDistance extends BasicSimilarityMeasureAdapter {

    @Override
    public float similarity(List<Float> v1, List<Float> v2) throws CouldNotComputeSimilarity {
        if (v1.size() != v2.size() || v1.isEmpty()) {
            throw new CouldNotComputeSimilarity("Not enought values");
        }
        float distancia = 0;
        Iterator<Float> i1 = v1.listIterator();
        Iterator<Float> i2 = v2.listIterator();
        for (int i = 0; i < v1.size(); i++) {
            distancia += Math.abs(i1.next() - i2.next());
        }
        return 1 / (1 + distancia);
    }
}
