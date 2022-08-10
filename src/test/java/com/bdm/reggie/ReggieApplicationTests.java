package com.bdm.reggie;

import com.bdm.reggie.common.SendSmg;
import com.bdm.reggie.util.MailUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ReggieApplicationTests {

    @Autowired
    private MailUtils mailUtils;

    @Test
    void contextLoads() {
        mailUtils.sendEmail("1474051104@qq.com", "测试", "测试");
    }

}
