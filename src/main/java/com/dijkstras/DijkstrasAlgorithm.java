package com.dijkstras;

import java.util.ArrayList;
import java.util.PriorityQueue;

public class DijkstrasAlgorithm {

    static class Pair {
        int node, dist;

        public Pair(int node, int dist) {
            this.node = node;
            this.dist = dist;
        }
    }

    public static int dijkstra(ArrayList<GraphGUI.Edge>[] graph, int src, int dest) {
        int[] dist = new int[graph.length];
        for (int i = 0; i < graph.length; i++) {
            dist[i] = Integer.MAX_VALUE;
        }
        dist[src] = 0;

        PriorityQueue<Pair> pq = new PriorityQueue<>((a, b) -> a.dist - b.dist);
        pq.add(new Pair(src, 0));

        boolean[] visited = new boolean[graph.length];

        while (!pq.isEmpty()) {
            Pair current = pq.poll();
            if (visited[current.node]) continue;
            visited[current.node] = true;

            for (GraphGUI.Edge edge : graph[current.node]) {
                if (dist[current.node] + edge.wt < dist[edge.dest]) {
                    dist[edge.dest] = dist[current.node] + edge.wt;
                    pq.add(new Pair(edge.dest, dist[edge.dest]));
                }
            }
        }

        return dist[dest] != Integer.MAX_VALUE ? dist[dest] : -1; // Return -1 if unreachable
    }
}
