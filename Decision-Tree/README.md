# Decision Tree

This folder contains the class definitions for **DecisionTree** and **Node**.

The **DecisionTree** class represents the decision tree and maintains its root node and a String
of the attributes found in the training/test set (used for displaying).

It is also responsible for training, testing, and displaying the decision tree that is produced
based on the user's provided training set, test set, and maximum number of training instances.

The **Node** class represents a node in the decision tree and is utilized by the DecisionTree class.

Each node is responsible for maintaining its left and right child nodes (if any), its parent node,
the attribute that it split on (if any), the attributes that its parents split on, its entropy
value, the percent of 0s that are found at it, and whether it is a leaf or not.

Nodes are also responsible for determining if a given attribute is found in their parents.

2018