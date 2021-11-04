import java.util.Dictionary;
import java.util.Hashtable;

public class Transaction extends GraphNode {
	private String hashOfTransaction;
	private int timeOfTransaction = -1;

	@Override
	public String toString()
	{
		return "Transaction time: " + timeOfTransaction + " Hash: " + hashOfTransaction;
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
}
