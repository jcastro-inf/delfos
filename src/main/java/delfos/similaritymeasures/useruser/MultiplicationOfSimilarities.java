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
package delfos.similaritymeasures.useruser;

public class MultiplicationOfSimilarities extends SimilarityCombination {

    public MultiplicationOfSimilarities() {
        super();
    }

    public MultiplicationOfSimilarities(UserUserSimilarity similarityA, UserUserSimilarity similarityB) {
        super(similarityA, similarityB);
    }

    @Override
    public double combine(double valueA, double valueB) {
        valueA = Math.max(0, valueA);
        valueB = Math.max(0, valueB);
        valueA = Math.min(1, valueA);
        valueB = Math.min(1, valueB);

        return valueA * valueB;
    }

}
