package test;

import benchmark.internal.BenchmarkN;
import benchmark.objects.*;


class Base {
    A[] a;

    public void seta(A[] arr) {
        a=arr;
    }

    public A geti(int i) {
        return a[i];
    }
}

public class testMain {
    public static void main(String[] args) {
        BenchmarkN.alloc(2);
        A a = new A();
        BenchmarkN.alloc(3);
        A[] b = new A[10];
        BenchmarkN.alloc(4);
        Base base = new Base();
        base.seta(b);
        b[args.length]=a;
        A c = base.geti(2);
        BenchmarkN.test(1, c);
    }
}