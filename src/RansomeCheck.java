import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;

public class RansomeCheck {
	
	final String FILE_PATH = "sortedFiles/SortedDataFinal.csv";
	final int YEAR;
	
	// Contains each ransome address from the sorted file
	// Stored as <Day, List of hashes>
	HashMap<Integer, LinkedList <String>> table;
	
	RansomeCheck (int year)
	{
		this.YEAR = year;
		initChecker(year);
	}
	
	// Initialize the checker
	private void initChecker (int YEAR)
	{
		this.table = new HashMap <>(365);
	
		FileReader fr = null;
		BufferedReader br = null;
		
		try {
			fr = new FileReader (FILE_PATH);
			br = new BufferedReader (fr);
			
			String line;
			
			line = br.readLine(); // Skip first line
			while ((line = br.readLine()) != null)
			{
				String[] row = line.split(",");
				
				String hash = row [0];
				int year = Integer.parseInt(row[1]);
				int day = Integer.parseInt(row[2]);
				
				// Can ignore different years
				if (year != this.YEAR)
					continue;

				if (this.table.containsKey(day))
				{
					// No need to add duplicate hashes (If any exist)
					if (!this.table.get(day).contains(hash))
						this.table.get(day).add(hash);
				}
				else
				{
					LinkedList<String> temp = new LinkedList<>();
					temp.add(hash);
					
					this.table.put(day, temp);
				}
			}

		} catch (FileNotFoundException e) {
			System.err.println ("Failed to load sorted data file");
			e.printStackTrace();
			System.exit(0);
		} catch (IOException e) {
			System.err.println ("Failed to read sorted data file");
			e.printStackTrace();
			System.exit(0);
		} finally {
			
			try {
				if (fr != null)
					fr.close();
				if (br != null)
					br.close();
			} catch (IOException e1) {
				System.err.println ("Failed to close sorted data file");
				e1.printStackTrace();
				System.exit(0);
			}
			
			fr = null;
			br = null;
		}
	}
	
	// Returns true if the given hash is of a ransome address
	public boolean isRansomeWareAddress(String hash, int year, int day)
	{
		if (year != this.YEAR)
		{
			System.err.println ("Error. Years do not match with the ransome checker.");
			System.exit(0);
		}
		
		return this.table.get(day) != null && this.table.get(day).contains(hash);
	}
	
}
