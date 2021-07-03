package io.github.patrickdoc;

import org.junit.Rule;
import static org.junit.Assert.*;
import org.junit.Test;
import java.io.File;

import java.util.Arrays;

public class DependencyMatrixTest {

    @Test
    public void testFile() throws Exception {
        DependencyMatrix graph = DependencyMatrix.fromDotFile("src/test/resources/cluster.dot");
        assertTrue(3 == graph.nodeIds.size());
        logDependencyMatrix(graph);
        int[][] expected = new int[][]
            {{1, 0, 0}
            ,{1, 1, 1}
            ,{0, 0, 1}};
        assertTrue(Arrays.deepEquals(expected, graph.dependencies));

    }

    private void logDependencyMatrix(DependencyMatrix graph) {
        for (int y = 0; y < graph.nodeIds.size(); y++) {
            System.out.print("{");
            for (int x = 0; x < graph.nodeIds.size(); x++) {
                System.out.print(" ");
                System.out.print(graph.dependencies[y][x]);
            }
            System.out.println(" }");
        }
    }
}
