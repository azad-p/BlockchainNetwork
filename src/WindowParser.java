import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
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
	// The bitcoin network
	private final Graph<GraphNode, Integer> graph;

	private final HashMap<String, Transaction> transactions;
	private final HashMap<String, Address> addresses;

	private int YEAR_OF_DATASET;
	private final String FILE_FOLDER;
	private final String INPUT_FILE_NAME;
	private final String OUTPUT_FILE_NAME;

	private byte[] MONTHS;
	
	WindowParser (int YEAR_OF_DATASET_TO_PARSE, byte[] MONTHS_TO_EXTRACT_FEATURES)
	{
		final int HASH_SET_SIZE = 16 * 256;
		
		this.YEAR_OF_DATASET = YEAR_OF_DATASET_TO_PARSE;
		this.FILE_FOLDER = "sortedFiles";
		this.INPUT_FILE_NAME = "Sorted_inputs" + YEAR_OF_DATASET_TO_PARSE + '_';
		this.OUTPUT_FILE_NAME = "Sorted_outputs" + YEAR_OF_DATASET_TO_PARSE + '_';

		this.transactions = new HashMap<String, Transaction>(HASH_SET_SIZE, 1.0f);
		this.addresses = new HashMap<String, Address>(HASH_SET_SIZE, 1.0f);
		this.graph = new DirectedSparseGraph<>();
		this.MONTHS = MONTHS_TO_EXTRACT_FEATURES;
	}
	
	public void parseOutput() {
		parseOutputFiles();
	}

	public void linkInputFileToOutputs() {
		parseInputFiles ();
	}
	
	private void parseOutputFiles ()
	{
		int edgeCount = graph.getEdgeCount();
		FileInputStream fileStream = null;
		Scanner fileSc = null;
		
		for (int i = 0; i < MONTHS.length; i++) {
			System.out.println ("Reading output file: " + MONTHS [i]);

			try {
				// Run through the input files for each month
				fileStream = new FileInputStream(FILE_FOLDER + '/' + OUTPUT_FILE_NAME + MONTHS [i] + ".txt");
				fileSc = new Scanner (fileStream, "UTF-8");

				System.out.println ("Placed file into memory. Now going through the file..");
				while (fileSc.hasNextLine()) {
					
					String wholeLine = fileSc.nextLine();
					
					// Next 'window'
					if (wholeLine.isEmpty())
					{
						System.out.println ("Completed a window");
						continue;
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
						// Just take the first 3 characters of the address hash. It's not important anyways so saves memory
						//  [That's why we do substring]
						String addressHash = lineSc.next().substring(0, 3).intern();

						// Do not store 'no address' hashes. Just use an empty string.
						// Just to save memory. We don't need it anyways.
						if (addressHash.charAt(0) == 'n')
							addressHash = "";
						
						long amountSent = lineSc.nextLong();

						// Create address for output of the transaction
						Address addr = new Address (amountSent, addressHash);

						// Addresses with no hash are not added to our table
						//if (addressHash != "") <--- Removed to save memory
						//	addAddressToTable(addr);

						// Add information to the graph
						graph.addVertex(addr);
						graph.addEdge(edgeCount++, trans, addr);
					}
					
					lineSc.close();
					lineSc = null;
				}

				fileSc.close();
				fileSc = null;
				fileStream.close();
				fileStream = null;
				System.gc(); // Free memory after each file

			} catch (IOException e) {
				System.err.println("A problem has occurred reading the file.");
				e.printStackTrace();
				System.exit(0);
			} finally {
				// Close any file
				if (fileStream != null)
					try {
						fileStream.close();
					} catch (IOException e) {
						System.err.println ("A problem occurred with reading the files");
						e.printStackTrace();
						System.exit(0);
					}
				if (fileSc != null)
					fileSc.close();

				// Assuring de-allocation of reference for garbage collector
				fileStream = null;
				fileSc = null;
			}
		}
	}

	private void parseInputFiles ()
	{
		int edgeCount = graph.getEdgeCount ();
		FileInputStream fileStream = null;
		Scanner fileSc = null;

		for (int i = 0; i < MONTHS.length; i++) {
			System.out.println ("Reading input file: " + MONTHS [i]);

			try {
				// Run through the input files for each month
				fileStream = new FileInputStream(FILE_FOLDER + '/' + INPUT_FILE_NAME + MONTHS [i] + ".txt");
				fileSc = new Scanner (fileStream, "UTF-8");

				System.out.println ("Placed file into memory. Now going through the file..");
				while (fileSc.hasNextLine()) {
					
					String wholeLine = fileSc.nextLine();
					
					// Next 'window'
					if (wholeLine.isEmpty())
					{
						System.out.println ("Completed a window");
						continue;
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
							address = Address.createDummyAddress(indexOfInput, YEAR_OF_DATASET);
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
								address = Address.createDummyAddress(indexOfInput, YEAR_OF_DATASET);
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

				fileSc.close();
				fileSc = null;
				fileStream.close();
				fileStream = null;
				System.gc(); // Free memory after each file

			} catch (IOException e) {
				System.err.println("A problem has occurred");
				System.exit(0);
			} finally {
				// If any scanner were not closed, close them
				if (fileStream != null)
					try {
						fileStream.close();
					} catch (IOException e) {
						System.err.println ("A problem occurred with reading the files");
						e.printStackTrace();
						System.exit(0);
					}
				if (fileSc != null)
					fileSc.close();

				// Assuring de-allocation of reference for garbage collector
				fileStream = null;
				fileSc = null;
			}
		}
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
	public Graph<GraphNode, Integer> getGraph() { return graph; }
}
