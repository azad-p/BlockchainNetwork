# BlockChain Network

Building a network with the input/outputs of transactions in Bitcoin.


# Build dependencies

This project utilizes the Jung library for Graphing the Network.

The jar files for this library have been provided in the main-path of the project.

If the jar files fail, jung-2.0.1 can be downloaded here https://sourceforge.net/projects/jung/files/jung/ 

You may be required to add the jar files to your build path if they are not already added.

* NOTE: This is not required for Part A. We have removed Jung temporarily for improved memory usage, as we had implemented this part in two different ways *

## Eclipse

For Eclipse, select all the jar files, right click -> Build Path -> Add to build path

This should add the jar files to the referenced libraries section.

# How to Run

Once the Jung Library is added to the project (If not already) through the build dependencies section, download the data-set https://chartalist.org/BitcoinData.html

The folder used must be placed from inside BlockChainNetwork folder (relative path).

In MainClass.java, near the top of the class a YEAR variable is given. Set its value equal to the year of the data-set you had chosen.

The data-set is included in .gitignore, so it MUST be added manually; with the exception of the 2009 data-set.

MainClass.java contains the main class. Located in the src folder.

* NOTE: This is not required for Part A. We have removed Jung temporarily for improved memory usage, as we had implemented this part in two different ways *

## Output

Some output may be provided as per testing. That is, if you use YEAR = 2009, a transaction will be printed as per testing the result.

# Out of Memory ?

Files are read entirely, one by one, and freed in-between. The more memory the better.

The largest file in the 2015 data-set is ~1.5GB.

For 2015, you will need to set the heap size to 32768 MB.

Pass the command -Xmx32768m to the VM to do this.

# Execution times (Part A)

*NOTE* Due to the memory usage we have decided to run on different files from the 2015 dataset.

This way we can still verify the correctness of the network, and only need to improve our mememory management to run the 2015 dataset.

Connor Hryhoruk

Dataset tested on: 2012

CPU: AMD Ryzen 7 2700X

~Execution times [Output from execution] ~

Milliseconds to load output file: 45064ms (45.064 s)

Milliseconds to load input file and link it with output 45965ms (45.965 s)

Milliseconds of total execution: 91029ms (91.029 s)
