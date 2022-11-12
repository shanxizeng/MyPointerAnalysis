package pta;

import java.util.*;
import java.util.Map.Entry;

import soot.*;
import soot.jimple.*;
import soot.jimple.toolkits.callgraph.ReachableMethods;
import soot.util.queue.QueueReader;
import javafx.util.Pair;
import soot.jimple.internal.*;

public class WholeProgramTransformer extends SceneTransformer {

	private void typeAnalysis(Map<Object,List<soot.Type>> possibleTypes) {
		ReachableMethods reachableMethods = Scene.v().getReachableMethods();
		QueueReader<MethodOrMethodContext> qr = reachableMethods.listener();
		Map<SootMethod, List<Object>> calling = new HashMap<SootMethod, List<Object>>();
		List<Pair<Object,Object>> edges=new LinkedList<Pair<Object,Object>>();
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
				for (Unit u : sm.getActiveBody().getUnits()) {
					if (u instanceof InvokeStmt) {
						InvokeExpr ie = ((InvokeStmt) u).getInvokeExpr();
						if (ie.getMethod().toString().equals
								("<benchmark.internal.BenchmarkN: void alloc(int)>")||
								ie.getMethod().toString().equals
										("<test.BenchmarkN: void alloc(int)>")) {
						} else if (ie.getMethod().toString().equals
								("<benchmark.internal.BenchmarkN: void test(int,java.lang.Object)>")||
								ie.getMethod().toString().equals
										("<test.BenchmarkN: void test(int,java.lang.Object)>")) {
						} else {
							System.out.println("Method::" + ie.toString());
							if (ie.getArgs().size() > 0) {
								for (int i = 0; i < ie.getArgs().size(); i++) {
									Value pr=ie.getMethod().getActiveBody().getParameterRefs().get(i);
								}
							}
							if (!ie.getMethod().isStatic() && ie instanceof SpecialInvokeExpr) {
								if (!calling.containsKey(ie.getMethod())) {
									calling.put(ie.getMethod(), new LinkedList<Object>());
								}
								SpecialInvokeExpr sie = (SpecialInvokeExpr) ie;
								System.out.println("1: " + ((RefType)((JimpleLocal)sie.getBase()).getType()).getSootClass() + ":"
										+ sie.getBase() + ":" + ie.getMethod());
								calling.get(ie.getMethod()).add((Object) sie.getBase());
							}
							if (!ie.getMethod().isStatic() && ie instanceof VirtualInvokeExpr) {
								if (!calling.containsKey(ie.getMethod())) {
									calling.put(ie.getMethod(), new LinkedList<Object>());
								}
								VirtualInvokeExpr sie = (VirtualInvokeExpr) ie;
								System.out.println("9: " + ((RefType)((JimpleLocal)sie.getBase()).getType()).getSootClass()
										+ ":" + sie.getBase() + ":" + ie.getMethod());
								calling.get(ie.getMethod()).add((Object) sie.getBase());
							}
						}
					} else if (u instanceof DefinitionStmt) {
						DefinitionStmt ds = (DefinitionStmt) u;
						Value rop = ds.getRightOp();
						Value lop = ds.getLeftOp();
						if (rop instanceof NewExpr) {
							NewExpr ne = (NewExpr) rop;
							if(!possibleTypes.containsKey(lop)) {
								possibleTypes.put(lop,new LinkedList<soot.Type>());
							}
							possibleTypes.get(lop).add(ne.getType());
						} else if (lop instanceof Local && rop instanceof Local) {
							edges.add(new Pair<>((Object)rop,(Object)lop));
						} else if (lop instanceof Local && rop instanceof FieldRef) {
							if(rop instanceof AbstractInstanceFieldRef) {
								AbstractInstanceFieldRef f = (AbstractInstanceFieldRef) rop;
							}
							else {
								FieldRef f = (FieldRef) rop;
								edges.add(new Pair<>((Object)rop,(Object)f));
								if(!possibleTypes.containsKey(f)) {
									possibleTypes.put(f,new LinkedList<soot.Type>());
									possibleTypes.get(f).add(f.getType());
								}
								if(!possibleTypes.containsKey(rop)) {
									possibleTypes.put(rop,new LinkedList<soot.Type>());
								}
							}
						} else if (lop instanceof FieldRef && rop instanceof Local) {
							if(lop instanceof AbstractInstanceFieldRef) {
								AbstractInstanceFieldRef f = (AbstractInstanceFieldRef) lop;
							}
							else {
								FieldRef f = (FieldRef) lop;
								edges.add(new Pair<>((Object)rop,(Object)f));
							}
						} else if (lop instanceof FieldRef && rop instanceof FieldRef) {
						} else if (lop instanceof Local && rop instanceof InvokeStmt) {
							InvokeStmt is=(InvokeStmt) rop;
						} else if (lop instanceof Local && rop instanceof InvokeExpr) {
							InvokeExpr is=(InvokeExpr) rop;
							List<Value> rp=is.getArgs();
							for(int i=0;i<is.getMethod().getParameterCount();i++) {
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
							}
							System.out.println("6:"+is.getClass()+":"+is.toString());
						} else if(lop instanceof Local && rop instanceof ParameterRef) {
						} else if(lop instanceof Local && rop instanceof ThisRef) {
							if(calling.containsKey(sm)) {
								for(Object o:calling.get(sm)) {
								}
							}
							else {
							}
						}
						else {
						}
					} else if(u instanceof ReturnStmt) {
						ReturnStmt rs=(ReturnStmt) u;
						if(sm.getReturnType()==null) {
						}
						else {
						}
					}
					else {
					}
				}
			}
		}
	}

	@Override
	protected void internalTransform(String arg0, Map<String, String> arg1) {
		System.out.println(arg0);
		ReachableMethods reachableMethods = Scene.v().getReachableMethods();
		QueueReader<MethodOrMethodContext> qr = reachableMethods.listener();
		AnswerPrinter debugInfo = new AnswerPrinter("log.txt");
		Set<Integer> Ids = new HashSet<Integer>();
		Map<Object,List<soot.Type>> possibleTypes = new HashMap<Object,List<soot.Type>>();
//		typeAnalysis(possibleTypes);
		Map<SootMethod, List<SootMethod>> maybeCalling=new HashMap<SootMethod,List<SootMethod>>();
		List<List<Body>> callingPos = new LinkedList<List<Body>>();
		Map<SootMethod, List<Body>> methodsBodies = new HashMap<SootMethod, List<Body>>();
		int callingCnt=0;
		while (qr.hasNext()) {
			SootMethod sm = qr.next().method();
			if (sm.isJavaLibraryMethod()) {
				continue;
			}
			SootClass sc = sm.getDeclaringClass();
			if(!maybeCalling.containsKey(sm)) {
				maybeCalling.put(sm, new LinkedList<SootMethod>());
				maybeCalling.get(sm).add(sm);
			}
			while(sc.hasSuperclass()) {
				sc=sc.getSuperclass();
				if(!sc.declaresMethod(sm.getName(),sm.getParameterTypes())) {
					continue;
				}
				SootMethod temp=sc.getMethod(sm.getName(),sm.getParameterTypes());
				if(temp.isFinal()) break;
				if(!maybeCalling.containsKey(temp))
					maybeCalling.put(temp, new LinkedList<SootMethod>());
				maybeCalling.get(temp).add(sm);
			}
		}
		qr = reachableMethods.listener();
		while (qr.hasNext()) {
			SootMethod sm = qr.next().method();
			if (sm.isJavaLibraryMethod()) {
				continue;
			}
			if(!methodsBodies.containsKey(sm)) {
				methodsBodies.put(sm, new LinkedList<>());
			}
			methodsBodies.get(sm).add(sm.getActiveBody());
			if (sm.hasActiveBody()) {
				for (Unit u : sm.getActiveBody().getUnits()) {
					if (u instanceof InvokeStmt) {
						InvokeExpr ie = ((InvokeStmt) u).getInvokeExpr();
						if (ie.getMethod().toString().equals
								("<benchmark.internal.BenchmarkN: void alloc(int)>")||
								ie.getMethod().toString().equals
										("<test.BenchmarkN: void alloc(int)>")) {
						} else if (ie.getMethod().toString().equals
								("<benchmark.internal.BenchmarkN: void test(int,java.lang.Object)>")||
								ie.getMethod().toString().equals
										("<test.BenchmarkN: void test(int,java.lang.Object)>")) {
						} else if(maybeCalling.containsKey(ie.getMethod())) {
							callingCnt++;
							List<Body> b = new LinkedList<Body>();
							for(SootMethod m : maybeCalling.get(ie.getMethod())) {
								Body temp=(Body) m.getActiveBody().clone();
								if(!methodsBodies.containsKey(m)) {
									methodsBodies.put(m, new LinkedList<Body>());
								}
								methodsBodies.get(m).add(temp);
								b.add(temp);
							}
							callingPos.add(b);
							System.out.println("Method::" + ie.toString());
						}
					} else if (u instanceof DefinitionStmt) {
						DefinitionStmt ds = (DefinitionStmt) u;
						Value rop = ds.getRightOp();
						Value lop = ds.getLeftOp();
						if (lop instanceof Local && rop instanceof InvokeExpr) {
							InvokeExpr ie=(InvokeExpr) rop;
							if(maybeCalling.containsKey(ie.getMethod())) {
								callingCnt++;
								List<Body> b = new LinkedList<Body>();
								for(SootMethod m : maybeCalling.get(ie.getMethod())) {
									Body temp=(Body) m.getActiveBody().clone();
									if(!methodsBodies.containsKey(m)) {
										methodsBodies.put(m, new LinkedList<Body>());
									}
									methodsBodies.get(m).add(temp);
									b.add(temp);
								}
								callingPos.add(b);
								System.out.println("Method::" + ie.toString());
							}
						}
					}
				}
			}
		}
		System.out.println(callingPos.size());
		System.out.println(callingCnt);
		TreeMap<Integer, Object> queries = new TreeMap<Integer, Object>();
		Map<Body, List<Object>> calling = new HashMap<Body, List<Object>>();
		Anderson anderson = new Anderson();
		CFL cfl=new CFL();
		qr = reachableMethods.listener();
		callingCnt=0;
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
			int callingTemp=callingCnt;
			if (sm.hasActiveBody()) {
				debugInfo.append("------------");
				debugInfo.append("function:" + sm.getDeclaringClass().toString() + "::" + sm.getName() + '\n');
				for(Body b : methodsBodies.get(sm)) {
					callingTemp=callingCnt;
					for (Unit u : b.getUnits()) {
//						System.out.println("S: " + u);
//						System.out.println(u.getClass());
						debugInfo.append(u.toString() + '\n' + "\t\t" + u.getClass().toString() + '\n');
						if (u instanceof InvokeStmt) {
							InvokeExpr ie = ((InvokeStmt) u).getInvokeExpr();
							if (ie.getMethod().toString().equals
									("<benchmark.internal.BenchmarkN: void alloc(int)>") ||
									ie.getMethod().toString().equals
											("<test.BenchmarkN: void alloc(int)>")) {
								allocId = ((IntConstant) ie.getArgs().get(0)).value;
								Ids.add(allocId);
							} else if (ie.getMethod().toString().equals
									("<benchmark.internal.BenchmarkN: void test(int,java.lang.Object)>") ||
									ie.getMethod().toString().equals
											("<test.BenchmarkN: void test(int,java.lang.Object)>")) {
								Value v = ie.getArgs().get(1);
								int id = ((IntConstant) ie.getArgs().get(0)).value;
								queries.put(id, (Object) v);
							} else {
								System.out.println("Method::" + ie.toString());
								if (maybeCalling.containsKey(ie.getMethod())) {
									for (Body body : callingPos.get(callingTemp)) {
										if (ie.getArgs().size() > 0) {
											for (int i = 0; i < ie.getArgs().size(); i++) {
												Value pr = body.getParameterRefs().get(i);
												anderson.addAssignConstraint((Object) ie.getArg(i), (Object) pr);
												cfl.addAssign((Object) ie.getArg(i), (Object) pr);
												System.out.println("5:" + ie.getArg(i).toString() + " " + pr.toString());
												System.out.println(pr.getClass().toString());
											}
										}
										if (ie instanceof SpecialInvokeExpr) {
											if (!calling.containsKey(body)) {
												calling.put(body, new LinkedList<Object>());
											}
											SpecialInvokeExpr sie = (SpecialInvokeExpr) ie;
											System.out.println("1: " + ((RefType) ((JimpleLocal) sie.getBase()).getType()).getSootClass()
													+ ":" + sie.getBase() + ":" + body.getMethod());
											calling.get(body).add((Object) sie.getBase());
										}
										if (ie instanceof VirtualInvokeExpr) {
											if (!calling.containsKey(body)) {
												calling.put(body, new LinkedList<Object>());
											}
											VirtualInvokeExpr sie = (VirtualInvokeExpr) ie;
											System.out.println("9: " + ((RefType) ((JimpleLocal) sie.getBase()).getType()).getSootClass()
													+ ":" + sie.getBase() + ":" + body.getMethod());
											calling.get(body).add((Object) sie.getBase());
										}
									}
									callingTemp++;
								} else {
									System.out.println("10:" + ie.toString());
									SootMethod m = ie.getMethod();
									if (ie.getArgs().size() > 0 && m.hasActiveBody()) {
										for (int i = 0; i < ie.getArgs().size(); i++) {
											Value pr = m.getActiveBody().getParameterRefs().get(i);
											anderson.addAssignConstraint((Object) ie.getArg(i), (Object) pr);
											cfl.addAssign((Object) ie.getArg(i), (Object) pr);
											System.out.println("5:" + ie.getArg(i).toString() + " " + pr.toString());
											System.out.println(pr.getClass().toString());
										}
									}
									if (!m.isStatic() && ie instanceof SpecialInvokeExpr) {
										if (!calling.containsKey(m.getActiveBody())) {
											calling.put(m.getActiveBody(), new LinkedList<Object>());
										}
										SpecialInvokeExpr sie = (SpecialInvokeExpr) ie;
										System.out.println("1: " + ((RefType) ((JimpleLocal) sie.getBase()).getType()).getSootClass() + ":"
												+ sie.getBase() + ":" + m);
										calling.get(m.getActiveBody()).add((Object) sie.getBase());
									}
									if (!m.isStatic() && ie instanceof VirtualInvokeExpr) {
										if (!calling.containsKey(m.getActiveBody())) {
											calling.put(m.getActiveBody(), new LinkedList<Object>());
										}
										VirtualInvokeExpr sie = (VirtualInvokeExpr) ie;
										System.out.println("9: " + ((RefType) ((JimpleLocal) sie.getBase()).getType()).getSootClass()
												+ ":" + sie.getBase() + ":" + m);
										calling.get(m.getActiveBody()).add((Object) sie.getBase());
									}
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
								if (rop instanceof AbstractInstanceFieldRef) {
									AbstractInstanceFieldRef f = (AbstractInstanceFieldRef) rop;
									System.out.println("FieldRef " + f.getField().getName() + " " + f.getFieldRef().name());
									anderson.addAssignConstraint((Object) f.getField(), (Object) lop);
									cfl.addGet((Object) f.getBase(), (Object) lop, (Object) f.getField());
									System.out.println(rop.getClass());
									System.out.println(rop.toString() + rop.getType().toString() + " " +
											lop.toString() + lop.getType().toString());
								} else {
									FieldRef f = (FieldRef) rop;
									System.out.println("FieldRef " + f.getField().getName() + " " + f.getFieldRef().name());
									anderson.addAssignConstraint((Object) f.getField(), (Object) lop);
									cfl.addAssign((Object) f.getField(), (Object) lop);
									System.out.println(rop.getClass());
									System.out.println(rop.toString() + rop.getType().toString() + " " +
											lop.toString() + lop.getType().toString());
								}
							} else if (lop instanceof FieldRef && rop instanceof Local) {
								if (lop instanceof AbstractInstanceFieldRef) {
									AbstractInstanceFieldRef f = (AbstractInstanceFieldRef) lop;
									anderson.addAssignConstraint((Object) rop, (Object) f.getField());
									cfl.addPut((Object) rop, (Object) f.getBase(), (Object) f.getField());
								} else {
									FieldRef f = (FieldRef) lop;
									anderson.addAssignConstraint((Object) rop, (Object) f.getField());
									cfl.addAssign((Object) rop, (Object) f.getField());
								}
							} else if (lop instanceof FieldRef && rop instanceof FieldRef) {
								System.out.println("??????????");
							} else if (lop instanceof Local && rop instanceof InvokeExpr) {
								InvokeExpr is = (InvokeExpr) rop;
								List<Value> rp = is.getArgs();
								if (maybeCalling.containsKey(is.getMethod())) {
									for (Body body : callingPos.get(callingTemp)) {
										for (int i = 0; i < body.getMethod().getParameterCount(); i++) {
											anderson.addAssignConstraint((Object) rp.get(i),
													(Object) body.getParameterLocal(i));
										}
										if (is instanceof VirtualInvokeExpr) {
											if (calling.containsKey(body)) {
												calling.get(body).add(((VirtualInvokeExpr) is).getBase());
											} else {
												calling.put(body, new LinkedList<Object>());
												calling.get(body).add(((VirtualInvokeExpr) is).getBase());
											}
										} else {
											System.out.println("8:" + is.toString());
										}
										anderson.addAssignConstraint((Object) body, (Object) lop);
										cfl.addAssign((Object) body, (Object) lop);
									}
									callingTemp++;
								} else {
									SootMethod m = is.getMethod();
									System.out.println("11:" + is);
									if (m.hasActiveBody()) {
										for (int i = 0; i < m.getParameterCount(); i++) {
											anderson.addAssignConstraint((Object) rp.get(i),
													(Object) m.getActiveBody().getParameterLocal(i));
										}
									}
									if (is instanceof VirtualInvokeExpr) {
										if (calling.containsKey(m.getActiveBody())) {
											calling.get(m.getActiveBody()).add(((VirtualInvokeExpr) is).getBase());
										} else {
											calling.put(m.getActiveBody(), new LinkedList<Object>());
											calling.get(m.getActiveBody()).add(((VirtualInvokeExpr) is).getBase());
										}
									} else {
										System.out.println("8:" + is.toString());
									}
									anderson.addAssignConstraint((Object) m.getActiveBody(), (Object) lop);
									cfl.addAssign((Object) m.getActiveBody(), (Object) lop);
								}
								System.out.println("6:" + is.getClass() + ":" + is.toString());
							} else if (lop instanceof Local && rop instanceof ParameterRef) {
								anderson.addAssignConstraint((Object) rop, (Object) lop);
								cfl.addAssign((Object) rop, (Object) lop);
								System.out.println(rop.toString() + rop.getType().toString() + " " +
										lop.toString() + lop.getType().toString());
								System.out.println(".................");
							} else if (lop instanceof Local && rop instanceof ThisRef) {
								if (calling.containsKey(b)) {
									for (Object o : calling.get(b)) {
										cfl.addAssign(o, (Object) lop);
										System.out.println("2:" + o.toString());
									}
								} else {
									System.out.println("7:" + sm.toString());
								}
							} else {
								System.out.println("???" + u.toString());
								System.out.println("???" + ds.getRightOp().getClass());
							}
						} else if (u instanceof ReturnStmt) {
							ReturnStmt rs = (ReturnStmt) u;
							if (sm.getReturnType() == null) {
								System.out.println("!!!!!!!!!!!!");
							} else {
								System.out.println("aaaaaaaaaaaaaaa");
								System.out.println(rs.getOp().toString() + ":" + sm.toString() + ":" + sm.getClass());
								anderson.addAssignConstraint((Object) rs.getOp(), (Object) b);
								cfl.addAssign((Object) rs.getOp(), (Object) b);
							}
						} else {
							System.out.println("??????" + u.toString());
						}
					}
				}
			}
			callingCnt=callingTemp;
			//}
		}
		debugInfo.flush();
		debugInfo.close();
		AnswerPrinter printer = new AnswerPrinter("result.txt");

//		anderson.run();
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
		cfl.run();
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
