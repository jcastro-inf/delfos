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
 * Coeficiente que mide la dependencia entre los valores de dos vectores. Es 1
 * si los vectores tienen != 0 las mismas componentes y es == 1 si nunca hay
 * coincidencia de componentes (!=0)
 *
 * Definición original: In that paper, a "similarity ratio" is given over
 * bitmaps, where each bit of a fixed-size array represents the presence or
 * absence of a characteristic in the plant being modelled. The definition of
 * the ratio is the number of common bits, divided by the number of bits set in
 * either sample. NOTA: Se utiliza solo para comparar vectores de bits (o
 * derivados de éstos).
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 1.0 Unknown date
 * @version 1.1 20-Mar-2013
 */
public class Tanimoto extends BasicSimilarityMeasureAdapter {

    @Override
    public float similarity(List<Float> v1, List<Float> v2) throws CouldNotComputeSimilarity {
        if (v1.size() != v2.size()) {
            throw new CouldNotComputeSimilarity("Vector size is diferent");
        }
        float c1 = 0, c2 = 0, shr = 0;

        Iterator<Float> i1 = v1.listIterator();
        Iterator<Float> i2 = v2.listIterator();
        while (i1.hasNext()) {
            Float n1 = i1.next();
            Float n2 = i2.next();
            if (n1 != 0) {
                c1 += 1;
            }
            if (n2 != 0) {
                c2 += 1;
            }
            if (n1 != 0 && n2 != 0) {
                shr += 1;
            }
        }

        float similarity = 1 - (shr / (c1 + c2 - shr));
        return similarity;
    }
}
