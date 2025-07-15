package app.util;

public class Sleep {
    public static void sleepSafely(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Thread was interrupted, failed to complete operation");
        }
    }
}
