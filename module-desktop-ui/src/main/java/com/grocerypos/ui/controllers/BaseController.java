package com.grocerypos.ui.controllers;

import javafx.concurrent.Task;
import javafx.application.Platform;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Lớp cơ sở cho các Controller JavaFX.
 * Cung cấp tiện ích chạy tác vụ nền.
 */
public abstract class BaseController {

    protected <T> void runInBackground(
            Supplier<T> backgroundTask,
            Consumer<T> onSuccess,
            Consumer<Throwable> onError) {

        Task<T> task = new Task<>() {
            @Override protected T call() { return backgroundTask.get(); }
        };

        task.setOnSucceeded(e -> onSuccess.accept(task.getValue()));
        task.setOnFailed(e -> onError.accept(task.getException()));

        new Thread(task).start();
    }
}
