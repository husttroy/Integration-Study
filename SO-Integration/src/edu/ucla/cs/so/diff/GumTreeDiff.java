package edu.ucla.cs.so.diff;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.github.gumtreediff.actions.ActionGenerator;
import com.github.gumtreediff.actions.RootAndLeavesClassifier;
import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.actions.model.Delete;
import com.github.gumtreediff.actions.model.Insert;
import com.github.gumtreediff.actions.model.Move;
import com.github.gumtreediff.actions.model.Update;
import com.github.gumtreediff.client.Run;
import com.github.gumtreediff.gen.Generators;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.matchers.Matchers;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeContext;

public class GumTreeDiff {
	private String src;
	private String dst;
	
	private Set<ITree> srcUpdTrees;
//    protected Set<ITree> dstUpdTrees;
    private Set<ITree> srcMovTrees;
//    protected Set<ITree> dstMovTrees;
    private Set<ITree> srcDelTrees;
    private Set<ITree> dstInsTrees;
    
    public TreeContext srcContext;
    public TreeContext dstContext;
	
	public GumTreeDiff(String src, String dst) {
		this.src = src;
		this.dst = dst;
		this.srcUpdTrees = new HashSet<ITree>();
//		this.dstUpdTrees = new HashSet<ITree>();
		this.srcMovTrees = new HashSet<ITree>();
//		this.dstMovTrees = new HashSet<ITree>();
		this.srcDelTrees = new HashSet<ITree>();
		this.dstInsTrees = new HashSet<ITree>();
	}
	
	public List<Action> diff() throws UnsupportedOperationException, IOException {
		Run.initGenerators();
		srcContext = Generators.getInstance().getTree(src);
		dstContext = Generators.getInstance().getTree(dst);
		ITree srcTree = srcContext.getRoot();
		ITree dstTree = dstContext.getRoot();
		Matcher m = Matchers.getInstance().getMatcher(srcTree, dstTree); // retrieve the default matcher
		m.match();
		ActionGenerator g = new ActionGenerator(srcTree, dstTree, m.getMappings());
		g.generate();
		
		return g.getActions();
	}
	
	public void cluster(List<Action> raw) {
		// adjust actions
		
		
		// cluster
		for(Action act : raw) {
			if(act instanceof Insert) {
				Insert ins = (Insert)act;
				ITree node = ins.getNode();
				this.dstInsTrees.add(node);
			} else if (act instanceof Delete) {
				Delete del = (Delete)act;
				ITree node = del.getNode();
				this.srcDelTrees.add(node);
			} else if (act instanceof Move) {
				Move mov = (Move)act;
				ITree node = mov.getNode();
				this.srcMovTrees.add(node);
			} else if (act instanceof Update) {
				Update upd = (Update)act;
				ITree node = upd.getNode();
				this.srcUpdTrees.add(node);
			}
		}
		
		Set<ITree> tmp = new HashSet<ITree>();
		for(ITree node : dstInsTrees) {
			if(!dstInsTrees.contains(node.getParent())) {
				tmp.add(node);
			}
		}
		
		dstInsTrees.retainAll(tmp);
		
		tmp.clear();
		for(ITree node : srcDelTrees) {
			if(!srcDelTrees.contains(node.getParent())) {
				tmp.add(node);
			}
		}
		
		srcDelTrees.retainAll(tmp);
		
		tmp.clear();
		for(ITree node : srcMovTrees) {
			if(!srcMovTrees.contains(node.getParent())) {
				tmp.add(node);
			}
		}
		
		srcMovTrees.retainAll(tmp);
		
		tmp.clear();
		for(ITree node : srcUpdTrees) {
			if(!srcUpdTrees.contains(node.getParent())) {
				tmp.add(node);
			}
		}
		
		srcUpdTrees.retainAll(tmp);
		
		// filter actions
		List<Action> simplied = new ArrayList<Action>();
		for(Action act : raw) {
			if(act instanceof Insert) {
				Insert ins = (Insert)act;
				ITree node = ins.getNode();
				if(dstInsTrees.contains(node)) {
					simplied.add(act);
				}
			} else if (act instanceof Delete) {
				Delete del = (Delete)act;
				ITree node = del.getNode();
				if(srcDelTrees.contains(node)) {
					simplied.add(del);
				}
			} else if (act instanceof Move) {
				Move mov = (Move)act;
				ITree node = mov.getNode();
				if(srcMovTrees.contains(node)) {
					simplied.add(mov);
				}
			} else if (act instanceof Update) {
				Update upd = (Update)act;
				ITree node = upd.getNode();
				if(srcUpdTrees.contains(node)) {
					simplied.add(upd);
				}
			}
		}
	}
	
	public void print(List<Action> actions) {
		for(Action act : actions) {
			if(act instanceof Insert) {
				Insert ins = (Insert)act;
				ITree node = ins.getNode();
				ITree parent = ins.getParent();
				if(node.getLabel().isEmpty() && node.getType() == 78) {
					System.out.println("Insert MarkerAnnotation: " + node.getChild(0).getLabel() + " into " + parent.toPrettyString(dstContext));
				} else {
					System.out.println("Insert " + node.toPrettyString(srcContext) + " into " + parent.toPrettyString(srcContext));
				}
				
			} else if (act instanceof Delete) {
				Delete del = (Delete)act;
				ITree node = del.getNode();
				ITree parent = node.getParent();
				System.out.println("Delete " + node.toPrettyString(srcContext) + " at " + parent.toPrettyString(srcContext));
			} else if (act instanceof Move) {
				Move mov = (Move)act;
				ITree node = mov.getNode();
				ITree parent = mov.getParent();
				System.out.println("Move " + node.toPrettyString(srcContext) + " into " + parent.toPrettyString(dstContext));
			} else if (act instanceof Update) {
				Update upd = (Update)act;
				ITree node = upd.getNode();
				String value = upd.getValue();
				System.out.println("Update " + node.toPrettyString(srcContext) + " to " + value);
			}
		}
	}
	
	public static void main(String[] args) throws UnsupportedOperationException, IOException {	
		String file1 = "/home/troy/research/Integration-Study/sample/so-1/so-10525288-3.java";
		String file2 = "/home/troy/research/Integration-Study/sample/so-1/carved-gh-235-272.java";
		GumTreeDiff diff = new GumTreeDiff(file1, file2);
		List<Action> actions = diff.diff();
		System.out.println(actions.size());
		diff.print(actions);
	}
}
