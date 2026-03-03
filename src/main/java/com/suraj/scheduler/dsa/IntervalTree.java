package com.suraj.scheduler.dsa;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.suraj.scheduler.entity.Task;

public class IntervalTree {

    private class Node {
        Task task;
        LocalDateTime maxEnd;
        Node left, right;

        Node(Task task) {
            this.task = task;
            this.maxEnd = task.getEndTime();
        }
    }

    private Node root;

    public void insert(Task task) {
        root = insert(root, task);
    }

    private Node insert(Node node, Task task) {
        if (node == null) return new Node(task);

        if (task.getStartTime().isBefore(node.task.getStartTime())) {
            node.left = insert(node.left, task);
        } else {
            node.right = insert(node.right, task);
        }

        if (node.maxEnd.isBefore(task.getEndTime())) node.maxEnd = task.getEndTime();
        return node;
    }

    public boolean isOverlapping(Task task) {
        return isOverlapping(root, task);
    }

    private boolean isOverlapping(Node node, Task task) {
        if (node == null) return false;

        if (overlap(node.task, task)) return true;

        if (node.left != null && node.left.maxEnd.isAfter(task.getStartTime()))
            return isOverlapping(node.left, task);

        return isOverlapping(node.right, task);
    }

    private boolean overlap(Task a, Task b) {
        return !a.getEndTime().isBefore(b.getStartTime()) && !b.getEndTime().isBefore(a.getStartTime());
    }

    public List<Task> getAllTasks() {
        List<Task> result = new ArrayList<>();
        inorder(root, result);
        return result;
    }

    private void inorder(Node node, List<Task> result) {
        if (node == null) return;
        inorder(node.left, result);
        result.add(node.task);
        inorder(node.right, result);
    }
}
