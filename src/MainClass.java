import edu.uci.ics.jung.graph.Graph;
import java.util.*;

public class MainClass {
	
	static final int YEAR = 2009;
	
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
		
		// Fetch the results
		Graph bitcoinNetwork = parser.getBitcoinNetwork();
		
		Dictionary<String, Transaction> allTransactions = parser.getTransactions();
		Dictionary<String, Address> allAddresses = parser.getAddresses();
		
		
		// A test of the network
		System.out.println(allTransactions.get("35288d269cee1941eaebb2ea85e32b42cdb2b04284a56d8b14dcc3f5c65d6055").getOutputs().get(0));
	}
}
