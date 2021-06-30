package org.port67;

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
        stableSort(result);
        List<Triplet> labelled = label(result);

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
            result.add(new Triplet(u.find(t.a), u.find(t.b), t.distance));
            u.union(t.a, t.b);
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
        counter--;

        Integer[] chain = new Integer[counter];
        int chainIx = 0;

        int a, b, c;
        int min;
        while (S.size() > 1) {
            if (chainIx <= 3) {
                a = S.first();
                chain[0] = a;
                chainIx = 1;

                b = S.tailSet(a, false).first();
                min = distances.get(new Pair(a, b));
                for (Integer i : S.tailSet(b, false)) {
                    int dist = distances.get(new Pair(a, i));
                    if (dist < min) {
                        min = dist;
                        b = i;
                    }
                }
            } else {
                chainIx -= 3;
                a = chain[chainIx - 1];
                b = chain[chainIx];
                min = distances.get(a < b ? new Pair(a, b) : new Pair(b, a));
            }

            do {
                chain[chainIx] = b;
    
                for (Integer i : S.headSet(b, false)) {
                    int dist = distances.get(new Pair(i, b));
                    if (dist < min) {
                        min = dist;
                        a = i;
                    }
                }
                for (Integer i : S.tailSet(b, false)) {
                    int dist = distances.get(new Pair(b, i));
                    if (dist < min) {
                        min = dist;
                        a = i;
                    }
                }
                b = a;
                a = chain[chainIx++];
            } while (b != chain[chainIx - 2]);
    
            Triplet t = new Triplet(a, b, min);
            result.add(t);
    
            S.remove((Integer) a);
            S.remove((Integer) b);
    
            counter++;
            size.put(counter, size.get(a) + size.get(b));
    
            // a must be < b here
            if (a > b) {
                int tmp = a;
                a = b;
                b = tmp;
            }
    
            for (Integer i : S.headSet(a, false)) {
                distances.put(new Pair(i, counter), method(distances.get(new Pair(i, a)), distances.get(new Pair(i, b))));
            }
            for (Integer i : S.subSet(a, false, b, false)) {
                distances.put(new Pair(i, counter), method(distances.get(new Pair(a, i)), distances.get(new Pair(i, b))));
            }
            for (Integer i : S.tailSet(b, false)) {
                distances.put(new Pair(i, counter), method(distances.get(new Pair(a, i)), distances.get(new Pair(b, i))));
            }
    
            S.add(counter);
        }

        return result;
    }

    public static int method(Integer a, Integer b) {
        return a < b ? a : b;
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
