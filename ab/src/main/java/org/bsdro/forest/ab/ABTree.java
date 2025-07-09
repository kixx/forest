package org.bsdro.forest.ab;

import java.util.*;

public class ABTree {

    private final int a; // min children
    private final int b; // max children
    private Node root;

    public ABTree(int a, int b) {
        if (a < 2 || a > b / 2) throw new IllegalArgumentException("Require 2 <= a <= b/2");
        this.a = a;
        this.b = b;
        this.root = new ABTreeLeaf(); // initially a leaf
    }

    public boolean contains(int key) {
        return contains(root, key);
    }

    private boolean contains(Node node, int key) {
        int i = Collections.binarySearch(node.keys, key);
        if (i >= 0) {
            return true;
        }

        if (node.isLeaf()) {
            return false;
        }

        ABTreeInnerNode innerNode = (ABTreeInnerNode) node;
        int childIndex = -i - 1;
        if (childIndex >= innerNode.children.size()) {
            childIndex = innerNode.children.size() - 1;
        }
        return contains(innerNode.children.get(childIndex), key);
    }

    public void insert(int key) {
        Node r = root;
        if (isFull(r)) {
            ABTreeInnerNode newRoot = new ABTreeInnerNode();
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
            ABTreeInnerNode innerNode = (ABTreeInnerNode) node;
            int i = findChildIndex(node, key);
            // Check if i is within bounds before accessing
            if (i >= innerNode.children.size()) {
                i = innerNode.children.size() - 1;
            }
            Node child = innerNode.children.get(i);
            if (isFull(child)) {
                splitChild(innerNode, i);
                if (key > node.keys.get(i)) { // Changed from >= to > to fix potential issue
                    i++;
                }
                // Ensure i is within bounds after increment
                if (i >= innerNode.children.size()) {
                    i = innerNode.children.size() - 1;
                }
            }
            insertNonFull(innerNode.children.get(i), key);
        }
    }


    private void splitChild(ABTreeInnerNode parent, int index) {
        Node fullNode = parent.children.get(index);
        Node newNode;

        if (fullNode.isLeaf()) {
            newNode = new ABTreeLeaf();
        } else {
            newNode = new ABTreeInnerNode();
        }

        int mid = fullNode.keys.size() / 2;

        // Split keys
        newNode.keys.addAll(fullNode.keys.subList(mid, fullNode.keys.size()));
        fullNode.keys.subList(mid, fullNode.keys.size()).clear();

        // Split children if internal
        if (!fullNode.isLeaf()) {
            ABTreeInnerNode innerFullNode = (ABTreeInnerNode) fullNode;
            ABTreeInnerNode innerNewNode = (ABTreeInnerNode) newNode;
            innerNewNode.children.addAll(innerFullNode.children.subList(mid + 1, innerFullNode.children.size()));
            innerFullNode.children.subList(mid + 1, innerFullNode.children.size()).clear();
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

    public boolean remove(int key) {
        if (!contains(key)) {
            return false;
        }

        boolean result = removeFromNode(root, key);

        // If root is an inner node with no keys and only one child, make that child the new root
        if (!root.isLeaf() && root.keys.isEmpty()) {
            ABTreeInnerNode innerRoot = (ABTreeInnerNode) root;
            if (innerRoot.children.size() == 1) {
                root = innerRoot.children.get(0);
            }
        }

        return result;
    }

    private boolean removeFromNode(Node node, int key) {
        int i = Collections.binarySearch(node.keys, key);

        if (node.isLeaf()) {
            // Key found in leaf node, remove it
            if (i >= 0) {
                node.keys.remove(i);
                return true;
            }
            return false;
        }

        ABTreeInnerNode innerNode = (ABTreeInnerNode) node;

        // Key found in internal node
        if (i >= 0) {
            // Get the predecessor from the left child
            Node leftChild = innerNode.children.get(i);
            if (leftChild.keys.size() >= a) { // Has enough keys to borrow
                // Find the largest key in the left subtree (predecessor)
                int predecessor = findLargestKey(leftChild);
                // Replace the key with its predecessor
                node.keys.set(i, predecessor);
                // Remove the predecessor from the left subtree
                removeFromNode(leftChild, predecessor);
            } else {
                // Get the successor from the right child
                Node rightChild = innerNode.children.get(i + 1);
                if (rightChild.keys.size() >= a) { // Has enough keys to borrow
                    // Find the smallest key in the right subtree (successor)
                    int successor = findSmallestKey(rightChild);
                    // Replace the key with its successor
                    node.keys.set(i, successor);
                    // Remove the successor from the right subtree
                    removeFromNode(rightChild, successor);
                } else {
                    // Merge left and right children
                    mergeNodes(innerNode, i);
                    // Now the key and right child are in the left child
                    // Remove the key from the merged node
                    removeFromNode(innerNode.children.get(i), key);
                }
            }
            return true;
        }

        // Key not found in this node, determine which child to search
        int childIndex = -i - 1;
        if (childIndex >= innerNode.children.size()) {
            childIndex = innerNode.children.size() - 1;
        }

        Node child = innerNode.children.get(childIndex);

        // Ensure the child has at least 'a' keys before descending
        if (child.keys.size() < a) {
            ensureChildHasMinKeys(innerNode, childIndex);
            // After ensuring min keys, childIndex might be invalid due to merging
            // Recalculate childIndex
            childIndex = -i - 1;
            if (childIndex >= innerNode.children.size()) {
                childIndex = innerNode.children.size() - 1;
            }
        }

        return removeFromNode(innerNode.children.get(childIndex), key);
    }

    private int findLargestKey(Node node) {
        if (node.isLeaf()) {
            return node.keys.get(node.keys.size() - 1);
        }
        ABTreeInnerNode innerNode = (ABTreeInnerNode) node;
        return findLargestKey(innerNode.children.get(innerNode.children.size() - 1));
    }

    private int findSmallestKey(Node node) {
        if (node.isLeaf()) {
            return node.keys.get(0);
        }
        ABTreeInnerNode innerNode = (ABTreeInnerNode) node;
        return findSmallestKey(innerNode.children.get(0));
    }

    private void mergeNodes(ABTreeInnerNode parent, int index) {
        Node leftChild = parent.children.get(index);
        Node rightChild = parent.children.get(index + 1);

        // Add the separator key from parent to the left child
        leftChild.keys.add(parent.keys.remove(index));

        // Add all keys from right child to left child
        leftChild.keys.addAll(rightChild.keys);

        // If these are inner nodes, move the children as well
        if (!leftChild.isLeaf()) {
            ABTreeInnerNode leftInner = (ABTreeInnerNode) leftChild;
            ABTreeInnerNode rightInner = (ABTreeInnerNode) rightChild;
            leftInner.children.addAll(rightInner.children);
        }

        // Remove the right child from the parent
        parent.children.remove(index + 1);
    }

    private void ensureChildHasMinKeys(ABTreeInnerNode parent, int childIndex) {


        // Try to borrow from left sibling
        if (childIndex > 0) {
            Node leftSibling = parent.children.get(childIndex - 1);
            if (leftSibling.keys.size() >= a) {
                borrowFromLeftSibling(parent, childIndex);
                return;
            }
        }

        // Try to borrow from right sibling
        if (childIndex < parent.children.size() - 1) {
            Node rightSibling = parent.children.get(childIndex + 1);
            if (rightSibling.keys.size() >= a) {
                borrowFromRightSibling(parent, childIndex);
                return;
            }
        }

        // Merge with a sibling
        if (childIndex > 0) {
            // Merge with left sibling
            mergeNodes(parent, childIndex - 1);
        } else {
            // Merge with right sibling
            mergeNodes(parent, childIndex);
        }
    }

    private void borrowFromLeftSibling(ABTreeInnerNode parent, int childIndex) {
        Node child = parent.children.get(childIndex);
        Node leftSibling = parent.children.get(childIndex - 1);

        // Move separator key from parent to child
        child.keys.add(0, parent.keys.get(childIndex - 1));

        // Move largest key from left sibling to parent
        parent.keys.set(childIndex - 1, leftSibling.keys.remove(leftSibling.keys.size() - 1));

        // If these are inner nodes, move the rightmost child of left sibling to child
        if (!child.isLeaf()) {
            ABTreeInnerNode childInner = (ABTreeInnerNode) child;
            ABTreeInnerNode leftInner = (ABTreeInnerNode) leftSibling;
            childInner.children.add(0, leftInner.children.remove(leftInner.children.size() - 1));
        }
    }

    private void borrowFromRightSibling(ABTreeInnerNode parent, int childIndex) {
        Node child = parent.children.get(childIndex);
        Node rightSibling = parent.children.get(childIndex + 1);

        // Move separator key from parent to child
        child.keys.add(parent.keys.get(childIndex));

        // Move smallest key from right sibling to parent
        parent.keys.set(childIndex, rightSibling.keys.remove(0));

        // If these are inner nodes, move the leftmost child of right sibling to child
        if (!child.isLeaf()) {
            ABTreeInnerNode childInner = (ABTreeInnerNode) child;
            ABTreeInnerNode rightInner = (ABTreeInnerNode) rightSibling;
            childInner.children.add(rightInner.children.remove(0));
        }
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
            ABTreeInnerNode innerNode = (ABTreeInnerNode) node;
            System.out.println(indent + "Internal: " + node.keys);
            for (Node child : innerNode.children) {
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
