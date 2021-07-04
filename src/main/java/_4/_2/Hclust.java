package io.github.patrickdoc;

import java.io.BufferedReader;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

// https://arxiv.org/pdf/1109.2378.pdf
// https://uc-r.github.io/hc_clustering
public class Hclust {

    public final List<Triplet> dendrogram;
    public final Map<Integer, String> nodeLabels;

    public Hclust(List<Triplet> dendrogram, Map<Integer, String> nodeLabels) {
        this.dendrogram = dendrogram;
        this.nodeLabels = nodeLabels;
    }

    public static Hclust fromDistanceMatrix(DistanceMatrix distanceMatrix) {
        int n = distanceMatrix.nodeIds.size();

        Map<Pair, Integer> distances = new HashMap<>();
        for (int i = 0; i < n; i++) {
            for (int j = i; j < n; j++) {
                distances.put(new Pair(i,j), distanceMatrix.distances[i][j]);
            }
        }

        List<Triplet> result = nnChainCore(n, distances);
        //stableSort(result);
        // Currently not using this optimization
        //List<Triplet> labelled = label(result);
        List<Triplet> labelled = result;

        Map<Integer, String> labels = new HashMap<>();
        for (Map.Entry<String, Integer> e : distanceMatrix.nodeIds.entrySet()) {
            labels.put(e.getValue(), e.getKey());
        }

        return new Hclust(labelled, labels);
    }

    public static void stableSort(List<Triplet> input) {
        input.sort((e1, e2) -> Integer.compare(e1.distance, e2.distance));
    }

    public static List<Triplet> label(List<Triplet> input) {
        List<Triplet> result = new ArrayList<>();
        int n = input.size() + 1;
        UnionFind u = new UnionFind(n);
        for (Triplet t : input) {
            int normalizedA = t.a + n;
            int normalizedB = t.b + n;
            result.add(new Triplet(u.find(normalizedA), u.find(normalizedB), t.distance));
            u.union(normalizedA, normalizedB);
        }
        return result;
    }

    public static List<Triplet> nnChainCore(int n, Map<Pair, Integer> distances) {
        List<Triplet> result = new ArrayList<>();

        TreeSet<Integer> S = new TreeSet<>();
        Map<Integer, Integer> size = new HashMap();
        int counter = 0;
        for (counter = 0; counter < n; counter++) {
            S.add(counter);
            size.put(counter, 1);
        }

        Integer[] chain = new Integer[n];
        int chainIx = 0;

        int idx1, idx2, c;
        int min;
        while (S.size() > 1) {
            if (chainIx <= 3) {
                idx1 = S.first();
                chain[0] = idx1;
                chainIx = 1;

                idx2 = S.tailSet(idx1, false).first();
                min = distances.get(new Pair(idx1, idx2));
                int tailVal = idx2;
                for (Integer i : S.tailSet(tailVal, false)) {
                    int dist = distances.get(new Pair(idx1, i));
                    if (dist < min) {
                        min = dist;
                        idx2 = i;
                    }
                }
            } else {
                chainIx -= 3;
                idx1 = chain[chainIx - 1];
                idx2 = chain[chainIx];
                min = distances.get(idx1 < idx2 ? new Pair(idx1, idx2) : new Pair(idx2, idx1));
            }

            do {
                chain[chainIx] = idx2;
    
                for (Integer i : S.headSet(idx2, false)) {
                    int dist = distances.get(new Pair(i, idx2));
                    if (dist < min) {
                        min = dist;
                        idx1 = i;
                    }
                }
                for (Integer i : S.tailSet(idx2, false)) {
                    int dist = distances.get(new Pair(idx2, i));
                    if (dist < min) {
                        min = dist;
                        idx1 = i;
                    }
                }

                idx2 = idx1;
                idx1 = chain[chainIx++];
            } while (idx2 != chain[chainIx - 2]);

            Triplet t = new Triplet(idx1 - n, idx2 - n, min);
            result.add(t);

            S.remove((Integer) idx1);
            S.remove((Integer) idx2);

            size.put(counter, size.get(idx1) + size.get(idx2));
    
            // idx1 must be < idx2 here
            if (idx1 > idx2) {
                int tmp = idx1;
                idx1 = idx2;
                idx2 = tmp;
            }
    
            Integer newVal;
            for (Integer i : S.headSet(idx1, false)) {
                newVal = method(distances.get(new Pair(i, idx1)), distances.get(new Pair(i, idx2)));
                distances.put(new Pair(i, counter), newVal);
            }
            for (Integer i : S.subSet(idx1, false, idx2, false)) {
                newVal = method(distances.get(new Pair(idx1, i)), distances.get(new Pair(i, idx2)));
                distances.put(new Pair(i, counter), newVal);
            }
            for (Integer i : S.tailSet(idx2, false)) {
                newVal = method(distances.get(new Pair(idx1, i)), distances.get(new Pair(idx2, i)));
                distances.put(new Pair(i, counter), newVal);
            }

            S.add(counter);
            counter++;
        }

        return result;
    }

    public static int method(Integer a, Integer b) {
        return a > b ? a : b;
    }

    public static class Pair {
        int a;
        int b;

        public Pair(int a, int b) {
            this.a = a;
            this.b = b;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Pair p = (Pair) o;

            return a == p.a && b == p.b;
        }

        public int hashCode() {
            return 31 * a + b;
        }
    }

    public static class Triplet {
        int a;
        int b;
        int distance;

        public Triplet(int a, int b, int distance) {
            this.a = a;
            this.b = b;
            this.distance = distance;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Triplet t = (Triplet) o;

            return a == t.a && b == t.b && distance == t.distance;
        }

        public int hashCode() {
            return 31 * a + b;
        }
    }

    public static class UnionFind {
        Integer[] parent;
        int nextLabel;

        public UnionFind(int n) {
            parent = new Integer[2*n - 1];
            for (int i = 0; i < 2*n - 2; i++) {
                parent[i] = null;
            }
            nextLabel = n + 1;
        }

        public void union(int m, int n) {
            parent[m] = nextLabel;
            parent[n] = nextLabel;
            nextLabel++;
        }

        public int find(int n) {
            int p = n;
            while (parent[n] != null) {
                n = parent[n];
            }
            while (parent[p] != null && n != parent[p]) {
                int next = parent[p];
                parent[p] = n;
                p = next;
            }
            return n;
        }
    }

}
