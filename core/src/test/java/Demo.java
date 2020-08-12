import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Demo {
    public static void main(String[] args) {
//        var lock = new ReentrantReadWriteLock(false);
//        lock.readLock().lock();
//        lock.writeLock().lock();
        System.out.println(null instanceof Integer);
        int i = 1;
        test(i);
    }

    public static void test(Object a) {
        System.out.println(a instanceof Integer);
    }
}
