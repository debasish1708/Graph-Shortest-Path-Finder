package com.dijkstras;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class GraphGUI extends JFrame {
    private final ArrayList<Point> nodes; // Store node positions
    private final ArrayList<Edge> edges;  // Store edges
    private final ArrayList<Edge>[] graph; // Graph structure
    private int nodeCount = 0; // Total nodes

    public GraphGUI() {
        nodes = new ArrayList<>();
        edges = new ArrayList<>();
        graph = new ArrayList[100]; // Graph array

        // Initialize graph list
        for (int i = 0; i < 100; i++) {
            graph[i] = new ArrayList<>();
        }

        // Setup canvas
        JPanel canvas = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);


                // Draw nodes
                g.setColor(Color.BLUE);
                for (Point p : nodes) {
                    g.fillOval(p.x - 15, p.y - 15, 30, 30);
                    g.drawString("Node " + nodes.indexOf(p), p.x - 10, p.y - 20);
                }
                // Draw edges
                g.setColor(Color.BLACK);
                for (Edge e : edges) {
                    Point src = nodes.get(e.src);
                    Point dest = nodes.get(e.dest);
                    drawArrow(g2d, src.x, src.y, dest.x, dest.y);
                    g.drawString(Integer.toString(e.wt), (src.x + dest.x) / 2, (src.y + dest.y) / 2);
                }
            }
        };

        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Left-click to create node
                if (SwingUtilities.isLeftMouseButton(e)) {
                    nodes.add(e.getPoint());
                    nodeCount++;
                    repaint();
                }
                // Right-click to connect nodes
                else if (SwingUtilities.isRightMouseButton(e)) {
                    if (nodeCount > 1) {
                        String sourceNode = JOptionPane.showInputDialog("Enter source node ID:");
                        String destNode = JOptionPane.showInputDialog("Enter destination node ID:");
                        String weight = JOptionPane.showInputDialog("Enter distance between them:");

                        try {
                            int src = Integer.parseInt(sourceNode);
                            int dest = Integer.parseInt(destNode);
                            int wt = Integer.parseInt(weight);

                            edges.add(new Edge(src, dest, wt));
                            graph[src].add(new Edge(src, dest, wt)); // Add edge to graph

                            repaint(); // Redraw graph with the new edge
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(null, "Invalid input.");
                        }
                    }
                }
            }
        });

        JButton findShortestDistBtn = new JButton("Find Shortest Distance");
        findShortestDistBtn.addActionListener(e -> {
            String src = JOptionPane.showInputDialog("Enter source node ID:");
            String dest = JOptionPane.showInputDialog("Enter destination node ID:");

            try {
                int source = Integer.parseInt(src);
                int destination = Integer.parseInt(dest);

                // Call Dijkstra's algorithm
                int shortestDist = DijkstrasAlgorithm.dijkstra(graph, source, destination);

                JOptionPane.showMessageDialog(null, "Shortest distance: " + shortestDist);
                System.out.println("Shortest distance: " + shortestDist);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Invalid input.");
            }
        });

        this.setLayout(new BorderLayout());
        this.add(canvas, BorderLayout.CENTER);
        this.add(findShortestDistBtn, BorderLayout.SOUTH);

        this.setTitle("Graph Shortest Distance Finder");
        this.setSize(800, 600);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
    }

    private void drawArrow(Graphics2D g2d, int x1, int y1, int x2, int y2) {
        int arrowSize = 10;  // Size of the arrowhead
        double angle = Math.atan2(y2 - y1, x2 - x1);

        // Draw line (edge)
        g2d.drawLine(x1, y1, x2, y2);

        // Draw arrowhead
        int xArrow1 = (int) (x2 - arrowSize * Math.cos(angle - Math.PI / 6));
        int yArrow1 = (int) (y2 - arrowSize * Math.sin(angle - Math.PI / 6));
        int xArrow2 = (int) (x2 - arrowSize * Math.cos(angle + Math.PI / 6));
        int yArrow2 = (int) (y2 - arrowSize * Math.sin(angle + Math.PI / 6));

        g2d.drawLine(x2, y2, xArrow1, yArrow1);
        g2d.drawLine(x2, y2, xArrow2, yArrow2);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GraphGUI::new);
    }

    // Edge class to represent connections between nodes
    static class Edge {
        int src, dest, wt;

        public Edge(int src, int dest, int wt) {
            this.src = src;
            this.dest = dest;
            this.wt = wt;
        }
    }
}
