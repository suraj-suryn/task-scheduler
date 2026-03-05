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
    public boolean hasCycle(Task newTask, List<Task> tasks) {

        if (newTask.getDependencies() == null || newTask.getDependencies().isEmpty()) {
            return false;
        }

        String[] deps = newTask.getDependencies().split(",");

        for (String dep : deps) {

            Long depId = Long.parseLong(dep.trim());

            if (depId.equals(newTask.getId())) {
                return true;
            }

            for (Task t : tasks) {
                if (t.getId().equals(depId) &&
                        t.getDependencies() != null &&
                        t.getDependencies().contains(String.valueOf(newTask.getId()))) {

                    return true;
                }
            }
        }

        return false;
    }

}
