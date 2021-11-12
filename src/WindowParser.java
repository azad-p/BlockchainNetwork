import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;

// Similar to the FileParser class, this file will parse the network
// However, this class only parses in 24hour windows, then stores the results
// This class will also be reading sorted files, NOT the regular dataset
// Sorted files should be placed in the folder sortedFile/ with the corresponding name: Sorted_{input or output}{year}_{month}
public class WindowParser extends Parser {

	// Set to true if you wish to print the results to the console
	private final boolean PRINT_RESULTS = false;
	
	private final HashMap<String, Transaction> transactions; // FIXME perhaps use Integer, Transactions, where int is the index in graph ?

	private byte[] MONTHS;
	
	final int WHITE_ADDR_LIMIT; // Limit of white addresses that we consider per window
	int totalWindowsInOutputFiles, totalWindowsInInputFiles;
	
	// FIXME writer to the file of the MONTHS, so that we dont override eachother
	String writeToFile = "results/featureExtraction.csv";
	
	WindowParser (byte[] MONTHS_TO_EXTRACT_FEATURES, int ADDR_LIMIT)
	{
		final int HASH_SET_SIZE_INIT = 16 * 256;
	
		this.WHITE_ADDR_LIMIT = ADDR_LIMIT;
		this.transactions = new HashMap<String, Transaction>(HASH_SET_SIZE_INIT, 1.0f);
		this.MONTHS = MONTHS_TO_EXTRACT_FEATURES;
	}
	
	public void beginFeatureExtractions(int YEAR_OF_DATASET)
	{
		try {
			
			// Will create and reset the file
			FileWriter reset = new FileWriter(writeToFile, false);
			reset.append("Note: Window features are placed at the end of the addresses for that window [Final column for the day].\n\n");
			reset.append("Address");
			reset.append(",");
			reset.append("Year");
			reset.append(",");
			reset.append("Day");
			reset.append(",");
			reset.append("Amount Sent");
			reset.append(",");
			reset.append("Income");
			reset.append(",");
			reset.append("Neighbours");
			reset.append(",");
			reset.append("CoAddresses");
			reset.append(",");
			reset.append("Is Randsome");
			
			reset.flush();
			reset.close();
			reset = null;
			
		} catch (IOException e) {
			System.out.println ("Cannot writer results");
			e.printStackTrace();
			System.exit(0);
		}
		
		// Initialize any features
		totalWindowsInOutputFiles = 0;
		totalWindowsInInputFiles = 0;
		
		extractFeatures (YEAR_OF_DATASET);
	}
	
	private void displayAddressResults(int year, int day, String hash, double income, long amountSent, int numNeighbours, int numCoAddresses, boolean isRansome, BufferedWriter writer)
	{
		if (PRINT_RESULTS)
			printFeatures(hash, income, amountSent, numNeighbours, numCoAddresses, isRansome);
		
		writeAddressFeatures(year, day, hash, income, amountSent, numNeighbours, numCoAddresses, isRansome, writer);
	}
	
	private void displayWindowResults(int year, int day, int totalTrans, int numRansomeAddresses, int numWhiteAddresses, BufferedWriter writer)
	{
		if (PRINT_RESULTS)
			printWindow(year, day, totalTrans, numRansomeAddresses, numWhiteAddresses);
		
		writeWindowFeatures(year, day, totalTrans, numRansomeAddresses, numWhiteAddresses, writer);
	}
	
	// Print some of the features to the console
	private void printFeatures(String hash, double income, long amountSent, int numNeighbours, int numCoAddresses, boolean isRansome)
	{
		System.out.println ("Total windows in output file: " + totalWindowsInOutputFiles);
		System.out.println ("Total windows in input file: " + totalWindowsInInputFiles);
		System.out.println (hash + " income: " + income);
		System.out.println ("Amount sent: " + amountSent);
		System.out.println ("Neighbours: " + numNeighbours);
		System.out.println ("CoAddresses: " + numCoAddresses);
		System.out.println ("Is ransome address: " + isRansome);
	}
	
	private void printWindow(int year, int day, int totalTrans, int numRansome, int numWhite)
	{
		System.out.println ("Printing window: year " + year + ", day " + day);
		System.out.println ("Total ransome addresses in window: " + numRansome);
		System.out.println ("Total transactions in window: " + totalTrans);
	}
	
	// Write our final results to a csv file
	private void writeAddressFeatures(int year, int day, String hash, double income, long amountSent, int numNeighbours, int numCoAddresses, boolean isRansome, BufferedWriter writer)
	{
		try {
			writer.append("\n");
			writer.append(hash);
			writer.append(",");
			writer.append("" + year);
			writer.append(",");
			writer.append("" + day);
			writer.append(",");
			writer.append("" + amountSent);
			writer.append(",");
			writer.append("" + income);
			writer.append(",");
			writer.append("" + numNeighbours);
			writer.append(",");
			writer.append("" + numCoAddresses);
			writer.append(",");
			writer.append("" + isRansome);
			
			writer.flush();
		} catch (IOException e) {
			System.out.println ("Could not write results to file.");
			e.printStackTrace();
			System.exit(0);
		}
		
	}
	
	// Write our final results to a csv file
	private void writeWindowFeatures(int year, int day, int totalTrans, int numRansomeAddresses, int numWhiteAddresses, BufferedWriter writer)
	{
		try {
			writer.append("\n");
			writer.append("~~~~ Features of the above window ~~~~");
			writer.append("\n");
			writer.append(",");
			writer.append("" + year);
			writer.append(",");
			writer.append("" + day);
			writer.append(",");
			writer.append(",");
			writer.append(",");
			writer.append(",");
			writer.append(",");
			writer.append(",");
			writer.append("Total transactions in window: " + totalTrans);
			writer.append(",");
			writer.append("Ransome addresses in window: " + numRansomeAddresses);
			writer.append(",");
			writer.append("White addresses in window: " + numWhiteAddresses);
			writer.append("\n");
			
			writer.flush();
		} catch (IOException e) {
			System.out.println ("Could not write results to file.");
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	
	// Grab the features
	// These are features of a specific window
	private void getFeaturesFromGraph(int year, int day, Graph<GraphNode, Integer> graph)
	{
		Collection<GraphNode> vertices = graph.getVertices();
		
		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// Features for a window (All addresses on current year/day)
		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		int totalNumTransactions = 0;
		int totalWhiteAddresses = 0, totalRansomeAddresses = 0;
		BufferedWriter writer = null;
		
		try {
			
			writer = new BufferedWriter(new FileWriter(writeToFile, true));
			
		} catch (IOException e) {
			System.out.println ("Could not write results.");
			e.printStackTrace();
			System.exit(0);
		}
		
		assertBool (writer != null, "Writer was not defined");
		
		for (GraphNode node : vertices)
		{
			if (node instanceof Address)
			{	
				Address adr = (Address)node;
				
				String hash = adr.getAddrHash();
				boolean ransomeWare = isRansomeWareAddress (hash, year, day) != null;
				
				if (!ransomeWare)
				{
					++totalWhiteAddresses;
					
					// Don't bother extracting features for all these extra white addresses
					// We won't be storing it anyway.
					// Only grab features that we will use
					if (totalWhiteAddresses > WHITE_ADDR_LIMIT)
						continue;
				}
				else
				{
					++totalRansomeAddresses;
				}
				
				// ~~~~~~~~~~~~~~~~~~~~~~~
				// Features for an address
				// ~~~~~~~~~~~~~~~~~~~~~~~
				double income = 0.0d;
				long amountSent = 0;
				int numNeighbours = 0;
				int numCoAddresses = 0;
				
				Collection<Integer> inEdges = graph.getInEdges(adr);
				double incomeRet = getIncome (adr, graph.getSource((int)(inEdges.toArray())[0]), graph);
				
				// Negative income is just zero
				income = incomeRet;
				
				amountSent = adr.getBtcSent();
				numNeighbours = graph.getNeighborCount(adr);
				
				// Check for co-addresses of this transaction
				Iterator<Integer> it = graph.getOutEdges(adr).iterator();
				
				HashSet<Integer> coAddressMap = new HashSet<Integer>(150); // Place co-addresses here
																		   // Stores the index in the graph
				
				while (it.hasNext())
				{
					Integer sentTo = it.next();
					GraphNode trans = graph.getDest(sentTo);
					
					assertBool (trans instanceof Transaction, "Address should be sending to a transaction.");
					
					Object[] edgesArr = graph.getInEdges(trans).toArray();
					int firstNode = -1;
					
					if (edgesArr.length > 0)
						firstNode = (int)edgesArr [0];
					
					// Every extra inputs on the transaction means there is a co-address
					// Since two addresses will be inputs to the same address
					for (int i = 1; i < edgesArr.length; i++)
					{
						// These should be addresses
						int other = (int)edgesArr [i];
						GraphNode test = graph.getSource((int)edgesArr [i]);
						
						assertBool (!(test instanceof Transaction), "Transaction should be receiving from an address.");
						
						// Anything not already considered as a co-address is now a co-address
						if (!coAddressMap.contains(firstNode))
						{
							coAddressMap.add(firstNode);
						}
						if (!coAddressMap.contains(other))
						{
							coAddressMap.add(other);
						}
					}
				}
				
				numCoAddresses = coAddressMap.size();
				
				// Free memory for garbage collector
				coAddressMap.clear();
				coAddressMap = null;
				
				// Now we write our results for these addresses
				displayAddressResults (year, day, hash, income, amountSent, numNeighbours, numCoAddresses, ransomeWare, writer);
			}
			else if (node instanceof Transaction)
			{
				++totalNumTransactions;
			}
			else {

			}
		}
		
		displayWindowResults (year, day, totalNumTransactions, totalRansomeAddresses, totalWhiteAddresses, writer);
		
		try {
			writer.flush();
			writer.close();
		} catch (IOException e) {
			System.out.println ("Failed to close buffer");
			e.printStackTrace();
			System.exit(0);
		}
		
		writer = null;
	}
	
	// Extracts the features for a specific year in the dataset
	private void extractFeatures(int YEAR_OF_DATASET) {
		
		// Bitcoin network
		Graph<GraphNode, Integer> graph = null;
		
		// Determine the files we need to go over
		final String FILE_FOLDER = "sortedFiles";
		final String INPUT_FILE_NAME = "Sorted_inputs" + YEAR_OF_DATASET + '_';
		final String OUTPUT_FILE_NAME = "Sorted_outputs" + YEAR_OF_DATASET + '_';
		
		InputStream fileStreamOutput = null;
		BufferedReader fileScOutput = null;
		InputStream fileStreamInput = null;
		BufferedReader fileScInput = null;
		
		for (int monthIndex = 0; monthIndex < MONTHS.length; ++monthIndex)
		{
			System.out.println ("Reading files for the month of: " + MONTHS [monthIndex]);
			
			try {
				
			fileStreamOutput = new FileInputStream(FILE_FOLDER + '/' + OUTPUT_FILE_NAME + MONTHS [monthIndex] + ".txt");
			fileScOutput = new BufferedReader (new InputStreamReader (fileStreamOutput));
			fileStreamInput = new FileInputStream(FILE_FOLDER + '/' + INPUT_FILE_NAME + MONTHS [monthIndex] + ".txt");
			fileScInput = new BufferedReader (new InputStreamReader (fileStreamInput));
			System.out.println ("Going through the files through each window..");
			
			} catch (IOException e) {
				System.err.println("A problem has occurred with trying to load the file. Is the directory correct?");
				e.printStackTrace();
				System.exit(0);
			}
			
			final int numDaysInMonth = (YearMonth.of (YEAR_OF_DATASET, MONTHS[monthIndex])).lengthOfMonth();
			
			// We will at MOST be going through this many days
			// When both files reach EOF, we will break
			for (int day = 1; day <= numDaysInMonth; ++day)
			{
				// Create the bitcoin network for this window
				graph = new DirectedSparseGraph<>();
				
				parseOutputFiles(fileScOutput, graph);
				parseInputFiles (YEAR_OF_DATASET, MONTHS [monthIndex], fileScInput, graph); // Links input file with output file
				
				deallocateMemory(fileStreamOutput, fileScOutput, fileStreamInput, fileScInput, graph, true);
			}
			
			// Close all files
			try {
				
				fileStreamOutput.close();
				fileScOutput.close();
				fileStreamInput.close();
				fileScInput.close();
				
			} catch (IOException e) { e.printStackTrace(); System.exit (0); System.err.println ("Failed to close a file."); }
			
			deallocateMemory(fileStreamOutput, fileScOutput, fileStreamInput, fileScInput, graph, false);
		}
	}
	
	private void parseOutputFiles (BufferedReader fileSc, Graph<GraphNode, Integer> graph)
	{
		int edgeCount = graph.getEdgeCount();
		String wholeLine;
		
		try {
			while ((wholeLine = fileSc.readLine()) != null) {
				transactions.clear();
				
				// Next 'window'
				if (wholeLine.isEmpty())
				{
					totalWindowsInOutputFiles++;
					
					// Completed execution of this window
					// Do nothing else
					// Feature extraction step is done when parsing the input files
					// This way we link them first
					return;
				}
				
				String[] lineInfo = wholeLine.split("\t");

				int time = Integer.parseInt(lineInfo [0]);
				String transHash = lineInfo [1].intern();
				int lineIndex = 2;
				
				Transaction trans = getTransFromTable(transHash);

				if (trans == null) {
					trans = addTransactionToTable(time, transHash);
					graph.addVertex(trans);
				}

				// Create each address that is an output to this transaction
				while (lineIndex < lineInfo.length)
				{
					String addressHash = lineInfo[lineIndex].intern();
					++lineIndex;

					// Do not store 'no address' hashes. Just use an empty string.
					// Just to save memory. We don't need it anyways.
					if (addressHash.charAt(0) == 'n')
						addressHash = "";
					
					long amountSent = Long.parseLong(lineInfo[lineIndex]);
					++lineIndex;
					
					// Create address for output of the transaction
					Address addr = new Address (amountSent, addressHash);

					// Add information to the graph
					graph.addVertex(addr);
					graph.addEdge(edgeCount++, trans, addr);
				}
				
				lineInfo = null;
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void parseInputFiles (int year, int month, BufferedReader fileSc, Graph<GraphNode, Integer> graph)
	{
		transactions.clear();
		
		int edgeCount = graph.getEdgeCount ();
		Transaction firstTrans = null; // For getting the day
		String wholeLine;
		
		try {
			while ((wholeLine = fileSc.readLine()) != null) {
				
				// Next 'window'
				if (wholeLine.isEmpty())
				{
					totalWindowsInInputFiles++;
					
					if (firstTrans != null)
					{
						// Obtained from https://stackoverflow.com/questions/45392163/java-get-current-day-from-unix-timestamp
						// Get the current day from a unix time
						int dayOfMonth = Integer.parseInt(new SimpleDateFormat("dd").format(new Date(firstTrans.getTimeOfTransaction() * 1000L)));
						int dayOfYear = (new GregorianCalendar (year, month - 1, dayOfMonth)).get (Calendar.DAY_OF_YEAR);
						
						System.out.println ("Getting features for the window: year " + year + " day " + dayOfYear);
						
						// Extract features from this window
						getFeaturesFromGraph (year, dayOfYear, graph);
						firstTrans = null;
					}
					
					// Complete execution of this window 
					return;
				}
				
				String[] lineInfo = wholeLine.split("\t");

				int transactionTime = Integer.parseInt(lineInfo [0]);
				String transactionHash = lineInfo [1].intern();
				int lineIndex = 2;
				
				Transaction trans = getTransFromTable(transactionHash);

				if (trans != null) {
					// Transaction is already in the hashTable
					// Just update any of its information
					trans.setTimeOfTransaction(transactionTime);
				} else {
					// Transaction is not in the Hashtable
					// Create the class, update any of its information, and add it to the Hashtable
					trans = addTransactionToTable(transactionTime, transactionHash);
					graph.addVertex(trans);
				}
				
				if (firstTrans == null)
					firstTrans = trans;
				
				// Check all of the transactions inputs
				while (lineIndex < lineInfo.length) {
					String inputHash = lineInfo[lineIndex].intern();
					++lineIndex;
					
					short indexOfInput = Short.parseShort(lineInfo [lineIndex]);
					++lineIndex;
					
					Transaction inputTrans = getTransFromTable(inputHash);

					if (inputTrans == null) {
						// Transaction is not in the Hashtable
						// Create the class, update any of its information, and add it to the Hashtable
						// Then link them together
						inputTrans = addTransactionToTable(-1, inputHash);
						graph.addVertex(inputTrans);
					}

					Address address = null;
					if (graph.getOutEdges(inputTrans) == null)
					{
						// Special case
						// Address was not found in the output files for this year
						// Create a dummy address to mimic the transaction. Note that, the btc will not be known
						
						address = Address.createDummyAddress(inputHash, indexOfInput, year);
					}
					else {

						// Output edges exist

						Collection<Integer> outputs = graph.getOutEdges(inputTrans);

						// Sort the outputs by their index in the graph
						ArrayList<Integer> outputsSorted = new ArrayList<Integer>(outputs);
						Collections.sort(outputsSorted);

						// Do not have an index for this output
						// Can occur if there was multiple index's from the input file but none were in the output file
						// In this case, outputs.size() < indedxOfInput would be true
						if (outputs == null || outputs.size() == 0 || outputs.size() <= indexOfInput)
						{
							address = Address.createDummyAddress(inputHash, indexOfInput, year);
						}
						else
						{
							GraphNode node = graph.getDest(outputsSorted.get(indexOfInput));

							if (node instanceof Address) {
								address = (Address) node;
							}
							else {
								System.err.println("Error, node is not an address");
								System.err.println("Exiting on failure");
								System.exit(0);
							}
						}
					}

					// Assertion, address must be initialized before this
					if (address == null) { System.err.println ("ERROR: Assertion ran. No address associated with the transaction."); System.exit(0); }

					// Link the information together on the graph & data
					graph.addEdge(edgeCount++, inputTrans, address);
					graph.addEdge(edgeCount++, address, trans);
				}
				
				lineInfo = null;
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// De-allocate the memory for this window
	// If onlyDealocateWindow is true, then we are not deallocating the streams/scanner
	private void deallocateMemory(InputStream fileStreamOutput, BufferedReader fileScOutput, InputStream fileStreamInput, BufferedReader fileScInput, 
			Graph<GraphNode, Integer> graph, boolean onlyDealocateWindow)
	{
		// De-allocate all memory for this window
		if (!onlyDealocateWindow)
		{
			fileStreamOutput = null;
			fileScOutput = null;
			fileStreamInput = null;
			fileScInput = null;
		}
		
		graph = null;
		
		//addresses.clear();
		transactions.clear();
		
		System.gc();
	}
	
	// x must be true
	private void assertBool(boolean x, String msg)
	{
		if (!x)
		{
			System.err.println ("Assertion ran! ");
			System.err.println (msg);
			System.exit(0);
		}
	}
	
	private double getIncome (Address address, GraphNode sender,  Graph<GraphNode, Integer> graph)
	{
		double res = 0.0d;
		Collection<Integer> outEdges = graph.getOutEdges(sender);
		
		// Calculate income
		for (Integer i : outEdges)
		{
			GraphNode input = graph.getDest(i);
			
			if (input instanceof Transaction)
			{
				System.err.println ("Assertion ran! ");
				System.err.println ("Transaction is an output to an address?");
				System.exit(0);
			}
			else
			{
				res += ((Address)input).getBtcSent();
			}
		}
		
		return res;
	}
	
	// Example method, returns true if it is a ransomeware address
	// Returns the label
	private String isRansomeWareAddress(String hash, int year, int day)
	{
		// Please do not complete this method
		// Write it in a different class
		// This is temporary
		return "";
	}
	
	// Adding a transaction to the hashTable
	private Transaction addTransactionToTable(int time, String transHash)
	{
		Transaction newTrans = new Transaction();

		// Update transactions info
		newTrans.setHashOfTransaction(transHash);
		newTrans.setTimeOfTransaction(time);

		transactions.put(transHash, newTrans);

		return newTrans;
	}

	// Getting a transaction from the hashtable
	private Transaction getTransFromTable (String transHash)
	{
		return transactions.get(transHash);
	}

	public HashMap<String, Transaction> getTransactions()
	{
		return transactions;
	}
}
