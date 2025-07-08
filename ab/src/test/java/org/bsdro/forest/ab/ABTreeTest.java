package org.bsdro.forest.ab;

import junit.framework.TestCase;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * Test class for ABTree with a=2 and b=4
 */
public class ABTreeTest extends TestCase {

    private ABTree tree;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    /**
     * Set up the test fixture.
     * Called before every test case method.
     */
    @Override
    protected void setUp() {
        // Create a new ABTree with a=2 and b=4
        tree = new ABTree(2, 4);
        // Clear the output stream
        outContent.reset();
        // Redirect System.out to capture output
        System.setOut(new PrintStream(outContent));
    }

    /**
     * Tears down the test fixture.
     * Called after every test case method.
     */
    @Override
    protected void tearDown() {
        // Reset System.out
        System.setOut(originalOut);
    }

    /**
     * Test inserting a single element
     */
    public void testInsertSingleElement() {
        tree.insert(10);
        tree.printTree();
        String output = outContent.toString();
        assertTrue("Tree should contain the inserted element", 
                  output.contains("Leaf: [10]"));
    }

    /**
     * Test inserting multiple elements that don't cause splits
     */
    public void testInsertMultipleElementsNoSplit() {
        tree.insert(10);
        tree.insert(20);
        tree.printTree();
        String output = outContent.toString();
        assertTrue("Tree should contain all inserted elements in a single leaf", 
                  output.contains("Leaf: [10, 20]"));
    }

    /**
     * Test inserting elements that cause a split
     */
    public void testInsertWithSplit() {
        // Insert enough elements to cause a split (b-1 = 3 elements)
        tree.insert(10);
        tree.insert(20);
        tree.insert(30);
        tree.insert(40);
        tree.printTree();
        String output = outContent.toString();

        // After split, we should have a root with one key and two children
        assertTrue("Tree should have split", output.contains("Internal: ["));
        assertTrue("Tree should have leaf nodes after split", output.contains("Leaf: ["));
    }

    /**
     * Test inserting elements in random order
     */
    public void testInsertRandomOrder() {
        int[] keys = {25, 10, 40, 30, 15, 5, 35, 20};
        for (int key : keys) {
            tree.insert(key);
        }
        tree.printTree();
        String output = outContent.toString();

        // Verify the tree structure contains all elements
        for (int key : keys) {
            assertTrue("Tree should contain key " + key, 
                      output.contains(String.valueOf(key)));
        }
    }

    /**
     * Test inserting duplicate elements
     */
    public void testInsertDuplicates() {
        tree.insert(10);
        tree.insert(10);
        tree.printTree();
        String output = outContent.toString();

        // Count occurrences of "10" in the output
        int count = output.split("10").length - 1;
        assertTrue("Tree should contain duplicate elements", count >= 2);
    }

    /**
     * Test creating a tree with invalid parameters
     */
    public void testInvalidTreeParameters() {
        try {
            ABTree invalidTree = new ABTree(1, 4); // a < 2
            fail("Should throw IllegalArgumentException for a < 2");
        } catch (IllegalArgumentException e) {
            // Expected exception
        }

        try {
            ABTree invalidTree = new ABTree(3, 5); // a > b/2
            fail("Should throw IllegalArgumentException for a > b/2");
        } catch (IllegalArgumentException e) {
            // Expected exception
        }
    }

    /**
     * Test the main method of ABTree
     */
    public void testMainMethod() {
        ABTree.main(new String[0]);
        String output = outContent.toString();

        // Verify that the main method executed without errors
        assertTrue("Main method should execute without errors", 
                  output.contains("Final tree"));

        // Verify that all test keys were inserted
        int[] keys = {10, 20, 5, 6, 12, 30, 7, 17, 3, 25, 22};
        for (int key : keys) {
            assertTrue("Tree should contain key " + key, 
                      output.contains(String.valueOf(key)));
        }
    }
}
