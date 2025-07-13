package com.dijkstras;

import java.awt.Point;

public class Node {
    public Point position;
    public int id;

    public Node(Point position, int id) {
        this.position = position;
        this.id = id;
    }
} 