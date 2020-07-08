package com.xlb.service.data.client.listener;

import com.xlb.service.data.client.manager.SingleDataManager;
import com.xlb.service.data.constant.Constant;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;

public class SingleDataListener implements MessageListener {

    private final SingleDataManager dataManager;

    public SingleDataListener(SingleDataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        var channel = new String(message.getChannel());
        var body = new String(message.getBody());
        var key = channel.replace(Constant.SINGLE_DATA_UPDATE_CHANNEL_PREFIX, "");
        dataManager.updateData(key, body);
    }
}
