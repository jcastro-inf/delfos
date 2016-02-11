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
package delfos.utils.fuzzyclustering;

import delfos.utils.fuzzyclustering.vector.DataVector;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @version 15-sep-2014
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 * @param <IdItem>
 */
public class FuzzyCluster<IdItem> implements Comparable {

    private static int nextIdCluster = 0;

    private static int getIdCluster() {
        return nextIdCluster++;
    }

    private final DataVector<IdItem> centroid;
    private final int idCluster;

    public FuzzyCluster(DataVector<IdItem> centroid) {
        this.centroid = new DataVector<>(centroid);
        this.idCluster = getIdCluster();
    }

    public DataVector<IdItem> centroid() {
        return centroid;
    }

    @Override
    public int compareTo(Object obj) {
        if (obj instanceof FuzzyCluster) {
            FuzzyCluster otherFuzzyCluster = (FuzzyCluster) obj;
            int diff = this.idCluster - otherFuzzyCluster.idCluster;
            if (diff > 0) {
                return 1;
            } else {
                if (diff < 0) {
                    return -1;
                } else {
                    return 0;
                }
            }
        }
        throw new IllegalStateException("Not comparable to " + obj);
    }

    @Override
    public String toString() {
        DecimalFormat df = new DecimalFormat();

        df.setMinimumFractionDigits(3);
        df.setMaximumFractionDigits(3);

        String ret = "{";

        Iterator<Map.Entry<IdItem, Double>> iterator = centroid.entrySet().iterator();

        {
            Map.Entry<IdItem, Double> entry = iterator.next();

            IdItem idItem = entry.getKey();
            Double value = entry.getValue();

            ret = ret + idItem.toString() + "=" + df.format(value);
        }

        for (; iterator.hasNext();) {
            Map.Entry<IdItem, Double> entry = iterator.next();

            IdItem idItem = entry.getKey();
            Double value = entry.getValue();

            ret = ret + ", " + idItem.toString() + "=" + df.format(value);
        }

        ret = ret + "}";
        return ret;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FuzzyCluster) {
            FuzzyCluster<IdItem> fuzzyCluster = (FuzzyCluster<IdItem>) obj;
            return this.idCluster == fuzzyCluster.idCluster;
        } else {
            return false;
        }

    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 53 * hash + Objects.hashCode(this.centroid);
        hash = 53 * hash + this.idCluster;
        return hash;
    }
}
