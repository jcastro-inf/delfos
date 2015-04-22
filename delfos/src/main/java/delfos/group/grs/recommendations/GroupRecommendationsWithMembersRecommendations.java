package delfos.group.grs.recommendations;

import delfos.dataset.basic.user.User;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.rs.recommendation.Recommendation;
import delfos.rs.recommendation.Recommendations;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Encapsula las recomendaciones hechas a un grupo.
 *
 * @author Jorge Castro Gallardo
 */
public class GroupRecommendationsWithMembersRecommendations extends Recommendations {

    private static final long serialVersionUID = 34235l;

    private final GroupOfUsers targetGroupOfUsers;
    private Map<User, Recommendations> membersRecommendations;

    protected GroupRecommendationsWithMembersRecommendations() {
        super();
        targetGroupOfUsers = null;
    }

    public GroupRecommendationsWithMembersRecommendations(GroupRecommendations groupRecommendations, Recommendations... membersRecommendations) {
        super(
                groupRecommendations.getTargetIdentifier(),
                groupRecommendations.getRecommendations(),
                groupRecommendations.getRecommendationComputationDetails()
        );

        checkMembersAreInGroup(groupRecommendations, membersRecommendations);

        this.targetGroupOfUsers = groupRecommendations.getGroupOfUsers();

        this.membersRecommendations = new TreeMap<>();
        for (Recommendations memberRecommendations : membersRecommendations) {
            User member = User.parseIdTarget(memberRecommendations.getTargetIdentifier());
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

        Set<Integer> expectedGroupMembers = new TreeSet<>(groupRecommendations.getGroupOfUsers().getGroupMembers());

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
