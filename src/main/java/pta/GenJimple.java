package pta;

public class GenJimple {
    public static void main(String[] args) {
        String classpath = args[0];
        String clazzname = args[1];
        System.out.printf("Tranform %s to jimple.\n", clazzname);
        soot.Main.main(new String[] {
                "-f", "J",
                "-soot-class-path", classpath,
                clazzname
        });
    }
}

