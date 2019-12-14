import java.util.Objects;

public class Runner {
    public static void main(String[] args) {

        Thread.currentThread().setUncaughtExceptionHandler(new Handler());
        print();

    }

    static void print() {
        String s = null;
        Objects.requireNonNull(s);
    }

    static class Handler implements Thread.UncaughtExceptionHandler {
        public void uncaughtException(Thread t, Throwable e) {
            System.out.println("Unhandled exception");
        }
    }

}
