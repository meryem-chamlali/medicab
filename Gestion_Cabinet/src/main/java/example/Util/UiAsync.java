package example.Util;

import javafx.concurrent.Task;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class UiAsync {

    private UiAsync() {}

    public static <T> void run(Supplier<T> supplier, Consumer<T> onSuccess, Consumer<Exception> onError) {
        Task<T> task = new Task<>() {
            @Override
            protected T call() throws Exception {
                return supplier.get();
            }
        };
        task.setOnSucceeded(ev -> {
            try {
                onSuccess.accept(task.getValue());
            } catch (Exception ex) {
                onError.accept(ex);
            }
        });
        task.setOnFailed(ev -> onError.accept((Exception) task.getException()));
        new Thread(task, "db-async").start();
    }

    public static void runVoid(Runnable work, Runnable onSuccess, Consumer<Exception> onError) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                work.run();
                return null;
            }
        };
        task.setOnSucceeded(ev -> onSuccess.run());
        task.setOnFailed(ev -> onError.accept((Exception) task.getException()));
        new Thread(task, "db-async").start();
    }
}
