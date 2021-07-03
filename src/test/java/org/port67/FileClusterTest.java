package io.github.patrickdoc;

import org.junit.Rule;
import static org.junit.Assert.*;
import org.junit.Test;
import java.io.File;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileClusterTest {

    @Test
    public void testInput() throws Exception {
        // actual
        Map<Integer, String> nodeLabels = new HashMap();
        nodeLabels.put(0, "a");
        nodeLabels.put(1, "b");
        nodeLabels.put(2, "c");

        List<Hclust.Triplet> dendrogram = new ArrayList<>();
        dendrogram.add(new Hclust.Triplet(2, 0, 0));
        dendrogram.add(new Hclust.Triplet(3, 1, 2));

        Hclust input = new Hclust(dendrogram, nodeLabels);

        FileCluster cluster = FileCluster.fromHclust(input);

        // expected
        FileCluster.BinaryTree expected = new FileCluster.BinaryTree(2);

        FileCluster.BinaryTree leftCluster = new FileCluster.BinaryTree(0);
        leftCluster.setLeft(new FileCluster.BinaryTree(2, "c"));
        leftCluster.setRight(new FileCluster.BinaryTree(0, "a"));
        expected.setLeft(leftCluster);

        FileCluster.BinaryTree rightCluster = new FileCluster.BinaryTree(1, "b");
        expected.setRight(rightCluster);

        System.out.println("expected:");
        logBinaryTree(expected, 0, true);
        System.out.println("actual:");
        logBinaryTree(cluster.tree, 0, true);
        assertEquals(expected, cluster.tree);
    }

    private static void logBinaryTree(FileCluster.BinaryTree tree, int indent, boolean applyIndent) {
        if (applyIndent) {
            for (int i = 0; i < indent; i++) {
                System.out.print(" ");
            }
        }

        String output = "{" + tree.value + ", " + tree.name + "} ";

        if (tree.name != null) {
            System.out.println(output);
        } else {
            System.out.print(output);
            logBinaryTree(tree.left, indent + output.length(), false);
            logBinaryTree(tree.right, indent + output.length(), true);
        }
    }
}
