package pta;

import java.util.Map;
import java.util.HashMap;
import java.util.TreeSet;
import soot.SootField;

class EdgeType {
    enum type {
        Assign, New, Put, Get, Alias, FlowTo, PointTo,
        Flow1, Flow2, Flow3, Point1, Point2, Point3, Point4, Void;
    }

    type Type;
    Object f;
    boolean reversed;

    EdgeType() {
        Type = type.Void;
        f = null;
        reversed = false;
    }

    EdgeType(type t) {
        Type = t;
        f = null;
        reversed = false;
    }

    EdgeType(type t, Object g) {
        Type = t;
        f = g;
        reversed = false;
    }

    EdgeType connectWith(EdgeType t) {
        EdgeType res = new EdgeType();
        if (reversed) {
            switch (Type) {
                case Assign: {
                    if (t.Type == type.Point1 && !t.reversed)
                        res.Type = type.Point1;
                    break;
                }
                case Get: {
                    if (t.Type == type.Point3 && f.equals(t.f) && !t.reversed) {
                        res.Type = type.Point2;
                        res.f = t.f;
                    }
                    break;
                }
                default: {
                    break;
                }
            }
        } else {
            switch (Type) {
                case PointTo: {
                    if (t.Type == type.FlowTo && !t.reversed)
                        res.Type = type.Alias;
                    break;
                }
                case New: {
                    if (t.Type == type.Flow1 && !t.reversed)
                        res.Type = type.FlowTo;
                    break;
                }
                case Assign:
                case Flow2: {
                    if (t.Type == type.Flow1 && !t.reversed)
                        res.Type = type.Flow1;
                    break;
                }
                case Put: {
                    if (t.Type == type.Flow3 && f.equals(t.f) && !t.reversed) {
                        res.Type = type.Flow2;
                        res.f = t.f;
                    }
                    break;
                }
                case Alias: {
                    if (t.Type == type.Get && !t.reversed) {
                        res.Type = type.Flow3;
                        res.f = t.f;
                    }
                    if (t.Type == type.Put && t.reversed) {
                        res.Type = type.Point3;
                        res.f = t.f;
                    }
                    break;
                }
                case Point1: {
                    if (t.Type == type.New && t.reversed)
                        res.Type = type.PointTo;
                    break;
                }
                case Point2: {
                    if (t.Type == type.Point1 && !t.reversed)
                        res.Type = type.Point1;
                    break;
                }
                default: {
                    break;
                }
            }
        }
        return res;
    }

    EdgeType reverse() {
        EdgeType res = new EdgeType(Type, f);
        res.reversed = !reversed;
        return res;
    }

    void print(AnswerPrinter printer) {
        if(f!=null)
            printer.append(Type.toString() + " " + f.toString() + " " + reversed + "\n");
        else
            printer.append(Type.toString() + " null " + reversed + "\n");
    }

    void print2(AnswerPrinter printer) {
        if(f!=null) {
            printer.append(Type.toString() + ((SootField)f).getName());
        }
        else
            printer.append(Type.toString());
    }

    boolean isVoid() {
        return Type==type.Void;
    }

    boolean same(EdgeType t) {
        return t.Type == Type && ((f == null && t.f == null) || f.equals(t.f)) && reversed == t.reversed;
    }

}


class Graph {

    int[] to, first, nxt, from;
    int tot;
    EdgeType[] w;
    Map<Integer,Integer> num;

    void add1(int u, int v, EdgeType wi) {
        to[tot] = v;
        nxt[tot] = first[u];
        from[tot] = u;
        w[tot] = wi;
        first[u] = tot++;
    }

    void add2(int u, int v, EdgeType wi) {
        for(int i=first[u];i!=0;i=nxt[i])
            if(to[i]==v&&w[i].same(wi))
                return;
        to[tot] = v;
        nxt[tot] = first[u];
        from[tot] = u;
        w[tot] = wi;
        first[u] = tot++;
        to[tot] = u;
        nxt[tot] = first[v];
        from[tot] = v;
        w[tot] = wi.reverse();
        first[v] = tot++;
    }

    Graph(int n, int m) {
        to = new int[m + 1];
        first = new int[n + 1];
        nxt = new int[m + 1];
        from = new int[m + 1];
        w = new EdgeType[m + 1];
        tot = 1;
        num=new HashMap<Integer,Integer>();
    }

    void print(AnswerPrinter printer, AnswerPrinter dotter, int cnt) {
        for (int i = 1; i <= cnt; i++) {
            printer.append(i + ":\n");
            for (int j = first[i]; j != 0; j = nxt[j]) {
                printer.append("\t" + to[j] + ":");
                w[j].print(printer);
                if (!w[j].reversed && (w[j].Type != EdgeType.type.Flow1 && w[j].Type != EdgeType.type.Flow2 &&
                        w[j].Type != EdgeType.type.Flow3 && w[j].Type != EdgeType.type.Point1 &&
                        w[j].Type != EdgeType.type.Point2 && w[j].Type != EdgeType.type.Point3)) {
                    dotter.append(i + "->" + to[j] + "[ label=\"");
                    w[j].print2(dotter);
                    dotter.append("\"];" + "\n");
                }
            }
        }
    }

    void trans() {
        int cur = 1;
        while (cur <= tot) {
            for (int i = first[to[cur]]; i != 0; i = nxt[i]) {
                EdgeType temp = w[cur].connectWith(w[i]);
                if (!temp.isVoid()) {
                    add2(from[cur], to[i], temp);
                }
            }
            for (int i = first[from[cur]]; i != 0; i = nxt[i]) {
                EdgeType temp = w[i].reverse().connectWith(w[cur]);
                if (!temp.isVoid()) {
                    add2(to[i], to[cur], temp);
                }
            }
            cur++;
        }
    }

    TreeSet<Integer> getPointsToSet(int local) {
        TreeSet<Integer> pts=new TreeSet<Integer>();
        for(int i=first[local];i!=0;i=nxt[i])
            if(w[i].Type==EdgeType.type.PointTo&&!w[i].reversed) {
                pts.add(num.get(to[i]));
            }
        return pts;
    }
}

public class CFL {
    Map<Object, Integer> objectId = new HashMap<Object, Integer>();
    Map<Integer, Integer> newId = new HashMap<Integer, Integer>();
    int cnt = 0;
    Graph g = new Graph(110000, 1100000);

    void addAssign(Object from, Object to) {
        int x1, x2;
        if (objectId.containsKey(from))
            x1 = objectId.get(from);
        else {
            x1 = ++cnt;
            objectId.put(from, x1);
        }
        if (objectId.containsKey(to))
            x2 = objectId.get(to);
        else {
            x2 = ++cnt;
            objectId.put(to, x2);
        }
        g.add2(x1, x2, new EdgeType(EdgeType.type.Assign));
    }

    void addPut(Object from, Object to, Object f) {
        int x1, x2;
        if (objectId.containsKey(from))
            x1 = objectId.get(from);
        else {
            x1 = ++cnt;
            objectId.put(from, x1);
        }
        if (objectId.containsKey(to))
            x2 = objectId.get(to);
        else {
            x2 = ++cnt;
            objectId.put(to, x2);
        }
        g.add2(x1, x2, new EdgeType(EdgeType.type.Put, f));
    }

    void addGet(Object from, Object to, Object f) {
        int x1, x2;
        if (objectId.containsKey(from))
            x1 = objectId.get(from);
        else {
            x1 = ++cnt;
            objectId.put(from, x1);
        }
        if (objectId.containsKey(to))
            x2 = objectId.get(to);
        else {
            x2 = ++cnt;
            objectId.put(to, x2);
        }
        g.add2(x1, x2, new EdgeType(EdgeType.type.Get, f));
    }

    void addNew(int from, Object to) {
        int x1, x2;
        if (newId.containsKey(from))
            x1 = newId.get(from);
        else {
            x1 = ++cnt;
            newId.put(from, x1);
        }
        if (objectId.containsKey(to))
            x2 = objectId.get(to);
        else {
            x2 = ++cnt;
            objectId.put(to, x2);
        }
        g.num.put(x1, from);
        g.add2(x1, x2, new EdgeType(EdgeType.type.New));
    }

    void run() {
        AnswerPrinter printer = new AnswerPrinter("cflInfo.txt");
        AnswerPrinter dotter = new AnswerPrinter("cflInfo.dot");
        dotter.append("digraph cfl{\n");
        for (Map.Entry<Object, Integer> x : objectId.entrySet()) {
            printer.append(x.getKey().toString() + ":" + x.getValue() + "\n");
        }
        for (Map.Entry<Integer, Integer> x : newId.entrySet()) {
            printer.append(x.getKey().toString() + ":" + x.getValue() + "\n");
        }
        g.print(printer, dotter, cnt);
        dotter.append("}\n");
        dotter.flush();
        dotter.close();
        printer.flush();
        for (int i = 1; i <= cnt; i++) {
            g.add2(i, i, new EdgeType(EdgeType.type.Flow1));
            g.add2(i, i, new EdgeType(EdgeType.type.Point1));
        }
        g.trans();
        AnswerPrinter dotter2 = new AnswerPrinter("cflInfo2.dot");
        dotter2.append("digraph cfl{\n");
        for (Map.Entry<Object, Integer> x : objectId.entrySet()) {
            printer.append(x.getKey().toString() + ":" + x.getValue() + "\n");
        }
        for (Map.Entry<Integer, Integer> x : newId.entrySet()) {
            printer.append(x.getKey().toString() + ":" + x.getValue() + "\n");
        }
        g.print(printer, dotter2, cnt);
        dotter2.append("}\n");
        dotter2.flush();
        printer.flush();
        printer.close();
        dotter2.close();
    }

    TreeSet<Integer> getPointsToSet(Object local) {
        if(!objectId.containsKey(local)) {
            System.out.println(local.toString());
            return null;
        }
        return g.getPointsToSet(objectId.get(local));
    }

}
