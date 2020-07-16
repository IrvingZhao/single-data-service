package com.xlb.service.data.client.util.http.config;

import com.xlb.service.data.client.util.http.enums.KeyStoreType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.nio.charset.Charset;

@Getter
@Setter
@Builder
public class ClientConfig {
    private Charset charset;
    private KeyStoreType clientType;
    private String clientStore;
    private String clientStoreKey;

    private KeyStoreType trustType;
    private String trustStore;
    private String trustStoreKey;
}
