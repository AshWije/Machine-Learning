/*
 * DecisionTree.java
 * Date: 2018
 * 
 * Description:
 * 		This file contains the class definition for DecisionTree.
 * 
 * 		The DecisionTree class represents the decision tree and maintains its root node and a String
 * 			of the attributes found in the training/test set (used for displaying).
 * 
 * 		It is also responsible for training, testing, and displaying the decision tree that is produced
 * 			based on the user's provided training set, test set, and maximum number of training instances.
 * 
 */

import java.io.*;
import java.util.Scanner;
import java.util.LinkedList;
import java.util.Queue;

public class DecisionTree {
	private Node root;
	private String attributes[];
	
	// default constructor for DecisionTree
	public DecisionTree() {
		root = new Node();
	}
	
	// train the decision tree with the provided training set and number of instances
	public void train(String trainingFile, int maxTrainingInstances) throws FileNotFoundException
	{
		// create a Scanner for reading the training data
		Scanner sc = new Scanner(new File(trainingFile));
		
		// skip any empty lines
		String next = sc.nextLine();
		while(next.trim().isEmpty())
			next = sc.nextLine();
		
		// identifies the classes from the first line of the training data
		attributes = next.split("\\s+");
		
		// set up a training set matrix
		int[][] trainingSet = new int[maxTrainingInstances][attributes.length];
		for(int i = 0; i < maxTrainingInstances; i++) {
			next = sc.nextLine();
			if(!next.trim().isEmpty()) {
				String[] splitLine = next.split("\\s+");
				for(int j = 0; j < attributes.length; j++)
					trainingSet[i][j] = Integer.parseInt(splitLine[j]);
			}
			else
				i--;
		}
		sc.close();
		
		// determine the number of 0 classes and 1 classes
		int num0s = 0;
		int num1s = 0;
		for(int i = 0; i < maxTrainingInstances; i++)
		{
			int classOfInstance = trainingSet[i][attributes.length-1]; 
			if(classOfInstance == 0)
				num0s++;
			else if(classOfInstance == 1)
				num1s++;
		}
		
		// calculate the percents of 0 and 1 classes
		double percent0s = (double)num0s / (double)(num0s + num1s);
		double percent1s = (double)num1s / (double)(num0s + num1s);
		
		// calculate logs of the percents appropriately
		double log0s = 0.0;
		double log1s = 0.0;
		
		if(percent0s != 0)
			log0s = (Math.log(percent0s)/Math.log(2));
		if(percent1s != 0)
			log1s = (Math.log(percent1s)/Math.log(2));
		
		// set entropy of the root, the total number of instances, and the percent of 0 class classifications
		root.entropy = (-1.0 * percent0s * log0s) - (percent1s * log1s);
		root.numTot = num0s + num1s;
		root.percent0s = percent0s;
		
		// create a queue for looping through the nodes and add the root to the queue to begin
		Queue<Node> q = new LinkedList<>();
		q.add(root);
		
		// while the queue is not empty
		while(q.peek() != null)
		{
			// remove a node from the queue
			Node parent = q.remove();
			
			// make sure that the node is not a leaf node (do not need to split a leaf node)
			if(!parent.leaf)
			{
				// set up arrays
				double[] IG = new double[attributes.length - 1];
				double[] entropyL = new double[attributes.length - 1];
				double[] entropyR = new double[attributes.length - 1];
				int[] numL = new int[attributes.length - 1];
				int[] numR = new int[attributes.length - 1];
				double[] percent0sL = new double[attributes.length - 1];
				double[] percent0sR = new double[attributes.length - 1];
				int[] pureL = new int[attributes.length - 1];
				int[] pureR = new int[attributes.length - 1];
				
				// loop through the attributes
				for (int attr = 0; attr < attributes.length - 1; attr++)
				{
					// initialize values for particular attribute in arrays
					entropyL[attr] = 0.0;
					entropyR[attr] = 0.0;
					numL[attr] = 0;
					numR[attr] = 0;
					percent0sL[attr] = 0.0;
					percent0sR[attr] = 0.0;
					pureL[attr] = -1;
					pureR[attr] = -1;
					
					// make sure that the attribute being considered hasn't been used earlier in the tree
					if(!parent.attrInParent(attr))
					{
						// loop twice, once for when the attribute=0 and once for when the attribute=1
						for(int attrSet = 0; attrSet < 2; attrSet++)
						{
							// determine the number of 0 classes and 1 classes
							num0s = 0;
							num1s = 0;
							for(int i = 0; i < maxTrainingInstances; i++)
							{
								// make sure all attributes of parents are accounted for in instance
								boolean consider = true;
								if(parent.parentAttrRels != null) {
									for(int j = 0; j < parent.parentAttrRels.length && consider; j++)
										if(trainingSet[i][parent.parentAttrRels[j][0]] != parent.parentAttrRels[j][1])
											consider = false;
								}
								
								// increment the number of 0s or 1s dependent on the class
								if(consider && trainingSet[i][attr] == attrSet)
								{
									int classOfInstance = trainingSet[i][attributes.length-1];
									if(classOfInstance == 0)
										num0s++;
									else if(classOfInstance == 1)
										num1s++;
								}
							}
							
							// if there are no instances of this case, use the entire tree to calculate the percents of 0 and 1 classes
							if (num0s + num1s == 0) {
								percent0s = root.percent0s;
								percent1s = 1 - root.percent0s;
							}
							
							// if there are instances of this case, use the instances to calculate the percents of 0 and 1 classes
							else {
								percent0s = (double)num0s / (double)(num0s + num1s);
								percent1s = (double)num1s / (double)(num0s + num1s);
							}
							
							// calculate logs of the percents appropriately
							log0s = 0.0;
							log1s = 0.0;
							
							if(percent0s != 0)
								log0s = (Math.log(percent0s)/Math.log(2));
							if(percent1s != 0)
								log1s = (Math.log(percent1s)/Math.log(2));
							
							// if first time looping, attrSet=0
							if(attrSet == 0)
							{
								// set values appropriately
								entropyL[attr] = (-1.0 * percent0s * log0s) - (percent1s * log1s);
								numL[attr] = num0s + num1s;
								percent0sL[attr] = percent0s;
								
								// if the node is pure, set pureL to the class of the node
								if(percent0s == 1.0)
									pureL[attr] = 0;
								else if(percent1s == 1.0)
									pureL[attr] = 1;
							}
							
							// if second time looping, attrSet=1
							else
							{
								// set values appropriately
								entropyR[attr] = (-1.0 * percent0s * log0s) - (percent1s * log1s);
								numR[attr] = num0s + num1s;
								percent0sR[attr] = percent0s;
								
								// if the node is pure, set pureR to the class of the node
								if(percent0s == 1.0)
									pureR[attr] = 0;
								else if(percent1s == 1.0)
									pureR[attr] = 1;
							}
						}
						
						// calculate the information gain
						IG[attr] = parent.entropy - ((entropyL[attr]*((double)numL[attr]/(double)parent.numTot)) + (entropyR[attr]*((double)numR[attr]/(double)parent.numTot)));
					}
					
					// if the attribute being considered has been used earlier in the tree, do not consider it
					else
						IG[attr] = -1;
				}
				
				// determine the max information gain possible
				int indexOfMax = 0;
				for(int i = 1; i < IG.length; i++) {
					if(IG[i] > IG[indexOfMax])
						indexOfMax= i;
				}
				
				// if the information gain is not beneficial, do not split at the attribute
				if(IG[indexOfMax] < 0)
				{
					// the node must be an impure leaf
					parent.leaf = true;
					
					// set the class of the node to be the most frequent class
					if(parent.percent0s > 0.5)
						parent.Class = 0;
					else if (parent.percent0s < 0.5)
						parent.Class = 1;
					else
						if(root.percent0s >= 0.5)
							parent.Class = 0;
						else
							parent.Class = 1;
				}
				
				// if the information gain is beneficial, split at the attribute
				else
				{
					// set the attribute that was split on
					parent.currentAttrIndex = indexOfMax;
					
					boolean pL = false;
					boolean pR = false;
					int ClassL = -1;
					int ClassR = -1;
					
					//determine if nodes are pure and assign their classes if they are pure
					if(pureL[indexOfMax] != -1) {
						pL = true;
						ClassL = pureL[indexOfMax];
					}
					if(pureR[indexOfMax] != -1) {
						pR = true;
						ClassR = pureR[indexOfMax];
					}
					
					// create the nodes
					Node left = new Node(parent, entropyL[indexOfMax], numL[indexOfMax], 0, percent0sL[indexOfMax], ClassL, pL);
					Node right = new Node(parent, entropyR[indexOfMax], numR[indexOfMax], 1, percent0sR[indexOfMax], ClassR, pR);
					
					// assign the nodes as children of the parent
					parent.setChildren(left, right);
					
					// add the nodes to the queue
					q.add(left);
					q.add(right);
				}
			}
		}
	}
	
	// tests the decision tree on the test file and returns the accuracy
	public double test(String testFile) throws FileNotFoundException
	{
		// create a Scanner for reading the test data
		Scanner sc = new Scanner(new File(testFile));
		
		// ignore empty lines and the first line (attributes)
		String next = sc.nextLine();
		while(next.trim().isEmpty())
			next = sc.nextLine();
		
		// keep track of the total number of instances and the number of correct classifications
		int numInstances = 0;
		int numCorrect = 0;
		
		// loop through the lines of the test file
		while(sc.hasNextLine())
		{
			// ignore empty lines
			next = sc.nextLine();
			if(!next.trim().isEmpty())
			{
				// increment the total number of instances
				numInstances++;
				
				// get the next line of the test file
				String[] splitLine = next.split("\\s+");
				
				// determine if the next instance is classified correctly, if it is increment the number of correct classifications
				if(checkInstance(splitLine))
					numCorrect++;
			}
		}
		sc.close();
		
		// calculate the accuracy of the decision tree
		double accuracy = ((double)numCorrect/(double)numInstances) * 100;
		
		// return the accuracy of the decision tree on the test file
		return accuracy;
	}

	// determine if the instance is classified correctly
	public boolean checkInstance(String[] splitLine)
	{
		// begin with the root
		Node next = root;
		
		// loop through nodes until a leaf is found
		while(next != null && !next.leaf)
		{
			// check whether the attribute from the node is 0 or 1 in the instance, if 0, move to the left, if 1, move to the right 
			if(Integer.parseInt(splitLine[next.currentAttrIndex]) == 0)
				next = next.left;
			else
				next = next.right;
		}
		
		// if a leaf was reached and the class of the instance is equal to the class of the leaf, then return true, otherwise return false
		if(next != null && next.leaf && next.Class == Integer.parseInt(splitLine[splitLine.length - 1]))
			return true;
		else
			return false;
	}
	
	// traverse the decision tree from a node while outputting that node's details and tracking the depth
	public void DFS(Node n, int depth)
	{
		// if n is not null or undefined
		if(n != null && n.currentAttrIndex != -1)
		{
			// loop through the depth of the node and output the corresponding number of '|'s
			for(int i = 0; i < depth; i++)
				System.out.print("| ");
			
			// output the attribute that node n corresponds to and traverse the tree if that attribute is 0 (to the left)
			System.out.print(attributes[n.currentAttrIndex] + " = " + 0 + " :");
			
			// if the left node is a leaf, output the class of the leaf, otherwise continue traversing tree and increment the depth
			if(n.left.leaf)
				System.out.println("  " + n.left.Class);
			else {
				System.out.print("\n");
				DFS(n.left, depth + 1);
			}
			
			// loop through the depth of the node and output the corresponding number of '|'s
			for(int i = 0; i < depth; i++)
				System.out.print("| ");
			
			// output the attribute that node n corresponds to and traverse the tree if that attribute is 1 (to the right)
			System.out.print(attributes[n.currentAttrIndex] + " = " + 1 + " :");
			
			// if the right node is a leaf, output the class of the leaf, otherwise continue traversing tree and increment the depth
			if(n.right.leaf)
				System.out.println("  " + n.right.Class);
			else {
				System.out.print("\n");
				DFS(n.right, depth + 1);
			}
		}
	}
	
	// display the decision tree
	public void display() {
		// call DFS on the root of the decision tree and start the depth at 0
		DFS(root, 0);
	}
}

