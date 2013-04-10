package de.fub.agg2graph.input;
import java.util.ArrayList;
import java.util.List;

import de.fub.agg2graph.agg.AggNode;


public class AggTestToFile {

//	static List<ArrayList<AggNode>> myResult = new ArrayList<ArrayList<AggNode>>();
//	
//	static List<ArrayList<AggNode>> getSerialize(AggNode root) {
//		List<ArrayList<AggNode>> result = new ArrayList<ArrayList<AggNode>>();
//		if(root != null) {
//			ArrayList<AggNode> trace = new ArrayList<AggNode>();
//			result.add(trace);
//			trace.add(root);
//			if(root.getOut().size() == 1)
//				continueBranch(result, trace, root.getOut().get(0));
//			else if(root.getOut().size() > 1) {
//				continueBranch(result, trace, root.getOut().get(0));
//				for(int i = 1; i < root.getOut().size(); i++) {
//					ArrayList<AggNode> branchTrace = new ArrayList<AggNode>();
//					result.add(branchTrace);
//					continueBranch(result, branchTrace, root.getOut().get(i));
//				}
//			}
//		}
//		
//		return result;
//	}
//	
//	static void continueBranch(List<ArrayList<AggNode>> tree, ArrayList<AggNode> trace, AggNode node) {
//		trace.add(node);
//		ArrayList<AggNode> out = null;
//		if(node.getOut().size() == 1) {
//			out = new ArrayList<AggNode>(node.getOut());
//		}
//			continueBranch(tree, trace, node.getOut().get(0));
//		else if(node.getOut().size() > 1) {
//			continueBranch(tree, trace, node.getOut().get(0));
//			for(int i = 1; i < node.getOut().size(); i++) {
//				ArrayList<AggNode> branchTrace = new ArrayList<AggNode>();
//				tree.add(branchTrace);
//				continueBranch(tree, branchTrace, node.getOut().get(i));
//			}
//		}
//	}
	
	public static void main(String[] args) {
//		Node a1 = new Node(1);
//		Node a2 = new Node(2);
//		Node a3 = new Node(3);
//		Node a4 = new Node(4);
//		Node a5 = new Node(5);
//		Node a6 = new Node(6);
//		Node a7 = new Node(7);
//		Node a8 = new Node(8);
//		Node a9 = new Node(9);
//		Node a10 = new Node(10);
//		Node a11 = new Node(11);
//		Node a12 = new Node(12);
//		Node a13 = new Node(13);
//		Node a14 = new Node(14);
//		Node a15 = new Node(15);
//		Node a16 = new Node(16);
//		Node a17 = new Node(17);
//		Node a18 = new Node(18);
//		Node a19 = new Node(19);
//		Node a20 = new Node(20);
//		
//		a1.children.add(a2);
//		a1.children.add(a8);
//		a1.children.add(a11);
//		a1.children.add(a12);
//		a2.children.add(a3);
//		a2.children.add(a5);
//		a2.children.add(a7);
//		a3.children.add(a4);
//		a5.children.add(a6);
//		a8.children.add(a9);
//		a8.children.add(a10);
//		a11.children.add(a13);
//		a11.children.add(a14);
//		a11.children.add(a15);
//		a13.children.add(a16);
//		a16.children.add(a17);
//		a17.children.add(a18);
//		a17.children.add(a19);
//		a14.children.add(a20);
//		
//		myResult = getSerialize(a1);
//		System.out.println(myResult);
	}

}
