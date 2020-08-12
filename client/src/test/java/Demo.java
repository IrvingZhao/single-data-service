import com.xlb.service.data.client.remote.MessageRequest;
import com.xlb.service.data.client.util.http.HttpClient;
import com.xlb.service.data.client.util.http.config.ClientConfig;
import com.xlb.service.data.client.util.http.enums.KeyStoreType;
import com.xlb.service.data.client.util.http.message.HttpMessage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;

public class Demo {
    public static void main(String[] args) {
//        System.out.println(MessageFormat.format("/demo/{0}{1,/#}", "1111"));
//        System.setProperty("javax.net.debug", "all");
//        TestRequest();
        testP12();
    }

    public static void testP12() {
        MessageRequest request = new MessageRequest("https://single.xiuleba.com.cn/index.html", "", "");
        var config = ClientConfig.builder()
                .clientType(KeyStoreType.PKCS12)
                .clientStore("classpath:message.p12")
                .clientStoreKey("message")
                .trustType(KeyStoreType.JKS)
                .trustStore("classpath:server.jks")
                .trustStoreKey("server").build();

        HttpClient client = new HttpClient(config);
        client.sendMessage(request);
        var response = request.getResponse();
        System.out.println(response);
//        System.out.println(response.getData());
    }

    public static void normal() {
        DemoMessage request = new DemoMessage("http://www.baidu.com");
        var config = ClientConfig.builder().charset(StandardCharsets.UTF_8).build();
        var client = new HttpClient(config);
        client.sendMessage(request);
    }

    @Getter
    @RequiredArgsConstructor
    public static class DemoMessage implements HttpMessage {
        private final String requestUrl;

        @Override
        public void setResponseStream(InputStream inputStream) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String tmp;
                while ((tmp = reader.readLine()) != null) {
                    System.out.println(tmp);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public static void TestRequest() {
//        MessageRequest request = new MessageRequest("https://127.0.0.1/data", "demo-wechat", "");
//        ClientConfig config = new ClientConfig();
//        config.setCertificateKey("token-client")
//                .setCertificatePath("classpath:sslClient.p12")
//                .setCertificateType(KeyStoreType.PKCS12);
//
//        HttpClient client = new HttpClient(config);
//        client.sendMessage(request);
//        var response = request.getResponse();
//        System.out.println(response);
//        System.out.println(response.getData());
    }
}
