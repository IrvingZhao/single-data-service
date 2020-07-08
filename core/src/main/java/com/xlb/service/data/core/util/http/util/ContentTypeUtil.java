package com.xlb.service.data.core.util.http.util;

import org.apache.hc.core5.http.ContentType;

import java.io.IOException;
import java.util.Properties;

public class ContentTypeUtil {

    private static final Properties typeProps = new Properties();

    static {
        try {
            typeProps.load(ContentTypeUtil.class.getResourceAsStream("/http.mime.types.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ContentType getContentType(String fileName) {
        String[] nameArr = fileName.split("\\.");
        String prefix = nameArr[nameArr.length - 1];
        return ContentType.create(typeProps.getProperty("." + prefix, "application/octet-stream"));
    }

}
