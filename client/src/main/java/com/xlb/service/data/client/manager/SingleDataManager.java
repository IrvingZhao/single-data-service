package com.xlb.service.data.client.manager;

import com.xlb.service.data.client.remote.MessageRequest;
import com.xlb.service.data.client.remote.MessageResponse;
import com.xlb.service.data.client.util.http.HttpClient;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@RequiredArgsConstructor
@Slf4j
public class SingleDataManager {
    private final HttpClient client;

    private final String remoteUrl;

    private Map<String, ReadWriteLock> lockMap = new ConcurrentHashMap<>();
    private Map<String, String> dataMap = new HashMap<>();

    /**
     * 强制刷新数据
     */
    public void reloadData(String name) {
        var lock = lockMap.computeIfAbsent(name, (k) -> new ReentrantReadWriteLock(true));
        if (lock.writeLock().tryLock()) {
            var remoteData = this.getRemoteData(name);
            dataMap.put(name, remoteData);
        } else {
            lock.readLock().lock();
            lock.readLock().unlock();
        }
    }

    /**
     * 获取数据
     */
    public String getData(String name) {
        var lock = lockMap.computeIfAbsent(name, (k) -> new ReentrantReadWriteLock(true));
        try {
            lock.readLock().lock();
            if (dataMap.containsKey(name)) {
                return dataMap.get(name);
            }
        } finally {
            lock.readLock().unlock();
        }
        this.reloadData(name);
        return this.getData(name);
    }

    public void updateData(String name, String data) {
        var lock = lockMap.computeIfAbsent(name, (k) -> new ReentrantReadWriteLock(true));
        if (lock.writeLock().tryLock()) {
            this.dataMap.put(name, data);
            lock.writeLock().unlock();
        } else {
            lock.readLock().lock();
            lock.readLock().unlock();
        }
    }

    private String getRemoteData(String name) {
        var localData = dataMap.get(name);
        MessageRequest request = new MessageRequest(remoteUrl, name, localData);
        client.sendMessage(request);
        MessageResponse response = request.getResponse();
        if ("000000".equals(response.getCode())) {
            return response.getData();
        } else {
            log.error("refresh remote data error, reason:[{}]", response.getMsg());
            throw new RuntimeException("refresh remote data error," + response.getMsg());
        }
    }

}
