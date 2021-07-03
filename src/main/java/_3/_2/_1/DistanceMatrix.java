package io.github.patrickdoc;

import java.io.BufferedReader;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.HashMap;
import java.util.Map;

public class DistanceMatrix {

    protected Map<String, Integer> nodeIds;
    protected int[][] distances;

    public DistanceMatrix() {
    }

    public static DistanceMatrix fromDependencyMatrix(DependencyMatrix dependencyMatrix) {
        DistanceMatrix result = new DistanceMatrix();
        result.nodeIds = dependencyMatrix.nodeIds;

        result.distances = new int[result.nodeIds.size()][result.nodeIds.size()];

        for (int node1 = 0; node1 < result.nodeIds.size(); node1++) {
            for (int node2 = node1 + 1; node2 < result.nodeIds.size(); node2++) {
                int distance = 0;
                for (int x = 0; x < result.nodeIds.size(); x++) {
                    if (dependencyMatrix.dependencies[node1][x] != dependencyMatrix.dependencies[node2][x]) {
                        distance++;
                    }
                }
                result.distances[node1][node2] = distance;
                result.distances[node2][node1] = distance;
            }
        }
        return result;
    }
}
