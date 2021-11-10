import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/*
 * This class sorts the edge files and placed them into the sortedFiles directory
 * Files are sorted by the time of the transactions occurring
 * An empty line is placed to indicate the 'end of a day' (Windows are between empty lines)
 * 
 * This file should only need to be executed once
 */
public class FileSorter {

	// Year of the datasets we are sorting
	private static final int YEAR = 2015;
	
	// We are sorting the input files
	// FALSE means output files
	private static final boolean SORT_INPUTS = false;
	
	// Files are sorted one at a time so memory is a non-issue
	// Set this to the month you wish to sort
	// For example, to sort inputs2015_10, this should be 10
	// to sort inputs2015_2, this should be 2
	private static final int MONTH_TO_SORT = 12; 
	
	// Debug mode will end execution after N lines, to test the output
	private static final boolean DEBUG_MODE = false;
	private static final int DEBUG_N = 51100;
	
	private static final String sortedFolderPath = "sortedFiles/"; // To assure this path is used on where the sorted files are placed
	private static final String sortedFileName;
	private static final String unsortedFileFullPath;

	// Initializing some constants
	static
	{
		final String FOLDER_OF_FILES_TO_SORT = "edges" + YEAR;
		final String INPUT_FILE_NAME = "inputs" + YEAR + '_' + MONTH_TO_SORT;
		final String OUTPUT_FILE_NAME = "outputs" + YEAR + '_' + MONTH_TO_SORT;
		
		if (SORT_INPUTS)
		{
			sortedFileName = "Sorted_" + INPUT_FILE_NAME;
			unsortedFileFullPath = FOLDER_OF_FILES_TO_SORT + '/' + INPUT_FILE_NAME;
		}
		else
		{
			sortedFileName = "Sorted_" + OUTPUT_FILE_NAME;
			unsortedFileFullPath = FOLDER_OF_FILES_TO_SORT + '/' + OUTPUT_FILE_NAME;
		}
	}
	
	static ArrayList<LineObject> sortFile ()
	{
		// Similar to how we created the transaction network, we will read in the file and parse the line
		BufferedReader fileSc = null;
		FileReader reader = null;
		
		final int NUM_PREDICTED_LINES = 100000;
		ArrayList<LineObject> lineValues = new ArrayList<>(NUM_PREDICTED_LINES);
		
		try {
			
			// Run through the input files for each month
			System.out.println ("Reading file into memory ...");
			
			reader = new FileReader(unsortedFileFullPath + ".txt");
			fileSc = new BufferedReader(reader);
			
			System.out.println ("File: " + unsortedFileFullPath + ".txt" + " loaded into memory...");
			System.out.println ("Going through the file to sort it ... (This may take some time)");
			
			String inputLine = null;
			int numberOfLinesSurpassed = 0;
			
			// Continue until EOF is reached
			while ((inputLine = fileSc.readLine()) != null) {
				
				// Determine the time of the transaction
				int timeEndIndex = inputLine.indexOf('\t');
				
				int transactionTime = Integer.parseInt(inputLine.substring(0, timeEndIndex));
				String resOfLine = inputLine.substring(timeEndIndex);
				
				lineValues.add(new LineObject (transactionTime, resOfLine));
				
				numberOfLinesSurpassed++;
				
				// Finish early for debugging purposes
				if (DEBUG_MODE && numberOfLinesSurpassed >= DEBUG_N)
					break;
			}
			
		System.out.println ("Gone through the file in its entirity");
		System.out.println ("Now sorting the file ...");
		System.out.println ("Number of lines to sort: " + lineValues.size());
			
		// Sort the lineValues array
		// Obtained from https://stackoverflow.com/questions/10396970/sort-a-list-that-contains-a-custom-class
		Collections.sort(lineValues, new Comparator<LineObject>() {
		    public int compare(LineObject first, LineObject second)  {
		        return first.transactionTime - second.transactionTime;
		    }
		});
		
		System.out.println ("The file has been sorted");
			
		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// ~~~~~ Closing files and deallocating  ~~~~
		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
			
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
			reader = null;
		}
		
		return lineValues;
	}
	
	static void writeFile(ArrayList<LineObject> contents)
	{
		FileWriter fw = null;
		BufferedWriter bw = null;
		PrintWriter writer = null;
		
		try {
			
			fw = new FileWriter (sortedFolderPath + sortedFileName + ".txt", false);
			bw = new BufferedWriter (fw);
			writer = new PrintWriter (bw);
			
			// Now we have the sorted list
			// Write the results to the file
			System.out.println ("Writing contents of sorted file to a new file ...");
			
			// Create an empty line if we are on a new day
			int currentDay = -1;
			int lastDay = -1;
			
			for (int i = 0; i < contents.size(); i++)
			{
				LineObject curLine = contents.get(i);
				currentDay = curLine.dayOfTransaction;
				
				if (lastDay == -1)
				{
					System.out.println ("On day " + currentDay + "...");
					lastDay = currentDay;
				}
				
				// We are now on a new day
				else if (lastDay != currentDay)
				{
					// Assertion check
					// We should not be moving backwards [The next day should always be larger from the start]
					// Otherwise the file is not properly sorted
					if (currentDay < lastDay)
					{
						System.err.println ("Assertion failed. Earlier day is placed later in the sorted file.");
						System.err.println ("Sorting is not accurate");
						System.exit(0);
					}

					writer.println (); // Empty line on a new day
					lastDay = currentDay;
					System.out.println ("On day " + currentDay + "...");
				}
				
				writer.println (curLine.toString());
			}
			
			writer.println(); // Extra empty line at the EOF, so we can read the final empty line
			writer.flush();
		
		} catch (IOException e) { e.printStackTrace(); System.out.println ("Failed to write contents to file."); } 
		finally {
			try {
				if (fw != null)
					fw.close();
				if (bw != null)
					bw.close();
				if (writer != null)
					writer.close();
			} catch (IOException e) { System.out.println ("Failed to close files"); e.printStackTrace(); System.exit(0); }
		}
	}
	
	public static void main(String[] args) 
	{
		
		ArrayList<LineObject> lineValues = sortFile();
		
		writeFile (lineValues);
		
		lineValues = null;
		
		System.out.println ("Application completed successfully!");
		System.out.println ("The file " + unsortedFileFullPath + ".txt" + " Should now be sorted in the sortedFiles directory.");
	}
}

// Line of an object (Time, rest of data)
class LineObject {
	int transactionTime;
	int dayOfTransaction; // NOTE: Since files are already broken down by year/month, same days are also the same month and year
	String restOfLine;
	
	LineObject (int time, String line)
	{
		this.transactionTime = time;
		this.restOfLine = line;
		
		// Obtained from https://stackoverflow.com/questions/45392163/java-get-current-day-from-unix-timestamp
		// Good solution to getting the day from epoch time

		long timeMultiplier = 1000L;
		this.dayOfTransaction = Integer.parseInt(new SimpleDateFormat("dd").format(new Date(time * timeMultiplier)));
	}
	
	// Cast the entire line together
	@Override
	public String toString()
	{
		return transactionTime + restOfLine;
	}
}