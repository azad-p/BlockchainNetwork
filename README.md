# BlockChain Network

Building a network with the input/outputs of transactions in Bitcoin.


# Build dependencies

This project utilizes the Jung library for Graphing the Network.

The jar files for this library have been provided in the main-path of the project.

If the jar files fail, jung-2.0.1 can be downloaded here https://sourceforge.net/projects/jung/files/jung/ 

You may be required to add the jar files to your build path if they are not already added.

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

Some output may be provided as per testing. This should show a few transactions outputs/inputs, which can be verified by searching the data-set directly.

# Out of Memory ?

Files are read entirely, one by one, and freed inbetween. The more memory the better.

The largest file in the 2015 data-set is ~1.5GB.

For 2015, you will need to set the heap size to 32768 MB.

Pass the command -Xmx32768m to the VM to do this.

# Execution time (Part A)