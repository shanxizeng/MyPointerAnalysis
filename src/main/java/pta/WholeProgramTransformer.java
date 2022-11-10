package pta;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Set;
import java.util.HashSet;

import soot.Local;
import soot.MethodOrMethodContext;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.DefinitionStmt;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.NewExpr;
import soot.jimple.FieldRef;
import soot.jimple.toolkits.callgraph.ReachableMethods;
import soot.util.queue.QueueReader;

public class WholeProgramTransformer extends SceneTransformer {

	@Override
	protected void internalTransform(String arg0, Map<String, String> arg1) {
		System.out.println(arg0);

		TreeMap<Integer, Object> queries = new TreeMap<Integer, Object>();
		Anderson anderson = new Anderson();

		ReachableMethods reachableMethods = Scene.v().getReachableMethods();
		QueueReader<MethodOrMethodContext> qr = reachableMethods.listener();
		AnswerPrinter debugInfo = new AnswerPrinter("log.txt");
		Set<Integer> Ids = new HashSet<Integer>();
		while (qr.hasNext()) {
			SootMethod sm = qr.next().method();
			//if (sm.toString().contains("Hello")) {
			//System.out.println(sm);
			int allocId = 0;
			if (sm.isJavaLibraryMethod()) {
//					System.out.println(sm.toString());
				continue;
			}
			if (sm.hasActiveBody()) {
				debugInfo.append("------------");
				debugInfo.append("function:" + sm.getDeclaringClass().toString() + "::" + sm.getName() + '\n');
				for (Unit u : sm.getActiveBody().getUnits()) {
//						System.out.println("S: " + u);
//						System.out.println(u.getClass());
					debugInfo.append(u.toString() + '\n' + "\t\t" + u.getClass().toString() + '\n');
					if (u instanceof InvokeStmt) {
						InvokeExpr ie = ((InvokeStmt) u).getInvokeExpr();
						if (ie.getMethod().toString().equals
								("<benchmark.internal.BenchmarkN: void alloc(int)>")) {
							allocId = ((IntConstant) ie.getArgs().get(0)).value;
							Ids.add(allocId);
						} else if (ie.getMethod().toString().equals
								("<benchmark.internal.BenchmarkN: void test(int,java.lang.Object)>")) {
							Value v = ie.getArgs().get(1);
							int id = ((IntConstant) ie.getArgs().get(0)).value;
							queries.put(id, (Object) v);
						} else {

						}
					}
					else if (u instanceof DefinitionStmt) {
						DefinitionStmt ds = (DefinitionStmt) u;
						Value rop=ds.getRightOp();
						Value lop=ds.getLeftOp();
						if (rop instanceof NewExpr) {
							//System.out.println("Alloc " + allocId);
							anderson.addNewConstraint(allocId, (Object) ds.getLeftOp());
						}
						else if (lop instanceof Local && rop instanceof Local) {
							anderson.addAssignConstraint((Object) rop, (Object) lop);
							System.out.println(rop.toString()+rop.getType().toString()+" "+
									lop.toString()+lop.getType().toString());
						}
						else if (lop instanceof Local && rop instanceof FieldRef) {
							FieldRef f=(FieldRef) rop;
							System.out.println("FieldRef "+f.getField().getName()+" "+f.getFieldRef().name());
							anderson.addAssignConstraint((Object) f.getField(),(Object) lop);
							System.out.println(rop.toString()+rop.getType().toString()+" "+
									lop.toString()+lop.getType().toString());
						}
						else if (lop instanceof FieldRef && rop instanceof Local) {
							FieldRef f=(FieldRef) lop;
							anderson.addAssignConstraint((Object) rop,(Object) f.getField());
						}
						else if (lop instanceof FieldRef && rop instanceof FieldRef) {
							System.out.println("??????????");
						}
						else {
							System.out.println("???"+u.toString());
						}
					}
					else {
						System.out.println("???"+u.toString());
					}
				}
			}
			//}
		}
		debugInfo.flush();
		debugInfo.close();

		anderson.run();
		AnswerPrinter printer = new AnswerPrinter("result.txt");
		for (Entry<Integer, Object> q : queries.entrySet()) {
			System.out.println(q.getKey().intValue());
			TreeSet<Integer> result = anderson.getPointsToSet(q.getValue());
			if(result==null) {
				printer.append(q.getKey().toString() + ":");
				for (Integer i : Ids) {
					if(i!=0)
						printer.append(" " + i);
				}
				printer.append("\n");
			}
			else {
				System.out.println(result.size());
				printer.append(q.getKey().toString() + ":");
				for (Integer i : result) {
					if(i!=0)
						printer.append(" " + i);
				}
				printer.append("\n");
			}
		}
		printer.flush();
		printer.close();

	}

}
