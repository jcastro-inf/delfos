package delfos.common.parameters.chain;

import delfos.common.parameters.Parameter;
import delfos.common.parameters.ParameterOwner;
import delfos.group.casestudy.defaultcase.GroupCaseStudy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents a chain of parameters and values of a given parameter owner.
 *
 * @author Jorge Castro Gallardo
 *
 */
public class ParameterChain {

    /**
     * Returns the parameter chains that are common to at least two
     * groupCaseStudyResults and also have at least two case study with
     * different value for the terminal value.
     *
     * @param groupCaseStudys
     * @return
     */
    public static List<ParameterChain> obtainDifferentChains(List<GroupCaseStudy> groupCaseStudys) {

        if (groupCaseStudys.isEmpty()) {
            return Collections.EMPTY_LIST;
        }

        List<ParameterChain> allParameterChains = obtainAllParameterChains(
                groupCaseStudys.iterator().next());

        for (GroupCaseStudy groupCaseStudy : groupCaseStudys) {
            List<ParameterChain> thisCaseStudyParameterChains
                    = obtainAllParameterChains(groupCaseStudy);

            List<ParameterChain> notMatchedWithExisting = new ArrayList<>();

            for (ParameterChain parameterChain : thisCaseStudyParameterChains) {

                List<ParameterChain> matchesWith = allParameterChains.stream()
                        .filter(parameterChain2 -> parameterChain.isCompatible(parameterChain2))
                        .collect(Collectors.toList());

                if (matchesWith.isEmpty()) {
                    notMatchedWithExisting.add(parameterChain);
                }
            }

            if (!notMatchedWithExisting.isEmpty()) {
                allParameterChains.addAll(notMatchedWithExisting);
            }
        }

        //Delete chains applicable to only one groupCaseStudy
        List<ParameterChain> chainsApplicableToMoreThanOne = allParameterChains.stream()
                .filter(parameterChain -> {
                    List<GroupCaseStudy> applicableTo = groupCaseStudys.stream()
                    .filter(groupCaseStudy -> parameterChain.isApplicableTo(groupCaseStudy)).collect(Collectors.toList());
                    boolean applicableToMoreThanOne = applicableTo.size() > 1;
                    return applicableToMoreThanOne;
                })
                .collect(Collectors.toList());

        List<ParameterChain> chainsWithMoreThanOneDifferentValue = chainsApplicableToMoreThanOne.stream()
                .filter(parameterChain -> {
                    Set<Object> differentValues = groupCaseStudys.stream()
                    .filter(groupCaseStudy -> parameterChain.isApplicableTo(groupCaseStudy))
                    .map(groupCaseStudy -> parameterChain.getValueOn(groupCaseStudy)).collect(Collectors.toSet());

                    if (differentValues.isEmpty()) {
                        throw new IllegalStateException("There must be at least one different value.");
                    }

                    boolean moreThanOneDifferentValue = differentValues.size() > 1;

                    return moreThanOneDifferentValue;
                }).collect(Collectors.toList());

        return chainsWithMoreThanOneDifferentValue;
    }

    public boolean isDataValidationParameter() {
        if (nodes.isEmpty()) {
            return !leaf.getParameter().equals(GroupCaseStudy.GROUP_RECOMMENDER_SYSTEM);
        } else {
            return nodes.get(0).getParameter() != GroupCaseStudy.GROUP_RECOMMENDER_SYSTEM;
        }
    }

    public boolean isTechniqueParameter() {
        if (nodes.isEmpty()) {
            return leaf.getParameter().equals(GroupCaseStudy.GROUP_RECOMMENDER_SYSTEM);
        } else {
            return nodes.get(0).getParameter() == GroupCaseStudy.GROUP_RECOMMENDER_SYSTEM;
        }
    }

    public static List<ParameterChain> obtainDataValidationParameterChains(GroupCaseStudy groupCaseStudy) {
        List<ParameterChain> allParameterChains = obtainAllParameterChains(groupCaseStudy);

        List<ParameterChain> dataValidationParameterChains
                = allParameterChains.stream()
                .filter(chain -> chain.isDataValidationParameter())
                .collect(Collectors.toList());

        return dataValidationParameterChains;
    }

    public static List<ParameterChain> obtainTechniqueParameterChains(GroupCaseStudy groupCaseStudy) {
        List<ParameterChain> allParameterChains = obtainAllParameterChains(groupCaseStudy);

        List<ParameterChain> dataValidationParameterChains
                = allParameterChains.stream()
                .filter(chain -> chain.isTechniqueParameter())
                .collect(Collectors.toList());

        return dataValidationParameterChains;
    }

    public static List<ParameterChain> obtainAllParameterChains(ParameterOwner rootParameterOwner) {

        List<ParameterChain> allParameterChains = new ArrayList<>();

        ParameterChain rootChain = new ParameterChain(rootParameterOwner);

        Collection<Parameter> groupCaseStudyParameters = rootParameterOwner.getParameters();

        for (Parameter parameter : groupCaseStudyParameters) {
            Object parameterValue = rootParameterOwner.getParameterValue(parameter);
            allParameterChains.add(rootChain.createWithLeaf(parameter, parameterValue));

            if (parameterValue instanceof ParameterOwner) {
                ParameterOwner parameterValueParameterOwner = (ParameterOwner) parameterValue;

                List<ParameterChain> chains
                        = obtainAllParameterChains(parameterValueParameterOwner);

                for (ParameterChain chain : chains) {
                    ParameterChain newChain = rootChain.addChain(chain, parameter);
                    allParameterChains.add(newChain);
                }
            }
        }

        return allParameterChains;
    }

    private final Root root;
    private final List<Node> nodes;
    private final Leaf leaf;

    public ParameterChain(ParameterOwner parameterOwner) {
        this.root = new Root(parameterOwner);
        this.nodes = Collections.unmodifiableList(new ArrayList<>());
        this.leaf = null;
    }

    private ParameterChain(Root root, List<Node> nodes, Leaf leaf) {
        this.root = root;
        this.nodes = Collections.unmodifiableList(new ArrayList<>(nodes));
        this.leaf = leaf;
    }

    public ParameterChain createWithNode(Parameter parameter, ParameterOwner parameterOwner) {
        if (leaf != null) {
            throw new IllegalStateException("Cannot add node after adding leaf");
        }

        List<Node> newNodes = new ArrayList<>(nodes);
        newNodes.add(new Node(parameter, parameterOwner));

        newNodes = Collections.unmodifiableList(newNodes);

        return new ParameterChain(root, newNodes, leaf);
    }

    public ParameterChain createWithLeaf(Parameter parameter, Object parameterValue) {
        return new ParameterChain(root, nodes, new Leaf(parameter, parameterValue));
    }

    public boolean isCompatible(ParameterChain parameterChain) {

        boolean rootMatch = parameterChain.root.isCompatibleWith(this.root);
        if (!rootMatch) {
            return false;
        }

        int firstChainSize = this.nodes.size();
        boolean nodesSizesMatch = parameterChain.nodes.size() == firstChainSize;
        if (!nodesSizesMatch) {
            return false;
        }

        for (int i = 0; i < firstChainSize; i++) {
            final int thisNodeIndex = i;
            final Node firstChainNode = this.nodes.get(thisNodeIndex);

            boolean nodeIsCompatible = parameterChain.nodes.get(thisNodeIndex).isCompatibleWith(firstChainNode);
            if (!nodeIsCompatible) {
                return false;
            }
        }

        boolean leafMatch = parameterChain.leaf.isCompatibleWith(this.leaf);
        if (!leafMatch) {
            return false;
        }
        return true;
    }

    public static boolean areCompatible(ParameterChain... parameterChains) {
        return areCompatible(Arrays.asList(parameterChains));
    }

    public static boolean areCompatible(List<ParameterChain> parameterChains) {

        return parameterChains.stream()
                .allMatch(parameterChain
                        -> parameterChains.stream()
                        .allMatch(parameterChain2 -> parameterChain.isCompatible(parameterChain2)));

    }

    public static boolean areSame(ParameterChain... parameterChains) {
        return areSame(Arrays.asList(parameterChains));
    }

    public static boolean areSame(List<ParameterChain> parameterChains) {
        if (areCompatible(parameterChains)) {

            ParameterChain firstChain = parameterChains.iterator().next();
            Leaf firstLeaf = parameterChains.iterator().next().leaf;

            List<ParameterChain> parameterChainsThatMatchLeaf = parameterChains.stream()
                    .filter(parameterChain -> parameterChain.leaf.equals(firstLeaf))
                    .collect(Collectors.toList());

            return parameterChains.stream()
                    .allMatch(parameterChain -> parameterChain.leaf.equals(firstLeaf));
        } else {
            return false;
        }
    }

    public List<ParameterChain> createAllTerminalParameterChains(ParameterOwner parameterOwner) {
        List<ParameterChain> parameterChains = new ArrayList<>();

        parameterChains.add(new ParameterChain(parameterOwner));

        return parameterChains;
    }

    /**
     * Adds the specified chain to the current chain nodes. It moves the root
     * element of the parameter chain to this chain nodes as the first node.
     *
     * @param chain
     * @return
     */
    private ParameterChain addChain(ParameterChain chain, Parameter parameter) {

        Root newRoot = this.root;

        List<Node> newNodes = new ArrayList<>(chain.nodes);
        newNodes.add(0, new Node(parameter, chain.root.getParameterOwner()));

        Leaf newLeaf = chain.leaf;

        return new ParameterChain(newRoot, newNodes, newLeaf);
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();

        str.append(this.root.getParameterOwner().getName()).append(" ==> ");

        if (!nodes.isEmpty()) {
            for (Node node : nodes) {
                str.append(node.getParameter().getName());
                str.append(" = ");

                if (node.getParameterOwner() != null) {
                    str.append(node.getParameterOwner().getName());
                } else {
                    str.append(node.getParameterOwner());
                }

                str.append(" -> ");
            }

            str.delete(str.length() - 4, str.length());
            str.append(" ==> ");
        }

        str.append(leaf.getParameter().getName());
        str.append(" = ");

        if (leaf.getParameterValue() != null) {
            str.append(leaf.getParameterValue().toString());
        } else {
            str.append(leaf.getParameterValue());
        }

        return str.toString();
    }

    protected Leaf getLeaf() {
        return leaf;
    }

    protected List<Node> getNodes() {
        return nodes;
    }

    protected Root getRoot() {
        return root;
    }

    public boolean isAlias() {
        return leaf.getParameter().equals(ParameterOwner.ALIAS);
    }

    public boolean isApplicableTo(ParameterOwner parameterOwner) {

        if (!root.getParameterOwner().getClass().equals(parameterOwner.getClass())) {
            return false;
        }

        ParameterOwner parameterOwnerToGetValue = parameterOwner;

        for (Node node : nodes) {
            if (!parameterOwnerToGetValue.haveParameter(node.getParameter())) {
                return false;
            }
            parameterOwnerToGetValue = (ParameterOwner) parameterOwnerToGetValue.getParameterValue(
                    node.getParameter());
        }

        return parameterOwnerToGetValue.haveParameter(leaf.getParameter());
    }

    public Object getValueOn(ParameterOwner parameterOwner) {
        if (!isApplicableTo(parameterOwner)) {
            throw new IllegalArgumentException("ParameterOwner is not compatible: " + root.getParameterOwner().getClass() + " != " + parameterOwner.getClass());
        }

        ParameterOwner parameterOwnerToGetValue = parameterOwner;

        for (Node node : nodes) {
            parameterOwnerToGetValue = (ParameterOwner) parameterOwnerToGetValue.getParameterValue(
                    node.getParameter());
        }

        return parameterOwnerToGetValue.getParameterValue(leaf.getParameter());
    }

    public static String printListOfChains(Collection<ParameterChain> allChains) {

        StringBuilder str = new StringBuilder();

        ArrayList<ParameterChain> allParameterChains = new ArrayList<>(allChains);

        allParameterChains.sort((ParameterChain o1, ParameterChain o2)
                -> o1.toString().compareTo(o2.toString()));

        str.append("=====================================================\n");
        str.append("all chains for now\n");
        for (ParameterChain chain : allParameterChains) {
            str.append(chain.toString()).append("\n");
        }
        str.append("=====================================================\n");

        return str.toString();
    }
}
