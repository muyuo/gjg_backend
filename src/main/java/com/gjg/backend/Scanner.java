package com.gjg.backend;

import com.gjg.backend.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Stack;

@Component
public class Scanner{

    private static final Logger log = LoggerFactory.getLogger(Scanner.class);
    private static Stack<Task> taskQueue = new Stack<>();

    @Scheduled(fixedRate = 1000)
    public void scanTasks() {
        while (!taskQueue.empty()) {
            Task task = taskQueue.pop();
            log.info("Task found. Task name: " + task.taskName);
            task.givenTask.accept(task.parameter);
        }
    }

    public static void addTask(Task task) {
        taskQueue.push(task);
    }
}
