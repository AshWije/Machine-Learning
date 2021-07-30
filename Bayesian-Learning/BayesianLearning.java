/*
 * BayesianLearning.java
 * Date: 2018
 * 
 * Description:
 * 		This file contains the class definition for BayesianLearning.
 * 
 * 		The BayesianLearning class maintains a three-dimensional matrix of conditional probabilities, an
 * 			array of the two class values, and a String array of the attributes found in the training/test
 * 			set (used for displaying).
 * 
 * 		It is also responsible for training, testing, and displaying the results that are produced based
 * 			on the user's provided training set and test set.
 * 
 */

import java.io.*;
import java.util.Scanner;

public class BayesianLearning {
	private String attributes[];
	private double[][][] p;
	private double[] c;
	
	/* Description:	Trains using bayesian learning with the provided training set file and number of instances
	 *
	 * Input:		Training set file (trainingFile)
	 *				Number of instances (numInstances)
	 *
	 * Output:		N/A
	 * 
	 * Effect:		Assigns the entire array of attribute names (attributes)
	 *				Assigns the entire matrix of conditional probabilities (p)
	 *				Assigns the entire array of class probabilities (c)
	 */
	public void train(String trainingFile, int numInstances) throws FileNotFoundException
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
		int[][] trainingSet = new int[numInstances][attributes.length];
		for(int i = 0; i < numInstances; i++) {
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
		int totNum[] = new int[2];
		totNum[0] = 0;
		totNum[1] = 0;
		for(int i = 0; i < numInstances; i++)
		{
			int classOfInstance = trainingSet[i][attributes.length-1]; 
			if(classOfInstance == 0)
				totNum[0]++;
			else if(classOfInstance == 1)
				totNum[1]++;
		}

		// set up matrix for conditional probabilities, such that p[i][j][k] corresponds to P(i=j|k)
		p = new double[attributes.length-1][2][2];
		
		// set up array for class probabilities
		c = new double[2];
		
		// assign the values for P(C=0) and P(C=1)
		c[0] = (double)totNum[0] /(double)(totNum[0]+totNum[1]);
		c[1] = (double)totNum[1] /(double)(totNum[0]+totNum[1]);
		
		// loop through attributes
		for(int curAttr = 0; curAttr < attributes.length-1; curAttr++)
		{
			// loop through 2 attribute values
			for(int attrVal = 0; attrVal < 2; attrVal++)
			{
				// loop through 2 classes
				for(int classVal = 0; classVal < 2; classVal++)
				{
					// determine the number of instances matching in class value and attribute value
					int num = 0;
					for(int i = 0; i < numInstances; i++)
					{
						int classOfInstance = trainingSet[i][attributes.length-1];
						int attrOfInstance = trainingSet[i][curAttr];
						if(classOfInstance == classVal && attrOfInstance == attrVal)
							num++;
					}
					
					// assign the conditional probability
					if(totNum[classVal] != 0)
						p[curAttr][attrVal][classVal] = (double)num / (double)totNum[classVal];
					else
						p[curAttr][attrVal][classVal] = 0;
				}
			}
		}
	}
	
	/* Description: Tests the results on the test file and returns the accuracy
	 * 
	 * Input:		Test set file (testFile)
	 * 
	 * Output:		Accuracy of the training results on the test file
	 * 
	 * Effect:		N/A
	 */
	public double test(String testFile) throws FileNotFoundException
	{
		// create a Scanner for reading the test data
		Scanner sc = new Scanner(new File(testFile));
		
		// ignore first line (attributes)
		String next = sc.nextLine();
		
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
		
		// calculate the accuracy
		double accuracy = ((double)numCorrect / (double)numInstances) * 100;
		
		// return the accuracy
		return accuracy;
	}

	/* Description: Determine if the provided instance is classified correctly; utilized by the test method
	 * 
	 * Input:		Array containing the instance values for the attributes and class (splitLine)
	 * 
	 * Output:		True or false
	 * 
	 * Effect:		N/A
	 */
	public boolean checkInstance(String[] splitLine)
	{
		// calculate naive bayes classifiers
		double[] nb = new double[2];
		nb[0] = 1;
		nb[1] = 1;
		
		// loop through 2 classes
		for(int classVal = 0; classVal < 2; classVal++)
		{
			// loop through attributes and multiply conditional probabilities
			for(int i = 0; i < attributes.length-1; i++)
				nb[classVal] *= p[i][Integer.parseInt(splitLine[i])][classVal];
			
			// multiply class probability
			nb[classVal] *= c[classVal];
		}
		
		// class 0 is selected
		if(nb[0] >= nb[1])
		{
			// if the correct class is 0
			if(Integer.parseInt(splitLine[attributes.length-1]) == 0)
				return true;
			
			// if the correct class is 1
			else
				return false;
		}
		
		// class 1 is selected
		else
		{
			// if the correct class is 1
			if(Integer.parseInt(splitLine[attributes.length-1]) == 1)
				return true;
			
			// if the correct class is 0
			else
				return false;
		}
	}
	
	/* Description: Display the results
	 * 
	 * Input:		N/A
	 * 
	 * Output:		All class and conditional probabilities are displayed appropriately
	 * 
	 * Effect:		N/A
	 */
	public void display()
	{
		// loop through 2 classes
		for(int classVal = 0; classVal < 2; classVal++)
		{
			// print class probabilities
			System.out.printf("P(%s=%d)=%.2f ", attributes[attributes.length-1], classVal, c[classVal]);
			
			// loop through attributes
			for(int curAttr = 0; curAttr < attributes.length-1; curAttr++) {
				// loop through 2 attribute values
				for(int attrVal = 0; attrVal < 2; attrVal++)
					// print conditional probabilities
					System.out.printf("P(%s=%d|%d)=%.2f ", attributes[curAttr], attrVal, classVal, p[curAttr][attrVal][classVal]);
			}
			
			// print new line
			System.out.print("\n");
		}
	}
}
