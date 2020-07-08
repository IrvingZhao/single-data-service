import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Demo {
    public static void main(String[] args) {
        var lock = new ReentrantReadWriteLock(false);
        lock.readLock().unlock();
        lock.readLock().unlock();
    }
}
