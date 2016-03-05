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
package delfos.group.grs.recommendations;

import delfos.dataset.basic.user.User;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.rs.recommendation.Recommendation;
import delfos.rs.recommendation.Recommendations;
import delfos.rs.recommendation.RecommendationsToUser;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Encapsula las recomendaciones hechas a un grupo.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class GroupRecommendationsWithMembersRecommendations extends GroupRecommendations {

    private static final long serialVersionUID = 34235l;

    private final GroupOfUsers targetGroupOfUsers;
    private Map<User, Recommendations> membersRecommendations;

    protected GroupRecommendationsWithMembersRecommendations() {
        super();
        targetGroupOfUsers = null;
    }

    public GroupRecommendationsWithMembersRecommendations(
            GroupRecommendations groupRecommendations,
            Collection<RecommendationsToUser> membersRecommendations) {

        this(groupRecommendations, membersRecommendations.toArray(new RecommendationsToUser[0]));
    }

    public GroupRecommendationsWithMembersRecommendations(
            GroupRecommendations groupRecommendations,
            RecommendationsToUser... membersRecommendations) {
        super(
                groupRecommendations.getGroupOfUsers(),
                groupRecommendations.getRecommendations(),
                groupRecommendations.getRecommendationComputationDetails()
        );

        checkMembersAreInGroup(groupRecommendations, membersRecommendations);

        this.targetGroupOfUsers = groupRecommendations.getGroupOfUsers();

        this.membersRecommendations = new TreeMap<>();
        for (Recommendations memberRecommendations : membersRecommendations) {
            User member = (User) memberRecommendations.getTarget();
            this.membersRecommendations.put(member, memberRecommendations);
        }

    }

    /**
     * Devuelve las recomendaciones del grupo.
     *
     * @return recomendaciones del grupo.
     */
    @Override
    public Collection<Recommendation> getRecommendations() {
        return super.getRecommendations();
    }

    public GroupOfUsers getTargetGroupOfUsers() {
        return targetGroupOfUsers;
    }

    public Recommendations getMemberRecommendations(User memberOfGroup) {
        if (!getTargetGroupOfUsers().contains(memberOfGroup.getId())) {
            throw new IllegalArgumentException("User '" + memberOfGroup.toString() + "' not a member of group '" + getTargetGroupOfUsers() + "'");
        } else if (!membersRecommendations.containsKey(memberOfGroup)) {
            throw new IllegalArgumentException("User '" + memberOfGroup.toString() + "' not a member of group '" + getTargetGroupOfUsers() + "'");
        }

        return membersRecommendations.get(memberOfGroup);

    }

    private static void checkMembersAreInGroup(GroupRecommendations groupRecommendations, Recommendations[] membersRecommendations) {
        final Set<Integer> membersRecommendationsGroupMembers = new TreeSet<>();
        Arrays.asList(membersRecommendations)
                .stream()
                .forEach((memberRecommendations) -> {
                    Integer idMember = User.parseIdTarget(memberRecommendations.getTargetIdentifier()).getId();
                    membersRecommendationsGroupMembers.add(idMember);
                });

        Set<Integer> expectedGroupMembers = new TreeSet<>(groupRecommendations.getGroupOfUsers().getIdMembers());

        if (!expectedGroupMembers.equals(membersRecommendationsGroupMembers)) {
            throw new IllegalArgumentException(
                    "Group members '"
                    + expectedGroupMembers
                    + "' are different to specified single user recommendations '"
                    + membersRecommendationsGroupMembers
                    + "'");
        }
    }

}
