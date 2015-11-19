package delfos.common.parameters.chain;

import delfos.common.parameters.Parameter;
import delfos.common.parameters.ParameterOwner;
import java.util.ArrayList;
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

    private final Root root;
    private final List<Node> nodes;
    private final Leaf leaf;

    public ParameterChain(String rootName, ParameterOwner parameterOwner) {
        this.root = new Root(rootName, parameterOwner);
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

    public static boolean areCompatible(List<ParameterChain> parameterChains) {
        if (parameterChains.isEmpty()) {
            return true;
        }

        ParameterChain firstChain = parameterChains.iterator().next();
        Root firstRoot = parameterChains.iterator().next().root;

        boolean rootMatch = parameterChains.stream().allMatch(parameterChain -> parameterChain.root.equals(firstRoot));
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
                    .allMatch(parameterChain -> parameterChain.nodes.get(thisNodeIndex).equals(firstChainNode));
            if (!matchesNode) {
                return false;
            }
        }
        return true;
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

    public List<ParameterChain> createAllTerminalParameterChains(String rootName, ParameterOwner parameterOwner) {
        List<ParameterChain> parameterChains = new ArrayList<>();

        parameterChains.add(new ParameterChain(rootName, parameterOwner));

        return parameterChains;
    }
}
