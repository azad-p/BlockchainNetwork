import java.util.Arrays;
import java.util.Dictionary;

public class Transaction extends GraphNode {
	// TODO: Make private with getters / setters. Lazy day
	private String hashOfTransaction;
	private long timeOfTransaction = -1;

	// These linkedlists are in reverse order
	// In other words, the tail [last address] is the first index
	private Dictionary<Integer, Address> inputs;
	private Dictionary<Integer, Address> outputs;


	public String toString ()
	{
		return "Transaction hash: " + hashOfTransaction + " Time of trans: " + timeOfTransaction;
	}
	
	// ~~~~~~~~~~~~~~~~~
	// Getters / setters
	// ~~~~~~~~~~~~~~~~~
	
	public String getHashOfTransaction() {
		return hashOfTransaction;
	}

	public void setHashOfTransaction(String hashOfTransaction) {
		this.hashOfTransaction = hashOfTransaction;
	}

	public long getTimeOfTransaction() {
		return timeOfTransaction;
	}

	public void setTimeOfTransaction(long timeOfTransaction) {
		if (this.timeOfTransaction == -1)
			this.timeOfTransaction = timeOfTransaction;
		
		// A transaction shouldn't already have a different time than what we expect
		if (this.timeOfTransaction != -1 && this.timeOfTransaction != timeOfTransaction)
		{
			System.err.println ("A different time for this transaction has been recognized.");
			System.err.println ("Something may be wrong..");
			System.exit(0);
		}
	}

	public Dictionary<Integer, Address> getOutputs() {
		return outputs;
	}
	
	public Dictionary<Integer, Address> getInputs()
	{
		return inputs;
	}

	public void setOutputs(Dictionary<Integer, Address> outputs) {
		this.outputs = outputs;
	}

	public void setInputs(Dictionary<Integer, Address> inputs) {
		this.inputs = inputs;
	}
}
