package io.github.patrickdoc;

import org.junit.Rule;
import static org.junit.Assert.*;
import org.junit.Test;
import java.io.File;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class DistanceMatrixTest {

    @Test
    public void testInput() throws Exception {
        DependencyMatrix matrix = new DependencyMatrix();

        Map<String, Integer> nodeIds = new HashMap();
        nodeIds.put("a", 0);
        nodeIds.put("b", 1);
        nodeIds.put("c", 2);
        matrix.nodeIds = nodeIds;

        int[][] dependencies = new int[][]
            {{1, 0, 0}
            ,{1, 1, 1}
            ,{0, 0, 1}};
        matrix.dependencies = dependencies;

        DistanceMatrix dist = DistanceMatrix.fromDependencyMatrix(matrix);
        int[][] expected = new int[][]
            {{0, 2, 2}
            ,{2, 0, 2}
            ,{2, 2, 0}};
        logMatrix(dist);
        assertTrue(Arrays.deepEquals(expected, dist.distances));
    }

    private void logMatrix(DistanceMatrix matrix) {
        for (int y = 0; y < matrix.nodeIds.size(); y++) {
            System.out.print("{");
            for (int x = 0; x < matrix.nodeIds.size(); x++) {
                System.out.print(" ");
                System.out.print(matrix.distances[y][x]);
            }
            System.out.println(" }");
        }
    }
}
