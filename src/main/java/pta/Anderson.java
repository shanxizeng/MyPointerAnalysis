package pta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import soot.Local;

class AssignConstraint {
	Object from, to;
	AssignConstraint(Object from, Object to) {
		this.from = from;
		this.to = to;
	}
}

class NewConstraint {
	Object to;
	int allocId;
	NewConstraint(int allocId, Object to) {
		this.allocId = allocId;
		this.to = to;
	}
}

public class Anderson {
	private List<AssignConstraint> assignConstraintList = new ArrayList<AssignConstraint>();
	private List<NewConstraint> newConstraintList = new ArrayList<NewConstraint>();
	Map<Object, TreeSet<Integer>> pts = new HashMap<Object, TreeSet<Integer>>();
	void addAssignConstraint(Object from, Object to) {
		assignConstraintList.add(new AssignConstraint(from, to));
	}
	void addNewConstraint(int alloc, Object to) {
		if(alloc==0) {
			System.out.println("zero::::"+to.toString());
		}
		newConstraintList.add(new NewConstraint(alloc, to));		
	}
	void run() {
		for (NewConstraint nc : newConstraintList) {
			if (!pts.containsKey(nc.to)) {
				pts.put(nc.to, new TreeSet<Integer>());
			}
			pts.get(nc.to).add(nc.allocId);
		}
		for (boolean flag = true; flag; ) {
			flag = false;
			for (AssignConstraint ac : assignConstraintList) {
				if (!pts.containsKey(ac.from)) {
					continue;
				}	
				if (!pts.containsKey(ac.to)) {
					pts.put(ac.to, new TreeSet<Integer>());
				}
				if (pts.get(ac.to).addAll(pts.get(ac.from))) {
					flag = true;
				}
			}
		}
	}
	TreeSet<Integer> getPointsToSet(Object local) {
		if(!pts.containsKey(local)) {
			System.out.println(local.toString());
		}
		return pts.get(local);
	}
	
}
