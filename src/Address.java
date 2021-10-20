public class Address extends GraphNode {
    public String addrHash;
    public float btcSent;
    public Transaction receivee;
    public Transaction sendee;

    public Address () {}
    public Address(long amountSent, String hash)
    {
        this.addrHash = hash;
        this.btcSent = amountSent;
    }
}
