package com.suraj.scheduler.dsa;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import com.suraj.scheduler.entity.Task;

public class PriorityHeap {

    private List<Task> heap;
    

    private final PriorityQueue<Task> heap =
            new PriorityQueue<>((a, b) -> b.getPriority() - a.getPriority());


    public Task getNextTask() {
        return heap.poll();
    }

    public PriorityHeap() {
        heap = new ArrayList<>();
    }

    public void add(Task task) {
        heap.add(task);
        heapifyUp(heap.size() - 1);
    }

    public Task poll() {
        if (heap.isEmpty()) return null;
        Task top = heap.get(0);
        Task last = heap.remove(heap.size() - 1);
        if (!heap.isEmpty()) {
            heap.set(0, last);
            heapifyDown(0);
        }
        return top;
    }

    public Task peek() {
        return heap.isEmpty() ? null : heap.get(0);
    }

    public boolean isEmpty() {
        return heap.isEmpty();
    }

    public int size() {
        return heap.size();
    }

    private void heapifyUp(int index) {
        while (index > 0) {
            int parent = (index - 1) / 2;
            if (heap.get(index).getPriority() > heap.get(parent).getPriority()) {
                swap(index, parent);
                index = parent;
            } else break;
        }
    }

    private void heapifyDown(int index) {
        int left, right, largest;
        while (true) {
            left = 2 * index + 1;
            right = 2 * index + 2;
            largest = index;

            if (left < heap.size() && heap.get(left).getPriority() > heap.get(largest).getPriority())
                largest = left;
            if (right < heap.size() && heap.get(right).getPriority() > heap.get(largest).getPriority())
                largest = right;

            if (largest != index) {
                swap(index, largest);
                index = largest;
            } else break;
        }
    }

    private void swap(int i, int j) {
        Task temp = heap.get(i);
        heap.set(i, heap.get(j));
        heap.set(j, temp);
    }

    public List<Task> getAllTasks() {
        return new ArrayList<>(heap);
    }
}
