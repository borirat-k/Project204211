
package Background;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

/**
 *
 * @author HawksSalatan
 */
public class NeuralNetwork {
    
    private int hiddenSize, inputSize, outputSize, iters;
    private double[][] weightsItoH;
    private double[][] weightsHtoO;
    private double[] ah;
    private double[] ai;
    private double[] ao;
    private double LEARNING_RATE;
    
    public NeuralNetwork(double learningRate, int inputSize, int hiddenSize, int outputSize) {
	defaultInit(learningRate, inputSize, hiddenSize, outputSize);
    }
    
    public NeuralNetwork(String filename, double defLr, int defInSize, int defHiSize, int defOutSize) throws FileNotFoundException {
        File file = new File(filename);
        Scanner input = new Scanner(file);
        try {
                this.LEARNING_RATE = input.nextDouble();
                this.inputSize     = input.nextInt();
                this.hiddenSize    = input.nextInt();
                this.outputSize    = input.nextInt();
        } catch (Exception e) {
                input.close();
                defaultInit(defLr, defInSize, defHiSize, defOutSize);
                return;
        }
        init();
        loadWeights(input, this.LEARNING_RATE, this.inputSize, this.hiddenSize, this.outputSize);
        input.close();
    }
    
    private void init() {
	this.weightsItoH = new double[this.inputSize][this.hiddenSize];
	this.weightsHtoO = new double[this.hiddenSize][this.outputSize];
	this.ai 		 = new double[this.inputSize];
	this.ah 		 = new double[this.hiddenSize];
	this.ao 		 = new double[this.outputSize];
	ah[this.hiddenSize - 1]   = 1.0; // Bias units
	ai[this.inputSize  - 1]   = 1.0;
	iters = 0;
    }
    
    private void defaultInit(double learningRate, int inputSize, int hiddenSize, int outputSize) {
	this.LEARNING_RATE = learningRate;
	this.inputSize   = inputSize + 1;
	this.hiddenSize  = hiddenSize + 1;
	this.outputSize  = outputSize;
	init();
	randomizeWeights();
    }
    
    public void loadWeights(String filename) {
        try {
            Scanner input = new Scanner(new File(filename));
            double lr = input.nextDouble();
            int inSize = input.nextInt();
            int hiSize = input.nextInt();
            int outSize = input.nextInt();
            loadWeights(input, lr, inSize, hiSize, outSize);
            input.close();
        } catch (Exception e) {
            randomizeWeights();
        }
    }
    
    private void loadWeights(Scanner in, double lr, int inSize, int hiSize, int outSize) {
        if (lr != LEARNING_RATE || inputSize != inSize || hiSize != hiddenSize || outSize != outputSize) {
            randomizeWeights();
            return;
        }
        for (int i = 0; i < inputSize; i++)
            for (int j = 0; j < hiddenSize; j++)
                weightsItoH[i][j] = in.nextDouble();
        for (int j = 0; j < hiddenSize; j++)
            for (int k = 0; k < outputSize; k++)
                weightsHtoO[j][k] = in.nextDouble();
    }
    
    public void saveWeights(String filename) throws IOException {
            FileWriter f = new FileWriter(new File(filename));
            f.write(LEARNING_RATE + " " + inputSize + " " + hiddenSize + " " + outputSize + " \n");
            for (int i = 0; i < inputSize; i++)
                for (int j = 0; j < hiddenSize; j++)
                    f.write(String.format("%f\n", weightsItoH[i][j]));
            for (int j = 0; j < hiddenSize; j++)
                for (int k = 0; k < outputSize; k++)
                    f.write(String.format("%f\n", weightsHtoO[j][k]));
            f.close();
    }
    
    public void randomizeWeights() {
	for (int i = 0; i < inputSize; i++)
            for (int j = 0; j < hiddenSize; j++)
		weightsItoH[i][j] = rand(-1.0, 1.0);
	for (int j = 0; j < hiddenSize; j++)
            for (int k = 0; k < outputSize; k++)
		weightsHtoO[j][k] = rand(-1.0, 1.0);
    }
    
    private double rand(double a, double b) {
	return a + (b - a) * Math.random();
    }
    
    private double sigmoid(double x) {
	return 1./(1 + Math.exp(-x));
	//return Math.tanh(x);
    }
    
    private double dSigmoid(double y){
        return y * (1 - y);
        //sigmoid(x) * (1 - sigmoid(x))
    }
    
    private void forwardPropagation(int[] inputs) {
	// Compute activations for input layer neurons
	for (int i = 0; i < inputSize - 1; i++)
		ai[i] = inputs[i];

	// Compute activations for hidden layer neurons
	for (int j = 0; j < hiddenSize - 1; j++) {
		ah[j] = 0.0;
		for (int i = 0; i < inputSize; i++)
			ah[j] += weightsItoH[i][j] * ai[i];
		ah[j] = sigmoid(ah[j]);
	}

	// Compute activations for output layer neurons
	for (int k = 0; k < outputSize; k++) {
		ao[k] = 0.0;
		for (int j = 0; j < hiddenSize; j++)
			ao[k] += ah[j] * weightsHtoO[j][k];
		ao[k] = sigmoid(ao[k]);
	}
    }
    
    private void backPropagation(double[] errors) {
	// Compute delta for output layer neuron
	double[] deltak = new double[outputSize];
	for (int k = 0; k < outputSize; k++)
		deltak[k] = dSigmoid(ao[k]) * errors[k];
		
	// Compute delta for hidden layer neurons
	double[] deltaj = new double[hiddenSize];
	for (int j = 0; j < hiddenSize; j++)
		for (int k = 0; k < outputSize; k++)
			deltaj[j] += dSigmoid(ah[j]) * deltak[k] * weightsHtoO[j][k];

	// Update weights from input to hidden layer
	for (int i = 0; i < inputSize; i++)
		for (int j = 0; j < hiddenSize; j++)
			weightsItoH[i][j] += LEARNING_RATE * deltaj[j] * ai[i];

	// Update weights from hidden to output layer
	for (int j = 0; j < hiddenSize; j++)
		for (int k = 0; k < outputSize; k++)
			weightsHtoO[j][k] += LEARNING_RATE * deltak[k] * ah[j];
    }
    
    public void train(int[][] inputs, int[][] outputs, int iterLimit) {
	for (int c = 0; c < iterLimit; c++, iters++)
		for (int i = 0; i < inputs.length; i++) {
			forwardPropagation(inputs[i]);
			double[] errors = new double[outputSize];
			for (int k = 0; k < outputSize; k++)
				errors[k] = outputs[i][k] - ao[k];
			backPropagation(errors);
		}
    }
    
    public int resultIndex(int[] pattern) {
	forwardPropagation(pattern);
	return interpret();
    }
    
    private int interpret() {
	if (outputSize == 1) return (ao[0] < 0.5)? 0 : 1;
	int index = 0;
	double max = ao[0];
	for (int k = 1; k < outputSize; k++)
            if (ao[k] > max) {
		max = ao[k]; index = k;
            }
	return index;
    }
    
    private int maxIndex(int[] pattern) {
	int index = 0;
	double max = pattern[0];
	for (int k = 1; k < outputSize; k++)
            if (pattern[k] > max) {
               	max = pattern[k]; index = k;
            }
	return index;
    }
    
    public double[] test(int[][] inputs, int[][] outputs, boolean print) {
	double[] r = {0.0, 0.0};
	System.out.println("Iterations: " + iters);
	for (int i = 0; i < inputs.length; i++) {
		int x = resultIndex(inputs[i]);
		int expected = maxIndex(outputs[i]);
		if (print) System.out.println("Expected: " + expected + " " + Arrays.toString(outputs[i]) +
									  " Result: " + x + " " + Arrays.toString(ao));
		for (int k = 0; k < outputSize; k++)
			r[1] += (outputs[i][k] - ao[k]) * (outputs[i][k] - ao[k]);
		if (expected == x) r[0] += 1.0/inputs.length;
		r[1] += (expected - x)*(expected - x)/(double)inputs.length;
	}
	r[1] *= 0.5;
	if (print) {
		System.out.println("Success rate:  " + r[0]*100 + "%");
		System.out.println("Squared Error: " + String.format("%.8f", r[1]));
		// ERROR = 0.5 * sum(norm(expected - output)**2)
	}
	return r;
    }
    
    public int iters() {
	return iters;
    }
    
    public static void main(String[] args) {
	int[][] inputs = {
			{0, 0},
			{0, 1},
			{1, 0},
			{1, 1}
	};
	int[][] outputs = {{1, 0, 0, 0}, {0, 1, 0, 0}, {0, 0, 1, 0}, {0, 0, 0, 1}};
	NeuralNetwork nn = new NeuralNetwork(0.3, 2, 5, 4);
	nn.train(inputs, outputs, 10000);
	nn.test(inputs, outputs, true);
    }
    
}
