package delfos.rs.explanation;

/**
 *
 * @version 09-sep-2014
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 * @param <GroupModel>
 * @param <Explanation>
 */
public class GroupModelWithExplanation<GroupModel, Explanation> {

    private final GroupModel groupModel;
    private final Explanation explanation;

    public GroupModelWithExplanation(GroupModel groupModel, Explanation explanation) {
        this.groupModel = groupModel;
        this.explanation = explanation;
    }

    public GroupModel getGroupModel() {
        return groupModel;
    }

    public Explanation getExplanation() {
        return explanation;
    }
}
