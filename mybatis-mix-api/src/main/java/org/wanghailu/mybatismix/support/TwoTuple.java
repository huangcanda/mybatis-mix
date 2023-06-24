package org.wanghailu.mybatismix.support;

import java.util.Objects;

/**
 * 二元组
 * Created by cdhuang on 2019/7/15.
 */
public class TwoTuple<A, B> {
    
    public static <A,B> TwoTuple<A,B> of(A a,B b){
        return new TwoTuple<>(a,b);
    }

    private A first;

    private B second;

    public TwoTuple(A a, B b){
        first = a;
        second = b;
    }

    public A getFirst() {
        return first;
    }

    public B getSecond() {
        return second;
    }

    public void setSecond(B second) {
        this.second = second;
    }

    public void setFirst(A first) {
        this.first = first;
    }
    
    @Override
    public String toString(){
        return "(" + first + ", " + second + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TwoTuple<?, ?> twoTuple = (TwoTuple<?, ?>) o;
        return Objects.equals(first, twoTuple.first) &&
                Objects.equals(second, twoTuple.second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }
}
