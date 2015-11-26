package delfos.common.parameters.chain;

import delfos.common.parameters.Parameter;
import delfos.common.parameters.ParameterOwner;
import delfos.group.casestudy.defaultcase.GroupCaseStudy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
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
    public static List<ParameterChain> obtainDataValidationDifferentChains(List<GroupCaseStudy> groupCaseStudys) {

        List<ParameterChain> parameterChains = new ArrayList<>();

        for (GroupCaseStudy groupCaseStudy : groupCaseStudys) {
            List<ParameterChain> obtainAllParameterChains = obtainAllParameterChains(groupCaseStudy);
            parameterChains.addAll(obtainAllParameterChains);
        }
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public static List<ParameterChain> obtainDataValidationParameterChains(GroupCaseStudy groupCaseStudy) {
        List<ParameterChain> allParameterChains = obtainAllParameterChains(groupCaseStudy);

        List<ParameterChain> dataValidationParameterChains
                = allParameterChains.stream()
                .filter(chain -> chain.nodes.isEmpty() || chain.nodes.get(0).getParameter() != GroupCaseStudy.GROUP_RECOMMENDER_SYSTEM
                ).collect(Collectors.toList());

        return dataValidationParameterChains;
    }

    public static List<ParameterChain> obtainTechniqueParameterChains(GroupCaseStudy groupCaseStudy) {
        List<ParameterChain> allParameterChains = obtainAllParameterChains(groupCaseStudy);

        List<ParameterChain> dataValidationParameterChains
                = allParameterChains.stream()
                .filter(chain -> !(chain.nodes.isEmpty() || chain.nodes.get(0).getParameter() != GroupCaseStudy.GROUP_RECOMMENDER_SYSTEM)
                ).collect(Collectors.toList());

        return dataValidationParameterChains;
    }

    public static List<ParameterChain> obtainAllParameterChains(ParameterOwner rootParameterOwner) {

        List<ParameterChain> allParameterChains = new ArrayList<>();

        ParameterChain rootChain = new ParameterChain(rootParameterOwner);

        Collection<Parameter> groupCaseStudyParameters = rootParameterOwner.getParameters();

        for (Parameter parameter : groupCaseStudyParameters) {
            Object parameterValue = rootParameterOwner.getParameterValue(parameter);

            if (parameterValue instanceof ParameterOwner) {
                ParameterOwner parameterValueParameterOwner = (ParameterOwner) parameterValue;

                List<ParameterChain> chains
                        = obtainAllParameterChains(parameterValueParameterOwner);

                for (ParameterChain chain : chains) {
                    ParameterChain newChain = rootChain.addChain(chain, parameter);
                    allParameterChains.add(newChain);
                }
            } else {
                allParameterChains.add(rootChain.createWithLeaf(parameter, parameterValue));
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

    public static boolean areCompatible(ParameterChain... parameterChains) {
        return areCompatible(Arrays.asList(parameterChains));
    }

    public static boolean areCompatible(List<ParameterChain> parameterChains) {
        if (parameterChains.isEmpty()) {
            return true;
        }

        ParameterChain firstChain = parameterChains.iterator().next();
        Root firstRoot = parameterChains.iterator().next().root;

        boolean rootMatch = parameterChains.stream().allMatch(parameterChain -> parameterChain.root.isCompatibleWith(firstRoot));
        if (!rootMatch) {
            return false;
        }

        int firstChainSize = firstChain.nodes.size();
        boolean nodesSizesMatch = parameterChains.stream().allMatch(parameterChain -> parameterChain.nodes.size() == firstChainSize);
        if (!nodesSizesMatch) {
            return false;
        }

        for (int i = 0; i < firstChainSize; i++) {
            final int thisNodeIndex = i;
            final Node firstChainNode = firstChain.nodes.get(thisNodeIndex);

            boolean matchesNode = parameterChains.stream()
                    .allMatch(parameterChain -> parameterChain.nodes.get(thisNodeIndex).isCompatibleWith(firstChainNode));
            if (!matchesNode) {
                return false;
            }
        }

        Leaf firstLeaf = parameterChains.iterator().next().leaf;

        boolean leafMatch = parameterChains.stream().allMatch(
                parameterChain -> parameterChain.leaf.isCompatibleWith(firstLeaf)
        );
        if (!leafMatch) {
            return false;
        }
        return true;
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

    public boolean isCompatibleWith(ParameterOwner parameterOwner) {
        List<ParameterChain> allParameterChains = obtainAllParameterChains(parameterOwner);

        List<ParameterChain> compatibleParameterChains = new ArrayList<>();

        int i = 0;
        for (ParameterChain parameterChain : allParameterChains) {

            if (i == 8) {
                System.out.println("stap!");
            }
            System.out.println(i + "\t" + parameterChain.toString());
            if (areCompatible(this, parameterChain)) {
                compatibleParameterChains.add(parameterChain);
            }

            i++;
        }

        List<ParameterChain> compatibleParameterChains_2 = allParameterChains
                .stream().filter(parameterChain -> areCompatible(this, parameterChain)).collect(Collectors.toList());

        if (compatibleParameterChains.isEmpty()) {
            return false;
        } else if (compatibleParameterChains.size() == 1) {
            return true;
        } else {
            return false;
        }
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
                str.append(node.getParameterOwner().getName());

                str.append(" -> ");
            }

            str.delete(str.length() - 4, str.length());
            str.append(" ==> ");
        }

        str.append(leaf.getParameter().getName());
        str.append(" = ");
        str.append(leaf.getParameterValue().toString());

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

}
