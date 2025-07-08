package org.bsdro.forest.ab;

import java.util.ArrayList;
import java.util.List;

class ABTreeInnerNode extends Node {
    List<Node> children = new ArrayList<>();

    @Override
    boolean isLeaf() {
        return false;
    }
}
