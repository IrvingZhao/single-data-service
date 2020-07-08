package com.xlb.service.data.client.remote;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageResponse {
    private String code;
    private String msg;
    private String data;
}
