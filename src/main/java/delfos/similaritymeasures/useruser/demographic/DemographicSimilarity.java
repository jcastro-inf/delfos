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
package delfos.similaritymeasures.useruser.demographic;

import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.features.Feature;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.user.User;
import delfos.similaritymeasures.CosineCoefficient;
import delfos.similaritymeasures.SimilarityMeasureAdapter;
import delfos.similaritymeasures.useruser.UserUserSimilarity;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * User user similarity defined in the paper:
 * <p>
 * <p>
 * Vozalis, Manolis, and Konstantinos G. Margaritis. "Collaborative filtering enhanced by demographic correlation." In
 * AIAI symposium on professional practice in AI, of the 18th world computer congress. 2004.
 *
 * @author jcastro
 */
public class DemographicSimilarity extends SimilarityMeasureAdapter implements UserUserSimilarity {

    private static final CosineCoefficient cosine = new CosineCoefficient();

    @Override
    public double similarity(DatasetLoader<? extends Rating> datasetLoader, User user1, User user2) throws UserNotFound, CannotLoadRatingsDataset {

        if (user1.getFeatures().isEmpty() && user2.getFeatures().isEmpty()) {
            return Double.NaN;
        }

        if (user1.equals(user2)) {
            return 1;
        }

        List<Double> user1Features = new ArrayList<>();
        List<Double> user2Features = new ArrayList<>();

        Feature age = user1.getFeatures().stream().filter(feature -> feature.getName().equalsIgnoreCase("age")).findAny().orElse(null);

        boolean user1hasAge = user1.getFeatures().contains(age);
        boolean user2hasAge = user2.getFeatures().contains(age);

        if (user1hasAge && user2hasAge) {

            double user1age = ((Number) user1.getFeatureValue(age)).doubleValue();
            double user2age = ((Number) user2.getFeatureValue(age)).doubleValue();

            user1Features.addAll(ageToVectorValue(user1age));
            user2Features.addAll(ageToVectorValue(user2age));
        } else {
            throw new IllegalStateException("arg");
        }

        Feature gender = user1.getFeatures().stream().filter(feature -> feature.getName().equalsIgnoreCase("gender")).findAny().orElse(null);
        boolean user1hasGender = user1.getFeatures().contains(gender);
        boolean user2hasGender = user2.getFeatures().contains(gender);

        if (user1hasGender && user2hasGender) {
            Object user1gender = user1.getFeatureValue(gender);
            Object user2gender = user2.getFeatureValue(gender);

            user1Features.addAll(genderToVectorValue(user1gender));
            user2Features.addAll(genderToVectorValue(user2gender));
        } else {
            throw new IllegalStateException("arg");
        }

        Feature occupation = user1.getFeatures().stream().filter(feature -> feature.getName().equalsIgnoreCase("occupation")).findAny().orElse(null);

        List<String> allOccupations = datasetLoader.getUsersDataset().getAllFeatureValues(occupation).stream()
                .map(value -> value.toString())
                .sorted()
                .collect(Collectors.toList());

        boolean user1hasOccupation = user1.getFeatures().contains(occupation);
        boolean user2hasOccupation = user2.getFeatures().contains(occupation);

        if (user1hasOccupation && user2hasOccupation) {
            Object user1Occupation = user1.getFeatureValue(occupation);
            Object user2Occupation = user2.getFeatureValue(occupation);

            user1Features.addAll(occupationToVectorValue(allOccupations, user1Occupation));
            user2Features.addAll(occupationToVectorValue(allOccupations, user2Occupation));
        } else {
            throw new IllegalStateException("arg");
        }

        double similarity = cosine.similarity(user1Features, user2Features);

        return similarity;

    }

    @Override
    public double similarity(DatasetLoader<? extends Rating> datasetLoader, int idUser1, int idUser2) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private List<Double> ageToVectorValue(double user2age) {

        List<Double> list = new ArrayList<>();

        list.add(.0);
        list.add(.0);
        list.add(.0);
        list.add(.0);

        if (0 <= user2age && user2age <= 18) {
            list.set(0, 1.0);
        } else if (18 < user2age && user2age <= 29) {
            list.set(1, 1.0);
        } else if (29 < user2age && user2age <= 49) {
            list.set(2, 1.0);
        } else {
            //user is older than 49
            list.set(3, 1.0);
        }
        return list;
    }

    private Collection<? extends Double> genderToVectorValue(Object user1gender) {

        List<Double> list = new ArrayList<>();

        list.add(.0);
        list.add(.0);

        switch (user1gender.toString()) {
            case "M":
                list.set(0, 1.0);
                break;
            case "F":
                list.set(1, 1.0);
                break;

            default:
                throw new IllegalStateException("arg");
        }

        return list;
    }

    private Collection<? extends Double> occupationToVectorValue(List<String> allOccupations, Object user1occupation) {

        List<Double> list = new ArrayList<>();

        IntStream.range(0, allOccupations.size()).forEach(i -> list.add(0.0));

        int binarySearch = Collections.binarySearch(allOccupations, user1occupation.toString());

        if (binarySearch >= 0) {
            list.set(binarySearch, 1.0);
        } else {
            throw new IllegalArgumentException("arg");
        }

        return list;
    }

}
