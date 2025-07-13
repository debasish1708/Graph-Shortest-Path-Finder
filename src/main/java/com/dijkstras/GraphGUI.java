package com.dijkstras;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Stack;

public class GraphGUI extends JFrame {
    private final ArrayList<Point> nodes; // Store node positions
    private final ArrayList<Edge> edges;  // Store edges
    private final ArrayList<Edge>[] graph; // Graph structure
    private int nodeCount = 0; // Total nodes
    private ArrayList<Integer> highlightedPath = new ArrayList<>(); // Store highlighted path
    private boolean isPathHighlighted = false;

    // Animation-related fields
    private Timer animationTimer;
    private int animationStep = 0;
    private boolean isAnimating = false;
    private static final int ANIMATION_DELAY = 100; // milliseconds per step (faster for smoother animation)
    private static final int ANIMATION_STEPS = 60; // total animation steps (more steps for smoother animation)

    // Modern color scheme
    private static final Color PRIMARY_COLOR = new Color(52, 152, 219);
    private static final Color SECONDARY_COLOR = new Color(41, 128, 185);
    private static final Color ACCENT_COLOR = new Color(46, 204, 113);
    private static final Color BACKGROUND_COLOR = new Color(236, 240, 241);
    private static final Color NODE_COLOR = new Color(52, 152, 219);
    private static final Color EDGE_COLOR = new Color(149, 165, 166);
    private static final Color HIGHLIGHTED_PATH_COLOR = new Color(46, 204, 113); // Green color
    private static final Color HIGHLIGHTED_NODE_COLOR = new Color(46, 204, 113); // Green color
    private static final Color ANIMATION_COLOR = new Color(46, 204, 113); // Bright green for animation

    private final Stack<Action> undoStack = new Stack<>();
    private final Stack<Action> redoStack = new Stack<>();

    public GraphGUI() {
        nodes = new ArrayList<>();
        edges = new ArrayList<>();
        graph = new ArrayList[100]; // Graph array

        // Initialize graph list
        for (int i = 0; i < 100; i++) {
            graph[i] = new ArrayList<>();
        }

        setupUI();

        // Add keyboard shortcuts for Undo (Ctrl+Z) and Redo (Ctrl+Y)
        InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getRootPane().getActionMap();
        inputMap.put(KeyStroke.getKeyStroke("control Z"), "undo");
        inputMap.put(KeyStroke.getKeyStroke("control Y"), "redo");
        actionMap.put("undo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                undo();
            }
        });
        actionMap.put("redo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                redo();
            }
        });
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

                // Draw arrowhead to indicate direction (from src to dest)
                drawEdgeArrowhead(g2d, src, dest, isHighlighted);

                // Draw animated path if currently animating
                if (isAnimating && isHighlighted) {
                    drawAnimatedPath(g2d, src, dest);
                }

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

    // Draw an arrowhead at the end of an edge to indicate direction
    private void drawEdgeArrowhead(Graphics2D g2d, Point src, Point dest, boolean isHighlighted) {
        // Arrowhead parameters
        int nodeRadius = 15; // Should match node drawing
        int arrowSize = 12;
        double angle = Math.atan2(dest.y - src.y, dest.x - src.x);

        // Position the arrowhead just before the destination node
        int arrowX = (int) (dest.x - nodeRadius * Math.cos(angle));
        int arrowY = (int) (dest.y - nodeRadius * Math.sin(angle));

        double arrowAngle = Math.PI / 7;
        int x1 = (int) (arrowX - arrowSize * Math.cos(angle - arrowAngle));
        int y1 = (int) (arrowY - arrowSize * Math.sin(angle - arrowAngle));
        int x2 = (int) (arrowX - arrowSize * Math.cos(angle + arrowAngle));
        int y2 = (int) (arrowY - arrowSize * Math.sin(angle + arrowAngle));

        // Set color
        if (isHighlighted) {
            g2d.setColor(HIGHLIGHTED_PATH_COLOR);
        } else {
            g2d.setColor(EDGE_COLOR);
        }
        // Draw filled arrowhead
        int[] xPoints = {arrowX, x1, x2};
        int[] yPoints = {arrowY, y1, y2};
        g2d.fillPolygon(xPoints, yPoints, 3);
        // Draw white outline
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(1.2f));
        g2d.drawPolygon(xPoints, yPoints, 3);
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
            
            // Draw animated node effect if currently animating
            if (isAnimating && isHighlighted) {
                drawAnimatedNode(g2d, p, i);
            }
            
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

    private void drawAnimatedPath(Graphics2D g2d, Point src, Point dest) {
        // Find which edge this is in the path sequence
        int edgeIndex = -1;
        for (int i = 0; i < highlightedPath.size() - 1; i++) {
            int pathSrc = highlightedPath.get(i);
            int pathDest = highlightedPath.get(i + 1);
            
            // Find the node indices for the current edge
            int srcIndex = -1, destIndex = -1;
            for (int j = 0; j < nodes.size(); j++) {
                if (nodes.get(j).equals(src)) srcIndex = j;
                if (nodes.get(j).equals(dest)) destIndex = j;
            }
            
            if ((pathSrc == srcIndex && pathDest == destIndex) ||
                (pathSrc == destIndex && pathDest == srcIndex)) {
                edgeIndex = i;
                break;
            }
        }
        
        if (edgeIndex == -1) return;
        
        // Calculate the progress for this specific edge
        float totalProgress = (float) animationStep / ANIMATION_STEPS;
        float edgeProgress = Math.max(0, Math.min(1, (totalProgress - (float) edgeIndex / (highlightedPath.size() - 1)) * (highlightedPath.size() - 1)));
        
        if (edgeProgress > 0 && edgeProgress <= 1) {
            // Calculate the current position along this edge
            int currentX = (int) (src.x + (dest.x - src.x) * edgeProgress);
            int currentY = (int) (src.y + (dest.y - src.y) * edgeProgress);
            
            // Draw a glowing effect at the current position
            int glowSize = 25;
            RadialGradientPaint glow = new RadialGradientPaint(
                currentX, currentY, glowSize,
                new float[]{0.0f, 0.7f, 1.0f},
                new Color[]{
                    new Color(ANIMATION_COLOR.getRed(), ANIMATION_COLOR.getGreen(), ANIMATION_COLOR.getBlue(), 200),
                    new Color(ANIMATION_COLOR.getRed(), ANIMATION_COLOR.getGreen(), ANIMATION_COLOR.getBlue(), 100),
                    new Color(ANIMATION_COLOR.getRed(), ANIMATION_COLOR.getGreen(), ANIMATION_COLOR.getBlue(), 0)
                }
            );
            
            g2d.setPaint(glow);
            g2d.fillOval(currentX - glowSize, currentY - glowSize, glowSize * 2, glowSize * 2);
            
            // Draw a moving dot with pulsing effect
            int dotSize = 8 + (int)(4 * Math.sin(animationStep * 0.5));
            g2d.setColor(ANIMATION_COLOR);
            g2d.fillOval(currentX - dotSize/2, currentY - dotSize/2, dotSize, dotSize);
            
            // Draw a white border around the dot
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(2.0f));
            g2d.drawOval(currentX - dotSize/2, currentY - dotSize/2, dotSize, dotSize);
            
            // Draw a trail effect
            for (int i = 1; i <= 3; i++) {
                float trailProgress = edgeProgress - (i * 0.1f);
                if (trailProgress > 0) {
                    int trailX = (int) (src.x + (dest.x - src.x) * trailProgress);
                    int trailY = (int) (src.y + (dest.y - src.y) * trailProgress);
                    int trailSize = dotSize - i * 2;
                    if (trailSize > 0) {
                        g2d.setColor(new Color(ANIMATION_COLOR.getRed(), ANIMATION_COLOR.getGreen(), ANIMATION_COLOR.getBlue(), 100 - i * 30));
                        g2d.fillOval(trailX - trailSize/2, trailY - trailSize/2, trailSize, trailSize);
                    }
                }
            }
        }
    }

    private void drawAnimatedNode(Graphics2D g2d, Point p, int nodeIndex) {
        // Find the position of this node in the path
        int pathIndex = highlightedPath.indexOf(nodeIndex);
        if (pathIndex == -1) return;
        
        // Calculate animation progress for this specific node
        float nodeProgress = (float) pathIndex / (highlightedPath.size() - 1);
        float currentProgress = (float) animationStep / ANIMATION_STEPS;
        
        // Only animate if we've reached this node in the sequence
        if (currentProgress >= nodeProgress) {
            // Calculate the intensity of the glow effect
            float intensity = Math.min(1.0f, (currentProgress - nodeProgress) * 2.0f);
            
            // Draw pulsing glow effect with multiple layers
            int maxGlowSize = 50;
            int glowSize = (int) (maxGlowSize * intensity);
            
            if (glowSize > 0) {
                // Outer glow
                RadialGradientPaint outerGlow = new RadialGradientPaint(
                    p.x, p.y, glowSize,
                    new float[]{0.0f, 0.6f, 1.0f},
                    new Color[]{
                        new Color(ANIMATION_COLOR.getRed(), ANIMATION_COLOR.getGreen(), ANIMATION_COLOR.getBlue(), (int)(80 * intensity)),
                        new Color(ANIMATION_COLOR.getRed(), ANIMATION_COLOR.getGreen(), ANIMATION_COLOR.getBlue(), (int)(40 * intensity)),
                        new Color(ANIMATION_COLOR.getRed(), ANIMATION_COLOR.getGreen(), ANIMATION_COLOR.getBlue(), 0)
                    }
                );
                
                g2d.setPaint(outerGlow);
                g2d.fillOval(p.x - glowSize, p.y - glowSize, glowSize * 2, glowSize * 2);
                
                // Inner glow with pulsing effect
                int pulseSize = (int) (30 + 10 * Math.sin(animationStep * 0.3 + pathIndex));
                RadialGradientPaint innerGlow = new RadialGradientPaint(
                    p.x, p.y, pulseSize,
                    new float[]{0.0f, 0.8f, 1.0f},
                    new Color[]{
                        new Color(ANIMATION_COLOR.getRed(), ANIMATION_COLOR.getGreen(), ANIMATION_COLOR.getBlue(), (int)(150 * intensity)),
                        new Color(ANIMATION_COLOR.getRed(), ANIMATION_COLOR.getGreen(), ANIMATION_COLOR.getBlue(), (int)(80 * intensity)),
                        new Color(ANIMATION_COLOR.getRed(), ANIMATION_COLOR.getGreen(), ANIMATION_COLOR.getBlue(), 0)
                    }
                );
                
                g2d.setPaint(innerGlow);
                g2d.fillOval(p.x - pulseSize, p.y - pulseSize, pulseSize * 2, pulseSize * 2);
            }
        }
    }

    private void handleMouseClick(MouseEvent e) {
        // Left-click to create node
        if (SwingUtilities.isLeftMouseButton(e)) {
            Point p = e.getPoint();
            nodes.add(p);
            nodeCount++;
            isPathHighlighted = false; // Clear previous path
            updateStats();
            repaint();
            // Track action
            undoStack.push(new AddNodeAction(p));
            redoStack.clear();
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
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Create main form panel
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        
        JTextField sourceField = new JTextField();
        JTextField destField = new JTextField();
        JTextField weightField = new JTextField();
        
        // Ensure the first field gets focus when dialog appears
        Timer timer = new Timer(100, e -> sourceField.requestFocusInWindow());
        timer.setRepeats(false);
        timer.start();
        
        formPanel.add(new JLabel("Source Node ID:"));
        formPanel.add(sourceField);
        formPanel.add(new JLabel("Destination Node ID:"));
        formPanel.add(destField);
        formPanel.add(new JLabel("Weight:"));
        formPanel.add(weightField);
        
        // Create arrow visualization panel
        JPanel arrowPanel = createArrowVisualizationPanel(sourceField, destField);
        
        // Add components to main panel
        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(arrowPanel, BorderLayout.CENTER);
        
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
                    Edge edge = new Edge(src, dest, wt);
                    edges.add(edge);
                    graph[src].add(edge);
                    isPathHighlighted = false; // Clear previous path
                    updateStats();
                    repaint();
                    // Track action
                    undoStack.push(new AddEdgeAction(edge));
                    redoStack.clear();
                }
                
            } catch (NumberFormatException ex) {
                showMessage("Please enter valid numbers!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JPanel createArrowVisualizationPanel(JTextField sourceField, JTextField destField) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
                "Direction Visualization",
                TitledBorder.CENTER,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14),
                PRIMARY_COLOR
            ),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        panel.setBackground(new Color(248, 249, 250));

        // Create the arrow visualization component
        JPanel arrowVizPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Get the current values
                String sourceText = sourceField.getText();
                String destText = destField.getText();
                
                int width = getWidth();
                int height = getHeight();
                
                // Draw background gradient
                GradientPaint gradient = new GradientPaint(0, 0, new Color(255, 255, 255), 
                                                          width, height, new Color(248, 249, 250));
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, width, height);
                
                if (!sourceText.isEmpty() && !destText.isEmpty()) {
                    try {
                        int sourceId = Integer.parseInt(sourceText);
                        int destId = Integer.parseInt(destText);
                        
                        // Check if nodes exist
                        if (sourceId >= 0 && sourceId < nodeCount && destId >= 0 && destId < nodeCount) {
                            // Draw source node
                            int sourceX = width / 4;
                            int nodeY = height / 2;
                            int nodeRadius = 25;
                            
                            // Source node with blue color
                            g2d.setColor(PRIMARY_COLOR);
                            g2d.fillOval(sourceX - nodeRadius, nodeY - nodeRadius, nodeRadius * 2, nodeRadius * 2);
                            g2d.setColor(Color.WHITE);
                            g2d.setStroke(new BasicStroke(2.0f));
                            g2d.drawOval(sourceX - nodeRadius, nodeY - nodeRadius, nodeRadius * 2, nodeRadius * 2);
                            
                            // Source node label
                            g2d.setColor(Color.WHITE);
                            g2d.setFont(new Font("Segoe UI", Font.BOLD, 14));
                            String sourceLabel = "S: " + sourceId;
                            FontMetrics fm = g2d.getFontMetrics();
                            int sourceTextWidth = fm.stringWidth(sourceLabel);
                            g2d.drawString(sourceLabel, sourceX - sourceTextWidth / 2, nodeY + 5);
                            
                            // Draw destination node
                            int destX = 3 * width / 4;
                            
                            // Destination node with green color
                            g2d.setColor(ACCENT_COLOR);
                            g2d.fillOval(destX - nodeRadius, nodeY - nodeRadius, nodeRadius * 2, nodeRadius * 2);
                            g2d.setColor(Color.WHITE);
                            g2d.drawOval(destX - nodeRadius, nodeY - nodeRadius, nodeRadius * 2, nodeRadius * 2);
                            
                            // Destination node label
                            g2d.setColor(Color.WHITE);
                            String destLabel = "D: " + destId;
                            int destTextWidth = fm.stringWidth(destLabel);
                            g2d.drawString(destLabel, destX - destTextWidth / 2, nodeY + 5);
                            
                            // Draw animated arrow
                            drawAnimatedArrowBetweenNodes(g2d, sourceX, destX, nodeY);
                            
                        } else {
                            // Show error message
                            g2d.setColor(new Color(231, 76, 60));
                            g2d.setFont(new Font("Segoe UI", Font.BOLD, 12));
                            String errorMsg = "Invalid node IDs! Use 0 to " + (nodeCount - 1);
                            FontMetrics fm = g2d.getFontMetrics();
                            int textWidth = fm.stringWidth(errorMsg);
                            g2d.drawString(errorMsg, (width - textWidth) / 2, height / 2);
                        }
                    } catch (NumberFormatException e) {
                        // Show instruction message
                        g2d.setColor(new Color(127, 140, 141));
                        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                        String instruction = "Enter valid node IDs to see direction";
                        FontMetrics fm = g2d.getFontMetrics();
                        int textWidth = fm.stringWidth(instruction);
                        g2d.drawString(instruction, (width - textWidth) / 2, height / 2);
                    }
                } else {
                    // Show instruction message
                    g2d.setColor(new Color(127, 140, 141));
                    g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    String instruction = "Enter source and destination IDs to see direction";
                    FontMetrics fm = g2d.getFontMetrics();
                    int textWidth = fm.stringWidth(instruction);
                    g2d.drawString(instruction, (width - textWidth) / 2, height / 2);
                }
                
                g2d.dispose();
            }
        };
        
        arrowVizPanel.setPreferredSize(new Dimension(300, 120));
        arrowVizPanel.setBackground(Color.WHITE);
        
        // Add document listeners to update the visualization
        sourceField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { arrowVizPanel.repaint(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { arrowVizPanel.repaint(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { arrowVizPanel.repaint(); }
        });
        
        destField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { arrowVizPanel.repaint(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { arrowVizPanel.repaint(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { arrowVizPanel.repaint(); }
        });
        
        panel.add(arrowVizPanel, BorderLayout.CENTER);
        
        // Add timer to continuously update the animation
        Timer animationTimer = new Timer(50, e -> arrowVizPanel.repaint());
        animationTimer.start();
        
        return panel;
    }

    private void drawAnimatedArrowBetweenNodes(Graphics2D g2d, int sourceX, int destX, int nodeY) {
        // Calculate arrow parameters
        int arrowLength = destX - sourceX - 50; // Leave space for nodes
        int arrowStartX = sourceX + 25;
        int arrowEndX = destX - 25;
        
        // Draw arrow line
        g2d.setColor(PRIMARY_COLOR);
        g2d.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.drawLine(arrowStartX, nodeY, arrowEndX, nodeY);
        
        // Draw arrow head
        int arrowHeadSize = 12;
        int[] xPoints = {arrowEndX, arrowEndX - arrowHeadSize, arrowEndX - arrowHeadSize};
        int[] yPoints = {nodeY, nodeY - arrowHeadSize/2, nodeY + arrowHeadSize/2};
        
        g2d.setColor(ACCENT_COLOR);
        g2d.fillPolygon(xPoints, yPoints, 3);
        
        // Draw arrow outline
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawPolygon(xPoints, yPoints, 3);
        
        // Draw animated dots along the arrow
        long currentTime = System.currentTimeMillis();
        double animationProgress = (currentTime % 2000) / 2000.0; // 2 second cycle
        
        for (int i = 0; i < 3; i++) {
            double dotProgress = (animationProgress + i * 0.3) % 1.0;
            int dotX = (int) (arrowStartX + dotProgress * (arrowEndX - arrowStartX));
            int dotSize = 6 - i * 2;
            if (dotSize > 0) {
                g2d.setColor(new Color(ACCENT_COLOR.getRed(), ACCENT_COLOR.getGreen(), ACCENT_COLOR.getBlue(), 200 - i * 60));
                g2d.fillOval(dotX - dotSize/2, nodeY - dotSize/2, dotSize, dotSize);
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
            "• Ctrl+z Undo, Ctrl+y Redo",
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

        // Undo/Redo buttons
        JButton undoBtn = createStyledButton("Undo", new Color(241, 196, 15));
        undoBtn.addActionListener(e -> undo());
        JButton redoBtn = createStyledButton("Redo", new Color(52, 152, 219));
        redoBtn.addActionListener(e -> redo());

        panel.add(findPathBtn);
        panel.add(Box.createVerticalStrut(10));
        panel.add(clearPathBtn);
        panel.add(Box.createVerticalStrut(10));
        panel.add(clearAllBtn);
        panel.add(Box.createVerticalStrut(10));
        panel.add(undoBtn);
        panel.add(Box.createVerticalStrut(10));
        panel.add(redoBtn);

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
                
                g2d.fill(new java.awt.geom.RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 15, 15));
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
                    
                    // Start the animation
                    startPathAnimation();
                    
                    // Show result dialog after animation
                    Timer resultTimer = new Timer(ANIMATION_DELAY * ANIMATION_STEPS + 1000, e -> {
                        showPathResult(resultObj);
                    });
                    resultTimer.setRepeats(false);
                    resultTimer.start();
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

    private void startPathAnimation() {
        // Stop any existing animation
        if (animationTimer != null) {
            animationTimer.stop();
        }
        
        // Reset animation state
        animationStep = 0;
        isAnimating = true;
        
        // Create and start the animation timer
        animationTimer = new Timer(ANIMATION_DELAY, e -> {
            animationStep++;
            
            if (animationStep >= ANIMATION_STEPS) {
                // Animation complete
                isAnimating = false;
                animationTimer.stop();
            }
            
            repaint();
        });
        
        animationTimer.start();
    }

    private void clearPath() {
        // Stop any ongoing animation
        if (animationTimer != null) {
            animationTimer.stop();
        }
        isAnimating = false;
        animationStep = 0;
        
        isPathHighlighted = false;
        highlightedPath.clear();
        repaint();
    }

    private void clearAll() {
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to clear all nodes and edges?", 
            "Confirm Clear", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            // Stop any ongoing animation
            if (animationTimer != null) {
                animationTimer.stop();
            }
            isAnimating = false;
            animationStep = 0;
            
            // Track action
            undoStack.push(new ClearAllAction(nodes, edges, graph, nodeCount));
            redoStack.clear();
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

    // Action base class and subclasses
    private abstract class Action {
        abstract void undo();
        abstract void redo();
    }
    private class AddNodeAction extends Action {
        private final Point point;
        public AddNodeAction(Point point) { this.point = point; }
        void undo() {
            nodes.remove(nodes.size() - 1);
            nodeCount--;
            updateStats();
            repaint();
        }
        void redo() {
            nodes.add(point);
            nodeCount++;
            updateStats();
            repaint();
        }
    }
    private class AddEdgeAction extends Action {
        private final Edge edge;
        public AddEdgeAction(Edge edge) { this.edge = edge; }
        void undo() {
            edges.remove(edge);
            graph[edge.src].removeIf(e -> e.dest == edge.dest && e.wt == edge.wt);
            updateStats();
            repaint();
        }
        void redo() {
            edges.add(edge);
            graph[edge.src].add(edge);
            updateStats();
            repaint();
        }
    }
    private class ClearAllAction extends Action {
        private final ArrayList<Point> oldNodes;
        private final ArrayList<Edge> oldEdges;
        private final ArrayList<Edge>[] oldGraph;
        private final int oldNodeCount;
        public ClearAllAction(ArrayList<Point> nodes, ArrayList<Edge> edges, ArrayList<Edge>[] graph, int nodeCount) {
            this.oldNodes = new ArrayList<>(nodes);
            this.oldEdges = new ArrayList<>(edges);
            this.oldGraph = new ArrayList[100];
            for (int i = 0; i < 100; i++) {
                this.oldGraph[i] = new ArrayList<>(graph[i]);
            }
            this.oldNodeCount = nodeCount;
        }
        void undo() {
            nodes.clear();
            nodes.addAll(oldNodes);
            edges.clear();
            edges.addAll(oldEdges);
            for (int i = 0; i < 100; i++) {
                graph[i].clear();
                graph[i].addAll(oldGraph[i]);
            }
            nodeCount = oldNodeCount;
            updateStats();
            repaint();
        }
        void redo() {
            nodes.clear();
            edges.clear();
            nodeCount = 0;
            for (int i = 0; i < 100; i++) graph[i].clear();
            updateStats();
            repaint();
        }
    }

    // Undo/Redo methods
    private void undo() {
        if (!undoStack.isEmpty()) {
            Action action = undoStack.pop();
            action.undo();
            redoStack.push(action);
        }
    }
    private void redo() {
        if (!redoStack.isEmpty()) {
            Action action = redoStack.pop();
            action.redo();
            undoStack.push(action);
        }
    }
}
