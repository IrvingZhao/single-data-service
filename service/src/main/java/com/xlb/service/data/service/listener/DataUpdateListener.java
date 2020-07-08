package com.xlb.service.data.service.listener;

import com.xlb.service.data.service.service.DataInfoService;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Component
public class DataUpdateListener implements MessageListener {

    private final DataInfoService infoService;

    public DataUpdateListener(DataInfoService infoService) {
        this.infoService = infoService;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        var channel = new String(message.getChannel());
        var body = new String(message.getBody());
        var keyword = channel.replace("single_data_refresh_", "");
        infoService.updateDataByKeyword(keyword, body);
    }
}
