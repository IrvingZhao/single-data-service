import com.xlb.service.data.core.data.wechat.WechatTokenInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Slf4j
public class Main {
    private final Map<String, String> dataInfoMap = new ConcurrentHashMap<>();
    private final Map<String, ReadWriteLock> lockMap = new ConcurrentHashMap<>();

    public static void main(String[] args) throws InterruptedException {
        WechatTokenInfo info = new WechatTokenInfo();
        Map<String, String> config = new HashMap<>();
        config.put("appId", "wx192234b0ea230461");
        config.put("appSecurity", "b6b68e578ef6591fab14fdbdc123e3c5");
        info.init(config);
        long start = System.currentTimeMillis();
        info.refresh();
        long end = System.currentTimeMillis();
        System.out.println(end - start);
        start = System.currentTimeMillis();
        info.refresh();
        end = System.currentTimeMillis();
        System.out.println(end - start);
//        this.appId = config.get("appId");
//        this.appSecurity = config.get("appSecurity");
//        Main me = new Main();
//        me.dataInfoMap.put("demo-wechat", "123");
//        StringBuilder builder = new StringBuilder();
//        long start = System.currentTimeMillis();
//        for (int i = 0; i < 100; i++) {
//            String s1 = me.getData("demo-wechat"+i);
//            builder.append(s1);
//        }
//        long end = System.currentTimeMillis();
//        System.out.println(end - start);
//        System.out.println(builder);

//        ReadWriteLock lock = new ReentrantReadWriteLock(false);
//        Runnable run1 = () -> {
//            while (true) {
//                lock.readLock().lock();
//                try {
//                    System.out.println("read lock");
//                    Thread.sleep(10L);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                lock.readLock().unlock();
//                System.out.println("read un lock");
//            }
//        };
//        ExecutorService es = Executors.newFixedThreadPool(10);
//        es.submit(run1);
//        for (int i = 0; i < 4; i++) {
//            es.submit(buildTask(lock, "Thread" + i));
//        }
    }

    public String getData(String name) {
        var lock = lockMap.computeIfAbsent(name, (k) -> new ReentrantReadWriteLock(false));
        try {
            lock.readLock().lock();
            var data = dataInfoMap.get(name);
            if (StringUtils.isNotBlank(data)) {
                return data;
            }
        } finally {
            lock.readLock().unlock();
        }
//        this.refresh(name);
        return dataInfoMap.get(name);
    }

    static Runnable buildTask(ReadWriteLock lock, String name) {
        return () -> {
            try {
                log.info("thread start [{}] ", name);
                var res = lock.writeLock().tryLock(1500, TimeUnit.MILLISECONDS);
                log.info("get lock result [{}]", res);
                if (res) {
                    log.info("thread hasLock [{}] ", name);
//                    lock.writeLock().lockInterruptibly(); // 中断其他线程
                    Thread.sleep(2000L);
                    lock.writeLock().unlock();
                    log.info("thread unlock write [{}]", name);
                } else {
                    log.info("thread lock failed [{}]", name);
                    lock.readLock().lock();
                    lock.readLock().unlock();
                    log.info("exception read un lock");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.info("thread finished [{}] ", name);
        };
    }
}
