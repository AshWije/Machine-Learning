/*
 * NeuralNetwork.java
 * Date: 2018
 * 
 * Description:
 * 		This file contains the class definition for NeuralNetwork.
 * 
 * 		The NeuralNetwork class maintains the number of training instances, iterations, and learning
 * 			rate from the user, a String array of the attributes found in the training/test set (used
 * 			for displaying), an array containing of the outputs of each iteration, a two-dimensional
 * 			matrix of the training set, and a two dimensional matrix of the weights for each attribute
 * 			during each iteration.
 * 
 * 		It is also responsible for training, testing, and displaying the results that are produced based
 * 			on the user's provided training set and test set.
 * 
 */

import java.io.*;
import java.util.Scanner;

public class NeuralNetwork {
	int numTrainingInstances, numIterations;
	double learningRate;
	String[] attributes;
	double[] output;
	int[][] trainingSet;
	double[][] weights;
	
	/* Description:	
	 *
	 * Input:		Training set file (trainingFile)
	 *				Number of instances (instances)
	 *				Number of iterations (iterations)
	 *				Learning rate (lr)
	 *
	 * Output:		N/A
	 * 
	 * Effect:		Assigns number of training instances (numTrainingInstances)
	 * 				Assigns number of iterations (numIterations)
	 * 				Assigns learning rate (learningRate)
	 * 				Assigns the entire array of attribute names (attributes)
	 * 				Initialize the array of output values (output)
	 *				Assigns the entire matrix of training set values (trainingSet)
	 *				Initialize the matrix of weights (weights)
	 */
	public NeuralNetwork(String trainingFile, int instances, int iterations, double lr) throws FileNotFoundException
	{
		// initialize variables
		numTrainingInstances = instances;
		numIterations = iterations;
		learningRate = lr;
		
		// count number of instances in training file
		Scanner sc = new Scanner(new File(trainingFile));
		
		// skip any empty lines
		String next = sc.nextLine();
		while(next.trim().isEmpty())
			next = sc.nextLine();
		
		// identifies the classes from the first line of the training data
		attributes = next.split("\\s+");
		
		// initialize weights matrix and output array with zeroes
		weights = new double[numIterations][attributes.length - 1];
		output = new double[numIterations];
		for(int i = 0; i < numIterations; i++) {
			output[i] = 0;
			for(int j = 0; j < attributes.length - 1; j++)
				weights[i][j] = 0;
		}
		
		// set up a training set matrix
		trainingSet = new int[numTrainingInstances][attributes.length];
		for(int i = 0; i < numTrainingInstances; i++) {
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
	}
	
	/* Description: Calculates the sigmoid of a value
	 * 
	 * Input:		The value (t)
	 * 
	 * Output:		The sigmoid of the input
	 * 
	 * Effect:		N/A
	 */
	public double sigmoid(double t) {
		return 1.0 / (1.0+(Math.pow(Math.E, -t)));
	}
	
	/* Description: Trains the neural network on the training set
	 * 
	 * Input:		N/A
	 * 
	 * Output:		N/A
	 * 
	 * Effect:		Assigns the entire array of outputs (output)
	 * 				Assigns the entire matrix of weights (weights)
	 */
	public void train()
	{
		// loop through iterations
		for(int i = 0; i < numIterations; i++)
		{
			// calculate the dot product
			double wx = dotProductWX(i-1, i);
			double sigmoidWX = sigmoid(wx);
			double error = trainingSet[i % numTrainingInstances][attributes.length - 1] - sigmoidWX;
			
			// loop through attributes and calculate the weights
			for(int attr = 0; attr < attributes.length - 1; attr++)
			{
				// if the first iteration, use zero as the initial value to calculate the weight, otherwise use the weight value from the previous iteration
				if(i == 0)
					weights[i][attr] = 0 + (learningRate * error * trainingSet[i % numTrainingInstances][attr] * sigmoidWX * (1.0-sigmoidWX));
				else
					weights[i][attr] = weights[i-1][attr] + (learningRate * error * trainingSet[i % numTrainingInstances][attr] * sigmoidWX * (1.0-sigmoidWX));
			}
			
			// calculate the output of the current iteration
			output[i] = sigmoid(dotProductWX(i, i));
		}
	}

	/* Description: Calculates the dot product of the weights and the training set instance on a given iteration; utilized during training
	 * 
	 * Input:		Iteration of the weights to consider (weightIndex)
	 * 				Current iteration (iteration)
	 * 
	 * Output:		Dot product of the weights and the training set instance
	 * 
	 * Effect:		N/A
	 */
	public double dotProductWX(int weightIndex, int iteration)
	{
		// if this is the first iteration, return 0
		if(weightIndex < 0)
			return 0;
		
		// initialize dot product to zero
		double dp = 0;
		
		// loop through attributes
		for(int a = 0; a < attributes.length - 1; a++)
			// increment dot product by the weight * instance for each attribute
			dp += (weights[weightIndex][a] * trainingSet[iteration % numTrainingInstances][a]);
		
		// return the dot product
		return dp;
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
		// create a Scanner for reading the test file
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
		
		// calculate the accuracy of the neural network on the test file
		double accuracy = ((double)numCorrect/(double)numInstances) * 100;
		
		// return the accuracy of the neural network on the test file
		return accuracy;
	}
	
	/* Description: Determine if the provided instance is classified correctly; utilized during testing
	 * 
	 * Input:		Array containing the instance values for the attributes and class (splitLine)
	 * 
	 * Output:		True or false
	 * 
	 * Effect:		N/A
	 */
	public boolean checkInstance(String[] splitLine)
	{
		// initialize dot product to zero
		double dp = 0;

		// ensure that there is at least one iteration
		if(numIterations > 0)
			// loop through attributes
			for(int a = 0; a < attributes.length - 1; a++)
				// increment dot product by the weight * instance for each attribute
				dp += (weights[numIterations-1][a] * Integer.parseInt(splitLine[a]));

		// if the sigmoid unit predicts the correct output return true, otherwise return false
		if(Math.round(sigmoid(dp)) == Integer.parseInt(splitLine[attributes.length-1]))
			return true;
		else
			return false;
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
		// loop through iterations
		for(int i = 0; i < numIterations; i++)
		{
			// print at the beginning of each iteration
			System.out.printf("After iteration %d: ", i+1);
			
			// loop through attributes and print weights of all attributes at the iteration
			for(int attr = 0; attr < attributes.length - 1; attr++)
				System.out.printf("w(%s) = %.4f, ", attributes[attr], weights[i][attr]);
			
			// print output
			System.out.printf("output = %.4f\n", output[i]);
		}
	}
}