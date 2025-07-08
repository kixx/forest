package org.bsdro.forest.ab;

import java.util.ArrayList;
import java.util.List;

abstract class Node {
    List<Integer> keys = new ArrayList<>();

    abstract boolean isLeaf();
}

