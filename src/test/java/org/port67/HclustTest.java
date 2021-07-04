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
            {{0, 1, 1}
            ,{1, 0, 1}
            ,{0, 1, 0}};
        matrix.dependencies = dependencies;

        DistanceMatrix dist = DistanceMatrix.fromDependencyMatrix(matrix);

        Hclust result = Hclust.fromDistanceMatrix(dist);
        logTriplets(result.dendrogram);

        List<Hclust.Triplet> expected = new ArrayList<>();
        expected.add(new Hclust.Triplet(-1, -3, 1));
        expected.add(new Hclust.Triplet(0, -2, 3));

        assertEquals(expected, result.dendrogram);
    }

    @Test
    public void testMultiMerge() throws Exception {
        DistanceMatrix dist = new DistanceMatrix();

        Map<String, Integer> nodeIds = new HashMap();
        nodeIds.put("a", 0);
        nodeIds.put("b", 1);
        nodeIds.put("c", 2);
        nodeIds.put("d", 3);
        nodeIds.put("e", 4);
        dist.nodeIds = nodeIds;

        int[][] distances = new int[][]
            {{0, 1, 1, 1, 1}
            ,{1, 0, 0, 0, 1}
            ,{1, 0, 0, 0, 1}
            ,{1, 0, 0, 0, 1}
            ,{1, 1, 1, 1, 0}};
        dist.distances = distances;

        Hclust result = Hclust.fromDistanceMatrix(dist);
        logTriplets(result.dendrogram);

        List<Hclust.Triplet> expected = new ArrayList<>();
        expected.add(new Hclust.Triplet(-3, -4 , 0));
        expected.add(new Hclust.Triplet(0, -2, 0));
        expected.add(new Hclust.Triplet(-1, -5, 1));
        expected.add(new Hclust.Triplet(2, 1, 1));

        assertEquals(expected, result.dendrogram);
    }

    @Test
    public void testMultiMerge2() throws Exception {
        DistanceMatrix dist = new DistanceMatrix();

        Map<String, Integer> nodeIds = new HashMap();
        nodeIds.put("a", 0);
        nodeIds.put("b", 1);
        nodeIds.put("c", 2);
        nodeIds.put("d", 3);
        nodeIds.put("e", 4);
        dist.nodeIds = nodeIds;

        int[][] distances = new int[][]
            {{0, 0, 0, 0, 1}
            ,{0, 0, 0, 0, 0}
            ,{0, 0, 0, 0, 0}
            ,{0, 0, 0, 0, 0}
            ,{1, 0, 0, 0, 0}};
        dist.distances = distances;

        Hclust result = Hclust.fromDistanceMatrix(dist);
        logTriplets(result.dendrogram);

        List<Hclust.Triplet> expected = new ArrayList<>();
        expected.add(new Hclust.Triplet(-4, -5, 0));
        expected.add(new Hclust.Triplet(-2, -3, 0));
        expected.add(new Hclust.Triplet(1, -1, 0));
        expected.add(new Hclust.Triplet(2, 0, 1));

        assertEquals(expected, result.dendrogram);
    }

    private void logTriplets(List<Hclust.Triplet> triplets) {
        for (Hclust.Triplet t : triplets) {
            System.out.println("(" + t.a + ", " + t.b + ", " + t.distance + ")");
        }
    }
}
