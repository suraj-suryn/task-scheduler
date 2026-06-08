package com.suraj.scheduler.dsa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.suraj.scheduler.entity.Task;

public class DependencyGraph {

    private final Map<Long, List<Long>> adjList = new HashMap<>();

    public void addDependency(Long taskId, Long dependsOnId) {
        adjList.putIfAbsent(taskId, new ArrayList<>());
        adjList.get(taskId).add(dependsOnId);
    }

    public List<Long> topologicalSort(Set<Long> allTaskIds) {

        List<Long> result = new ArrayList<>();
        Set<Long> visited = new HashSet<>();
        Set<Long> onStack = new HashSet<>();

        for (Long id : allTaskIds) {
            if (!visited.contains(id)) {
                if (!dfs(id, visited, onStack, result)) {
                    throw new RuntimeException("Cycle detected in dependencies!");
                }
            }
        }

        Collections.reverse(result);
        return result;
    }

    private boolean dfs(Long id,
                        Set<Long> visited,
                        Set<Long> onStack,
                        List<Long> result) {

        visited.add(id);
        onStack.add(id);

        List<Long> neighbors = adjList.getOrDefault(id, Collections.emptyList());

        for (Long neighborId : neighbors) {

            if (!visited.contains(neighborId)) {
                if (!dfs(neighborId, visited, onStack, result))
                    return false;
            }
            else if (onStack.contains(neighborId)) {
                return false; // cycle detected
            }
        }

        onStack.remove(id);
        result.add(id);
        return true;
    }
    public boolean hasCycle(Task newTask, List<Task> existingTasks) {

        if (newTask.getDependencies() == null || newTask.getDependencies().isEmpty()) {
            return false;
        }

        // Build a temporary adjacency list including the new task
        Map<Long, List<Long>> tempAdj = new HashMap<>();

        for (Task t : existingTasks) {
            if (t.getDependencies() != null && !t.getDependencies().isEmpty()) {
                List<Long> depIds = new ArrayList<>();
                for (String dep : t.getDependencies().split(",")) {
                    try { depIds.add(Long.parseLong(dep.trim())); } catch (NumberFormatException ignored) {}
                }
                tempAdj.put(t.getId(), depIds);
            }
        }

        // Add the new task's dependencies (use -1L as temporary ID if not yet saved)
        Long newId = newTask.getId() != null ? newTask.getId() : -1L;
        List<Long> newDeps = new ArrayList<>();
        for (String dep : newTask.getDependencies().split(",")) {
            try { newDeps.add(Long.parseLong(dep.trim())); } catch (NumberFormatException ignored) {}
        }
        tempAdj.put(newId, newDeps);

        // DFS cycle detection over the full graph
        Set<Long> visited = new HashSet<>();
        Set<Long> onStack = new HashSet<>();

        for (Long id : tempAdj.keySet()) {
            if (!visited.contains(id)) {
                if (dfsCycleCheck(id, tempAdj, visited, onStack)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean dfsCycleCheck(Long id, Map<Long, List<Long>> adj,
                                   Set<Long> visited, Set<Long> onStack) {
        visited.add(id);
        onStack.add(id);

        for (Long neighbor : adj.getOrDefault(id, Collections.emptyList())) {
            if (!visited.contains(neighbor)) {
                if (dfsCycleCheck(neighbor, adj, visited, onStack)) return true;
            } else if (onStack.contains(neighbor)) {
                return true;
            }
        }

        onStack.remove(id);
        return false;
    }

}
