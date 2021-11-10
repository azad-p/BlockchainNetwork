public class Address extends GraphNode {
    private String addrHash;
    private long btcSent;
    
    public Address () {}
    public Address(long amountSent, String hash)
    {
        this.addrHash = hash;
        this.btcSent = amountSent;
    }

    // Creates a dummy address [SPECIAL CASE]
    // These are ones that may have been created at a later year than the transaction itself
    // Thus, we do not know how much bitcoin was sent, although, we do know who is receiving and sending the address
    public static Address createDummyAddress (int indexOfTransaction, int year)
    {
        Address dummyAddress = new Address ();
        dummyAddress.btcSent = 0; // Undefined, we cannot know this, maybe in a different year

        // Custom hash of the address
        dummyAddress.addrHash = indexOfTransaction + " in " + year;

        return dummyAddress;
    }

    @Override
    public String toString ()
    {
        String addrName = addrHash;
        String sentBtc = null;

        if (addrName == "")
            addrName = "NoName";

        // If the btcSent is 0 in an address, we may have the special case where the address was not in the output file
        // IE: May have been sent on a different year. Thus, the btc sent is unknown.
        if (btcSent == 0)
        {
            sentBtc = "Unknown";
        }
        else
        {
            sentBtc = String.valueOf(btcSent);
        }

        return "\nsending " + sentBtc + " Through the address: " + addrName;
    }

    // ~~~~~~~~~~~~~~~~~
    // Getters / setters
    // ~~~~~~~~~~~~~~~~~

    public String getAddrHash() {
        if (addrHash.isEmpty())
            return "NoName";
    	
        return addrHash;
    }
    public void setAddrHash(String addrHash) {
        this.addrHash = addrHash;
    }
    public long getBtcSent() {
        return btcSent;
    }
    public void setBtcSent(long btcSent) {
        this.btcSent = btcSent;
    }
}
