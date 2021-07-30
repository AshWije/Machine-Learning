/*
 * Main.java
 * Date: 2018
 * 
 * Description:
 * 		This program will predict the class values of a given test set by using the provided training
 * 			and preliminary test data. To do this, it will utilize the Weka library and train five of
 * 			its classifiers (attribute selected classifier, bagging, random committee, random sub space,
 * 			and random forest) to all be used together to predict the class values.
 * 
 * 		This program will output:
 * 				An approximate accuracy of the predictions
 * 
 */

import java.io.*;
import java.util.Objects;
import java.util.Scanner;

import weka.core.Instances;
import weka.core.Utils;
import weka.core.converters.ConverterUtils.DataSource;
import weka.classifiers.Evaluation;
import weka.classifiers.meta.AttributeSelectedClassifier;
import weka.classifiers.meta.Bagging;
import weka.classifiers.meta.RandomCommittee;
import weka.classifiers.meta.RandomSubSpace;
import weka.classifiers.trees.RandomForest;

public class Main {

	public static void main(String[] args) throws Exception {
		String trainArff = "trainAndPrelim.arff";
		String predictionFile = "final-nmv-noclass.txt";
		String predictionArff = "final-nmv-noclass.arff";

		/* Used for making the arff files that are needed for the weka classifiers
		 makeArff("txt", "arff"); */
		
		// count the number of instances in prediction file
		Scanner sc1 = new Scanner(new File(predictionFile));
		int numInstances = 0;
		while(sc1.hasNextLine())
			if(!sc1.nextLine().trim().isEmpty())
				numInstances++;
		sc1.close();
		
		// set up the train data source for the weka classifiers
		DataSource trainSource = new DataSource(trainArff);
		Instances trainData = trainSource.getDataSet();
		trainData.setClassIndex(trainData.numAttributes() - 1);
		
		// set up the test data source for the weka classifiers
		DataSource testSource = new DataSource(predictionArff);
		Instances testData = testSource.getDataSet();
		testData.setClassIndex(testData.numAttributes() - 1);

		// train the attribute selected classifier
		System.out.println("Start training weka attribute selected classifier...");
		AttributeSelectedClassifier wekaASC = new AttributeSelectedClassifier();
		String options = "-W weka.classifiers.trees.RandomForest -- -depth 19";
		wekaASC.setOptions(Utils.splitOptions(options));
		wekaASC.buildClassifier(trainData);
		System.out.println("Finished training.");
		/* Used for collecting statistics
		Evaluation evalASC = new Evaluation(trainData);
		evalASC.evaluateModel(wekaASC, testData);
		System.out.println("ASC Accuracy="+ ((evalASC.correct()/(evalASC.correct()+evalASC.incorrect())) * 100)); */
		
		System.out.println("\nStart training weka random forest...");
		RandomForest wekaRF = new RandomForest();
		options = "-depth 19";
		wekaRF.setOptions(Utils.splitOptions(options));
		wekaRF.buildClassifier(trainData);
		System.out.println("Finished training.");
		/* Used for collecting statistics
		Evaluation evalRF = new Evaluation(trainData);
		evalRF.evaluateModel(wekaRF, testData);
		System.out.println("RF Accuracy="+ ((evalRF.correct()/(evalRF.correct()+evalRF.incorrect())) * 100)); */
		
		System.out.println("\nStart training weka random sub space classifier...");
		RandomSubSpace wekaRSS = new RandomSubSpace();
		options = "-P 0.9 -S 1 -num-slots 1 -I 10 -W weka.classifiers.trees.REPTree -- -M 2 -V 0.001 -N 3 -S 1 -L -1 -I 0.0";
		wekaRSS.setOptions(Utils.splitOptions(options));
		wekaRSS.buildClassifier(trainData);
		System.out.println("Finished training.");
		/* Used for collecting statistics
		Evaluation evalRSS = new Evaluation(trainData);
		evalRSS.evaluateModel(wekaRSS, testData);
		System.out.println("RSS Accuracy="+ ((evalRSS.correct()/(evalRSS.correct()+evalRSS.incorrect())) * 100)); */
		
		System.out.println("\nStart training weka bagging classifier...");
		Bagging wekaB = new Bagging();
		options = "-W weka.classifiers.trees.RandomForest";
		wekaB.setOptions(Utils.splitOptions(options));
		wekaB.buildClassifier(trainData);
		System.out.println("Finished training.");
		/* Used for collecting statistics
		Evaluation evalB = new Evaluation(trainData);
		evalB.evaluateModel(wekaB, testData);
		System.out.println("B Accuracy="+ ((evalB.correct()/(evalB.correct()+evalB.incorrect())) * 100)); */
		
		System.out.println("\nStart training weka random committee classifier...");
		RandomCommittee wekaRC = new RandomCommittee();
		options = "-S 1 -num-slots 1 -I 10 -W weka.classifiers.trees.RandomTree -- -K 0 -M 1.0 -V 0.001 -S 1 -depth 19";
		wekaRC.setOptions(Utils.splitOptions(options));
		wekaRC.buildClassifier(trainData);
		System.out.println("Finished training.");
		/* Used for collecting statistics
		Evaluation evalRC = new Evaluation(trainData);
		evalRC.evaluateModel(wekaRC, testData);
		System.out.println("RC Accuracy="+ ((evalRC.correct()/(evalRC.correct()+evalRC.incorrect())) * 100)); */
		
		// used for guessing accuracy
		int unknown = 0;
		int perfect = 0;
		
		/* Used for collecting statistics
		int perfectAndCorrect = 0;
		int correct = 0; */
		
		// consider every instance in the prediction file
		int i = 0;
		String toWrite = "";
		Scanner sc2 = new Scanner(new File(predictionFile));
		while(sc2.hasNextLine()) {
			String next = sc2.nextLine();
			if(!next.trim().isEmpty()) {
				/* Used for collecting statistics
				String[] splitLine = next.split("\\s+");
				*/
				
				// determine what class this current instance belongs in
				int wRFInstance = (int)wekaRF.classifyInstance(testData.get(i));
				int wRSSInstance = (int)wekaRSS.classifyInstance(testData.get(i));
				int wBInstance = (int)wekaB.classifyInstance(testData.get(i));
				int wRCInstance = (int)wekaRC.classifyInstance(testData.get(i));
				int wASCInstance = (int)wekaASC.classifyInstance(testData.get(i));
				
				double numClassifiers = 5.0;
				double sum = wRCInstance + wRFInstance + wRSSInstance + wBInstance + wASCInstance;
				
				if(i != 0)
					toWrite += "\n";
				
				// all classifiers guess the same class for the current instance
				if(sum == 0 || sum == numClassifiers) {
					toWrite += wRFInstance;
					perfect++;
					/* Used for collecting statistics
					if(Integer.parseInt(splitLine[splitLine.length-1]) == wRFInstance) {
						perfectAndCorrect++;
						correct++;
					} */
				}
				// all classifiers do not guess the same class for the current instance
				else {
					unknown++;
					toWrite += Math.round(((double)sum) / numClassifiers);
					/* Used for collecting statistics
					if(Integer.parseInt(splitLine[splitLine.length-1]) == Math.round(((double)sum) / numClassifiers))
						correct++; */
				}
				i++;
			}
		}
		sc2.close();
		
		// make an approximation for the accuracy
		System.out.println("\nApproximate Accuracy="+(((((double)perfect*0.83)+((double)unknown*0.6)) / (double)numInstances) * 100));
		
		/* Used for outputting statistics
		System.out.println("\nUnknown="+unknown+"\nPerfect="+perfect+"\nCorrect="+correct+"\nPerfect and Correct="+perfectAndCorrect);
		System.out.println("Real Accuracy="+(((double)correct/numInstances)*100)); */
		
		// write to the prediction file
		BufferedWriter writer = new BufferedWriter(new FileWriter("prediction.txt"));
	    writer.write(toWrite);
	    writer.close();
	}
	
	// create an attribute-relation file format (arff) file given a text file with data as input
	static void makeArff(String file, String arff) throws IOException
	{
		// count number of attributes
		Scanner sc1 = new Scanner(new File("attr.txt"));
		int numAttrs = 0;
		while(sc1.hasNextLine()) {
			if(!sc1.nextLine().trim().isEmpty())
				numAttrs++;
		}
		sc1.close();
		
		// create attributes using the 'attr.txt' file
		Attribute attrs[] = new Attribute[numAttrs];
		Scanner sc2 = new Scanner(new File("attr.txt"));
		for(int i = 0; i < numAttrs; i++)
		{
			String next = sc2.nextLine();
			if(!next.trim().isEmpty())
			{
				String splitLine[] = next.split("\\s+|:");
				String name = splitLine[0];
				double values[];
				double start = 0;
				double end = 0;
				boolean cont;
				if(Objects.equals(splitLine[2], "cont"))
				{
					cont = true;
					String valList[] = splitLine[3].split("\\.\\.");
					for(int j = 0; j < valList.length; j++)
						if(Objects.equals(valList[j].charAt(valList[j].length()-1), '.'))
							end = Double.parseDouble(valList[j].substring(0, valList[j].length()-1));
						else
							if(j == valList.length-1)
								end = Double.parseDouble(valList[j]);
							else
								start = Double.parseDouble(valList[j]);

					int min = 5;
					int num = (int)(end-start) + 1;
					if(num <= min) {
						values = new double[num];
						double v = start;
						for(int j = 0; j < num; j++) {
							values[j] = v;
							v = v++;
						}
					}
					else {
						values = new double[min];
						double v = start;
						for(int j = 0; j < min; j++) {
							values[j] = v;
							v += (end-start)/min;
						}
					}
				}
				else
				{
					String valList[] = splitLine[2].split(",|\\s*\\(\\?\\)\\.|\\s*\\?\\.");
					values = new double[valList.length];
					for(int j = 0; j < valList.length; j++)
						if(Objects.equals(valList[j].charAt(valList[j].length()-1), '.'))
							values[j] = Double.parseDouble(valList[j].substring(0, valList[j].length()-1));
						else
							values[j] = Double.parseDouble(valList[j]);
					cont = false;
					start = values[0];
					end = values[values.length-1];
				}
				Attribute a = new Attribute(name, start, end, values, cont);
				attrs[i] = a;
			}
			else
				i--;
		}
		sc2.close();
		
		// header section
		String to = "@RELATION rel\n\n";
		for(int i = 0; i < attrs.length; i++) {
			to += "@ATTRIBUTE "+attrs[i].name;
			if(attrs[i].cont)
				to += " NUMERIC\n";
			else
			{
				String t = "";
				for(int j = 0; j < attrs[i].values.length; j++)
				{
					t += (int)attrs[i].values[j];
					if(j != attrs[i].values.length-1)
						t += ",";
				}
				to += " {"+t+"}\n";
			}
		}
		to += "\n@DATA";
		
		// data section
		Scanner sc3 = new Scanner(new File(file));
		while(sc3.hasNextLine()) {
			String next = sc3.nextLine();
			if(!next.trim().isEmpty()) {
				String line = "\n"+next.replace(' ', ',');
				to+=line;
			}
		}
		sc3.close();
		
		// write to arff
		BufferedWriter w = new BufferedWriter(new FileWriter(arff));
	    w.write(to);
	    w.close();
		System.out.println("Made arff: " + arff);
	}
}