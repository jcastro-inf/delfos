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

import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.ParameterOwnerRestriction;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.user.User;
import delfos.similaritymeasures.PearsonCorrelationCoefficient;
import delfos.similaritymeasures.SimilarityMeasureAdapter;

public abstract class SimilarityCombination extends SimilarityMeasureAdapter implements UserUserSimilarity {

    private static final Parameter SIMILARITY_A = new Parameter("SimilarityA", new ParameterOwnerRestriction(UserUserSimilarity.class, new PearsonCorrelationCoefficient()));
    private static final Parameter SIMILARITY_B = new Parameter("SimilarityB", new ParameterOwnerRestriction(UserUserSimilarity.class, new PearsonCorrelationCoefficient()));

    public SimilarityCombination() {
        super();
        addParameter(SIMILARITY_A);
        addParameter(SIMILARITY_B);
    }

    public SimilarityCombination(UserUserSimilarity similarityA, UserUserSimilarity similarityB) {
        this();
        setParameterValue(SIMILARITY_A, similarityA);
        setParameterValue(SIMILARITY_B, similarityB);
    }

    public UserUserSimilarity getSIMILARITY_A() {
        return (UserUserSimilarity) getParameterValue(SIMILARITY_A);
    }

    public UserUserSimilarity getSIMILARITY_B() {
        return (UserUserSimilarity) getParameterValue(SIMILARITY_B);
    }

    @Override
    public double similarity(DatasetLoader<? extends Rating> datasetLoader, int idUser1, int idUser2) {
        if (idUser1 == idUser2) {
            return 1;
        }

        UserUserSimilarity similarityA = getSIMILARITY_A();
        UserUserSimilarity similarityB = getSIMILARITY_B();

        double valueA = similarityA.similarity(datasetLoader, idUser1, idUser2);
        double valueB = similarityB.similarity(datasetLoader, idUser1, idUser2);

        return combine(valueA, valueB);
    }

    @Override
    public double similarity(DatasetLoader<? extends Rating> datasetLoader, User user1, User user2) {
        if (user1.equals(user2)) {
            return 1;
        }

        UserUserSimilarity similarityA = getSIMILARITY_A();
        UserUserSimilarity similarityB = getSIMILARITY_B();

        double valueA = similarityA.similarity(datasetLoader, user1, user2);
        double valueB = similarityB.similarity(datasetLoader, user1, user2);

        return combine(valueA, valueB);
    }

    public abstract double combine(double valueA, double valueB);

    @Override
    public int hashCode() {
        UserUserSimilarity similarityA = getSIMILARITY_A();
        UserUserSimilarity similarityB = getSIMILARITY_B();

        int hash = 7;

        hash = 97 * hash + similarityA.hashCode();
        hash = 97 * hash + similarityB.hashCode();

        return hash;
    }

}
