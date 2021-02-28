package com.gjg.backend.model;

import java.util.List;
import java.util.function.Consumer;

public class Task {
    public String taskName;
    public Object parameter;
    public Consumer<Object> givenTask;

    public Task(Object parameter, Consumer<Object> givenTask) {
        this.parameter = parameter;
        this.givenTask = givenTask;
    }

    public Task(Object parameter, Consumer<Object> givenTask, String taskName) {
        this.parameter = parameter;
        this.givenTask = givenTask;
        this.taskName = taskName;
    }
}
