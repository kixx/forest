package org.bsdro.forest.ab;

import java.util.*;

public class ABTree {
    private class Node {
        List<Integer> keys = new ArrayList<>();
        List<Node> children = new ArrayList<>();

        boolean isLeaf() {
            return children.isEmpty();
        }
    }

    private final int a; // min children
    private final int b; // max children
    private Node root;

    public ABTree(int a, int b) {
        if (a < 2 || a > b / 2) throw new IllegalArgumentException("Require 2 <= a <= b/2");
        this.a = a;
        this.b = b;
        this.root = new Node(); // initially a leaf
    }

    public void insert(int key) {
        Node r = root;
        if (isFull(r)) {
            Node newRoot = new Node();
            newRoot.children.add(r);
            splitChild(newRoot, 0);
            root = newRoot;
        }
        insertNonFull(root, key);
    }

    private void insertNonFull(Node node, int key) {
        if (node.isLeaf()) {
            int i = Collections.binarySearch(node.keys, key);
            if (i < 0)  {
                i = -i - 1;
            }
            node.keys.add(i, key);
        } else {
            int i = findChildIndex(node, key);
            // Check if i is within bounds before accessing
            if (i >= node.children.size()) {
                i = node.children.size() - 1;
            }
            Node child = node.children.get(i);
            if (isFull(child)) {
                splitChild(node, i);
                if (key > node.keys.get(i)) { // Changed from >= to > to fix potential issue
                    i++;
                }
                // Ensure i is within bounds after increment
                if (i >= node.children.size()) {
                    i = node.children.size() - 1;
                }
            }
            insertNonFull(node.children.get(i), key);
        }
    }


    private void splitChild(Node parent, int index) {
        Node fullNode = parent.children.get(index);
        Node newNode = new Node();

        int mid = fullNode.keys.size() / 2;

        // Split keys
        newNode.keys.addAll(fullNode.keys.subList(mid, fullNode.keys.size()));
        fullNode.keys.subList(mid, fullNode.keys.size()).clear();

        // Split children if internal
        if (!fullNode.isLeaf()) {
            newNode.children.addAll(fullNode.children.subList(mid + 1, fullNode.children.size()));
            fullNode.children.subList(mid + 1, fullNode.children.size()).clear();
        }

        // Promote middle key of fullNode to parent
        int separator = newNode.keys.remove(0); // Remove from newNode and use as separator
        parent.keys.add(index, separator);
        parent.children.add(index + 1, newNode);
    }


    private int findChildIndex(Node node, int key) {
        int i = Collections.binarySearch(node.keys, key);
        return i >= 0 ? i + 1 : -i - 1;
    }

    private boolean isFull(Node node) {
        return node.keys.size() >= b - 1;
    }

    // Debug print
    public void printTree() {
        printTree(root, 0);
    }

    private void printTree(Node node, int depth) {
        String indent = "  ".repeat(depth);
        if (node.isLeaf()) {
            System.out.println(indent + "Leaf: " + node.keys);
        } else {
            System.out.println(indent + "Internal: " + node.keys);
            for (Node child : node.children) {
                printTree(child, depth + 1);
            }
        }
    }

    // Basic test
    public static void main(String[] args) {
        ABTree tree = new ABTree(2, 4); // (2,4)-tree
        int[] keys = {10, 20, 5, 6, 12, 30, 7, 17, 3, 25, 22};

        for (int k : keys) {
            System.out.println("===============================");
            System.out.println("Inserting: " + k);
            tree.insert(k);
            tree.printTree();
        }

        System.out.println("===============================");
        System.out.println("Final tree:");
        tree.printTree();
    }
}
