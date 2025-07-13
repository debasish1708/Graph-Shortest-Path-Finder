package com.dijkstras;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;

public class GraphGUI extends JFrame {
    private final ArrayList<Point> nodes; // Store node positions
    private final ArrayList<Edge> edges;  // Store edges
    private final ArrayList<Edge>[] graph; // Graph structure
    private int nodeCount = 0; // Total nodes
    private ArrayList<Integer> highlightedPath = new ArrayList<>(); // Store highlighted path
    private boolean isPathHighlighted = false;

    // Modern color scheme
    private static final Color PRIMARY_COLOR = new Color(52, 152, 219);
    private static final Color SECONDARY_COLOR = new Color(41, 128, 185);
    private static final Color ACCENT_COLOR = new Color(46, 204, 113);
    private static final Color BACKGROUND_COLOR = new Color(236, 240, 241);
    private static final Color NODE_COLOR = new Color(52, 152, 219);
    private static final Color EDGE_COLOR = new Color(149, 165, 166);
    private static final Color HIGHLIGHTED_PATH_COLOR = new Color(46, 204, 113); // Green color
    private static final Color HIGHLIGHTED_NODE_COLOR = new Color(46, 204, 113); // Green color

    public GraphGUI() {
        nodes = new ArrayList<>();
        edges = new ArrayList<>();
        graph = new ArrayList[100]; // Graph array

        // Initialize graph list
        for (int i = 0; i < 100; i++) {
            graph[i] = new ArrayList<>();
        }

        setupUI();
    }

    private void setupUI() {
        setTitle("Dijkstra's Algorithm - Shortest Path Finder");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BACKGROUND_COLOR);

        // Main layout
        setLayout(new BorderLayout(10, 10));
        ((JComponent) getContentPane()).setBorder(new EmptyBorder(20, 20, 20, 20));

        // Create header panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Create main content panel
        JPanel contentPanel = new JPanel(new BorderLayout(15, 15));
        contentPanel.setOpaque(false);

        // Create canvas
        JPanel canvas = createCanvas();
        contentPanel.add(canvas, BorderLayout.CENTER);

        // Create control panel
        JPanel controlPanel = createControlPanel();
        contentPanel.add(controlPanel, BorderLayout.EAST);

        add(contentPanel, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Dijkstra's Algorithm Visualizer");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(PRIMARY_COLOR);

        JLabel subtitleLabel = new JLabel("Create nodes, connect them, and find the shortest path!");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(127, 140, 141));

        headerPanel.add(titleLabel);
        headerPanel.add(Box.createHorizontalStrut(20));
        headerPanel.add(subtitleLabel);

        return headerPanel;
    }

    private JPanel createCanvas() {
        JPanel canvas = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                // Draw background gradient
                GradientPaint gradient = new GradientPaint(0, 0, new Color(248, 249, 250), 
                                                          getWidth(), getHeight(), new Color(241, 243, 244));
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                // Draw edges first (so they appear behind nodes)
                drawEdges(g2d);

                // Draw nodes
                drawNodes(g2d);
            }
        };

        canvas.setPreferredSize(new Dimension(800, 600));
        canvas.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleMouseClick(e);
            }
        });

        return canvas;
    }

    private void drawEdges(Graphics2D g2d) {
        for (Edge e : edges) {
            if (nodes.size() > e.src && nodes.size() > e.dest) {
                Point src = nodes.get(e.src);
                Point dest = nodes.get(e.dest);

                // Check if this edge is part of the highlighted path
                boolean isHighlighted = isPathHighlighted && isEdgeInPath(e.src, e.dest);
                
                if (isHighlighted) {
                    g2d.setStroke(new BasicStroke(4.0f));
                    g2d.setColor(HIGHLIGHTED_PATH_COLOR);
                } else {
                    g2d.setStroke(new BasicStroke(2.0f));
                    g2d.setColor(EDGE_COLOR);
                }

                // Draw edge line
                g2d.drawLine(src.x, src.y, dest.x, dest.y);

                // Draw weight label
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 12));
                String weightText = Integer.toString(e.wt);
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(weightText);
                int textHeight = fm.getAscent();
                
                // Draw background for weight label
                int labelX = (src.x + dest.x) / 2 - textWidth / 2;
                int labelY = (src.y + dest.y) / 2 + textHeight / 2;
                
                g2d.setColor(isHighlighted ? HIGHLIGHTED_PATH_COLOR : PRIMARY_COLOR);
                g2d.fillRoundRect(labelX - 5, labelY - textHeight - 2, textWidth + 10, textHeight + 4, 8, 8);
                
                g2d.setColor(Color.WHITE);
                g2d.drawString(weightText, labelX, labelY);
            }
        }
    }

    private void drawNodes(Graphics2D g2d) {
        for (int i = 0; i < nodes.size(); i++) {
            Point p = nodes.get(i);
            
            // Check if this node is part of the highlighted path
            boolean isHighlighted = isPathHighlighted && highlightedPath.contains(i);
            
            // Draw node shadow
            g2d.setColor(new Color(0, 0, 0, 30));
            g2d.fillOval(p.x - 17, p.y - 17, 34, 34);
            
            // Draw node
            if (isHighlighted) {
                g2d.setColor(HIGHLIGHTED_NODE_COLOR);
            } else {
                g2d.setColor(NODE_COLOR);
            }
            g2d.fillOval(p.x - 15, p.y - 15, 30, 30);
            
            // Draw node border
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(2.0f));
            g2d.drawOval(p.x - 15, p.y - 15, 30, 30);
            
            // Draw node number
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 12));
            String nodeText = Integer.toString(i);
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(nodeText);
            g2d.drawString(nodeText, p.x - textWidth / 2, p.y + 4);
        }
    }

    private boolean isEdgeInPath(int src, int dest) {
        for (int i = 0; i < highlightedPath.size() - 1; i++) {
            if ((highlightedPath.get(i) == src && highlightedPath.get(i + 1) == dest) ||
                (highlightedPath.get(i) == dest && highlightedPath.get(i + 1) == src)) {
                return true;
            }
        }
        return false;
    }

    private void handleMouseClick(MouseEvent e) {
                        // Left-click to create node
                if (SwingUtilities.isLeftMouseButton(e)) {
                    nodes.add(e.getPoint());
                    nodeCount++;
                    isPathHighlighted = false; // Clear previous path
                    updateStats();
                    repaint();
                }
        // Right-click to connect nodes
        else if (SwingUtilities.isRightMouseButton(e)) {
            if (nodeCount > 1) {
                showEdgeCreationDialog();
            } else {
                showMessage("Please create at least 2 nodes first!", "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private void showEdgeCreationDialog() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JTextField sourceField = new JTextField();
        JTextField destField = new JTextField();
        JTextField weightField = new JTextField();
        
        // Ensure the first field gets focus when dialog appears
        Timer timer = new Timer(100, e -> sourceField.requestFocusInWindow());
        timer.setRepeats(false);
        timer.start();
        
        panel.add(new JLabel("Source Node ID:"));
        panel.add(sourceField);
        panel.add(new JLabel("Destination Node ID:"));
        panel.add(destField);
        panel.add(new JLabel("Weight:"));
        panel.add(weightField);
        
        int result = JOptionPane.showConfirmDialog(this, panel, "Create Edge", 
                                                 JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            try {
                int src = Integer.parseInt(sourceField.getText());
                int dest = Integer.parseInt(destField.getText());
                int wt = Integer.parseInt(weightField.getText());
                
                if (src < 0 || src >= nodeCount || dest < 0 || dest >= nodeCount) {
                    showMessage("Invalid node IDs! Use IDs from 0 to " + (nodeCount - 1), "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                if (wt <= 0) {
                    showMessage("Weight must be positive!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Check if edge already exists
                boolean exists = false;
                for (Edge edge : graph[src]) {
                    if (edge.dest == dest) {
                        showMessage("Edge already exists between these nodes!", "Warning", JOptionPane.WARNING_MESSAGE);
                        exists = true;
                        break;
                    }
                }
                
                                if (!exists) {
                    edges.add(new Edge(src, dest, wt));
                    graph[src].add(new Edge(src, dest, wt));
                    isPathHighlighted = false; // Clear previous path
                    updateStats();
                    repaint();
                }
                
            } catch (NumberFormatException ex) {
                showMessage("Please enter valid numbers!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setOpaque(false);
        controlPanel.setPreferredSize(new Dimension(250, 0));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));

        // Instructions panel
        JPanel instructionsPanel = createInstructionsPanel();
        controlPanel.add(instructionsPanel);
        controlPanel.add(Box.createVerticalStrut(20));

        // Buttons panel
        JPanel buttonsPanel = createButtonsPanel();
        controlPanel.add(buttonsPanel);
        controlPanel.add(Box.createVerticalStrut(20));

        // Stats panel
        JPanel statsPanel = createStatsPanel();
        controlPanel.add(statsPanel);

        return controlPanel;
    }

    private JPanel createInstructionsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel titleLabel = new JLabel("Instructions");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        String[] instructions = {
            "• Left-click to create nodes",
            "• Right-click to connect nodes",
            "• Enter source and destination IDs",
            "• Specify the weight/distance",
            "• Click 'Find Shortest Path' to calculate"
        };

        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(10));

        for (String instruction : instructions) {
            JLabel label = new JLabel(instruction);
            label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            label.setForeground(new Color(127, 140, 141));
            panel.add(label);
            panel.add(Box.createVerticalStrut(5));
        }

        return panel;
    }

    private JPanel createButtonsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        JButton findPathBtn = createStyledButton("Find Shortest Path", ACCENT_COLOR);
        findPathBtn.addActionListener(e -> findShortestPath());

        JButton clearPathBtn = createStyledButton("Clear Path", new Color(149, 165, 166));
        clearPathBtn.addActionListener(e -> clearPath());

        JButton clearAllBtn = createStyledButton("Clear All", new Color(231, 76, 60));
        clearAllBtn.addActionListener(e -> clearAll());

        panel.add(findPathBtn);
        panel.add(Box.createVerticalStrut(10));
        panel.add(clearPathBtn);
        panel.add(Box.createVerticalStrut(10));
        panel.add(clearAllBtn);

        return panel;
    }

    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (getModel().isPressed()) {
                    g2d.setColor(color.darker());
                } else if (getModel().isRollover()) {
                    g2d.setColor(color.brighter());
                } else {
                    g2d.setColor(color);
                }
                
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 15, 15));
                g2d.dispose();
                
                super.paintComponent(g);
            }
        };
        
        button.setPreferredSize(new Dimension(200, 40));
        button.setMaximumSize(new Dimension(200, 40));
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        return button;
    }

    private JPanel statsPanel;
    private JLabel nodesLabel;
    private JLabel edgesLabel;

    private JPanel createStatsPanel() {
        statsPanel = new JPanel();
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.setBackground(Color.WHITE);
        statsPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel titleLabel = new JLabel("Statistics");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        nodesLabel = new JLabel("Nodes: " + nodeCount);
        nodesLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        nodesLabel.setForeground(new Color(127, 140, 141));

        edgesLabel = new JLabel("Edges: " + edges.size());
        edgesLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        edgesLabel.setForeground(new Color(127, 140, 141));

        statsPanel.add(titleLabel);
        statsPanel.add(Box.createVerticalStrut(10));
        statsPanel.add(nodesLabel);
        statsPanel.add(Box.createVerticalStrut(5));
        statsPanel.add(edgesLabel);

        return statsPanel;
    }

    private void updateStats() {
        if (nodesLabel != null && edgesLabel != null) {
            nodesLabel.setText("Nodes: " + nodeCount);
            edgesLabel.setText("Edges: " + edges.size());
            statsPanel.revalidate();
            statsPanel.repaint();
        }
    }

    private void findShortestPath() {
        if (nodeCount < 2) {
            showMessage("Please create at least 2 nodes first!", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JTextField sourceField = new JTextField();
        JTextField destField = new JTextField();
        
        // Ensure the first field gets focus when dialog appears
        Timer timer = new Timer(100, e -> sourceField.requestFocusInWindow());
        timer.setRepeats(false);
        timer.start();
        
        panel.add(new JLabel("Source Node ID:"));
        panel.add(sourceField);
        panel.add(new JLabel("Destination Node ID:"));
        panel.add(destField);
        
        int result = JOptionPane.showConfirmDialog(this, panel, "Find Shortest Path", 
                                                 JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            try {
                int source = Integer.parseInt(sourceField.getText());
                int destination = Integer.parseInt(destField.getText());
                
                if (source < 0 || source >= nodeCount || destination < 0 || destination >= nodeCount) {
                    showMessage("Invalid node IDs! Use IDs from 0 to " + (nodeCount - 1), "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Call Dijkstra's algorithm with path
                DijkstrasAlgorithm.Result resultObj = DijkstrasAlgorithm.dijkstraWithPath(graph, source, destination);

                if (resultObj.distance == -1) {
                    showMessage("No path exists between the selected nodes!", "No Path", JOptionPane.WARNING_MESSAGE);
                } else {
                    highlightedPath = resultObj.path;
                    isPathHighlighted = true;
                    repaint();
                    
                    // Show result dialog
                    showPathResult(resultObj);
                }
                
            } catch (NumberFormatException ex) {
                showMessage("Please enter valid numbers!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showPathResult(DijkstrasAlgorithm.Result result) {
        StringBuilder pathStr = new StringBuilder();
        for (int i = 0; i < result.path.size(); i++) {
            pathStr.append(result.path.get(i));
            if (i < result.path.size() - 1) {
                pathStr.append(" → ");
            }
        }
        
        String message = String.format("Shortest Distance: %d\n\nPath: %s", 
                                     result.distance, pathStr.toString());
        
        JOptionPane.showMessageDialog(this, message, "Shortest Path Found", 
                                    JOptionPane.INFORMATION_MESSAGE);
    }

    private void clearPath() {
        isPathHighlighted = false;
        highlightedPath.clear();
        repaint();
    }

    private void clearAll() {
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to clear all nodes and edges?", 
            "Confirm Clear", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            nodes.clear();
            edges.clear();
            nodeCount = 0;
            isPathHighlighted = false;
            highlightedPath.clear();
            
            // Clear graph
            for (int i = 0; i < 100; i++) {
                graph[i].clear();
            }
            
            updateStats();
            repaint();
        }
    }

    private void showMessage(String message, String title, int messageType) {
        JOptionPane.showMessageDialog(this, message, title, messageType);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            new GraphGUI().setVisible(true);
        });
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
