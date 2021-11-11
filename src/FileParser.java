import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;

public class FileParser extends Parser {

	// The bitcoin network
	private final Graph<GraphNode, Integer> graph;

	private final HashMap<String, Transaction> transactions;

	private int YEAR_OF_DATASET;
	private final String FILE_FOLDER;
	private final String INPUT_FILE_NAME;
	private final String OUTPUT_FILE_NAME;

	FileParser (int YEAR_OF_DATASET_TO_PARSE)
	{
		final int HASH_SET_SIZE = 16 * 256;
		
		this.YEAR_OF_DATASET = YEAR_OF_DATASET_TO_PARSE;
		this.FILE_FOLDER = "edges" + YEAR_OF_DATASET_TO_PARSE;
		this.INPUT_FILE_NAME = "inputs" + YEAR_OF_DATASET_TO_PARSE + '_';
		this.OUTPUT_FILE_NAME = "outputs" + YEAR_OF_DATASET_TO_PARSE + '_';

		this.transactions = new HashMap<String, Transaction>(HASH_SET_SIZE, 1.0f);
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

				System.out.println ("Placed file into memory. Now going through the file..");
				while ((inputLine = fileSc.readLine()) != null) {
					String entireLine = inputLine;

					// Parse the line
					lineSc = new Scanner (entireLine);
					entireLine = null;

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

						// Add information to the graph
						graph.addVertex(addr);
						graph.addEdge(edgeCount++, trans, addr);
					}

					lineSc.close();
					lineSc = null;
				}

				fileSc.close();
				fileSc = null;
				System.gc(); // Free memory after each file

			} catch (IOException e) {
				System.err.println("A problem has occurred reading the file.");
				e.printStackTrace();
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
		int edgeCount = graph.getEdgeCount ();
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

				System.out.println ("Placed file into memory. Now going through the file..");
				while ((inputLine = fileSc.readLine()) != null) {
					String entireLine = inputLine;

					// Parse the line
					lineSc = new Scanner(entireLine);
					entireLine = null;

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
							
							address = Address.createDummyAddress(inputHash, indexOfInput, YEAR_OF_DATASET);
						}
						else {

							// Output edges exist

							Collection<Integer> outputs = graph.getOutEdges(inputTrans);

							// Sort the outputs by their index in the graph
							ArrayList<Integer> outputsSorted = new ArrayList<Integer>(outputs);
							Collections.sort(outputsSorted);

							if (outputs == null || outputs.size() == 0 || outputs.size() <= indexOfInput)
							{
								address = Address.createDummyAddress(inputHash, indexOfInput, YEAR_OF_DATASET);
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
				System.gc(); // Free memory after each file

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
