# Markov Decision Process

This folder contains the class definition for **MDP** representing a Markov Decision Process.

The **MDP** class maintains the number of iterations to be run, the number of states in the input
file, the number of actions in the input file, two arrays of the state names, and the action
names, an array of the rewards associated with each state, a three-dimensional matrix of the
possible actions, and two two-dimensional matrices of the J values and optimal policy actions
respectively.

It is also responsible for running value iteration using Bellman's Equation and displaying the J
values and optimal policies that are produced based on the user's provided number of iterations.

2018