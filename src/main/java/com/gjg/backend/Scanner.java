package com.gjg.backend;

import com.gjg.backend.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.Queue;

@Component
public class Scanner{

    private static final Logger log = LoggerFactory.getLogger(Scanner.class);
    private static Queue<Task> taskQueue = new LinkedList<>();

    /**
     *
     * Scan the tasks every 1 second. And run the tasks in fifo order.
     * These tasks are used for write data to mysql.
     *
     */
    @Scheduled(fixedRate = 1000)
    public void scanTasks() {
        while (!taskQueue.isEmpty()) {
            Task task = taskQueue.poll();
            log.info("Task found. Task name: " + task.taskName);
            task.givenTask.accept(task.parameter);
        }
    }

    public static void addTask(Task task) {
        taskQueue.offer(task);
    }
}
