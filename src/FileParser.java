import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Scanner;

import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;

public class FileParser {
	
	// The bitcoin network
	private final Graph<GraphNode, Integer> graph;
	
	private final Hashtable<String, Transaction> transactions;
	private final Hashtable<String, Address> addresses;

	final int HASH_SET_OPTIONS = 16 * 16 * 16;
	
	private int YEAR_OF_DATASET;
	private final String FILE_FOLDER;
	private final String INPUT_FILE_NAME;
	private final String OUTPUT_FILE_NAME;
	
	FileParser (int YEAR_OF_DATASET_TO_PARSE)
	{
		this.YEAR_OF_DATASET = YEAR_OF_DATASET_TO_PARSE;
		this.FILE_FOLDER = "edges" + YEAR_OF_DATASET_TO_PARSE;
		this.INPUT_FILE_NAME = "inputs" + YEAR_OF_DATASET_TO_PARSE + '_';
		this.OUTPUT_FILE_NAME = "outputs" + YEAR_OF_DATASET_TO_PARSE + '_';
		
		this.transactions = new Hashtable<String, Transaction>(HASH_SET_OPTIONS);
		this.addresses = new Hashtable<String, Address>(HASH_SET_OPTIONS);
		this.graph = new DirectedSparseGraph<>();
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
		BufferedReader fileSc = null;
		Scanner lineSc = null;
		FileReader reader = null;

		for (int i = 1; i <= 12; i++) {
			System.out.println ("Reading output file: " + i);
			
			try {
				// Run through the input files for each month
				reader = new FileReader(FILE_FOLDER + '/' + OUTPUT_FILE_NAME + i + ".txt");
				fileSc = new BufferedReader(reader);
				String inputLine = null;
				
				while ((inputLine = fileSc.readLine()) != null) {
					String entireLine = inputLine;

					// Parse the line
					lineSc = new Scanner (entireLine);

					int time = lineSc.nextInt();
					String transHash = lineSc.next();

					Transaction trans = getTransFromTable(transHash);

					if (trans == null) {
						trans = addTransactionToTable(time, transHash);
						graph.addVertex(trans);
					}

					Dictionary<Byte, Address> addressesInOutput = new Hashtable<>();
					byte indexOfOutput = 0;

					// Create each address that is an output to this transaction
					while (lineSc.hasNext())
					{
						String addressHash = lineSc.next();

						// Do not store 'no address' hashes. Just use an empty string.
						if (addressHash.charAt(0) == 'n')
							addressHash = "";

						long amountSent = lineSc.nextLong();

						// Create address for output of the transaction
						Address addr = new Address (amountSent, addressHash);
						addr.setSendee(trans);
						addressesInOutput.put(indexOfOutput++, addr);

						// Addresses with no hash are not added to our table
						if (addressHash != "")
							addAddressToTable(addr);

						// Add information to the graph
						graph.addVertex(addr);
						graph.addEdge(edgeCount++, trans, addr);
					}

					// Add information to the transaction
					trans.setOutputs (addressesInOutput);

					lineSc.close();
					lineSc = null;
				}

				fileSc.close();
				fileSc = null;

			} catch (IOException e) {
				System.err.println("A problem has occurred");
				System.exit(0);
			} finally {
				// Close any file
				if (fileSc != null)
					try {
						fileSc.close();
					} catch (IOException e) {
						System.err.println ("A problem occurred with reading the files");
						e.printStackTrace();
						System.exit(0);
					}
				if (lineSc != null)
					lineSc.close();
				
				if (reader != null)
					try {
						reader.close();
					} catch (IOException e) {
						System.err.println ("A problem occurred with reading the files");
						e.printStackTrace();
						System.exit(0);
					}
				
				// Assuring de-allocation of reference for garbage collector
				fileSc = null;
				lineSc = null;
				reader = null;
			}
		}
	}

	private void parseInputFiles ()
	{
		int edgeCount = graph.getEdgeCount();
		BufferedReader fileSc = null;
		Scanner lineSc = null;
		FileReader reader = null;

		for (int i = 1; i <= 12; i++) {
			System.out.println ("Reading input file: " + i);
			
			try {
				// Run through the input files for each month
				reader = new FileReader(FILE_FOLDER + '/' + INPUT_FILE_NAME + i + ".txt");
				fileSc = new BufferedReader(reader);
				String inputLine = null;
				
				while ((inputLine = fileSc.readLine()) != null) {
					String entireLine = inputLine;

					// Parse the line
					lineSc = new Scanner(entireLine);

					int transactionTime = lineSc.nextInt();
					String transactionHash = lineSc.next();
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
						String inputHash = lineSc.next();
						byte indexOfInput = lineSc.nextByte();

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
						if (inputTrans.getOutputs() == null)
						{
							// Special case
							// Address was not found in the output files for this year
							// Create a dummy address to mimic the transaction. Note that, the btc will not be known
							System.out.println ("Here2");
							Address dummyAddress = Address.createDummyAddress(inputTrans, trans, indexOfInput, YEAR_OF_DATASET);

							Dictionary<Byte, Address> outputs = new Hashtable<>();
							outputs.put(indexOfInput, dummyAddress);
							inputTrans.setOutputs(outputs);
							address = dummyAddress;
						}
						else {
							// Fail
							// This may be a case where the output occurred in a different year, but we already created the outputs dictionary
							if (inputTrans.getOutputs().get(indexOfInput) == null)
							{
								System.out.println (".. Placing an output to an address that already existed..");
								System.out.println ("Here3");
								Address dummyAddress = Address.createDummyAddress(inputTrans, trans, indexOfInput, YEAR_OF_DATASET);
								inputTrans.getOutputs().put(indexOfInput, dummyAddress);
								address = dummyAddress;
							}
							else
							{System.out.println ("Here4");
								address = inputTrans.getOutputs().get(indexOfInput);
								inputTrans.getOutputs().put(indexOfInput, address);
								address.setSendee(inputTrans);
								address.setReceivee(trans);
							}
						}

						if (address == null) { System.err.println ("ERROR: Assertion ran. No address associated with the transaction."); System.exit(0); }

						// Link the information together on the graph & data
						graph.addEdge(edgeCount++, inputTrans, address);
						graph.addEdge(edgeCount++, address, trans);
						trans.AddInput(address);
					}

					lineSc.close();
					lineSc = null;
				}

				fileSc.close();
				fileSc = null;

			} catch (IOException e) {
				System.err.println("A problem has occurred");
				System.exit(0);
			} finally {
				// If any scanner were not closed, close them
				if (fileSc != null)
					try {
						fileSc.close();
					} catch (IOException e) {
						System.err.println ("A problem occurred with reading the files");
						e.printStackTrace();
						System.exit(0);
					}
				if (lineSc != null)
					lineSc.close();
				
				if (reader != null)
					try {
						reader.close();
					} catch (IOException e) {
						System.err.println ("A problem occurred with reading the files");
						e.printStackTrace();
						System.exit(0);
					}
				
				// Assuring de-allocation of reference for garbage collector
				fileSc = null;
				lineSc = null;
				reader = null;
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

	// Adding an address to the hashtable, and returning the address
	private Address addAddressToTable(Address address)
	{
		addresses.put(address.getAddrHash(), address);

		return address;
	}

	// Getting a transaction from the hashtable
	private Transaction getTransFromTable (String transHash)
	{
		return transactions.get(transHash);
	}
	
	// ~~~~~~~~~~~~~~~~~
	// Getters => Does not use setters
	// ~~~~~~~~~~~~~~~~~
	public Graph<GraphNode, Integer> getBitcoinNetwork()
	{
		return graph;
	}
	
	public Dictionary<String, Transaction> getTransactions()
	{
		return transactions;
	}
	
	public Dictionary<String, Address> getAddresses()
	{
		return addresses;
	}
}
