package pta;

import java.util.*;
import java.util.Map.Entry;

import soot.Local;
import soot.MethodOrMethodContext;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.*;
import soot.jimple.toolkits.callgraph.ReachableMethods;
import soot.util.queue.QueueReader;
import soot.jimple.internal.AbstractInstanceFieldRef;

public class WholeProgramTransformer extends SceneTransformer {

	@Override
	protected void internalTransform(String arg0, Map<String, String> arg1) {
		System.out.println(arg0);

		TreeMap<Integer, Object> queries = new TreeMap<Integer, Object>();
		Map<SootMethod, List<Object>> calling = new HashMap<SootMethod, List<Object>>();
		Anderson anderson = new Anderson();
		CFL cfl=new CFL();

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
			if(!sm.isStatic()) {

			}
			System.out.println(sm.toString());
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
								("<benchmark.internal.BenchmarkN: void alloc(int)>")||
							ie.getMethod().toString().equals
									("<test.BenchmarkN: void alloc(int)>")) {
							allocId = ((IntConstant) ie.getArgs().get(0)).value;
							Ids.add(allocId);
						} else if (ie.getMethod().toString().equals
								("<benchmark.internal.BenchmarkN: void test(int,java.lang.Object)>")||
								ie.getMethod().toString().equals
										("<test.BenchmarkN: void test(int,java.lang.Object)>")) {
							Value v = ie.getArgs().get(1);
							int id = ((IntConstant) ie.getArgs().get(0)).value;
							queries.put(id, (Object) v);
						} else {
							System.out.println("Method::" + ie.toString());
							if (ie.getArgs().size() > 0) {
								for (int i = 0; i < ie.getArgs().size(); i++) {
									Value pr=ie.getMethod().getActiveBody().getParameterRefs().get(i);
									anderson.addAssignConstraint((Object) ie.getArg(i), (Object) pr);
									cfl.addAssign((Object) ie.getArg(i), (Object) pr);
									System.out.println("5:"+ie.getArg(i).toString() + " " +pr.toString());
									System.out.println(pr.getClass().toString());
								}
							}
							if (!ie.getMethod().isStatic() && ie instanceof SpecialInvokeExpr) {
								if (!calling.containsKey(ie.getMethod())) {
									calling.put(ie.getMethod(), new LinkedList<Object>());
								}
								SpecialInvokeExpr sie = (SpecialInvokeExpr) ie;
								System.out.println("1: " + sie.getBase() + ":" + ie.getMethod());
								calling.get(ie.getMethod()).add((Object) sie.getBase());
							}
							if (!ie.getMethod().isStatic() && ie instanceof VirtualInvokeExpr) {
								if (!calling.containsKey(ie.getMethod())) {
									calling.put(ie.getMethod(), new LinkedList<Object>());
								}
								VirtualInvokeExpr sie = (VirtualInvokeExpr) ie;
								System.out.println("9: " + sie.getBase() + ":" + ie.getMethod());
								calling.get(ie.getMethod()).add((Object) sie.getBase());
							}
						}
					} else if (u instanceof DefinitionStmt) {
						DefinitionStmt ds = (DefinitionStmt) u;
						Value rop = ds.getRightOp();
						Value lop = ds.getLeftOp();
						if (rop instanceof NewExpr) {
							//System.out.println("Alloc " + allocId);
							anderson.addNewConstraint(allocId, (Object) ds.getLeftOp());
							cfl.addNew(allocId, (Object) ds.getLeftOp());
							allocId = 0;
						} else if (lop instanceof Local && rop instanceof Local) {
							anderson.addAssignConstraint((Object) rop, (Object) lop);
							cfl.addAssign((Object) rop, (Object) lop);
							System.out.println(rop.toString() + rop.getType().toString() + " " +
									lop.toString() + lop.getType().toString());
						} else if (lop instanceof Local && rop instanceof FieldRef) {
							if(rop instanceof AbstractInstanceFieldRef) {
								AbstractInstanceFieldRef f = (AbstractInstanceFieldRef) rop;
								System.out.println("FieldRef " + f.getField().getName() + " " + f.getFieldRef().name());
								anderson.addAssignConstraint((Object) f.getField(), (Object) lop);
								cfl.addGet((Object) f.getBase(), (Object) lop, (Object) f.getField());
								System.out.println(rop.getClass());
								System.out.println(rop.toString() + rop.getType().toString() + " " +
										lop.toString() + lop.getType().toString());
							}
							else {
								FieldRef f = (FieldRef) rop;
								System.out.println("FieldRef " + f.getField().getName() + " " + f.getFieldRef().name());
								anderson.addAssignConstraint((Object) f.getField(), (Object) lop);
								cfl.addAssign((Object) f.getField(), (Object) lop);
								System.out.println(rop.getClass());
								System.out.println(rop.toString() + rop.getType().toString() + " " +
										lop.toString() + lop.getType().toString());
							}
						} else if (lop instanceof FieldRef && rop instanceof Local) {
							if(lop instanceof AbstractInstanceFieldRef) {
								AbstractInstanceFieldRef f = (AbstractInstanceFieldRef) lop;
								anderson.addAssignConstraint((Object) rop, (Object) f.getField());
								cfl.addPut((Object) rop, (Object) f.getBase(), (Object) f.getField());
							}
							else {
								FieldRef f = (FieldRef) lop;
								anderson.addAssignConstraint((Object) rop, (Object) f.getField());
								cfl.addAssign((Object) rop, (Object) f.getField());
							}
						} else if (lop instanceof FieldRef && rop instanceof FieldRef) {
							System.out.println("??????????");
						} else if (lop instanceof Local && rop instanceof InvokeStmt) {
							InvokeStmt is=(InvokeStmt) rop;
							anderson.addAssignConstraint((Object) is.getInvokeExpr().getMethod(), (Object) lop);
							cfl.addAssign((Object) is.getInvokeExpr().getMethod(), (Object) lop);
							System.out.println("4:"+is.getClass()+":"+is.toString());
						} else if (lop instanceof Local && rop instanceof InvokeExpr) {
							InvokeExpr is=(InvokeExpr) rop;
							List<Value> rp=is.getArgs();
							for(int i=0;i<is.getMethod().getParameterCount();i++) {
								anderson.addAssignConstraint((Object) rp.get(i),
										(Object) is.getMethod().getActiveBody().getParameterLocal(i));
							}
							if(is instanceof VirtualInvokeExpr) {
								if(calling.containsKey(is.getMethod())) {
									calling.get(is.getMethod()).add(((VirtualInvokeExpr) is).getBase());
								}
								else {
									calling.put(is.getMethod(),new LinkedList<Object>());
									calling.get(is.getMethod()).add(((VirtualInvokeExpr) is).getBase());
								}
							}
							else {
								System.out.println("8:"+is.toString());
							}
							anderson.addAssignConstraint((Object) is.getMethod(), (Object) lop);
							cfl.addAssign((Object) is.getMethod(), (Object) lop);
							System.out.println("6:"+is.getClass()+":"+is.toString());
						} else if(lop instanceof Local && rop instanceof ParameterRef) {
							anderson.addAssignConstraint((Object) rop,(Object) lop);
							cfl.addAssign((Object) rop,(Object) lop);
							System.out.println(rop.toString() + rop.getType().toString() + " " +
									lop.toString() + lop.getType().toString());
							System.out.println(".................");
						} else if(lop instanceof Local && rop instanceof ThisRef) {
							if(calling.containsKey(sm)) {
								for(Object o:calling.get(sm)) {
									cfl.addAssign(o, (Object) lop);
									System.out.println("2:"+o.toString());
								}
							}
							else {
								System.out.println("7:"+sm.toString());
							}
						}
						else {
							System.out.println("???" + u.toString());
							System.out.println("???" + ds.getRightOp().getClass());
						}
					} else if(u instanceof ReturnStmt) {
						ReturnStmt rs=(ReturnStmt) u;
						if(sm.getReturnType()==null) {
							System.out.println("!!!!!!!!!!!!");
						}
						else {
							System.out.println("aaaaaaaaaaaaaaa");
							System.out.println(rs.getOp().toString()+":"+sm.toString()+":"+sm.getClass());
							anderson.addAssignConstraint((Object) rs.getOp(),(Object)sm);
							cfl.addAssign((Object) rs.getOp(),(Object)sm);
						}
					}
					else {
						System.out.println("???" + u.toString());
					}
				}
			}
			//}
		}
		debugInfo.flush();
		debugInfo.close();

		anderson.run();
		cfl.run();
		AnswerPrinter printer = new AnswerPrinter("result.txt");
//		for (Entry<Integer, Object> q : queries.entrySet()) {
//			System.out.println(q.getKey().intValue());
//			TreeSet<Integer> result = anderson.getPointsToSet(q.getValue());
//			if (result == null) {
//				printer.append(q.getKey().toString() + ":");
//				for (Integer i : Ids) {
//					if (i != 0)
//						printer.append(" " + i);
//				}
//				printer.append("\n");
//			} else {
//				System.out.println(result.size());
//				printer.append(q.getKey().toString() + ":");
//				for (Integer i : result) {
//					if (i != 0)
//						printer.append(" " + i);
//				}
//				printer.append("\n");
//			}
//		}
		for (Entry<Integer, Object> q : queries.entrySet()) {
			System.out.println(q.getKey().intValue());
			TreeSet<Integer> result = cfl.getPointsToSet(q.getValue());
			if (result == null) {
				printer.append(q.getKey().toString() + ":");
				for (Integer i : Ids) {
					if (i != 0)
						printer.append(" " + i);
				}
				printer.append("\n");
			} else {
				System.out.println(result.size());
				printer.append(q.getKey().toString() + ":");
				for (Integer i : result) {
					if (i != 0)
						printer.append(" " + i);
				}
				printer.append("\n");
			}
		}
		printer.flush();
		printer.close();
	}

}
