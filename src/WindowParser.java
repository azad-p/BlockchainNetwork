import java.util.HashMap;

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

	WindowParser (int YEAR_OF_DATASET_TO_PARSE)
	{
		final int HASH_SET_SIZE = 16 * 256;
		
		this.YEAR_OF_DATASET = YEAR_OF_DATASET_TO_PARSE;
		this.FILE_FOLDER = "edges" + YEAR_OF_DATASET_TO_PARSE;
		this.INPUT_FILE_NAME = "inputs" + YEAR_OF_DATASET_TO_PARSE + '_';
		this.OUTPUT_FILE_NAME = "outputs" + YEAR_OF_DATASET_TO_PARSE + '_';

		this.transactions = new HashMap<String, Transaction>(HASH_SET_SIZE, 1.0f);
		this.addresses = new HashMap<String, Address>(HASH_SET_SIZE, 1.0f);
		this.graph = new DirectedSparseGraph<>();
	}
	
	public void parseOutput() {
		parseOutputFiles();
	}

	public void linkInputFileToOutputs() {
		parseInputFiles ();
	}
	
	private void parseOutputFiles() {
		
	}
	
	private void parseInputFiles() {
		
	}
}
