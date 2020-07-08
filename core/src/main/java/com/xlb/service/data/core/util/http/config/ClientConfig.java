package com.xlb.service.data.core.util.http.config;

import com.xlb.service.data.core.util.http.enums.KeyStoreType;

import java.nio.charset.Charset;

public class ClientConfig {
    private String certificatePath;
    private String certificateKey;
    private KeyStoreType certificateType;
    private Charset charset;

    public ClientConfig setCertificatePath(String certificatePath) {
        this.certificatePath = certificatePath;
        return this;
    }

    public ClientConfig setCertificateKey(String certificateKey) {
        this.certificateKey = certificateKey;
        return this;
    }

    public ClientConfig setCertificateType(KeyStoreType certificateType) {
        this.certificateType = certificateType;
        return this;
    }

    public ClientConfig setCharset(Charset charset) {
        this.charset = charset;
        return this;
    }

    public String getCertificatePath() {
        return certificatePath;
    }

    public String getCertificateKey() {
        return certificateKey;
    }

    public KeyStoreType getCertificateType() {
        return certificateType;
    }

    public Charset getCharset() {
        return charset;
    }
}
