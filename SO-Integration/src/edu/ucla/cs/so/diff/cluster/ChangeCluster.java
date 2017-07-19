package edu.ucla.cs.so.diff.cluster;

import java.util.HashSet;
import java.util.Set;

import com.github.gumtreediff.actions.model.Action;

public class ChangeCluster {
	public Set<Action> changes;
	
	public ChangeCluster() {
		changes = new HashSet<Action>();
	}
}
