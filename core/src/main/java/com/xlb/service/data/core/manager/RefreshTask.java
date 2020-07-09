package com.xlb.service.data.core.manager;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.TimerTask;

@AllArgsConstructor
public class RefreshTask extends TimerTask {

    private final String name;
    private final SingleDataManager dataManager;
    @Getter
    @Setter
    private String oldData;

    @Override
    public void run() {
        var managerData = dataManager.getData(name);
        if (managerData.equals(oldData)) {
            dataManager.refresh(name);
        } else {
            this.oldData = managerData;
        }
    }
}
