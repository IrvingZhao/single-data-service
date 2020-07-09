package com.xlb.service.data.core.manager;

import com.xlb.service.data.constant.Constant;
import com.xlb.service.data.core.data.SingleDataFactory;
import com.xlb.service.data.core.data.SingleDataInfo;
import com.xlb.service.data.core.exception.RefreshParamException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.*;

@Slf4j
public class SingleDataManager implements InitializingBean {

    private static final String REFRESH_CHANNEL_PREFIX = Constant.SINGLE_DATA_UPDATE_CHANNEL_PREFIX;

    @Getter
    @Setter
    private SingleDataFactory dataFactory;
    @Getter
    @Setter
    private StringRedisTemplate stringRedisTemplate;

    private final Timer refreshTimer = new Timer();
    private final Map<String, RefreshTask> taskMap = new HashMap<>();
    private final Map<String, String> dataInfoMap = new ConcurrentHashMap<>();
    private final Map<String, ReadWriteLock> lockMap = new ConcurrentHashMap<>();

    public void afterPropertiesSet() throws Exception {
        stringRedisTemplate.execute((RedisConnection con) -> {
            var option = ScanOptions.scanOptions().match(Constant.DATA_DATA_PREFIX + "*").build();
            var serializer = StringRedisSerializer.UTF_8;
            con.scan(option).forEachRemaining(keys -> {
                var key = serializer.deserialize(keys);
                if (key != null) {
                    var name = key.replace(Constant.DATA_DATA_PREFIX, "");
                    var value = serializer.deserialize(con.stringCommands().get(keys));
                    dataInfoMap.put(name, value);
                }
            });
            return null;
        });
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
        this.refresh(name);
        return dataInfoMap.get(name);
    }

    public void refresh(String name) {
        var lock = lockMap.computeIfAbsent(name, (k) -> new ReentrantReadWriteLock(false));
        try {
            if (lock.writeLock().tryLock(18, TimeUnit.MILLISECONDS)) { // 第一次进入，锁失败场景
                try {
                    this.refreshDataTask(name);
                } finally {
                    lock.writeLock().unlock();
                }
            } else {
                lock.readLock().lock();
                lock.readLock().unlock();
            }
        } catch (InterruptedException e) {
            log.error("try write log error", e);
        }
    }

    // 实际刷新数据
    private void refreshDataTask(String name) {
        long start = System.currentTimeMillis();
        var localData = dataFactory.getSingleData(name);
        var refreshTask = taskMap.computeIfAbsent(name, (k) -> new RefreshTask(k, this, dataInfoMap.get(name)));
        if (localData == null) {
            dataInfoMap.remove(name); // 清空数据
            refreshTask.cancel();
            return;
        }
        var expireTime = this.refreshDataInfo(name, localData);
        var data = localData.getData();
        this.refreshCache(name, data, expireTime - 120);
        this.publishEvent(name, data);
        if (expireTime > 0) {
            var delay = (expireTime - 120) * 1000;
            if (refreshTask.scheduledExecutionTime() > delay) {
                refreshTask.cancel();
                refreshTask = taskMap.compute(name, (k, v) -> new RefreshTask(k, this, dataInfoMap.get(name)));
            }

            if (refreshTask.scheduledExecutionTime() == 0) {
                // 新增定时任务
                refreshTimer.schedule(refreshTask, delay, delay);
            }
        } else {
            refreshTask.cancel();
        }
        long end = System.currentTimeMillis();
        if (end - start > 40) { // 刷新任务执行市场，需大于 获取write lock 等待时长的两倍，并且刷新之前，都需要执行get，并进行新老比对
            log.error("refresh task less than 25 millisecond");
            try {
                Thread.sleep(40L);
            } catch (InterruptedException e) {
                log.error("sleep error", e);
            }
        }
    }

    private int refreshDataInfo(String name, SingleDataInfo dataInfo) {
        try {
            return dataInfo.refresh();
        } catch (RefreshParamException e) {
            var newDataInfo = dataFactory.getSingleData(name);
            if (!dataInfo.equals(newDataInfo)) {
                dataInfo.setAll(newDataInfo);
                return this.refreshDataInfo(name, dataInfo);
            }
            return -1;
        }
    }

    private void refreshCache(String name, String data, int expireSecond) {
        this.dataInfoMap.put(name, data);
        this.stringRedisTemplate.opsForValue().set(Constant.DATA_DATA_PREFIX + name, data, Duration.ofSeconds(expireSecond));
    }

    private void publishEvent(String name, String data) {
        // 推送消息
        stringRedisTemplate.convertAndSend(REFRESH_CHANNEL_PREFIX + name, data);
    }
}
