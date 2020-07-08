import java.util.Scanner;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        ReadWriteLock lock = new ReentrantReadWriteLock(false);

        new Thread(() -> {
            try {
                Thread.sleep(300L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("try write lock");
            var lockResult = lock.writeLock().tryLock();
            System.out.println("write lock " + lockResult);
            lock.writeLock().unlock();
            System.out.println("write lock finished");
        }).start();

        lock.readLock().lock();
        System.out.println("read - lock");
        Scanner scanner = new Scanner(System.in);
        scanner.next();
        lock.readLock().unlock();
    }
}
