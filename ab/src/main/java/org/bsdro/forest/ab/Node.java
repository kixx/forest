package org.bsdro.forest.ab;

import java.util.ArrayList;
import java.util.List;

class Node {
    List<Integer> keys = new ArrayList<>();
    List<Node> children = new ArrayList<>();

    boolean isLeaf() {
        return children.isEmpty();
    }
}
