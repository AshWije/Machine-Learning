/*
 * Node.java
 * Date: 2018
 * 
 * Description:
 * 		This file contains the class definition for Node.
 * 
 * 		The Node class represents a node in the decision tree and is utilized by the DecisionTree class.
 * 
 * 		Each Node is responsible for maintaining its left and right child nodes (if any), its parent node,
 * 			the attribute that it split on (if any), the attributes that its parents split on, its entropy
 * 			value, the percent of 0s that are found at it, and whether it is a leaf or not.
 * 
 * 		Nodes are also responsible for determining if a given attribute is found in their parents.
 * 
 */

public class Node {
	public Node left, right, parent;
	public int currentAttrIndex, currentAttrVal, Class, numTot;
	public int[][] parentAttrRels;
	public double entropy, percent0s;
	public boolean leaf;
	
	// default constructor for Node
	public Node() {
		left = null;
		right = null;
		parent = null;
		currentAttrIndex = -1;
		currentAttrVal = -1;
		parentAttrRels = null;
		percent0s = 0;
		Class = -1;
		entropy = 0.0;
		numTot = 0;
		leaf = false;
	}
	
	// constructor for Node
	public Node(Node p, double h, int n, int val, double p0, int C, boolean l) {
		parent = p;
		left = null;
		right = null;
		currentAttrIndex = -1;
		currentAttrVal = val;
		percent0s = p0;
		Class = C;
		entropy = h;
		numTot = n;
		leaf = l;
		
		// if parent is not the root
		if(p.parentAttrRels != null)
		{
			// add parent's attribute and value to the matrix
			parentAttrRels = new int[p.parentAttrRels.length + 1][2];
			parentAttrRels[0][0] = p.currentAttrIndex;
			parentAttrRels[0][1] = val;
			
			// add parent's parent attributes and value to the matrix
			for(int i = 1; i < p.parentAttrRels.length + 1; i++)
				for(int j = 0; j < 2; j++)
					parentAttrRels[i][j] = p.parentAttrRels[i-1][j];
		}
		
		// if parent is the root
		else {
			// add the root's attribute and value to the matrix
			parentAttrRels = new int[1][2];
			parentAttrRels[0][0] = p.currentAttrIndex;
			parentAttrRels[0][1] = val;
		}
	}
	
	// assign the child nodes
	public void setChildren(Node l, Node r) {
		left = l;
		right = r;
	}
	
	// determine if an attribute is found in the parents
	public boolean attrInParent(int attr)
	{
		// if the node is not the root
		if(parentAttrRels != null) {
			// loop through parent attributes
			for(int i = 0; i < parentAttrRels.length; i++)
				// if the attribute is found, return true
				if(parentAttrRels[i][0] == attr)
					return true;
		}
		
		// return false if the attribute is not found
		return false;
	}
}
