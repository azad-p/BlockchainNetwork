public class Address extends GraphNode {
    private String addrHash;
    private long btcSent;
    private Transaction receivee;
    private Transaction sendee;

    public Address () {}
    public Address(long amountSent, String hash)
    {
        this.addrHash = hash;
        this.btcSent = amountSent;
    }

    // Creates a dummy address [SPECIAL CASE]
    // These are ones that may have been created at a later year than the transaction itself
    // Thus, we do not know how much bitcoin was sent, although, we do know who is receiving and sending the address
    public static Address createDummyAddress (Transaction sender, Transaction receiver, int indexOfTransaction, int year)
    {
        Address dummyAddress = new Address ();
        dummyAddress.sendee = sender;
        dummyAddress.receivee = receiver;
        dummyAddress.btcSent = 0;
        
        // Custom hash of the address
        dummyAddress.addrHash = sender.getHashOfTransaction() + ':' + indexOfTransaction + " in " + year;

        return dummyAddress;
    }

    @Override
    public String toString ()
    {
        String addrName = addrHash;

        if (addrName == "")
            addrName = "NoName";

        return "Transaction: " + sendee + " Is sending " + btcSent + " Through the address " + addrName + " "
        		+ "To the transaction " + receivee;
    }
    
	// ~~~~~~~~~~~~~~~~~
	// Getters / setters
	// ~~~~~~~~~~~~~~~~~
    
	public String getAddrHash() {
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
	public Transaction getReceivee() {
		return receivee;
	}
	public void setReceivee(Transaction receivee) {
		this.receivee = receivee;
	}
	public Transaction getSendee() {
		return sendee;
	}
	public void setSendee(Transaction sendee) {
		this.sendee = sendee;
	}
}
