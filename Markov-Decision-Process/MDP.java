/*
 * MDP.java
 * Date: 2018
 * 
 * Description:
 * 		This file contains the class definition for MDP representing a Markov Decision Process.
 * 
 * 		The MDP class maintains the number of iterations to be run, the number of states in the input
 * 			file, the number of actions in the input file, two arrays of the state names, and the action
 * 			names, an array of the rewards associated with each state, a three-dimensional matrix of the
 * 			possible actions, and two two-dimensional matrices of the J values and optimal policy actions
 * 			respectively.
 * 
 * 		It is also responsible for running value iteration using Bellman's Equation and displaying the J
 * 			values and optimal policies that are produced based on the user's provided number of iterations.
 * 
 */

import java.io.*;
import java.util.Scanner;

public class MDP {
	int numIterations, numStates, numActions;
	String[] stateNames;
	String[] actionNames;
	double[] reward;
	double[][][] action;
	double[][] J;
	int[][] op;
	
	/* 
	 * Description:	Constructor that creates the MDP object
	 *
	 * Input:		Number of states (states)
	 * 				Maximum number of actions (maxActions)
	 * 				Number of iterations (iterations)
	 *
	 * Output:		N/A
	 * 
	 * Effect:		Assigns the number of states (numStates)
	 *				Assigns the number of actions (numActions)
	 *				Assigns the number of iterations (numIterations)
	 *				Assigns the entire array of state names (stateNames)
	 *				Assigns the entire array of action names (actionNames)
	 *				Assigns the entire array of rewards using the input file (reward)
	 *				Assigns the entire matrix of actions using the input file (action)
	 *				Initializes the matrix of J values (J)
	 *				Initializes the matrix of optimal policies (op)
	 */
	public MDP(int states, int maxActions, int iterations, String inputFile) throws FileNotFoundException
	{
		// set number of states and iterations
		numStates = states;
		numIterations = iterations;
		
		// initialize the state names and action names arrays
		stateNames = new String[numStates];
		actionNames = new String[maxActions];
		
		// initialize the reward array and the action matrix
		reward = new double[numStates];
		action = new double[numStates][numStates][maxActions];
		
		// initialize all action probabilities with zeroes
		for(int i = 0; i < numStates; i++) {
			for(int j = 0; j < numStates; j++) {
				for(int k = 0; k < maxActions; k++)
					action[i][j][k] = 0;
			}
		}
		
		// create a Scanner for reading the state names
		Scanner sc1 = new Scanner(new File(inputFile));
		
		// loop through states
		for(int i = 0; i < numStates; i++) {
			String next = sc1.nextLine();
			if(!next.trim().isEmpty()) {
				String[] splitLine = next.split("\\s+");
				
				// set state name
				stateNames[i] = splitLine[0];
			}
			else
				i--;
		}
		
		sc1.close();
		
		// create a Scanner for reading the input file
		Scanner sc2 = new Scanner(new File(inputFile));
		
		// keep track of the actions
		numActions = 0;
		
		// set rewards and actions based on input file
		for(int i = 0; i < numStates; i++) {
			String next = sc2.nextLine();
			if(!next.trim().isEmpty()) {
				String[] splitLine = next.split("\\s+");
				
				// set rewards
				reward[i] = Double.parseDouble(splitLine[1]);
				
				// loop through possible actions/probabilities
				for(int j = 2; j < splitLine.length; j = j+3) {
					// get the action name
					String actionName = splitLine[j].substring(1);
					
					// determine if the action exists in the list of action names and get that action, if it does
					int a = findAction(actionName);
					
					// if the action has not been previously used, add it to the list of action names
					if(a == -1) {
						actionNames[numActions] = actionName;
						a = numActions;
						numActions++;
					}
					
					// get the index for the state
					int stateTo = findState(splitLine[j+1]);
					
					// get the probability for the action
					double prob = Double.parseDouble(splitLine[j+2].substring(0, splitLine[j+2].length() - 1));
					
					// set the action
					action[i][stateTo][a] = prob;
				}
			}
			else
				i--;
		}
		sc2.close();
		
		// initialize J and optimal policy matrix
		J = new double[numIterations][numStates];
		op = new int[numIterations][numStates];
	}
	
	/* 
	 * Description:	Searches the list of action names for a particular name; utilized by the constructor
	 *
	 * Input:		The name of the action being searched for (actionName)
	 *
	 * Output:		The index in the state names list, if found
	 * 				-1, if not found
	 * 
	 * Effect:		N/A
	 */
	int findAction(String actionName)
	{
		// loop through the actions
		for(int i = 0; i < numActions; i++)
		{
			// if the action is found, return the index
			if(actionNames[i].equals(actionName))
				return i;
		}
		
		// if the action is not found, return -1
		return -1;
	}
	
	/* 
	 * Description:	Searches the list of state names for a particular name; utilized by the constructor
	 *
	 * Input:		The name of the state being searched for (stateName)
	 *
	 * Output:		The index in the action names list, if found
	 *				-1, if not found
	 * 
	 * Effect:		N/A
	 */
	int findState(String stateName)
	{
		// loop through the states
		for(int i = 0; i < numStates; i++)
		{
			// if the state is found, return the index
			if(stateNames[i].equals(stateName))
				return i;
		}
		
		// if the state is not found, return -1
		return -1;
	}
	
	/* 
	 * Description:	Runs value iteration on the Markov Decision Process using Bellman's equation
	 *
	 * Input:		The discount factor (discountFactor))
	 *
	 * Output:		N/A
	 * 
	 * Effect:		Assigns the entire matrix of J values (J)
	 *				Assigns the entire matrix of optimal policies (op)
	 */
	void valueIteration(double discountFactor)
	{	
		// loop through iterations
		for(int i = 0; i < numIterations; i++)
		{
			// loop through states
			for(int state = 0; state < numStates; state++)
			{
				// first iteration
				if(i == 0) {
					// set J
					J[i][state] = reward[state];
					
					// set optimal policy (select any action available)
					boolean found = false;
					for(int stateTo = 0; stateTo < numStates && !found; stateTo++)
						for(int a = 0; a < numActions && !found; a++)
							if(action[state][stateTo][a] > 0) {
								op[i][state] = a;
								found = true;
							}
				}
				
				// after the first iteration
				else {
					// create two-dimensional matrix of possible J values
					// 		Jtemp[a][0] will equal 1 if action a is possible, otherwise it is 0
					//		Jtemp[a][1] will equal the J value if action a is taken
					double[][] Jtemp = new double[numActions][2];
					
					// initialize Jtemp values to zero
					for(int a = 0; a < numActions; a++)
						for(int v = 0; v < 2; v++)
							Jtemp[a][v] = 0;
					
					// loop through all actions
					for(int a = 0; a < numActions; a++)
					{
						// loop through all states
						for(int stateTo = 0; stateTo < numStates; stateTo++)
						{
							// if an action from state to stateTo is possible
							if(action[state][stateTo][a] > 0)
								Jtemp[a][0] = 1;
							
							// summation of actions and previous J values
							Jtemp[a][1] += (action[state][stateTo][a] * J[i-1][stateTo]);
						}
						
						// calculation of possible J value
						Jtemp[a][1] *= discountFactor;
						Jtemp[a][1] += reward[state];
					}
					
					// initially no action is selected as the best action
					int bestAction = 0;
					boolean bestActionDefined = false;
					
					// loop through actions
					for(int a = 0; a < numActions; a++)
					{
						// if either no valid action has been selected yet or the J value of the selected best action is less than that of the valid action a, set the best action to be action a
						if(Jtemp[a][0] == 1 && ((!bestActionDefined) || (Jtemp[a][1] > Jtemp[bestAction][1]))) {
							bestAction = a;
							bestActionDefined = true;
						}
					}
					
					// set J and optimal policy
					J[i][state] = Jtemp[bestAction][1];
					op[i][state] = bestAction;
				}
			}
		}
	}
	
	/* 
	 * Description: Display the J values and optimal policies for each state of the Markov Decision Process for the given number of iterations
	 * 
	 * Input:		N/A
	 * 
	 * Output:		J value for each state of the Markov Decision Process for the number of iterations
	 * 				Optimal policy for each state of the Markov Decision Process for the number of iterations
	 * 
	 * Effect:		N/A
	 */
	void display()
	{
		// loop through iterations
		for(int i = 0; i < numIterations; i++)
		{
			// print at the beginning of each iteration
			System.out.printf("After iteration %d: ", i+1);
			
			// loop through states and print state, optimal policy, and J value
			for(int state = 0; state < numStates; state++)
				System.out.printf("(%s %s %.4f) ", stateNames[state], actionNames[op[i][state]], J[i][state]);
			
			// print new line
			System.out.printf("\n");
		}
	}
}
