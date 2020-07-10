package com.xlb.service.data.client.remote;

import com.xlb.service.data.client.util.base.ObjectStringSerialUtil;
import com.xlb.service.data.client.util.http.message.HttpMessage;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.ContentType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class MessageRequest implements HttpMessage {
    private static final ObjectStringSerialUtil SERIAL_UTIL = ObjectStringSerialUtil.getSerialUtil();

    public MessageRequest(String baseUrl, String keyword, String oldData) {
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(baseUrl).append("/info/").append(keyword);
        if (oldData != null && !oldData.equals("")) {
            urlBuilder.append("/").append(oldData);
        }
        this.requestUrl = urlBuilder.toString();
    }

    @Getter
    private final String requestUrl;
    @Setter
    private int responseCode;
    @Getter
    private MessageResponse response;

    @Override
    public Map<String, String> getRequestHead() {
        Map<String, String> head = new HashMap<>();
        head.put("accept", ContentType.APPLICATION_JSON.toString());
        return head;
    }

    @Override
    public void setResponseStream(InputStream inputStream) {
        if (responseCode == 200) {
            this.response = SERIAL_UTIL.parse(inputStream, MessageResponse.class, ObjectStringSerialUtil.SerialType.JSON);
        } else {
            log.error("request data error, [{}]", streamToString(inputStream));
        }
    }

    private String streamToString(InputStream stream) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        StringBuilder builder = new StringBuilder();
        String tmp = null;
        while (true) {
            try {
                if ((tmp = reader.readLine()) == null) break;
            } catch (IOException e) {
                e.printStackTrace();
            }
            builder.append(tmp);
        }
        return builder.toString();
    }
}
