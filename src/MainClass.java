import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.time.Instant;
import java.util.Hashtable;

public class MainClass {
	
	static final int YEAR = 2009;
	
	static final String FILE_FOLDER = "edges" + YEAR;
	static final String INPUT_FILE_NAME = "inputs" + YEAR + '_';
	static final String OUTPUT_FILE_NAME = "outputs" + YEAR + '_';
	
	static final int HASH_SET_OPTIONS = 16 * 16 * 16;
	static Hashtable<String, Transaction> transactions = new Hashtable<String, Transaction>(HASH_SET_OPTIONS);
	static Hashtable<String, Address> addresses = new Hashtable<String, Address>(HASH_SET_OPTIONS);

	static Transaction addTransactionToTable(long time, String transHash)
	{
		Transaction newTrans = new Transaction();
		
		// Update transactions inputs
		newTrans.hashOfTransaction = transHash;
		
		if (time != -1)
			newTrans.timeOfTransaction = time;
		
		String firstThreeChars = transHash.charAt(0) + "" + transHash.charAt(1) + transHash.charAt(2);
		transactions.put(firstThreeChars, newTrans);
		
		return newTrans;
	}

	static Address addAddressToTable(Address address)
	{
		String hash = address.addrHash;

		String firstThreeChars = hash.charAt(0) + "" + hash.charAt(1) + hash.charAt(2);
		addresses.put(firstThreeChars, address);

		return address;
	}

	static Transaction getTransFromTable (String transHash)
	{
		String firstThreeChars = transHash.charAt(0) + "" + transHash.charAt(1) + transHash.charAt(2);
		Transaction trans = transactions.get(firstThreeChars);

		return trans;
	}

	static void parseOutputFile (Graph graph)
	{
		int edgeCount = graph.getEdgeCount();

		for (int i = 1; i <= 12; i++) {
			try {
				// Run through the input files for each month
				Scanner sc = new Scanner(new File(FILE_FOLDER + '/' + OUTPUT_FILE_NAME + i + ".txt"));

				while (sc.hasNextLine()) {

					long time = sc.nextLong();
					String transHash = sc.next();

					Transaction trans = getTransFromTable(transHash);

					if (trans == null) {
						trans = addTransactionToTable(time, transHash);
						graph.addVertex(trans);
					}

					LinkedList <Address> addressesInOutput = new LinkedList<>();

					// Create each address that is an output to this transaction
					while (sc.hasNext())
					{
						String addressHash = sc.next();
						long amountSent = sc.nextLong();

						// Create address for output of the transaction
						Address addr = new Address (amountSent, addressHash);
						addressesInOutput.add(addr);
						addAddressToTable(addr);

						// Add information to the graph
						graph.addVertex(addr);
						graph.addEdge(edgeCount++, trans, addr);
					}

					// Add information to the transaction
					trans.outputs = addressesInOutput;
				}

			} catch (IOException e) {
				System.err.println("A problem has occurred");
			}
		}
	}

	static void parseInputFile (Graph graph)
	{
		int edgeCount = graph.getEdgeCount();

		for (int i = 1; i <= 12; i++)
		{
			try {
				// Run through the input files for each month
				Scanner sc = new Scanner (new File (FILE_FOLDER + '/' + INPUT_FILE_NAME + i + ".txt"));

				while (sc.hasNext())
				{
					long transactionTime = sc.nextLong();
					Instant instant = Instant.ofEpochSecond(transactionTime);

					System.out.println (instant + " Month: " + i);

					String transactionHash = sc.next();
					Transaction nextTrans = null;

					if (transactions.containsValue(transactionHash))
					{
						// Transaction is already in the hashTable
						// Just update any of its information
						nextTrans = transactions.get(transactionHash);
						nextTrans.timeOfTransaction = transactionTime;
					}
					else
					{
						// Transaction is not in the Hashtable
						// Create the class, update any of its information, and add it to the Hashtable
						nextTrans = addTransactionToTable (transactionTime, transactionHash);
						graph.addVertex(nextTrans);
					}

					// Check all of the transactions inputs
					while (sc.hasNext())
					{
						String inputHash = sc.next();
						int indexOfInput = sc.nextInt();

						Transaction inputTrans = null;

						if (transactions.containsValue (inputHash))
						{
							// Transaction is already in the hashTable
							// Link them together via reference from hashTable
							inputTrans = transactions.get (inputHash);
							//inputTrans.connections.put(indexOfInput, null);
							graph.addEdge (edgeCount, inputTrans, nextTrans);
						}
						else
						{
							// Transaction is not in the Hashtable
							// Create the class, update any of its information, and add it to the Hashtable
							// Then link them together
							inputTrans = addTransactionToTable (-1, inputHash);
							graph.addVertex(inputTrans);
							graph.addEdge(edgeCount, inputTrans, nextTrans);
						}
					}

					// Skip to next line
					sc.nextLine();
				}
			} catch (FileNotFoundException e) {

				System.err.println ("File could not be found.");
				e.printStackTrace();

			}
		}
	}
	
	public static void main(String[] args) {

		Graph<Transaction, Integer> bitcoinNetwork = new DirectedSparseGraph<>();

		parseOutputFile (bitcoinNetwork);
	}
}
