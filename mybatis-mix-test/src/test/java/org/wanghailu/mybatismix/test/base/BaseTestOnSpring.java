package org.wanghailu.mybatismix.test.base;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.wanghailu.mybatismix.test.SpringbootTestApplication;

/**
 * @author cdhuang
 * @date 2023/1/11
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringbootTestApplication.class)
public abstract class BaseTestOnSpring {
    
    @Before
    public void before() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>开始测试>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
    }
    
    @After
    public void after() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>结束测试>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
    }
}
