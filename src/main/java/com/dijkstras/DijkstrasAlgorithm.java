package com.dijkstras;

import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Stack;

public class DijkstrasAlgorithm {

    static class Pair {
        int node, dist;

        public Pair(int node, int dist) {
            this.node = node;
            this.dist = dist;
        }
    }

    static class Result {
        int distance;
        ArrayList<Integer> path;

        public Result(int distance, ArrayList<Integer> path) {
            this.distance = distance;
            this.path = path;
        }
    }

    public static int dijkstra(ArrayList<GraphGUI.Edge>[] graph, int src, int dest) {
        Result result = dijkstraWithPath(graph, src, dest);
        return result.distance;
    }

    public static Result dijkstraWithPath(ArrayList<GraphGUI.Edge>[] graph, int src, int dest) {
        int[] dist = new int[graph.length];
        int[] parent = new int[graph.length];
        
        for (int i = 0; i < graph.length; i++) {
            dist[i] = Integer.MAX_VALUE;
            parent[i] = -1;
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
                    parent[edge.dest] = current.node;
                    pq.add(new Pair(edge.dest, dist[edge.dest]));
                }
            }
        }

        // Reconstruct path
        ArrayList<Integer> path = new ArrayList<>();
        if (dist[dest] != Integer.MAX_VALUE) {
            int current = dest;
            Stack<Integer> pathStack = new Stack<>();
            while (current != -1) {
                pathStack.push(current);
                current = parent[current];
            }
            while (!pathStack.isEmpty()) {
                path.add(pathStack.pop());
            }
        }

        return new Result(dist[dest] != Integer.MAX_VALUE ? dist[dest] : -1, path);
    }
}
