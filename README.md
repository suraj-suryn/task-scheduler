# Task Scheduler Web App

A **Task Scheduler / Calendar Application** built with **Spring Boot**, demonstrating practical usage of **Data Structures and Algorithms (DSA)** like **Heap, Graph, and Interval Tree**.  

This app allows users to:  
- Add tasks with **priority**, **start/end times**, and **dependencies**.  
- View tasks **sorted by priority or deadline**.  
- Check for **overlapping tasks**.  
- Manage **task dependencies** (tasks cannot be completed before dependent tasks).  

---

## **DSA Features**

| Feature | Data Structure / Algorithm |
|---------|---------------------------|
| Top-priority tasks | Max Heap / Priority Queue |
| Task dependencies | Graph (Adjacency List) + Topological Sort |
| Overlapping tasks | Interval Tree / Segment Tree |
| Fast task lookup | HashMap |

---

## **Tech Stack**

- **Backend:** Spring Boot (Java)  
- **Database:** H2 (in-memory, can switch to MySQL/PostgreSQL)  
- **Frontend:** Thymeleaf (optional, can use React/Angular)  
- **Build Tool:** Maven  
- **Version Control:** Git + GitHub  

---

## **Prerequisites**

- Java 17+  
- Maven or Gradle  
- Git  
- STS / IntelliJ IDEA / Eclipse  

---

## **Setup Instructions**

1. **Clone the repository**

```bash
git clone https://github.com/suraj-suryn/task-scheduler.git
cd task-scheduler
