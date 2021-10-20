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
		this.timeOfTransaction = timeOfTransaction;
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
