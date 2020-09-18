import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Random;

/**
 * An instance of this class will be created when
 * the method newAction is invoked
 * @author Emmeleia Arakleitou
 */
public class Auction implements Serializable {

    private String itemDesc, highestBidderID,auctionStatus,auctionID,sellerID;
    private BigDecimal startingPrice, reservedPrice, highestBid;

    /**
     * Constructor of the class with the following parameters
     * @param itemDesc  A small description of the item to sold
     * @param startingPrice The starting price of the item
     * @param reservedPrice The minimum accepted price for an item
     * @param highestBid The current highest bid
     * @param highestBidderID The ID of the highest bidder
     * @param sellerID The ID of the seller
     * @param auctionStatus Status of the auction
     */
    public Auction(String itemDesc, BigDecimal startingPrice, BigDecimal reservedPrice,BigDecimal highestBid, String highestBidderID, String sellerID, String auctionStatus )
    {
        this.itemDesc = itemDesc;
        this.startingPrice = startingPrice;
        this.reservedPrice = reservedPrice;
        this.highestBid = highestBid;
        this.highestBidderID = highestBidderID;
        this.auctionStatus = auctionStatus;
        this.sellerID = sellerID;
    }

    /**
     * Create an actionID returns the auctionID
     * @return the ID of the auction used to invoke the method
     */
    public String createAuctionID()
    {
        Random rand = new Random();
        auctionID =  Integer.toString(rand.nextInt(1000) + 1);
        return auctionID;
    }

    /**
     * Accessor for itemDesc
     * @return The Description of the item to be sold
     */
    public String getItemDesc() {
        return itemDesc;
    }

    /**
     * Accessor for startingPrice
     * @return the starting price of the item
     */
    public BigDecimal getStartingPrice() {
        return startingPrice;
    }

    /**
     * Accessor for reservedPrice
     * @return the minimum acceptable price by the seller
     */
    public BigDecimal getReservedPrice() {
        return reservedPrice;
    }

    /**
     * Accessor for highestBid
     * @return the current highest bid for an auction
     */
    public BigDecimal getHighestBid() {
        return highestBid;
    }

    /**
     * Accessor for the highestBidderID
     * @return the ID of the highest bidder
     */
    public String getHighestBidderID() {
        return highestBidderID;
    }

    /**
     * Accessor for auctionStatus
     * @return the status of an auction
     */
    public String getAuctionStatus() {
        return auctionStatus;
    }

    /**
     * Accessor for the auctionID
     * @return The ID of the auction
     */
    public String getAuctionID() {
        return auctionID;
    }

    /**
     * Accessor for the seller ID
     * @return the ID of the seller
     */
    public String getSellerID() {
        return sellerID;
    }

    /**
     * Mutator for currentHighest
     * @param currentHighest The current highest bid
     */
    public void setHighestBid(BigDecimal currentHighest) {
        this.highestBid = currentHighest;
    }

    /**
     * Mutator for the highestBidderID
     * @param highestBidderID the ID of the highest bidder
     */
    public void setHighestBidderID(String highestBidderID)
    {
        this.highestBidderID = highestBidderID;
    }

    /**
     * Mutator for auction status
     * @param status the status of an auction
     */
    public void setAuctionStatus(String status)
    {
        this.auctionStatus = status;
    }
}