public class Address extends GraphNode {
    public String addrHash;
    public long btcSent;
    public Transaction receivee;
    public Transaction sendee;

    public Address () {}
    public Address(long amountSent, String hash)
    {
        this.addrHash = hash;
        this.btcSent = amountSent;
    }

    public static Address createDummyAddress (Transaction sender, Transaction receiver, int indexOfTransaction, int year)
    {
        Address dummyAddress = new Address ();
        dummyAddress.sendee = sender;
        dummyAddress.receivee = receiver;
        dummyAddress.btcSent = 0;
        dummyAddress.addrHash = sender.hashOfTransaction + ':' + indexOfTransaction + " in " + year;

        return dummyAddress;
    }

    @Override
    public String toString ()
    {
        String addrName = addrHash;

        if (addrName == "")
            addrName = "NoName";

        return "Transaction: " + sendee + " Is sending " + btcSent + " Through the address " + addrName + " To the transaction " + receivee;
    }
}
