import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;

import java.io.*;
import java.util.*;

public class MainClass {
	
	static final int YEAR = 2009;
	
	public static void main(String[] args) {

		// Create the parser. Going through the dataset for a given year.
		FileParser parser = new FileParser (YEAR);
		
		// Parse the output file, then link it with the input file
		parser.parseOutput();
		parser.linkInputFileToOutputs();
		
		// Fetch the results
		Graph bitcoinNetwork = parser.getBitcoinNetwork();
		
		Dictionary<String, Transaction> allTransactions = parser.getTransactions();
		Dictionary<String, Address> allAddresses = parser.getAddresses();
		
		
		// A test of the network
		System.out.println(allTransactions.get("35288d269cee1941eaebb2ea85e32b42cdb2b04284a56d8b14dcc3f5c65d6055").getOutputs().get(0));
	}
}
