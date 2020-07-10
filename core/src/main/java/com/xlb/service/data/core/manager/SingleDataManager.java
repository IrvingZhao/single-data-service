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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.*;

/**
 * 单数据管理器
 */
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

    /**
     * 初始化数据
     */
    public void afterPropertiesSet() {
        log.info("init single data manager");
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

    /**
     * 获取数据
     *
     * @param name 配置名
     */
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
        this.refresh(name, "");
        return dataInfoMap.get(name);
    }

    /**
     * 刷新数据
     *
     * @param name    数据名
     * @param oldData 原数据信息
     */
    public void refresh(String name, String oldData) {
        var threadName = Thread.currentThread().getName();
        log.info("refresh data start, [{}]", name);
        if (!validOldData(name, oldData)) { // check old-data is equals cur data
            return;
        }
        var lock = lockMap.computeIfAbsent(name, (k) -> new ReentrantReadWriteLock(false));
        try {
            if (lock.writeLock().tryLock(18, TimeUnit.MILLISECONDS)) { // 第一次进入，锁失败场景
                log.info("refresh data exec, [{}]", name);
                try {
                    this.refreshDataTask(name);
                } finally {
                    lock.writeLock().unlock();
                }
                log.info("refresh data exec finished, [{}]", name);
            } else {
                log.info("refresh data wait, [{}]", name);
                lock.readLock().lock();
                lock.readLock().unlock();
                log.info("refresh data wait end, [{}]", name);
            }
        } catch (InterruptedException e) {
            log.error("try write lock error", e);
        }
    }

    /**
     * 校验原数据
     */
    private boolean validOldData(String name, String oldData) {
        var lock = lockMap.computeIfAbsent(name, (k) -> new ReentrantReadWriteLock(false));
        lock.readLock().lock();
        try {
            var mapData = this.dataInfoMap.get(name);
            return StringUtils.isBlank(mapData) || StringUtils.equals(mapData, oldData);
        } finally {
            lock.readLock().unlock();
        }
    }

    // 实际刷新数据
    private void refreshDataTask(String name) {
        long start = System.currentTimeMillis();
        var localData = dataFactory.getSingleData(name);
        var refreshTask = taskMap.computeIfAbsent(name, (k) -> new RefreshTask(k, this));
        if (localData == null) {
            log.error("cannot find config of [{}]", name);
            dataInfoMap.remove(name); // 清空数据
            taskMap.remove(name); // 移除 task
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
                log.error("refresh task of [{}] delay is [{}], over than [{}]", name, refreshTask.scheduledExecutionTime(), delay);
                refreshTask.cancel();
                refreshTask = taskMap.compute(name, (k, v) -> new RefreshTask(k, this));
            }
            if (refreshTask.scheduledExecutionTime() == 0) {
                // 新增定时任务
                refreshTimer.schedule(refreshTask, delay, delay);
            }

        } else {
            log.error("[{}] expire time is less than zero", name);
            refreshTask.cancel();
            taskMap.remove(name); // 移除task
            dataInfoMap.remove(name); // 移除数据
        }
        long end = System.currentTimeMillis();
        if (end - start < 40) { // 刷新任务执行市场，需大于 获取write lock 等待时长的两倍，并且刷新之前，都需要执行get，并进行新老比对
            log.error("[{}] refresh task less than 40 millisecond,{}", name, end - start);
            try {
                Thread.sleep(40L);
            } catch (InterruptedException e) {
                log.error("sleep error", e);
            }
        }
        refreshTask.setOldData(data); // 设置 old 数据
    }

    /**
     * 刷新
     *
     * @param name     数据名
     * @param dataInfo 数据配置信息
     */
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

    /**
     * 刷新缓存
     *
     * @param name         数据名
     * @param data         数据值
     * @param expireSecond 失效时间，单位，秒
     */
    private void refreshCache(String name, String data, int expireSecond) {
        this.dataInfoMap.put(name, data);
        this.stringRedisTemplate.opsForValue().set(Constant.DATA_DATA_PREFIX + name, data, Duration.ofSeconds(expireSecond));
    }

    /**
     * 推送数据更新
     *
     * @param name 数据名
     * @param data 数据值
     */
    private void publishEvent(String name, String data) {
        // 推送消息
        stringRedisTemplate.convertAndSend(REFRESH_CHANNEL_PREFIX + name, data);
    }
}
