import org.jgroups.JChannel;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.ResponseMode;
import org.jgroups.blocks.RpcDispatcher;
import org.jgroups.util.RspList;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.math.BigDecimal;
import java.security.Key;
import java.util.HashMap;

/**
 * This class represents the front-end Server of the auctioning system
 * that connect all the cluster members to the channel to execute the requests made on the server.
 * @author Emmeleia Araklitou
 */
public class Auction_RemoteService extends UnicastRemoteObject implements Server_Interface{

    private static final String CLUSTER_NAME = "MY_CLUSTER";
    private static final int TIMEOUT = 1000;
    private JChannel channel;
    private RpcDispatcher dispatcher;
    private RequestOptions requestOptions;

    /**
     * Constructor of the class that calls the constructor of the superClass
     * and calls the method to connect the clusters to the channel
     * @throws RemoteException
     */
    public Auction_RemoteService() throws RemoteException {
        super();
        setUpCluster();
    }

    /**
     * Set up the Cluster by creating a channel
     * and then building the cluster on top of that.
     */
    public void setUpCluster()
    {
        try{
            this.channel = new JChannel();
            this.requestOptions = new RequestOptions(ResponseMode.GET_ALL, TIMEOUT);
            this.channel.setDiscardOwnMessages(true);
            this.dispatcher = new RpcDispatcher(this.channel,new Auction_Server_Replica());
            this.channel.connect(CLUSTER_NAME);
        }catch (Exception e)
        {
            System.out.println("[SERVER] Failed to connect to cluster!!");
        }
    }

    /**
     * This method is used to invoke the method on the cluster member to create an action an new Auction.
     * @param itemDesc A small description of the item to sold
     * @param startingPrice The starting price of the item
     * @param reservedPrice The minimum accepted price for an item
     * @return auctionID that will be used to bid for that specific item
     */
    @Override
    public synchronized String createNewAuction(String itemDesc, BigDecimal reservedPrice, BigDecimal startingPrice,String sellerID){
            String auctionID=null;
            try{
                RspList responses = this.dispatcher.callRemoteMethods(null,
                                                                "createAction",
                                                                new Object[]{itemDesc,reservedPrice,startingPrice,sellerID},
                                                                new Class[]{String.class,BigDecimal.class,BigDecimal.class,String.class},
                                                                this.requestOptions);
                for(Object response:responses.getResults())
                    auctionID = (String)response;
                return auctionID;
            }catch (Exception e)
            {
                System.out.println("[SERVER] Failed to connect to cluster!!");
            }
            return null;
    }

    /**
     * Invokes the method that closes an action
     * @param auctionID The ID of the auction to be closed
     * @return true if the auction exists otherwise it returns false
     */
    @Override
    public synchronized int closeAuction(String auctionID, String sellerID){
        int status =-1;
        try {
            RspList responses = this.dispatcher.callRemoteMethods(null,
                                                    "closeAuction1",
                                                                new Object[]{auctionID,sellerID},
                                                                new Class[]{String.class,String.class},
                                                                this.requestOptions);
            for(Object response:responses.getResults())
                status = (int) response;
            return status;
        }catch(Exception e)
        {
            System.out.println("[SERVER] Failed to connect to cluster!!");
        }
        return status;
    }

    /**
     * Calls method that checks if the ID is already used from another Client
     * @param clientID ID of Client
     * @param client Instance of the object client
     * @return true if ID in use false if ID available
     */
    public synchronized boolean validateClientID(String clientID, Client client){
        boolean status = false;
        try {
            RspList responses = this.dispatcher.callRemoteMethods(null,
                    "validateClientID1",
                    new Object[]{clientID,client},
                    new Class[]{String.class,Client.class},
                    this.requestOptions);
            for(Object response:responses.getResults())
                status = (boolean) response;
            return status;
        }catch(Exception e)
        {
            System.out.println("[SERVER] Failed to connect to cluster!!");
        }
        return status;
    }

    /**
     * Invokes method to check if the ID is associated with an entry in the HashMap
     * @param auctionID ID of the auction
     * @param activeAuctions HashMap with all the active auctions
     * @return true if auction entry exists otherwise returns false
     */
    @Override
    public synchronized boolean validateAuctionID(String auctionID, HashMap<String,Auction> activeAuctions){

        boolean status = false;
        try {
            RspList responses = this.dispatcher.callRemoteMethods(null,
                    "validateAuctionID1",
                    new Object[]{auctionID,activeAuctions},
                    new Class[]{String.class, HashMap.class},
                    this.requestOptions);
            for(Object response:responses.getResults())
                status = (boolean) response;
            return status;
        }catch(Exception e)
        {
            System.out.println("[SERVER] Failed to connect to cluster!!");
        }
        return status;
    }

    /**
     * Takes a input a price and invokes metheod to check if the bid is greater than the currentHighest bid
     * and change the auction values.
     * @param price User Bid
     * @param auctionID ID of the auction
     * @param bidderID ID of bidder
     * @return a message to indicate whether the bid is accepted
     */
    @Override
    public synchronized String bidFromItem(BigDecimal price, String auctionID,String bidderID) throws RemoteException {
        String msg = null;
        try {
            RspList responses = this.dispatcher.callRemoteMethods(null,
                    "bidFromItem1",
                    new Object[]{price,auctionID,bidderID},
                    new Class[]{BigDecimal.class, String.class,String.class},
                    this.requestOptions);
            for(Object response:responses.getResults())
                msg = (String) response;
            return msg;
        }catch(Exception e)
        {
            System.out.println("[SERVER] Failed to connect to cluster!!");
        }
        return msg;
    }

    /**
     * Invokes method on the on a server on the channel to check if
     * the highest bid is larger than the reserved price
     * @param auctionID ID of the auction
     * @return HighestBidder or null if reserved price is not met
     */
    @Override
    public synchronized Client announceWinner(String auctionID){

        Client client = null;
        try {
            RspList responses = this.dispatcher.callRemoteMethods(null,
                    "announceWinner1",
                    new Object[]{auctionID},
                    new Class[]{String.class},
                    this.requestOptions);
            for(Object response:responses.getResults())
                client = (Client) response;
            return client;
        }catch(Exception e)
        {
            System.out.println("[SERVER] Failed to connect to cluster!!");
        }
        return client;
    }

    /**
     * Accessor for activeAuctions
     * @return a list of all the current active auctions
     */
    @Override
    public synchronized HashMap<String,Auction> getActiveAuctions(){
        HashMap<String,Auction> activeAuctions = new HashMap<>();
        try{
            RspList responses = this.dispatcher.callRemoteMethods(null,
                                                                "getActiveAuctions1",
                                                                    new Object[]{},
                                                                    new Class[]{},
                                                                    this.requestOptions);
            for(Object response:responses.getResults())
                activeAuctions = (HashMap<String,Auction>)response;
            return activeAuctions;
        }catch (Exception e)
        {
            System.out.println("[SERVER] Failed to connect to cluster!!");
        }
        return null;
    }

    /**
     * Accessor for closedAuctions
     * @return list of all the closed auctions
     */
    public HashMap<String, Auction> getClosedAuctions(){
        HashMap<String, Auction> closedAuctions = new HashMap<>();
        try{
            RspList responses = this.dispatcher.callRemoteMethods(null,
                    "getClosedAuctions1",
                    new Object[]{},
                    new Class[]{},
                    this.requestOptions);
            for(Object response:responses.getResults())
                closedAuctions = (HashMap<String,Auction>)response;
            return closedAuctions;
        }catch (Exception e)
        {
            System.out.println("[SERVER] Failed to connect to cluster!!");
        }
        return null;
    }

    /**
     * Accessor for registered clients
     * @return
     */
    public HashMap<String,Client> getRegisteredClients(){
        HashMap<String, Client> registeredClients = new HashMap<>();
        try{
            RspList responses = this.dispatcher.callRemoteMethods(null,
                    "getRegisteredClients1",
                    new Object[]{},
                    new Class[]{},
                    this.requestOptions);
            for(Object response:responses.getResults())
                registeredClients = (HashMap<String,Client>)response;
            return registeredClients;
        }catch (Exception e)
        {
            System.out.println("[SERVER] Failed to connect to cluster!!");
        }
        return null;
    }

    /**
     * Invokes method from the cluster to validate if the user Name is already used by anothere user.
     * @param userName name to validate
     * @return
     */
    public boolean validateUserName(String userName)
    {
        try{
            RspList responses = this.dispatcher.callRemoteMethods(null,
                    "validateUserName1",
                    new Object[]{userName},
                    new Class[]{String.class},
                    this.requestOptions);
            boolean ans = false;
            for(Object response:responses.getResults())
                ans = (boolean)response;
            return ans;
        }catch (Exception e)
        {
            System.out.println("[SERVER] Failed to connect to cluster!!");
        }
        return false;
    }

    /**
     * Invokes method to authendicate the server
     * @param key Name of the file that holds the key
     * @param randomNum the chalenge to solve
     * @return
     */
    public synchronized AuthenticationReply serverAuthentication(String key, int randomNum )
    {
        try{
            RspList responses = this.dispatcher.callRemoteMethods(null,
                    "serverAuthentication1",
                    new Object[]{key,randomNum},
                    new Class[]{String.class,int.class},
                    this.requestOptions);
            AuthenticationReply authenticationReply = null;
            for(Object response:responses.getResults())
                authenticationReply = (AuthenticationReply) response;
            return authenticationReply;
        }catch (Exception e)
        {
            System.out.println("[SERVER] Failed to connect to cluster!!");
        }
        return null;
    }

    /**
     * Invokes method to authendicate the client
     * @param authenticationReply the random number encypted using the user password from input
     * @param keyFileName the name of the file to load the key
     * @return
     */
    public Boolean authenticateClint(AuthenticationReply authenticationReply, String keyFileName){

        boolean authenticated = false;

        try{
            RspList responses = this.dispatcher.callRemoteMethods(null,
                    "authenticateClint1",
                    new Object[]{authenticationReply,keyFileName},
                    new Class[]{AuthenticationReply.class,String.class},
                    this.requestOptions);

            for(Object response:responses.getResults())
                authenticated = (boolean) response;
            return authenticated;
        }catch (Exception e)
        {
            System.out.println("[SERVER] Failed to connect to cluster!!");
        }
        return authenticated;
    }

    /**
     * Invokes method to get the secret key for server authentication
     * @param keyFileName the name of the file that contains the password
     * @return
     */
    public Key getServerKey(String keyFileName) {

        Key secretKey = null;

        try{
            RspList responses = this.dispatcher.callRemoteMethods(null,
                    "getServerKey1",
                    new Object[]{keyFileName},
                    new Class[]{String.class},
                    this.requestOptions);

            for(Object response:responses.getResults())
                secretKey = (Key) response;
            return secretKey;
        }catch (Exception e)
        {
            System.out.println("[SERVER] Failed to connect to cluster!!");
        }
        return secretKey;
    }

    /**
     * Invokes a method that returns a client that corresponds to a specific password
     * @param password user password
     * @return
     */
    public Client getClientOnPass(String password){

        Client client= null;
        try{
            RspList responses = this.dispatcher.callRemoteMethods(null,
                    "getClientOnPass1",
                    new Object[]{password},
                    new Class[]{String.class},
                    this.requestOptions);

            for(Object response:responses.getResults())
                client = (Client) response;
            return client;
        }catch (Exception e)
        {
            System.out.println("[SERVER] Failed to connect to cluster!!");
        }
        return null;
    }
}
