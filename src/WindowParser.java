import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Scanner;

import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;

// Similar to the FileParser class, this file will parse the network
// However, this class only parses in 24hour windows, then stores the results
// This class will also be reading sorted files, NOT the regular dataset
// Sorted files should be placed in the folder sortedFile/ with the corresponding name: Sorted_{input or output}{year}_{month}
public class WindowParser extends Parser {

	private final HashMap<String, Transaction> transactions;
	private final HashMap<String, Address> addresses;

	private byte[] MONTHS;
	
	int totalWindowsInOutputFiles, totalWindowsInInputFiles;
	
	WindowParser (byte[] MONTHS_TO_EXTRACT_FEATURES)
	{
		final int HASH_SET_SIZE_INIT = 16 * 256;
		
		this.transactions = new HashMap<String, Transaction>(HASH_SET_SIZE_INIT, 1.0f);
		this.addresses = new HashMap<String, Address>(HASH_SET_SIZE_INIT, 1.0f);
		this.MONTHS = MONTHS_TO_EXTRACT_FEATURES;
	}
	
	public void beginFeatureExtractions(int YEAR_OF_DATASET)
	{
		// Initialize any features
		totalWindowsInOutputFiles = 0;
		totalWindowsInInputFiles = 0;
		
		extractFeatures (YEAR_OF_DATASET);
		displayResults();
	}
	
	private void displayResults()
	{
		printFeatures();
		writeFeatures();
	}
	
	// Print some of the features to the console
	private void printFeatures()
	{
		System.out.println ("Total windows in output file: " + totalWindowsInOutputFiles);
		System.out.println ("Total windows in input file: " + totalWindowsInInputFiles);
	}
	
	// Write our final results to a csv file
	private void writeFeatures()
	{
		
	}
	
	// Extracts the features for a specific year in the dataset
	private void extractFeatures(int YEAR_OF_DATASET) {
		
		// Bitcoin network
		Graph<GraphNode, Integer> graph = null;
		
		// Determine the files we need to go over
		final String FILE_FOLDER = "sortedFiles";
		final String INPUT_FILE_NAME = "Sorted_inputs" + YEAR_OF_DATASET + '_';
		final String OUTPUT_FILE_NAME = "Sorted_outputs" + YEAR_OF_DATASET + '_';
		
		FileInputStream fileStreamOutput = null;
		Scanner fileScOutput = null;
		FileInputStream fileStreamInput = null;
		Scanner fileScInput = null;
		
		for (int monthIndex = 0; monthIndex < MONTHS.length; monthIndex++)
		{
			System.out.println ("Reading files for the month of: " + MONTHS [monthIndex]);
			
			try {
				
			fileStreamOutput = new FileInputStream(FILE_FOLDER + '/' + OUTPUT_FILE_NAME + MONTHS [monthIndex] + ".txt");
			fileScOutput = new Scanner (fileStreamOutput, "UTF-8");
			fileStreamInput = new FileInputStream(FILE_FOLDER + '/' + INPUT_FILE_NAME + MONTHS [monthIndex] + ".txt");
			fileScInput = new Scanner (fileStreamInput, "UTF-8");
			System.out.println ("Placed Input/Output files into memory. Now going through the files through each window..");
			
			} catch (IOException e) {
				System.err.println("A problem has occurred with trying to load the file. Is the directory correct?");
				e.printStackTrace();
				System.exit(0);
			}
			
			final int numDaysInMonth = (YearMonth.of (YEAR_OF_DATASET, MONTHS[monthIndex])).lengthOfMonth();
			
			// We will at MOST be going through this many days
			// When both files reach EOF, we will break
			for (int day = 1; day <= numDaysInMonth; day++)
			{
				// Create the bitcoin network for this window
				graph = new DirectedSparseGraph<>();
				
				parseOutputFiles(MONTHS [monthIndex], fileScOutput, graph);
				parseInputFiles (YEAR_OF_DATASET, MONTHS [monthIndex], fileScInput, graph); // Links input file with output file
				
				deallocateMemory(fileStreamOutput, fileScOutput, fileStreamInput, fileScInput, graph, true);
				if (!fileScOutput.hasNextLine() && !fileScInput.hasNextLine())
					break;
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
	
	private void parseOutputFiles (int month, Scanner fileSc, Graph<GraphNode, Integer> graph)
	{	
		int edgeCount = graph.getEdgeCount();

		while (fileSc.hasNextLine()) {
			
			String wholeLine = fileSc.nextLine();
			
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
				
			Scanner lineSc = new Scanner(wholeLine);

			int time = lineSc.nextInt();
			String transHash = lineSc.next().intern();

			Transaction trans = getTransFromTable(transHash);

			if (trans == null) {
				trans = addTransactionToTable(time, transHash);
				graph.addVertex(trans);
			}

			// Create each address that is an output to this transaction
			while (lineSc.hasNext())
			{
				String addressHash = lineSc.next().intern();

				// Do not store 'no address' hashes. Just use an empty string.
				// Just to save memory. We don't need it anyways.
				if (addressHash.charAt(0) == 'n')
					addressHash = "";
				
				long amountSent = lineSc.nextLong();

				// Create address for output of the transaction
				Address addr = new Address (amountSent, addressHash);

				// Addresses with no hash are not added to our table
				if (addressHash != "")
					addAddressToTable(addr);

				// Add information to the graph
				graph.addVertex(addr);
				graph.addEdge(edgeCount++, trans, addr);
			}
			
			lineSc.close();
			lineSc = null;
		}
	}

	private void parseInputFiles (int year, int month, Scanner fileSc, Graph<GraphNode, Integer> graph)
	{
		int edgeCount = graph.getEdgeCount ();

		
		while (fileSc.hasNextLine()) {
			
			String wholeLine = fileSc.nextLine();
			
			// Next 'window'
			if (wholeLine.isEmpty())
			{
				totalWindowsInInputFiles++;
				
				// Extract features from this window
				
				
				// Complete execution of this window
				return;
			}
			Scanner lineSc = new Scanner(wholeLine);

			int transactionTime = lineSc.nextInt();
			String transactionHash = lineSc.next().intern();
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
			
			// Check all of the transactions inputs
			while (lineSc.hasNext()) {
				String inputHash = lineSc.next().intern();
				short indexOfInput = lineSc.nextShort();

				Transaction inputTrans = getTransFromTable(inputHash);

				if (inputTrans != null) {
					// Transaction is already in the hashTable
					// Link them together via reference from hashTable
					inputTrans = transactions.get(inputHash);

				} else {
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
					
					// FIXME Use prev transaction hash instead of year
					address = Address.createDummyAddress(indexOfInput, year);
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
						address = Address.createDummyAddress(indexOfInput, year);
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
			
			lineSc.close();
			lineSc = null;
		}
	}

	// De-allocate the memory for this window
	// If onlyDealocateWindow is true, then we are not deallocating the streams/scanner
	private void deallocateMemory(FileInputStream fileStreamOutput, Scanner fileScOutput, FileInputStream fileStreamInput, Scanner fileScInput, 
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
		
		addresses.clear();
		transactions.clear();
		
		System.gc();
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
	
	// Adding a address to the hashTable
	private Address addAddressToTable(Address addr)
	{
		addresses.put(addr.getAddrHash(), addr);

		return addr;
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
