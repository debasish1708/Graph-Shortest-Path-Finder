package com.dijkstras;

import java.util.*;

public class DijkstrasAlgorithm {
    public static class Result {
        public int distance;
        public ArrayList<Integer> path;
        public Result(int distance, ArrayList<Integer> path) {
            this.distance = distance;
            this.path = path;
        }
    }

    public static Result dijkstraWithPath(ArrayList<Edge>[] graph, int src, int dest) {
        int n = graph.length;
        int[] dist = new int[n];
        int[] prev = new int[n];
        boolean[] visited = new boolean[n];
        Arrays.fill(dist, Integer.MAX_VALUE);
        Arrays.fill(prev, -1);
        dist[src] = 0;
        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[1]));
        pq.add(new int[]{src, 0});
        while (!pq.isEmpty()) {
            int[] curr = pq.poll();
            int u = curr[0];
            if (visited[u]) continue;
            visited[u] = true;
            for (Edge e : graph[u]) {
                int v = e.dest;
                int w = e.wt;
                if (dist[u] + w < dist[v]) {
                    dist[v] = dist[u] + w;
                    prev[v] = u;
                    pq.add(new int[]{v, dist[v]});
                }
            }
        }
        ArrayList<Integer> path = new ArrayList<>();
        if (dist[dest] == Integer.MAX_VALUE) return new Result(-1, path);
        for (int at = dest; at != -1; at = prev[at]) path.add(0, at);
        return new Result(dist[dest], path);
    }
}
