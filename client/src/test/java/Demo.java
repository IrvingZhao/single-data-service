import com.xlb.service.data.client.remote.MessageRequest;
import com.xlb.service.data.client.util.http.HttpClient;
import com.xlb.service.data.client.util.http.config.ClientConfig;
import com.xlb.service.data.client.util.http.enums.KeyStoreType;

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
                .clientStore("classpath:client.p12")
                .clientStoreKey("client")
                .trustType(KeyStoreType.JKS)
                .trustStore("classpath:server.jks")
                .trustStoreKey("server").build();

        HttpClient client = new HttpClient(config);
        client.sendMessage(request);
        var response = request.getResponse();
        System.out.println(response);
//        System.out.println(response.getData());
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
