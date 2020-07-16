package com.xlb.service.data.client.util.http;

import com.xlb.service.data.client.util.http.config.ClientConfig;
import com.xlb.service.data.client.util.http.enums.HttpMethod;
import com.xlb.service.data.client.util.http.enums.KeyStoreType;
import com.xlb.service.data.client.util.http.enums.RequestType;
import com.xlb.service.data.client.util.http.message.HttpMessage;
import com.xlb.service.data.client.util.http.util.ContentTypeUtil;
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
import org.apache.hc.client5.http.ssl.TrustAllStrategy;
import org.apache.hc.client5.http.ssl.TrustSelfSignedStrategy;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.InputStreamEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.http.ssl.TLS;
import org.apache.hc.core5.ssl.SSLContexts;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.security.*;
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
    public void sendMessage(HttpMessage message) {
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

    private void generateSSLFactory1(HttpClientBuilder clientBuilder) {
        try {
            KeyStore clientStore = KeyStore.getInstance("PKCS12");
//            InputStream instream = new FileInputStream(new File(PFX_PATH));
            InputStream instream = HttpClient.class.getClassLoader().getResourceAsStream("client.p12");
            clientStore.load(instream, "client".toCharArray());

            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
//             初始化客户端密钥库
            kmf.init(clientStore, "client".toCharArray());
//            KeyManager[] kms = kmf.getKeyManagers();
//             创建信任库管理工厂实例

            KeyStore trustStore = KeyStore.getInstance("JKS");
            InputStream trustStream = HttpClient.class.getClassLoader().getResourceAsStream("server.jks");
            trustStore.load(trustStream, "server".toCharArray());
//            trustStore.load(new FileInputStream("D:\\tools\\jdk-11\\lib\\security\\cacerts"), "changeit".toCharArray());
            TrustManagerFactory tmf = TrustManagerFactory
                    .getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(trustStore);

//            SSLContext sslContext = SSLContexts.custom()
//                    .loadKeyMaterial(clientStore,"client".toCharArray())
//                    .loadTrustMaterial(trustStore, new TrustSelfSignedStrategy()).build();


            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());

            final SSLConnectionSocketFactory sslSocketFactory = SSLConnectionSocketFactoryBuilder.create()
                    .setSslContext(sslContext)
                    .setTlsVersions(TLS.V_1_2)
                    .build();

            final HttpClientConnectionManager cm = PoolingHttpClientConnectionManagerBuilder.create()
                    .setSSLSocketFactory(sslSocketFactory)
                    .build();
            clientBuilder.setConnectionManager(cm);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void generateSSLFactory(HttpClientBuilder clientBuilder) {
        try {
            var clientStore = this.createStore(this.config.getClientStore(), this.config.getClientStoreKey(), this.config.getClientType());
            var trustStore = this.createStore(this.config.getTrustStore(), this.config.getTrustStoreKey(), this.config.getTrustType());
            var contextBuilder = SSLContexts.custom();
            if (clientStore != null) {
                contextBuilder.loadKeyMaterial(clientStore, this.config.getClientStoreKey().toCharArray());
            }
            if (trustStore != null) {
                contextBuilder.loadTrustMaterial(trustStore, new TrustSelfSignedStrategy());
            }
            var sslSocketFactory = SSLConnectionSocketFactoryBuilder.create()
                    .setSslContext(contextBuilder.build()).build();
            var clientConnectManager = PoolingHttpClientConnectionManagerBuilder.create()
                    .setSSLSocketFactory(sslSocketFactory)
                    .build();
            clientBuilder.setConnectionManager(clientConnectManager);
        } catch (NoSuchAlgorithmException | KeyStoreException | UnrecoverableKeyException | KeyManagementException e) {
            e.printStackTrace();
        }
    }

    private KeyStore createStore(String keyPath, String keyPass, KeyStoreType type) {
        if (keyPath == null || "".equals(keyPath)) {
            return null;
        }
        var keyStream = getStream(keyPath);
        if (keyStream == null) {
            return null;
        }
        try {
            var keyStore = KeyStore.getInstance(type.name());
            keyStore.load(keyStream, keyPass.toCharArray());
            return keyStore;
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException e) {
            e.printStackTrace();
        }
        return null;
    }

    private InputStream getStream(String path) {
        try {
            InputStream keyStream;
            if (path.contains("classpath:")) {
                keyStream = HttpClient.class.getClassLoader().getResourceAsStream(path.replace("classpath:", ""));
            } else {
                keyStream = new FileInputStream(path);
            }
            return keyStream;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
