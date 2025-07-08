package org.bsdro.forest.avl;

public class AVLTree {
    private Node root;

    void updateHeight(Node node) {
        node.height = 1 + Math.max(height(node.left), height(node.right));
    }

    int height(Node node) {
        return node == null ? -1 : node.height;
    }

    int getBalanceFactor(Node node) {
        return node == null ? 0 : height(node.right) - height(node.left);
    }

    Node rotateRight(Node node) {
        Node left = node.left;
        Node leftRight = left.right;
        left.right = node;
        node.left = leftRight;
        updateHeight(node);
        updateHeight(left);
        return left;
    }

    Node rotateLeft(Node node) {
        Node right = node.right;
        Node rightLeft = right.left;
        right.left = node;
        node.right = rightLeft;
        updateHeight(node);
        updateHeight(right);
        return right;
    }

    Node rebalance(Node node) {
        updateHeight(node);
        int balanceFactor = getBalanceFactor(node);
        if (balanceFactor > 1) {
           if(height(node.right.right) > height(node.right.left)) {
               node = rotateLeft(node);
           } else {
               node.right = rotateRight(node.right);
               node = rotateLeft(node);
           }
        } else if (balanceFactor < -1) {
            if (height(node.left.left) > height(node.left.right)) {
                node = rotateRight(node);
            } else {
                node.left = rotateLeft(node.left);
                node = rotateRight(node);
            }
        }
        return node;
    }

    Node insert(Node root, int key) {
        if (root == null) {
            return new Node(key);
        } else if (root.key > key) {
            root.left = insert(root.left, key);
        } else if (root.key < key) {
            root.right = insert(root.right, key);
        } else {
            throw new IllegalArgumentException("Duplicate key");
        }
        return rebalance(root);
    }

    Node delete(Node node, int key) {
        if (node == null) {
            return null;
        } else if (node.key > key) {
            node.left = delete(node.left, key);
        } else if (node.key < key) {
            node.right = delete(node.right, key);
        } else {
            if (node.left == null || node.right == null) {
                node = node.left == null ? node.right : node.left;
            } else {
                Node temp = minValueNode(node.right);
                node.key = temp.key;
                node.right = delete(node.right, node.key);
            }
        }
        if (node != null) {
            node = rebalance(node);
        }
        return node;
    }

    Node minValueNode(Node node) {
        Node current = node;
        while (current.left != null) {
            current = current.left;
        }
        return current;
    }

    Node find(int key) {
        Node current = root;
        while (current != null) {
            if (current.key == key) {
                break;
            }
            current = key < current.key ? current.right : current.left;
        }
        return current;
    }
}
