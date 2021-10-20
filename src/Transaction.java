import java.util.Arrays;
import java.util.Dictionary;

public class Transaction extends GraphNode {
	// TODO: Make private with getters / setters. Lazy day
	public String hashOfTransaction;
	public long timeOfTransaction;

	// These linkedlists are in reverse order
	// In other words, the tail [last address] is the first index
	public Dictionary<Integer, Address> inputs;
	public Dictionary<Integer, Address> outputs;


	// public Dictionary<Integer, Address> connections;

	/*
	public Transaction[] inputTransactions;
	public OutputAddr<String, Long>[] outputAddresses;
	public OutputTrans<String, Integer>[] outputTransactions;
	
	class OutputAddr <String, Long>
	{
		String HashOfAddress;
		long AmountOfBitcoinSent;
	}
	
	class OutputTrans <String, Integer>
	{
		String OutputTransactionsHash;
		int indexOfOutput;
	} */

	public String toString ()
	{
		return "Transaction hash: " + hashOfTransaction + " Time of trans: " + timeOfTransaction;
	}
}
