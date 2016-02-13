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
 * Medida de similitud que utiliza la medida de manhatan. La medida de manhatan
 * se comporta como la distancia en una cuadrícula, sin utilizar desplazamientos
 * en diagonal.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 Unknown date
 * @version 1.1 20-Mar-2013
 */
public class Manhattan extends BasicSimilarityMeasureAdapter {

    private static final long serialVersionUID = 1L;

    @Override
    public float similarity(List<Float> v1, List<Float> v2) throws CouldNotComputeSimilarity {
        if (v1.size() != v2.size()) {
            throw new CouldNotComputeSimilarity("Vector size is diferent");
        }
        Iterator<Float> i1 = v1.listIterator();
        Iterator<Float> i2 = v2.listIterator();
        float distance = 0;
        while (i1.hasNext()) {
            Float n1 = i1.next();
            Float n2 = i2.next();
            distance += Math.abs(n1 - n2);
        }
        return 1 / (1 + distance);
    }
}
