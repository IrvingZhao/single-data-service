package com.xlb.service.data.client;

import com.xlb.service.data.client.manager.SingleDataManager;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

@SpringBootTest(classes = Application.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class ClientTest {

    @Resource
    private SingleDataManager dataManager;

    @Test
    public void dataReadTest() {
        dataManager.reloadData("demo-wechat");
        var dataA = dataManager.getData("demo-wechat");
        var dataB = dataManager.getData("demo-wechat");
        System.out.println(dataA);
        Assert.assertEquals(dataA, dataB);
    }

}
