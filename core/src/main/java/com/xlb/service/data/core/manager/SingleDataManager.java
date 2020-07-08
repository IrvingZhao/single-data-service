package com.xlb.service.data.core.manager;

import com.xlb.service.data.constant.Constant;
import com.xlb.service.data.core.data.SingleDataFactory;
import com.xlb.service.data.core.data.SingleDataInfo;
import com.xlb.service.data.core.exception.RefreshParamException;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class SingleDataManager {

    private static final String REFRESH_CHANNEL_PREFIX = Constant.SINGLE_DATA_UPDATE_CHANNEL_PREFIX;

    @Getter
    @Setter
    private SingleDataFactory dataFactory;
    @Getter
    @Setter
    private StringRedisTemplate stringRedisTemplate;

    private final Timer refreshTimer = new Timer();
    private final Map<String, RefreshTask> taskMap = new HashMap<>();
    private final Map<String, SingleDataInfo> dataInfoMap = new ConcurrentHashMap<>();
    private final Map<String, ReadWriteLock> lockMap = new ConcurrentHashMap<>();

    // TODO 添加更新数据方法
    // TODO dataInfo / configInfo 缓存逻辑调整

    public void refresh(String name) {
        var lock = lockMap.computeIfAbsent(name, (k) -> new ReentrantReadWriteLock(false));
        if (lock.writeLock().tryLock()) { // TODO 当有正在读取内容时，写锁无法获取的问题
            try {
                var localData = dataInfoMap.computeIfAbsent(name, dataFactory::getSingleData);
                if (localData == null) { // 未找到配置信息，直接返回
                    return;
                }
                var expireTime = this.execRefresh(name, localData); // 执行刷新
                this.publishEvent(name, localData); // 推送刷新成功的消息
                if (expireTime > 0) {
                    // 如果下次刷新时间大于0，需要刷新，设置单次执行的任务
                    // TODO 定时任务无法多次添加
                    // TODO 逻辑调整，获取任务，如果任务未添加，则添加，如果下一次执行时间大于过期时间，则重新放入定时任务
                    var refreshTask = taskMap.computeIfAbsent(name, (k) -> new RefreshTask(k, this));
                    refreshTimer.schedule(refreshTask, (expireTime - 120) * 1000);
                }
            } finally {
                lock.writeLock().unlock();
            }
        } else {
            lock.readLock().lock();
            lock.readLock().unlock();
        }
    }

    public String getData(String name) {
        var lock = lockMap.computeIfAbsent(name, (k) -> new ReentrantReadWriteLock(false));
        lock.readLock().lock();
        try {
            var localData = dataInfoMap.computeIfAbsent(name, dataFactory::getSingleData);
            if (localData == null) {
                return null;
            } else {
                if (StringUtils.isNotBlank(localData.getData())) {
                    return localData.getData();
                }
            }
        } finally {
            lock.readLock().unlock();
        }
        this.refresh(name);
        return this.getData(name);
    }

    private int execRefresh(String name, SingleDataInfo dataInfo) {
        try {
            return dataInfo.refresh();
        } catch (RefreshParamException e) {
            var newDataInfo = dataFactory.getSingleData(name);
            if (!dataInfo.equals(newDataInfo)) {
                dataInfo.setAll(newDataInfo);
                return this.execRefresh(name, dataInfo);
            }
            return -1;
        }
    }

    private void publishEvent(String name, SingleDataInfo dataInfo) {
        // 推送消息
        stringRedisTemplate.convertAndSend(REFRESH_CHANNEL_PREFIX + name, dataInfo.getData());
    }


}
