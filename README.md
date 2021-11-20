# BlockChain Network

Building a network with the input/outputs of transactions in Bitcoin.


# Build dependencies

This project utilizes the Jung library for Graphing the Network.

The jar files for this library have been provided in the main-path of the project.

If the jar files fail, jung-2.0.1 can be downloaded here https://sourceforge.net/projects/jung/files/jung/ 

You may be required to `add the jar files to your build path` if they are not already added.

## Eclipse

For Eclipse, select all the jar files, right click -> Build Path -> Add to build path

This should add the jar files to the referenced libraries section.

# How to Run

Once the Jung Library is added to the project (If not already) through the build dependencies section, download the data-set https://chartalist.org/BitcoinData.html

The folder used must be placed from inside BlockChainNetwork folder (relative path).

In MainClass.java, near the top of the class a YEAR variable is given. Set its value equal to the year of the data-set you had chosen.

The data-set is included in .gitignore, so it MUST be added manually; with the exception of the 2009 data-set.

MainClass.java contains the main class. Located in the src folder.

## Output

Some output could be provided as per testing.

# Out of Memory ?

Files are read entirely, one by one, and freed in-between. The more memory the better.

The largest file in the 2015 data-set is ~1.5GB.

For 2015, you will need to set the heap size to 32768 MB.

Pass the command -Xmx32768m to the VM to do this.

# Execution times (Part A)

`*NOTE*` Due to the memory usage we have decided to run on different files from the 2015 dataset.

This way we can still verify the correctness of the network, and only need to improve our mememory management to run the 2015 dataset.

`Connor Hryhoruk`

CPU: AMD Ryzen 7 2700X

Dataset tested on: `2011`

~Execution times [Output from execution] ~

Milliseconds to load output file: 45064ms (45.064 s)

Milliseconds to load input file and link it with output 45965ms (45.965 s)

Milliseconds of total execution: 91029ms (91.029 s)

Dataset tested on: `2012`

Milliseconds to load output file: 238193ms (238.193 s)

Milliseconds to load input file and link it with output 2069120ms (2069.12 s)

Milliseconds of total execution: 2307314ms (2307.314 s)

`Poupak Azad`

CPU: Intel Core i7-10750

Dataset tested on: `2012`

Milliseconds to load output file: 177898ms (177.898 s)

Milliseconds to load input file and link it with output 207913ms (207.913 s)

Milliseconds of total execution: 385812ms (385.812 s)

# Ransomware Features (Part B)

The memory usage issues from part A was largely fixed in part B. Part A uses the FileParser class, while part B uses both FileSorter & WindowParser class. The WindowParser is optimized such that memory usage from reading files is limited to a buffers size, the files are pre-sorted, and Scanner's are not used for parsing. The results from part B is a significant improvement to performance, and reduced memory usage overhead compared to part A.

These features are extracted for ransomware payments. 
Payments are split into 24 hour windows.

To run Part B, you must first sort the input/output files for the given year. To do so, run src/FileSorter.java, set the year to the one which you are sorting. Then set SORT_INPUTS to true or false, where true indicates to sort an input file, as well as MONTH_TO_SORT for the specific month to sort. 

The sorted files should be placed in the sortedFile/ directory.

Run src/Mainclass.java, with the cooresponding year and months in the given variables, and set WINDOW_FEATURE_EXTRACTION to true (false is part A).

The program extracts the following features for each address, placed in a results/featureExtraction.csv file:

* The address and its year/day
* Amount sent by the address
* The addresses Income
* Number of neighbours
* Number of co-addresses
* Number of successors (0 if there is no sending transaction for that address within the current window)
* If the address is a ransomeware (true/false)
* Total transactions in a window
* Number of white & ransome addresses in a window

The results/featureExtractionFinal.csv and results/featureExtraction.csv files are given in this branch for the 2015 and 2012 datasets, respectively.

`The program should take approximately 30-40 minutes for the 2015 dataset, and 3 minutes for the 2012 dataset`
