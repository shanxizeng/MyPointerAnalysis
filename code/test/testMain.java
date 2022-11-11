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

public class testMain {

    static A f() {
        BenchmarkN.alloc(2);
        A b = new A();
        return b;
    }


    public static void main(String[] args) {
        BenchmarkN.alloc(1);
        A a = new A();

        if (1 + 1 > 1) {
            a = f();
        }
        BenchmarkN.test(1, a);
    }
}