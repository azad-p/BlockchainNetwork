import java.util.Dictionary;
import java.util.Hashtable;

public class Transaction extends GraphNode {
	private String hashOfTransaction;
	private int timeOfTransaction = -1;

	// These linkedlists are in reverse order
	// In other words, the tail [last address] is the first index
	
	private Dictionary<Integer, Address> inputs;
	private Dictionary<Short, Address> outputs;


	public String toString ()
	{
		return "Transaction hash: " + hashOfTransaction + " Time of trans: " + timeOfTransaction;
	}
	
	// Add an address to the list of inputs
	// FIXME: Can do the same thing for the outputs, to make it cleaner
	public void AddInput(Address addr)
	{
		if (inputs == null)
			inputs = new Hashtable <>();
		
		// Input index's are placed in order of their arrival [This is how it is listed in the input file]
		int index = inputs.size();
		
		// Assertion, there is already an address at that index and its not the same one
		// This should not run, means there is a problem
		Address existingAddr = inputs.get(index);
		if (existingAddr != null && addr != existingAddr)
		{
			System.err.println ("A problem has occurred, cannot add an address at this index, once already exists.");
			System.err.println ("Occurs with index: " + index + " \nOn address: " + addr);
			System.err.println ("The address that already exists is given by:" + existingAddr);
			System.exit(0);
		}
		
		inputs.put( index, addr);
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

	public int getTimeOfTransaction() {
		return timeOfTransaction;
	}

	public void setTimeOfTransaction(int timeOfTransaction) {
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

	public Dictionary<Short, Address> getOutputs() {
		return outputs;
	}
	
	public Dictionary<Integer, Address> getInputs()
	{
		return inputs;
	}

	public void setOutputs(Dictionary<Short, Address> outputs) {
		this.outputs = outputs;
	}

	public void setInputs(Dictionary<Integer, Address> inputs) {
		this.inputs = inputs;
	}
}
