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

    static A f() {
        BenchmarkN.alloc(2);
        A b = new A();
        return b;
    }


    public static void main(String[] args) {
        BenchmarkN.alloc(1);
        Mid c = new C();
        BenchmarkN.alloc(2);
        A a = new A();
        BenchmarkN.alloc(3);
        A b = new A();
        c.setx(a);
        c.sety(b);
        A d = c.getx();
        BenchmarkN.test(1, d);
        Base m = new Mid();
        System.out.println(m.n);
    }
}