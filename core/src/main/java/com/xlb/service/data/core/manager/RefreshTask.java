package com.xlb.service.data.core.manager;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.TimerTask;

/**
 * 刷新任务
 */
@RequiredArgsConstructor
public class RefreshTask extends TimerTask {

    private final String name;
    private final SingleDataManager dataManager;
    @Getter
    @Setter
    private String oldData;

    @Override
    public void run() {
        dataManager.refresh(name, oldData);
    }
}
