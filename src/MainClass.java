import edu.uci.ics.jung.graph.Graph;
import java.util.*;

public class MainClass {
	
	static final int YEAR = 2015;
	
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
	
	public static void main(String[] args) {

		// Create the parser. Going through the dataset for a given year.
		FileParser parser = new FileParser (YEAR);
		
		// Run a performance check
		performanceTest (parser);

		// Some output for testing
		testOutput (parser);
	}
	
	// Provides some output just to test the network
	static void testOutput(FileParser parser)
	{
		
		// Fetch the results
		Dictionary<String, Transaction> allTransactions = parser.getTransactions();
		
		
		// A test of the network
		// The results would change per year
		// Data in 2015 is too large to print out the entire set of transactions or network. Just do it for 2009 to confirm if it works.
		if (YEAR == 2009)
		{
			System.out.println(allTransactions.get("35288d269cee1941eaebb2ea85e32b42cdb2b04284a56d8b14dcc3f5c65d6055").getOutputs().get((byte) 0));
			System.out.println(allTransactions.get("90ff15e5a80593977fb2f6666de2860584d39ebc3a41f65a0a1fdc3a851aefda"));
			System.out.println(allTransactions.get("90ff15e5a80593977fb2f6666de2860584d39ebc3a41f65a0a1fdc3a851aefda").getOutputs().get((byte) 0));
			// For printing the network & all transactions
			// Might be too large to output on a single line
			//System.out.println (allTransactions);
		}
	}
}
