package test;

class BenchmarkN {
    public BenchmarkN() {
    }

    public static void alloc(int id) {
        System.out.println("wpigwegh");
    }

    public static void test(int id, Object targetVariable) {
        System.out.println("ababababaa");
    }
}

class B {

    // Object B used as attribute of objects of type A

    public B() {
    }
}


class A {

    // Object A with attributes of type B

    public int i = 5;

    public B f = new B();
    public B g = new B();
    public B h;

    public A() {
    }

    public A(B b) {
        this.f = b;
    }

    public B getF() {
        return f;
    }
    public B getH() {
        return h;
    }
    public B id(B b) {
        return b;
    }

}

class Q {
    // Class P extends class Q

    private A a;

    public Q(A a) {
        this.a = a;
    }

    public void alias(A x) {
        this.a = x;
    }

    public A getA(){
        return a;
    }
}

class P extends Q {

    public P(A a) {
        super(a);
    }
}

class N {
    public String value = "";
    public N next;

    public N() {
        BenchmarkN.alloc(1);
        next = new N();
    }
}

interface I {
    // G and H implement I

    public A foo(A a);
}

class H implements I {
    // G and H implement I

    A a;

    public A foo(A a) {
        this.a = a;
        return a;
    }
}

class G implements I {
    // G and H implement I

    A a;

    public A foo(A a) {
        this.a = a;
        return a;
    }
}



class Base {
    A x=new A(),y=new A();
    public int n=1;

    public void setx(A z) {
        x=z;
    }
    public void sety(A a) {
        y=a;
    }

    public A getx() {
        return x;
    }
}

class Mid extends Base {
    public int n=2;
//    public A getx() {
//        BenchmarkN.alloc(4);
//        A a = new A();
//        return a;
//    }
}

class C extends Mid {
    public A getx() {
        return y;
    }
}

public class testMain {

    static void swapf(A a1, A a2) {
        B tmp;
        tmp = a1.f;
        a1.f = a2.f;
        a2.f = tmp;
        return;
    }

    public static void main(String[] args) {
        BenchmarkN.alloc(2);
        I a = new G();
        BenchmarkN.alloc(3);
        I b = new H();
        BenchmarkN.alloc(4);
        A c = new A();
        b.foo(a.foo(c));
        BenchmarkN.alloc(5);
        A d = new A();
        BenchmarkN.test(1, a.foo(d));
    }
}