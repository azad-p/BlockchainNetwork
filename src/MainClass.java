import edu.uci.ics.jung.graph.Graph;
import java.util.*;

public class MainClass {

	static final int YEAR = 2009;
	
	// How many white addresses are considered
	static final int WHITE_ADDRESS_LIMIT = 1000;
	
	// Set this to true for Part B of the assignment
	// False for part A of the assignment
	static final boolean WINDOW_FEATURE_EXTRACTION = true;
	
	// The months to extract features for {1 = january, 12 = december}
	// This is used only if WINDOW_FEATURE_EXTRACTION is true
	static final byte[] MONTHS_TO_EXTRACT = { (byte) 12 };

	// Test & Outputs the performance for parser a bitcoin network dataset (Input / Output files)
	// The parser goes through the output file first, then links the input files with the output files
	static void performanceTest (FileParser parser)
	{
		long startTime = System.nanoTime();

		// Parse the output file, then link it with the input file
		parser.parseOutput();

		long startTimeOfInputLink = System.nanoTime();

		parser.linkInputFileToOutputs();

		long endTime = System.nanoTime();

		// Note: The announcement is incorrect, this time should be divided by 1000000 NOT 1000
		// 1,000,000 nanoseconds is 1 millisecond
		long timeOfOutputParsing = (startTimeOfInputLink - startTime) / 1_000_000;
		long timeOfInputParsing = (endTime - startTimeOfInputLink) / 1_000_000;

		long totalTime = (endTime - startTime) / 1_000_000;

		System.out.println ("~~~~~~Performance Results~~~~~~~~~~");
		System.out.println ("Note: Data is read & Linked together immediately after being read for improved efficiency.\n We do not read ALL data, and then link it together.");
		System.out.println ("Milliseconds to load output file: " + timeOfOutputParsing + "ms (" + (timeOfOutputParsing * 0.001) + " s)");
		System.out.println ("Milliseconds to load input file and link it with output " + timeOfInputParsing + "ms (" + (timeOfInputParsing * 0.001) + " s)");
		System.out.println ("Milliseconds of total execution: " + totalTime + "ms (" + (totalTime * 0.001) + " s)");
		System.out.println ("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	}
	
	static void runWindowParser (WindowParser parser)
	{
		parser.beginFeatureExtractions(YEAR);
	}

	public static void main(String[] args) {

		// Part A
		if (!WINDOW_FEATURE_EXTRACTION)
		{
			// Create the parser. Going through the dataset for a given year.
			FileParser parser = new FileParser (YEAR);
			
			performanceTest (parser);
	
			// Some output for testing
			printGraph(parser.getGraph());
		}
		// Part B
		else
		{
			WindowParser parser = new WindowParser (MONTHS_TO_EXTRACT, WHITE_ADDRESS_LIMIT);
			
			runWindowParser (parser);
		}
		
		System.out.println ("Execution has completed.");
	}

	static void printGraph(Graph<GraphNode, Integer> graph)
	{
		// Grab a random transaction
		Transaction trans = (Transaction) graph.getSource(10);

		System.out.println ("Printing a transaction in the graph...");
		System.out.println (trans);

		Collection<Integer> outputs = graph.getOutEdges(trans);
		Collection<Integer> inputs = graph.getInEdges(trans);

		System.out.println ("Outputs");
		for (Integer i : outputs)
		{
			Address addr = (Address)graph.getDest(i);
			System.out.println(addr);

			Collection<Integer> outputAddrs = graph.getOutEdges(addr);
			for (Integer k : outputAddrs)
			{
				Transaction outputTrans = (Transaction)graph.getDest(k);
				System.out.println(outputTrans);
			}
		}

		System.out.println ("Inputs");
		for (Integer i : inputs)
		{
			Address addr = (Address)graph.getSource(i);
			System.out.println(addr);

			Collection<Integer> inputAddrs = graph.getInEdges(addr);
			for (Integer k : inputAddrs)
			{
				Transaction outputTrans = (Transaction)graph.getSource(k);
				System.out.println(outputTrans);
			}
		}

	}
}
