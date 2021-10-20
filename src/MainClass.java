import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;

import java.io.*;
import java.util.*;
import java.time.Instant;

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

		transactions.put(transHash, newTrans);
		
		return newTrans;
	}

	static Address addAddressToTable(Address address)
	{
		addresses.put(address.addrHash, address);

		return address;
	}

	static Transaction getTransFromTable (String transHash)
	{
		return transactions.get(transHash);
	}

	static void parseOutputFiles (Graph graph)
	{
		int edgeCount = graph.getEdgeCount();
		Scanner fileSc = null;
		Scanner lineSc = null;

		for (int i = 1; i <= 12; i++) {
			try {
				// Run through the input files for each month
				fileSc = new Scanner(new File(FILE_FOLDER + '/' + OUTPUT_FILE_NAME + i + ".txt"));

				while (fileSc.hasNextLine()) {
					String entireLine = fileSc.nextLine();

					// Parse the line
					lineSc = new Scanner (entireLine);

					long time = lineSc.nextLong();
					String transHash = lineSc.next();

					Transaction trans = getTransFromTable(transHash);

					if (trans == null) {
						trans = addTransactionToTable(time, transHash);
						graph.addVertex(trans);
					}

					Dictionary<Integer, Address> addressesInOutput = new Hashtable<>();
					int indexOfOutput = 0;

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
						addr.sendee = trans;
						addressesInOutput.put(indexOfOutput++, addr);

						// Addresses with no hash are not added to our table
						if (addressHash != "")
							addAddressToTable(addr);

						// Add information to the graph
						graph.addVertex(addr);
						graph.addEdge(edgeCount++, trans, addr);
					}

					// Add information to the transaction
					trans.outputs = addressesInOutput;

					lineSc.close();
					lineSc = null;
				}

				fileSc.close();
				fileSc = null;

			} catch (IOException e) {
				System.err.println("A problem has occurred");
			} finally {
				// If any scanner were not closed, close them
				if (fileSc != null)
					fileSc.close();
				if (lineSc != null)
					lineSc.close();
			}
		}
	}

	static void parseInputFiles (Graph graph)
	{
		int edgeCount = graph.getEdgeCount();

		Scanner fileSc = null;
		Scanner lineSc = null;

		for (int i = 1; i <= 12; i++) {
			try {
				// Run through the input files for each month
				fileSc = new Scanner(new File(FILE_FOLDER + '/' + INPUT_FILE_NAME + i + ".txt"));

				while (fileSc.hasNextLine()) {
					String entireLine = fileSc.nextLine();

					// Parse the line
					lineSc = new Scanner(entireLine);

					long transactionTime = lineSc.nextLong();
					String transactionHash = lineSc.next();
					Transaction nextTrans = getTransFromTable(transactionHash);

					if (nextTrans != null) {
						// Transaction is already in the hashTable
						// Just update any of its information
						nextTrans.timeOfTransaction = transactionTime;
					} else {
						// Transaction is not in the Hashtable
						// Create the class, update any of its information, and add it to the Hashtable
						nextTrans = addTransactionToTable(transactionTime, transactionHash);
						graph.addVertex(nextTrans);
					}

					// Check all of the transactions inputs
					while (lineSc.hasNext()) {
						String inputHash = lineSc.next();
						int indexOfInput = lineSc.nextInt();

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

						/*
						System.out.println ("~Input Trans info~");
						System.out.println (inputTrans.hashOfTransaction);
						System.out.println (indexOfInput);
						if (inputTrans.outputs != null) System.out.println (inputTrans.outputs.size());*/

						Address address = null;

						if (inputTrans.outputs == null)
						{
							// Special case
							// Address was not found in the output files for this year
							// Create a dummy address to mimick the transaction. Note that, the btc will not be known

							Address dummyAddress = Address.createDummyAddress(inputTrans, nextTrans, indexOfInput, YEAR);

							Dictionary<Integer, Address> outputs = new Hashtable<>();
							outputs.put(indexOfInput, dummyAddress);
							inputTrans.outputs = outputs;
							address = dummyAddress;
						}
						else {
							// Fail
							// This may be a case where the output occured in a different year, but we already created the outputs dictionary
							if (inputTrans.outputs.get(indexOfInput) == null)
							{
								System.out.println (".. Placing an output to an address that already existed..");

								Address dummyAddress = Address.createDummyAddress(inputTrans, nextTrans, indexOfInput, YEAR);
								inputTrans.outputs.put(indexOfInput, dummyAddress);
								address = dummyAddress;
							}

							address = inputTrans.outputs.get(indexOfInput);
						}

						if (address == null) { System.err.println ("ERROR: Assertion ran. No address associated with the transaction."); System.exit(0); }

						graph.addEdge(edgeCount++, inputTrans, address);
						graph.addEdge(edgeCount++, address, nextTrans);
						// address.sendee = inputTrans; Should not need to be written
						address.receivee = nextTrans;
					}

					lineSc.close();
					lineSc = null;
				}

				fileSc.close();
				fileSc = null;

			} catch (IOException e) {
				System.err.println("A problem has occurred");
			} finally {
				// If any scanner were not closed, close them
				if (fileSc != null)
					fileSc.close();
				if (lineSc != null)
					lineSc.close();
			}
		}
	}
	
	public static void main(String[] args) {

		Graph<Transaction, Integer> bitcoinNetwork = new DirectedSparseGraph<>();

		// Assumption: Output addresses are initialized before going through the input file
		parseOutputFiles (bitcoinNetwork);
		parseInputFiles (bitcoinNetwork);
		System.out.println(transactions.get("35288d269cee1941eaebb2ea85e32b42cdb2b04284a56d8b14dcc3f5c65d6055").outputs.get(0));

	}
}
