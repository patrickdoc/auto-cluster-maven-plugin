package org.port67;

import java.io.BufferedReader;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.HashMap;
import java.util.Map;

public class DependencyMatrix {

    public Map<String, Integer> nodeIds = new HashMap<>();
    public int[][] dependencies;

    public DependencyMatrix() {
    }

    public static DependencyMatrix fromDotFile(String filePath) throws IOException {
        DependencyMatrix result = new DependencyMatrix();

        Path path = Paths.get(filePath);

        // Find all node ids
        BufferedReader reader = Files.newBufferedReader(path);
        String line = reader.readLine();
        while (line != null) {
            if (line.startsWith("\"")) {
                String nodeName = line.split("\"", 0)[1];
                result.nodeIds.put(nodeName, result.nodeIds.size());
            } else if (line.startsWith("  \"")) {
                String[] splitLine = line.split("\"", 0);
                String srcNodeName = splitLine[1];
                result.nodeIds.computeIfAbsent(srcNodeName, x -> result.nodeIds.size());
                
                String destNodeName = splitLine[3];
                result.nodeIds.computeIfAbsent(destNodeName, x -> result.nodeIds.size());
            }
            line = reader.readLine();
        }
        reader.close();

        // Initialize dependency matrix
        result.dependencies = new int[result.nodeIds.size()][result.nodeIds.size()];

        // Record all dependencies
        reader = Files.newBufferedReader(path);
        line = reader.readLine();
        while (line != null) {
            if (line.startsWith("  \"")) {
                String[] splitLine = line.split("\"", 0);
                String srcNodeName = splitLine[1];
                int srcNodeId = result.nodeIds.get(srcNodeName);

                String destNodeName = splitLine[3];
                int destNodeId = result.nodeIds.get(destNodeName);

                result.dependencies[srcNodeId][destNodeId] = 1;
            }
            line = reader.readLine();
        }

        return result;
    }
}
