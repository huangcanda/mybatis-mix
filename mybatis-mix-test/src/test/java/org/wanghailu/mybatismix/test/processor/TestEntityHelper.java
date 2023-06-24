package org.wanghailu.mybatismix.test.processor;


import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @author cdhuang
 * @date 2021/12/24
 */
public class TestEntityHelper extends HashMap<String, TestEntity> {

    public List<String> stringList;

    public Map<Integer, TestEntity> integerStringMap;

    public int[] num;
    
    protected static TestEntity getShiroProperties(){
        return new TestEntity();
    }

    protected static TestEntity getShiroProperties(boolean uni){
        return new TestEntity();
    }

    protected <T extends Serializable> T getSelf (String test,T param){
        return param;
    }

    protected static TestEntity getShiroProperties(Object uni){
        return new TestEntity();
    }
    protected static TestEntity getShiroProperties2(Callable uni){
        return new TestEntity();
    }

    protected static void getShiroProperties2(String uni){
        System.out.println(uni);
    }

    protected static void getShiroProperties2(TestEntityHelper uni){
        System.out.println(uni);
    }

    protected static TestEntity getShiroProperties2(Runnable uni){
        return new TestEntity();
    }

    protected  static TestEntity abc;

    public List<String> getStringList() {
        return stringList;
    }

    public Map<Integer, TestEntity> getIntegerStringMap() {
        return integerStringMap;
    }
    
}
