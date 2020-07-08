package com.xlb.service.data.core.manager;

import lombok.RequiredArgsConstructor;

import java.util.TimerTask;

@RequiredArgsConstructor
public class RefreshTask extends TimerTask {

    private final String name;
    private final SingleDataManager dataManager;

    @Override
    public void run() {
        dataManager.refresh(name);
    }
}
