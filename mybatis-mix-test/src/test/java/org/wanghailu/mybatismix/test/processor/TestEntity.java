package org.wanghailu.mybatismix.test.processor;

/*import lombok.Data;*/
import org.wanghailu.mybatismix.annotation.AutoComment;
import org.wanghailu.mybatismix.model.EnableExactUpdate;

import javax.persistence.Transient;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

/**
 * @author cdhuang
 * @date 2022/1/7
 */
/*@Data*/
@AutoComment
@EnableExactUpdate
public class TestEntity implements Serializable {

    public static final String PREFIX = "framework.shiro";

    private final RememberMe rememberMe = new RememberMe();

    private static TestEntity getShiroProperties() {
        return new TestEntity();
    }

    @Transient
    protected TestEntity abc;
    /**
     * 是否允许多个人同时登录同一个帐号
     */
    private Boolean uniqueLogin = false;
    
    private String xxxStr;
    
    public Boolean getUniqueLogin() {
        Function<TestEntity, Boolean> function1 = new Function<TestEntity, Boolean>() {
            @Override
            public Boolean apply(TestEntity shiroTestEntity) {
                return shiroTestEntity.uniqueLogin = false;
            }
        };
        Function<TestEntity, String> function2 = thaha -> thaha.xxxStr = "111";
        java.util.List<String> tests = new ArrayList<>();
        java.util.List<TestEntity> tests2 = tests.stream().map(x->{
            if(uniqueLogin){
                return new TestEntity();
            }else{
                TestEntity serializable = new TestEntity();
                return serializable;
            }
        }).filter(x -> x.uniqueLogin = false).collect(toList());
        TestEntityHelper.getShiroProperties2(() -> this).uniqueLogin = false;
        new TestEntityHelper().getIntegerStringMap().get("name").uniqueLogin = false;
        new TestEntityHelper().get("111").uniqueLogin = Arrays.asList("111", "122").contains("111");
        new TestEntityHelper().getSelf("111", new TestEntity()).uniqueLogin = false;
        int i = 0;
        if (uniqueLogin) {
            i++;
            int j = 100;
            for (int z = 0; z < j; z++) {
                j--;
            }
            int z = 0;
            for (uniqueLogin = false; uniqueLogin = z < j; z++) {
                j--;
            }
        }
        Object[] obj = new Object[1];
        uniqueLogin = false;
        getShiroProperties().uniqueLogin = true;
        TestEntityHelper.getShiroProperties(false).abc.uniqueLogin = false;
        TestEntityHelper.abc.uniqueLogin = false;
        if (this.uniqueLogin) {
            uniqueLogin = false;
        }
        BooleanSupplier supplier = () -> uniqueLogin = false;
        supplier.getAsBoolean();
        Runnable runnable = () -> uniqueLogin = false;
        runnable = () -> {
            uniqueLogin = false;
            (rememberMe.cookieName = null).toCharArray();
        };
        new Thread(() -> setUniqueLogin(false)).start();
        ExecutorService service = null;
        service.submit(() -> setUniqueLogin(false));
        runnable = null;
        new Runnable() {
            @Override
            public void run() {
                uniqueLogin = false;
            }
        };
        System.out.println(uniqueLogin = runnable.toString() == "sss");
        Function<String, String> function = thaha -> thaha.substring(0, 2);
        System.out.println(function.apply("哈哈哈" + this.uniqueLogin));
        setUniqueLogin(null);
        System.out.println(i);
        return uniqueLogin = rememberMe.getCookieMaxAge() == null;
    }

    public void setUniqueLogin(Boolean uniqueLogin) {
        this.uniqueLogin = uniqueLogin;
    }
    public RememberMe getRememberMe() {
        return rememberMe;
    }

    /**
     * RememberMe
     */
    public static class RememberMe {
        /**
         * 记住我 cookie name（名称）
         */
        private String cookieName = "_srm";
        /**
         * 记住我 cookie 最大有效时间（秒），604800秒=7天、864000秒=10天、2592000秒 = 30天、设置 -1 关闭浏览器该cookie即失效；默认：-1
         */
        private Integer cookieMaxAge = -1;

        public String getCookieName() {
            return cookieName;
        }

        public void setCookieName(String cookieName) {
            this.cookieName = cookieName;
        }

        public Integer getCookieMaxAge() {
            return cookieMaxAge;
        }

        public void setCookieMaxAge(Integer cookieMaxAge) {
            this.cookieMaxAge = cookieMaxAge;
        }

    }
}
