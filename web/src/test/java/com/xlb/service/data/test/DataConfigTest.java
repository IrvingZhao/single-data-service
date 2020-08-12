package com.xlb.service.data.test;

import com.xlb.base.exception.CodeUnCheckException;
import com.xlb.service.data.core.config.SingleDataConfig;
import com.xlb.service.data.core.data.SingleDataFactory;
import com.xlb.service.data.service.error.ConfigError;
import com.xlb.service.data.service.manager.DataBaseConfigManager;
import com.xlb.service.data.web.SingleDataServiceApplication;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;

@SpringBootTest(classes = SingleDataServiceApplication.class)
@RunWith(SpringJUnit4ClassRunner.class)
@Slf4j
@Transactional
public class DataConfigTest {

    @Autowired
    private DataBaseConfigManager configManager;

    @Test
    public void testSave() {
        var param = new HashMap<String, String>();
        param.put("A", "123");
        param.put("B", "456");
        SingleDataConfig config = new SingleDataConfig("name", SingleDataFactory.Type.WECHAT, param);
        configManager.saveConfig(config);
        try {
            configManager.saveConfig(config);
        } catch (CodeUnCheckException e) {
            log.error("code has exits");
            Assert.assertEquals(e.getCode(), ConfigError.CONFIG_KEY_EXITS.getCode());
        }

        var config2 = configManager.getConfig("name");
        Assert.assertEquals(config2.getType(), SingleDataFactory.Type.WECHAT);
        Assert.assertEquals(config2.getConfig().get("A"), "123");
    }

    @Test
    public void testUpdate() {
        var param = new HashMap<String, String>();
        param.put("A", "123");
        param.put("B", "456");
        SingleDataConfig config = new SingleDataConfig("update-name", SingleDataFactory.Type.WECHAT, param);
        configManager.saveConfig(config);
        param.put("A", "test");
        config.setType(SingleDataFactory.Type.HUAWEI);
        configManager.updateConfig(config);

        var config2 = configManager.getConfig("update-name");
        Assert.assertEquals(config2.getType(), SingleDataFactory.Type.HUAWEI);
        Assert.assertEquals(config2.getConfig().get("A"), "test");
    }


}
