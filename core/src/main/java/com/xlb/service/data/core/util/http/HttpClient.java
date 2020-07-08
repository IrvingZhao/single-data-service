package com.xlb.service.data.core.util.http;

import com.xlb.service.data.core.util.http.enums.HttpMethod;
import com.xlb.service.data.core.util.http.enums.RequestType;
import com.xlb.service.data.core.util.http.message.HttpMessage;
import com.xlb.service.data.core.util.http.util.ContentTypeUtil;
import com.xlb.service.data.core.util.http.config.ClientConfig;
import org.apache.hc.client5.http.classic.methods.HttpUriRequest;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.client5.http.ssl.TrustSelfSignedStrategy;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.InputStreamEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.http.ssl.TLS;
import org.apache.hc.core5.ssl.SSLContexts;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.*;

/**
 * 请求发送类
 * <p>
 * 同一个对象视为同一个会话
 * </p>
 *
 * @author 赵嘉楠
 * @version 1.0
 * @since 1.0
 */
public class HttpClient {

    private CloseableHttpClient client;
    private ClientConfig config;
    private Charset charset;

    public HttpClient() {
        this(null);
    }

    public HttpClient(ClientConfig config) {
        this.config = config;
        generateClient();
    }

    public void setClientConfig(ClientConfig config) {
        this.config = config;
        generateClient();
    }

    /**
     * 根据配置信息重新生成请求客户端
     */
    private void generateClient() {
        HttpClientBuilder clientBuilder = HttpClients.custom();
        if (this.config == null) {
            this.charset = Charset.defaultCharset();
        } else {
            this.charset = this.config.getCharset();
            generateSSLFactory(clientBuilder);
        }
        this.client = clientBuilder.build();
    }

    /**
     * 发送消息
     *
     * @param message 消息对象
     */
    public void sendMessage(com.xlb.service.data.core.util.http.message.HttpMessage message) {
        try {
            HttpUriRequest request;
            HttpEntity entity = generateHttpEntity(message);
            String requestUrl = message.getRequestUrl();
            if (message.getRequestMethod() == HttpMethod.GET) {
                try {
                    String paramStr = EntityUtils.toString(entity, charset);
                    requestUrl = requestUrl + (requestUrl.contains("?") ? "&" : "?") + paramStr;
                    entity = null;
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            request = generateRequest(requestUrl, message.getRequestMethod());
            if (message.getRequestHead() != null) {
                message.getRequestHead().forEach(request::addHeader);
            }
            if (entity != null) {
                request.setEntity(entity);
            }
            CloseableHttpResponse response = client.execute(request);
            message.setResponseCode(response.getCode());
            Header[] headers = response.getHeaders();
            Map<String, String> responseHeader = new HashMap<>(headers.length);
            for (Header item : headers) {
                responseHeader.put(item.getName(), item.getValue());
            }
            message.setResponseHead(responseHeader);
            message.setResponseStream(response.getEntity().getContent());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据消息构建请求体
     */
    private HttpEntity generateHttpEntity(HttpMessage message) {
        HttpEntity result = null;
        RequestType type = message.getRequestType();
        if (message.getRequestMethod() == HttpMethod.GET) {
            result = buildUrlEncodingEntity(message.getRequestParams());
        } else if (type == RequestType.NORMAL) {
            result = buildUrlEncodingEntity(message.getRequestParams());
        } else if (type == RequestType.MULTIPART) {
            result = buildMultipartEntity(message.getRequestParams());
        } else if (type == RequestType.STREAM) {
            result = buildStreamEntity(message.getRequestStream());
        } else if (type == RequestType.STRING) {
            result = new StringEntity(message.getRequestBodyString(), ContentType.APPLICATION_JSON);
        }
        return result;
    }

    /**
     * 构建Multipart/Form 请求体
     */
    private HttpEntity buildMultipartEntity(Map<String, Object> requestParams) {
        MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
        entityBuilder.setCharset(charset);
        requestParams.forEach((key, value) -> {
            if (value == null) {
                return;
            }
            if (File.class.isAssignableFrom(value.getClass())) {
                String fileName = ((File) value).getName();
                entityBuilder.addBinaryBody(key, (File) value, ContentTypeUtil.getContentType(fileName), fileName);
            } else if (InputStream.class.isAssignableFrom(value.getClass())) {
                entityBuilder.addBinaryBody(key, (InputStream) value);
            } else if (String.class.isAssignableFrom(value.getClass())) {
                entityBuilder.addTextBody(key, (String) value, ContentType.create("text/plan", charset));
            } else {
                entityBuilder.addTextBody(key, value.toString(), ContentType.create("text/plan", charset));
            }
        });
        return entityBuilder.build();
    }

    /**
     * 构建URL请求体
     */
    private UrlEncodedFormEntity buildUrlEncodingEntity(Map<String, Object> requestParams) {
        List<NameValuePair> params = new ArrayList<>(requestParams.size());
        requestParams.forEach((key, value) -> params.add(new BasicNameValuePair(key, Objects.toString(value, ""))));
        return new UrlEncodedFormEntity(params, charset);
    }

    /**
     * 构建流请求体
     */
    private InputStreamEntity buildStreamEntity(InputStream stream) {
        return new InputStreamEntity(stream, ContentType.create("application/json", charset));
    }

    /**
     * 构建请求方法
     */
    private HttpUriRequest generateRequest(String url, HttpMethod method) {
        try {
            //            result.setUri(URI.create(url));
            return method.getMethodClass().getDeclaredConstructor(String.class).newInstance(url);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new RuntimeException("无法创建[" + method.name() + "]的请求方法", e);
        }
    }

    /**
     * 构建Https链接工厂
     */
    private void generateSSLFactory(HttpClientBuilder clientBuilder) {
        try {
            if (this.config.getCertificatePath().length() > 0) {
                KeyStore trustStore = KeyStore.getInstance(this.config.getCertificateType().name());
                InputStream keyStream;
                if (this.config.getCertificatePath().contains("classpath:")) {
                    keyStream = HttpClient.class.getClassLoader().getResourceAsStream(this.config.getCertificatePath().replace("classpath:", ""));
                } else {
                    keyStream = new FileInputStream(this.config.getCertificatePath());
                }
                trustStore.load(keyStream, this.config.getCertificateKey().toCharArray());
//                keyStream.close();
                SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(trustStore, new TrustSelfSignedStrategy()).build();

                // TODO 设置方式

                final SSLConnectionSocketFactory sslSocketFactory = SSLConnectionSocketFactoryBuilder.create()
                        .setSslContext(sslContext)
                        .setTlsVersions(TLS.V_1_2)
                        .build();

                final HttpClientConnectionManager cm = PoolingHttpClientConnectionManagerBuilder.create()
                        .setSSLSocketFactory(sslSocketFactory)
                        .build();

                clientBuilder.setConnectionManager(cm);

//                clientBuilder.setSSLSocketFactory(new SSLConnectionSocketFactory(sslContext, new String[]{"TLSv1"}, null, SSLConnectionSocketFactory.getDefaultHostnameVerifier()));
            }
        } catch (NullPointerException | KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException | KeyManagementException e) {
            e.printStackTrace();
        }
    }

}
