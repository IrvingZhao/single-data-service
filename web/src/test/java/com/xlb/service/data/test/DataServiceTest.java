package com.xlb.service.data.test;

import com.xlb.service.data.core.manager.SingleDataManager;
import com.xlb.service.data.web.SingleDataServiceApplication;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest(classes = SingleDataServiceApplication.class)
@RunWith(SpringJUnit4ClassRunner.class)
@Slf4j
public class DataServiceTest {

    @Resource
    private SingleDataManager dataManager;

    @Test
    public void singleGetToken() {
        String token = dataManager.getData("demo-wechat");
        System.out.println(token);
        Assert.assertNotNull(token);
    }

    @Test
    public void threadGetToken() throws InterruptedException {
        ExecutorService service = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 5; i++) {
            service.submit(getRunnable("loadTokenThread" + i));
        }
        Thread.sleep(10000L);
    }

    private Runnable getRunnable(String name) {
        return () -> {
            log.info("[{}] get token start", name);
            String token = dataManager.getData("demo-wechat");
            log.info("[{}] get token end: [{}]", name, token);
        };
    }

}
