package com.dijkstras;

import java.awt.Point;
import java.util.ArrayList;

public class Graph {
    public ArrayList<Node> nodes = new ArrayList<>();
    public ArrayList<Edge> edges = new ArrayList<>();
    public ArrayList<Edge>[] adjList;
    public int nodeCount = 0;

    @SuppressWarnings("unchecked")
    public Graph(int maxNodes) {
        adjList = new ArrayList[maxNodes];
        for (int i = 0; i < maxNodes; i++) {
            adjList[i] = new ArrayList<>();
        }
    }

    public int addNode(Point p) {
        Node node = new Node(p, nodeCount);
        nodes.add(node);
        nodeCount++;
        return node.id;
    }

    public void removeNode(int id) {
        // Remove the node
        nodes.removeIf(n -> n.id == id);
        
        // Remove all edges connected to this node
        edges.removeIf(e -> e.src == id || e.dest == id);
        adjList[id].clear();
        for (ArrayList<Edge> list : adjList) {
            list.removeIf(e -> e.src == id || e.dest == id);
        }
        
        // Decrement node count
        nodeCount--;
    }

    public void addEdge(int src, int dest, int wt) {
        Edge edge = new Edge(src, dest, wt);
        edges.add(edge);
        adjList[src].add(edge);
    }

    public void removeEdge(int src, int dest) {
        edges.removeIf(e -> e.src == src && e.dest == dest);
        adjList[src].removeIf(e -> e.dest == dest);
    }

    public void clear() {
        nodes.clear();
        edges.clear();
        nodeCount = 0;
        for (ArrayList<Edge> list : adjList) {
            list.clear();
        }
    }
} 