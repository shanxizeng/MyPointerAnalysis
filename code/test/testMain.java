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

class C extends Base {
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
        A a = new A();
        BenchmarkN.alloc(2);
        Base b=new Base();
        BenchmarkN.alloc(3);
        Base c=new C();
        if(1+1>1) {
            BenchmarkN.alloc(5);
            c=new Base();
        }
        c.sety(a);
        BenchmarkN.alloc(4);
        A x=new A();
        c.setx(x);
        A d=c.getx();
        BenchmarkN.test(1, d);
    }
}