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
package delfos.results.recommendation;

import delfos.casestudy.parallelisation.RecommendationTaskInput;
import delfos.casestudy.parallelisation.RecommendationTaskOutput;
import delfos.common.Global;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.user.User;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Almacena los resultados de recomendaciones de un sistema de recomendación
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @param <RecommendationModel>
 * @param <RatingType>
 */
public class RecommenderSystemResult<RecommendationModel extends Object, RatingType extends Rating> {

    protected Map<User, RecommendationTaskInput<RecommendationModel, RatingType>> recommendationInputs_byUser;
    protected Map<User, RecommendationTaskOutput> recommendationOutputs_byUser;

    private final String caseStudyAlias;
    private final int thisExecution;
    private final int thisSplit;

    private final long modelBuildTime;

    public RecommenderSystemResult(
            List<RecommendationTaskInput<RecommendationModel, RatingType>> recommendationTaskInputs,
            List<RecommendationTaskOutput> recommendationTaskOutputs,
            String caseStudyAlias,
            int thisExecution,
            int thisSplit,
            long modelBuildTime
    ) {

        validateSameUsers(recommendationTaskInputs, recommendationTaskOutputs);

        validateUniqueUsers(recommendationTaskInputs, recommendationTaskOutputs);

        this.recommendationInputs_byUser = recommendationTaskInputs.parallelStream()
                .collect(Collectors.toMap(
                        output -> output.getUser(),
                        output -> output));

        this.recommendationOutputs_byUser = recommendationTaskOutputs.parallelStream()
                .collect(Collectors.toMap(
                        output -> output.getUser(),
                        output -> output));
        this.caseStudyAlias = caseStudyAlias;
        this.thisExecution = thisExecution;
        this.thisSplit = thisSplit;
        this.modelBuildTime = modelBuildTime;
    }

    /**
     * Devuelve el número de grupos que han sido evaluados.
     *
     * @return Número de grupos evaluados
     */
    public int getNumUsers() {
        return recommendationOutputs_byUser.size();
    }

    /**
     * Devuelve el número de grupos que han sido evaluados.
     *
     * @return Número de grupos evaluados
     */
    public int getNumOutputs() {
        return recommendationOutputs_byUser.size();
    }

    public Collection<User> getGroupsOfUsers() {
        return recommendationInputs_byUser.keySet().stream().collect(Collectors.toList());
    }

    public Collection<RecommendationTaskInput> inputsIterator() {
        return recommendationInputs_byUser.values().stream().collect(Collectors.toList());
    }

    public Collection<RecommendationTaskOutput> outputsIterator() {
        return recommendationOutputs_byUser.values().stream().collect(Collectors.toList());
    }

    public RecommendationTaskInput getGroupInput(User user) {
        if (recommendationInputs_byUser.containsKey(user)) {
            return recommendationInputs_byUser.get(user);
        } else {
            throw new IllegalArgumentException("User '" + user.toString() + "' not in the input list");
        }
    }

    public RecommendationTaskOutput getGroupOutput(User user) {
        if (recommendationOutputs_byUser.containsKey(user)) {
            return recommendationOutputs_byUser.get(user);
        } else {
            throw new IllegalArgumentException("User '" + user.toString() + "' not in the output list");
        }
    }

    /**
     * @return the caseStudyAlias
     */
    public String getCaseStudyAlias() {
        return caseStudyAlias;
    }

    /**
     * @return the thisExecution
     */
    public int getThisExecution() {
        return thisExecution;
    }

    /**
     * @return the thisSplit
     */
    public int getThisSplit() {
        return thisSplit;
    }

    public long getModelBuildTime() {
        return modelBuildTime;
    }

    private void validateSameUsers(List<RecommendationTaskInput<RecommendationModel, RatingType>> recommendationInputs, List<RecommendationTaskOutput> recommendationOutputs) {
        Set<User> usersInput = recommendationInputs.stream()
                .map(task -> task.getUser())
                .collect(Collectors.toSet());

        Set<User> usersOutput = recommendationOutputs.stream()
                .map(task -> task.getUser())
                .collect(Collectors.toSet());

        boolean equals = usersInput.equals(usersOutput);

        if (!equals) {

            Set<User> inInputButNotInOutput = usersInput.stream().filter(user -> !usersOutput.contains(user)).collect(Collectors.toSet());
            Set<User> inOutputButNotInInput = usersOutput.stream().filter(user -> !usersInput.contains(user)).collect(Collectors.toSet());

            Global.showWarning("Groups found in Input But Not In Output: " + inInputButNotInOutput.toString());
            Global.showWarning("Groups found in Output But Not In Input: " + inOutputButNotInInput.toString());

            throw new IllegalArgumentException("Groups in the list of GRS input and in the GRS output are different!.");
        }
    }

    private void validateUniqueUsers(
            List<RecommendationTaskInput<RecommendationModel, RatingType>> recommendationTaskInputs,
            List<RecommendationTaskOutput> recommendationTaskOutputs) {
        Map<User, List<RecommendationTaskInput<RecommendationModel, RatingType>>> inputs_byUser = recommendationTaskInputs.stream()
                .collect(Collectors.groupingBy(input -> input.getUser()));

        inputs_byUser.forEach((user, inputs) -> {
            if (inputs.size() > 1) {
                throw new IllegalStateException("Users with multiple inputs, need to merge them");
            }

        });

        Map<User, List<RecommendationTaskOutput>> outputs_byUser = recommendationTaskOutputs.stream()
                .collect(Collectors.groupingBy(input -> input.getUser()));

        outputs_byUser.forEach((user, outputs) -> {
            if (outputs.size() > 1) {
                throw new IllegalStateException("Users with multiple inputs, need to merge them");
            }

        });
    }
}
