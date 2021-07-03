package org.port67;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileCluster {

    public final BinaryTree tree;

    private FileCluster(BinaryTree tree, Map<Integer, String> nodeLabels) {
        this.tree = tree;
    }

    public static FileCluster fromHclust(Hclust cluster) {

        Hclust.Triplet[] dendrogram = cluster.dendrogram.toArray(new Hclust.Triplet[0]);

        return new FileCluster(treeBuilder(dendrogram, cluster.nodeLabels, cluster.dendrogram.size() + cluster.nodeLabels.size() - 1),
                               cluster.nodeLabels);

    }

    public void writeFiles(boolean dryRun) throws IOException {
        // Delete any existing temp folders
        removeFolder(Paths.get("src/main/"), "auto-cluster-maven-plugin");

        String sourceDir = "src/main/java";

        Path destPath = Files.createTempDirectory(Paths.get("src/main"), "auto-cluster-maven-plugin");
        String destDir = "src/main/" + destPath.getFileName();

        writeTree(tree, sourceDir, destDir, -1);

        if (!dryRun) {
            // Delete all java files and empty directories from source
            Files.walk(Paths.get(sourceDir))
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach((f) -> {
                    if (f.getName().endsWith(".java")) {
                        f.delete();
                    }});

            // Move new structure over
            moveFolder(destPath, Paths.get(sourceDir), StandardCopyOption.REPLACE_EXISTING);

            // Delete temp folder
            removeFolder(destPath, "");
        }
    }

    public void moveFolder(Path source, Path target, CopyOption... options) throws IOException {
        Files.walkFileTree(source, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                    throws IOException {
                Files.createDirectories(target.resolve(source.relativize(dir)));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                    throws IOException {
                Files.move(file, target.resolve(source.relativize(file)), options);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public void removeFolder(Path source, String prefix) throws IOException {
        Files.walkFileTree(source, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                    throws IOException {
                if (dir.getFileName().toString().startsWith(prefix)) {
                    Files.walk(dir)
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }


    private void writeTree(BinaryTree tree, String sourceDirectory, String destDirectory, int lastValue) throws IOException {
        if (tree.name != null) {
            // File
            String path = tree.name.replaceAll("\\.", "/");
            path += ".java";
            String classFileName = path.substring(path.lastIndexOf("/") + 1);

            List<Path> sources = Files.find(Paths.get(sourceDirectory),
                                            10000,
                                            ((filePath, fileAttrs) -> classFileName.equals(filePath.getFileName().toString())))
                .collect(Collectors.toList());

            if (sources.size() == 0) {
                System.out.println("Warn: Could not find source for: " + classFileName);
                return;
            } else if (sources.size() > 1) {
                System.out.println("Warn: Duplicate source found for: " + classFileName);
                return;
            }

            Path source = sources.get(0);

            Path dest = Paths.get(destDirectory + "/" + path.substring(path.lastIndexOf("/")+1));
            Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
        } else {
            // Directory
            String newDirectory;
            if (tree.value == 0 || tree.value == lastValue) {
                newDirectory = destDirectory;
            } else {
                newDirectory = destDirectory + "/" + Integer.toString(tree.value);
                Files.createDirectories(Paths.get(newDirectory));
            }
            writeTree(tree.left, sourceDirectory, newDirectory, tree.value);
            writeTree(tree.right, sourceDirectory, newDirectory, tree.value);
        }
    }

    private static BinaryTree treeBuilder(Hclust.Triplet[] dendrogram, Map<Integer, String> labels, int dendIx) {
        if (dendIx < labels.size()) {
            // File
            return new BinaryTree(dendIx, labels.get(dendIx)); 
        } else {
            // Directory
            int index = dendIx - labels.size();
            BinaryTree result = new BinaryTree(dendrogram[index].distance);
            BinaryTree left = treeBuilder(dendrogram, labels, dendrogram[index].a);
            result.setLeft(left);
            BinaryTree right = treeBuilder(dendrogram, labels, dendrogram[index].b);
            result.setRight(right);
            return result;
        }
    }

    public static class BinaryTree {
        int value;
        String name;
        BinaryTree left;
        BinaryTree right;

        public BinaryTree(int value, String name) {
            this.value = value;
            this.name = name;
            this.left = null;
            this.right = null;
        }

        BinaryTree(int value) {
            this(value, null);
        }

        public void setLeft(BinaryTree left) {
            this.left = left;
        }

        public void setRight(BinaryTree right) {
            this.right = right;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            BinaryTree t = (BinaryTree) o;

            if (name != null) {
                return value == t.value && name.equals(t.name);
            } else {
                return value == t.value && left.equals(t.left) && right.equals(t.right);
            }
        }

    }
}
