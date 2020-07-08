package com.xlb.service.data.client.remote;

import com.xlb.service.data.client.util.base.ObjectStringSerialUtil;
import com.xlb.service.data.client.util.http.message.HttpMessage;
import lombok.Getter;
import lombok.Setter;
import org.apache.hc.core5.http.ContentType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

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
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String temp = null;
        StringBuilder builder = new StringBuilder();
        while (true) {
            try {
                if ((temp = reader.readLine()) == null) break;
            } catch (IOException e) {
                e.printStackTrace();
            }
            builder.append(temp);
        }
        System.out.println(builder.toString());
        if (responseCode == 200) {
            this.response = SERIAL_UTIL.parse(builder.toString(), MessageResponse.class, ObjectStringSerialUtil.SerialType.JSON);
        }
    }
}
