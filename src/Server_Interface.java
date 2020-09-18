import java.math.BigDecimal;
import java.security.Key;
import java.util.HashMap;
import java.rmi.*;
/**
 * This interface defines the behaviour of the methods
 * that will be implemented by the remote Service
 * @author  Emmeleia Arakleitou.
 */

public interface Server_Interface extends Remote{

    /**
     * This method creates an new Auction.
     * @param itemDesc A small description of the item to sold
     * @param startingPrice The starting price of the item
     * @param reservedPrice The minimum accepted price for an item
     * @return auctionID that will be used to bid for that specific item
     * @throws RemoteException ex
     */
    String createNewAuction(String itemDesc, BigDecimal startingPrice, BigDecimal reservedPrice, String sellerID) throws RemoteException;

     /**
     * This method closes an auction
     * @param auctionID The ID of the auction to be closed
     * @return true if the auction exists otherwise it returns false
     * @throws RemoteException ex
     */
    int closeAuction(String auctionID, String sellerID ) throws RemoteException;

    /**
     * Checks if the ID is already used from another Client
     * @param clientID ID of Client
     * @param client Instance of the object client
     * @return true if ID in use false if ID available
     * @throws RemoteException ex
     */
    boolean validateClientID(String clientID, Client client) throws RemoteException;

    /**
     * Checks if the ID is associated with an entry in the HashMap
     * @param auctionID ID of the auction
     * @param activeAuctions HashMap with all the active auctions
     * @return true if auction entry exists otherwise returns false
     * @throws RemoteException ex
     */
    boolean validateAuctionID(String auctionID,HashMap<String,Auction> activeAuctions) throws RemoteException;

    /**
     * Takes a input a price and checks if the bid is greater than the currentHighest bid
     * and change the auction values.
     * @param price User Bid
     * @param auctionID ID of the auction
     * @param bidderID ID of bidder
     * @return a message to indicate whether the bid is accepted
     * @throws RemoteException ex
     */
    String bidFromItem(BigDecimal price,String auctionID,String bidderID) throws RemoteException;

    /**
     * Check if the highest bid is larger than the reserved price
     * @param auctionID ID of the auction
     * @return HighestBidder or null if reserved price is not met
     * @throws RemoteException ex
     */
    Client announceWinner(String auctionID) throws RemoteException;

    /**
     * Accessor for activeAuctions
     * @return a list of all the current active auctions
     * @throws RemoteException ex
     */
    HashMap<String, Auction> getActiveAuctions() throws RemoteException;

    /**
     * Accessor for closedAuctions
     * @return list of all the closed auctions
     * @throws RemoteException ex
     */
    HashMap<String, Auction> getClosedAuctions() throws RemoteException;

    /**
     * Accessor for registered Clients
     * @return list of all the clients in the auctioning system
     * @throws RemoteException ex
     */
    HashMap<String,Client> getRegisteredClients() throws RemoteException;

    /**
     * Checks if the user name used to login belongs to a user
     * @param userName name to validate
     * @return true if name is valid faulse otherwise
     * @throws RemoteException
     */
    boolean validateUserName(String userName) throws RemoteException;

    /**
     * obtains the userPassoword from the file and generates a key based on the password
     * @param keyFileName the name of the file that contains the password
     * @return an istance of a key
     * @throws RemoteException
     */
    Key getServerKey(String keyFileName)throws RemoteException;

    /**
     * The server is authenticated by encrypting it's challenge using the key
     * stored when the user registered
     * @param key Name of the file that holds the key
     * @param randomNum the chalenge to solve
     * @return the solved challenge and his challenge for the client
     * @throws RemoteException
     */
    AuthenticationReply serverAuthentication(String key,int randomNum) throws RemoteException;

    /**
     * Decrypts the challenge of the client to authenticate the client
     * @param authenticationReply the random number encypted using the user password from input
     * @param keyFileName the name of the file to load the key
     * @return true if user is valid false otherwise
     * @throws RemoteException
     */
    Boolean authenticateClint(AuthenticationReply authenticationReply, String keyFileName) throws RemoteException;

    /**
     * Given a valid Password the client is returned.
     * @param password user password
     * @return Client
     * @throws RemoteException
     */
    Client getClientOnPass(String password) throws RemoteException;
}



