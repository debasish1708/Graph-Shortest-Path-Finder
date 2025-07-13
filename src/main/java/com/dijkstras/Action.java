package com.dijkstras;

import java.util.ArrayList;

public abstract class Action {
    public abstract void undo();
    public abstract void redo();

    public static class AddNodeAction extends Action {
        private final Graph graph;
        private final Node node;
        public AddNodeAction(Graph graph, Node node) {
            this.graph = graph;
            this.node = node;
        }
        public void undo() {
            graph.removeNode(node.id);
        }
        public void redo() {
            graph.nodes.add(node);
            graph.nodeCount++;
        }
    }

    public static class AddEdgeAction extends Action {
        private final Graph graph;
        private final Edge edge;
        public AddEdgeAction(Graph graph, Edge edge) {
            this.graph = graph;
            this.edge = edge;
        }
        public void undo() {
            graph.removeEdge(edge.src, edge.dest);
        }
        public void redo() {
            graph.addEdge(edge.src, edge.dest, edge.wt);
        }
    }

    public static class ClearAllAction extends Action {
        private final Graph graph;
        private final ArrayList<Node> oldNodes;
        private final ArrayList<Edge> oldEdges;
        private final ArrayList<Edge>[] oldAdjList;
        private final int oldNodeCount;
        @SuppressWarnings("unchecked")
        public ClearAllAction(Graph graph) {
            this.graph = graph;
            this.oldNodes = new ArrayList<>(graph.nodes);
            this.oldEdges = new ArrayList<>(graph.edges);
            this.oldAdjList = new ArrayList[graph.adjList.length];
            for (int i = 0; i < graph.adjList.length; i++) {
                this.oldAdjList[i] = new ArrayList<>(graph.adjList[i]);
            }
            this.oldNodeCount = graph.nodeCount;
        }
        public void undo() {
            graph.nodes.clear();
            graph.nodes.addAll(oldNodes);
            graph.edges.clear();
            graph.edges.addAll(oldEdges);
            for (int i = 0; i < graph.adjList.length; i++) {
                graph.adjList[i].clear();
                graph.adjList[i].addAll(oldAdjList[i]);
            }
            graph.nodeCount = oldNodeCount;
        }
        public void redo() {
            graph.nodes.clear();
            graph.edges.clear();
            graph.nodeCount = 0;
            for (int i = 0; i < graph.adjList.length; i++) graph.adjList[i].clear();
        }
    }
} 