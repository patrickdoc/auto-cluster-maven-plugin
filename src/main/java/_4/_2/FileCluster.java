package io.github.patrickdoc;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

import java.nio.file.CopyOption;
import java.nio.file.DirectoryNotEmptyException;
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

        return new FileCluster(treeBuilder(dendrogram, cluster.nodeLabels, cluster.dendrogram.size() - 1),
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
            removeJavaFiles(Paths.get(sourceDir));

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

    public void removeJavaFiles(Path source) throws IOException {
        Files.walkFileTree(source, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc)
                    throws IOException {
                if (exc != null) {
                    throw exc;
                }

                try {
                    Files.delete(dir);
                } catch (DirectoryNotEmptyException e) {
                    // Don't do anything if the directory contains non-Java files
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                    throws IOException {
                if (file.getFileName().toString().endsWith(".java")) {
                    Files.delete(file);
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }


    private void writeTree(BinaryTree sourceTree, String sourceDirectory, String destDirectory, int lastValue) throws IOException {
        if (sourceTree.name != null) {
            // File
            String path = sourceTree.name.replaceAll("\\.", "/");
            path += ".java";
            String classFileName = path.substring(path.lastIndexOf("/") + 1);

            List<Path> sources = Files.find(Paths.get(sourceDirectory),
                                            10000,
                                            ((filePath, fileAttrs) -> classFileName.equals(filePath.getFileName().toString())))
                .collect(Collectors.toList());

            if (sources.size() == 0) {
                // Could not find source file for, e.g. a generated source file
                return;
            } else if (sources.size() > 1) {
                // More than one source file found with same package/name
                return;
            }

            Path source = sources.get(0);

            Path dest = Paths.get(destDirectory + "/" + path.substring(path.lastIndexOf("/")+1));
            Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
        } else {
            // Directory
            String newDirectory;
            if (sourceTree.value == lastValue) {
                newDirectory = destDirectory;
            } else {
                newDirectory = destDirectory + "/_" + Integer.toString(sourceTree.value);
                Files.createDirectories(Paths.get(newDirectory));
            }
            writeTree(sourceTree.left, sourceDirectory, newDirectory, sourceTree.value);
            writeTree(sourceTree.right, sourceDirectory, newDirectory, sourceTree.value);
        }
    }

    private static BinaryTree treeBuilder(Hclust.Triplet[] dendrogram, Map<Integer, String> labels, int dendIx) {
        if (dendIx < 0) {
            // File
            return new BinaryTree(dendIx, labels.get(dendIx + labels.size()));
        } else {
            // Directory
            BinaryTree result = new BinaryTree(dendrogram[dendIx].distance);
            BinaryTree left = treeBuilder(dendrogram, labels, dendrogram[dendIx].a);
            result.setLeft(left);
            BinaryTree right = treeBuilder(dendrogram, labels, dendrogram[dendIx].b);
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
