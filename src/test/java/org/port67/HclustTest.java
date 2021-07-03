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

public class HclustTest {

    @Test
    public void testInput() throws Exception {
        DependencyMatrix matrix = new DependencyMatrix();

        Map<String, Integer> nodeIds = new HashMap();
        nodeIds.put("a", 0);
        nodeIds.put("b", 1);
        nodeIds.put("c", 2);
        matrix.nodeIds = nodeIds;

        int[][] dependencies = new int[][]
            {{0, 0, 0}
            ,{1, 0, 1}
            ,{0, 0, 0}};
        matrix.dependencies = dependencies;

        DistanceMatrix dist = DistanceMatrix.fromDependencyMatrix(matrix);

        Hclust result = Hclust.fromDistanceMatrix(dist);
        logTriplets(result.dendrogram);

        List<Hclust.Triplet> expected = new ArrayList<>();
        expected.add(new Hclust.Triplet(2, 0, 0));
        expected.add(new Hclust.Triplet(3, 1, 2));

        assertEquals(expected, result.dendrogram);
    }

    private void logTriplets(List<Hclust.Triplet> triplets) {
        for (Hclust.Triplet t : triplets) {
            System.out.println("(" + t.a + ", " + t.b + ", " + t.distance + ")");
        }
    }
}
