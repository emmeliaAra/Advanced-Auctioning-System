import java.security.NoSuchAlgorithmException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import javax.crypto.BadPaddingException;
import org.jgroups.blocks.RpcDispatcher;
import javax.crypto.spec.SecretKeySpec;
import org.jgroups.ReceiverAdapter;
import org.jgroups.util.Util;
import java.math.BigDecimal;
import org.jgroups.JChannel;
import javax.crypto.Cipher;
import org.jgroups.Message;
import java.security.Key;
import java.util.HashMap;
import org.jgroups.View;
import java.util.Map;
import java.io.*;

/**
 * This class represents the claster members that are serving the requests coming from the server
 * @author Emmeleia Arakleitou
 */
public class Auction_Server_Replica extends ReceiverAdapter{

    private HashMap<String,Client> registeredClients = new HashMap<>();
    private HashMap<String,Auction> closedAuctions = new HashMap<>();
    private HashMap<String,Auction> activeAuctions = new HashMap<>();
    private static final String SECOND_CLUSTER_NAME = "MY_SECOND_CLUSTER";
    private static final String CLUSTER_NAME = "MY_CLUSTER";
    private final State myState  = new State();
    private static final int TIMEOUT = 1000;
    private JChannel channelA, channelB;


    /**
     * This method creates a new action, updates the state of the data and returns
     * the new auctionID to the server.
     * @param itemDesc A small description of the item to sold
     * @param startingPrice The starting price of the item
     * @param reservedPrice The minimum accepted price for an item
     * @return auctionID that will be used to bid for that specific item
     */
    public String createAction(String itemDesc, BigDecimal reservedPrice, BigDecimal startingPrice,String sellerID)
    {
        String auctionID;
        Auction newAuction = new Auction(itemDesc,reservedPrice,startingPrice, BigDecimal.ZERO," ",sellerID, "Active");

        auctionID = newAuction.createAuctionID();

        //Create new actionID if the one created is already taken by another auction.
        if(activeAuctions.get(auctionID) != null)
            auctionID = newAuction.createAuctionID();

        activeAuctions.put(auctionID,newAuction);
        getNewState(this.channelB,this.activeAuctions,this.closedAuctions,this.registeredClients);
        return auctionID;
    }

    /**
     * This method tries to close an auction If succeed updates the state.
     * @param auctionID The ID of the auction to be closed
     * @return true if the auction exists otherwise it returns false
     */
    public int closeAuction1(String auctionID,String sellerID)
    {
        Auction auctionToClose;
        if(activeAuctions.get(auctionID) == null)
            return -1;
        else if(!sellerID.equals(activeAuctions.get(auctionID).getSellerID()))
            return 0;
        else {
            //Add the closed auction to the closedMap and remove it from the map with the active auctions.
            activeAuctions.get(auctionID).setAuctionStatus("Closed");
            auctionToClose = activeAuctions.get(auctionID);
            closedAuctions.put(auctionID,auctionToClose);
            activeAuctions.remove(auctionID);
            getNewState(this.channelB,this.activeAuctions,this.closedAuctions,this.registeredClients);
            return 1;
        }
    }

    /**
     * Checks if the ID is already used from another Client
     * @param clientID ID of Client
     * @param client Instance of the object client
     * @return true if ID in use false if ID available
     */
    public boolean validateClientID1(String clientID, Client client){
        if(registeredClients.containsKey(clientID))
            return true;
        registeredClients.put(clientID,client);
        getNewState(this.channelB,this.activeAuctions,this.closedAuctions,this.registeredClients);

        return false;
    }

    /**
     * Checks if the ID is associated with an entry in the HashMap
     * @param auctionID ID of the auction
     * @param activeAuctions HashMap with all the active auctions
     * @return true if auction entry exists otherwise returns false
     */
    public boolean validateAuctionID1(String auctionID, HashMap<String,Auction> activeAuctions){
        /* if auction with that auctionID exist returns true otherwise returns false */
        return activeAuctions.containsKey(auctionID);
    }

    /**
     * Takes a input a price and checks if the bid is greater than the currentHighest bid
     * and change the auction values.
     * @param price User Bid
     * @param auctionID ID of the auction
     * @param bidderID ID of bidder
     * @return a message to indicate whether the bid is accepted
     */
    public String bidFromItem1(BigDecimal price, String auctionID,String bidderID) {

        if(price.compareTo(activeAuctions.get(auctionID).getHighestBid()) == 1)
        {
            activeAuctions.get(auctionID).setHighestBid(price);
            activeAuctions.get(auctionID).setHighestBidderID(bidderID);
            return "Your bid has been accepted. ";
        }
        else
            return "Your bid has bin rejected because there is a higher bidder.";
    }


    /**
     * Check if the highest bid is larger than the reserved price
     * @param auctionID ID of the auction
     * @return HighestBidder or null if reserved price is not met
     */
    public Client announceWinner1(String auctionID){

        BigDecimal highestBid, reservedPrice;
        Auction auction = closedAuctions.get(auctionID);
        highestBid = auction.getHighestBid();
        reservedPrice = auction.getReservedPrice();
        Client winner;

        if(highestBid.compareTo(reservedPrice) == 1 || highestBid.compareTo(reservedPrice) == 0){
            winner = registeredClients.get(auction.getHighestBidderID());
            return winner;
        }else
            return null;
    }

    /**
     * Checks if the name used to login is register as a username.
     * @param userName input of client
     * @return true if user name exists , false otherwise.
     */
    public boolean validateUserName1(String userName)
    {
        boolean ans = false;
        for (Map.Entry<String, Client> entry : registeredClients.entrySet()) {
            Client client = entry.getValue();
            if(client.getName().equals(userName)){
                ans = true;
                break;
            }
        }
        return ans;
    }

    /**
     * Server get the random number, retireves the key from the file and encrypts the
     * number using the key
     * @param key The key generated from the password in the file
     * @param randomNum the challenge of the client for the server
     * @return Authentication reply-> server's challenge for the client and his challenge solved
     */
    public AuthenticationReply serverAuthentication1(String key, int randomNum )
    {
        Key secretKey = getServerKey1(key);
        String algorithm = secretKey.getAlgorithm();
        AuthenticationReply authenticationReply = null;
        try {
            //encrypts the random namber using the key from the file
            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.ENCRYPT_MODE,secretKey);
            byte[] encrypted = cipher.doFinal(Integer.toString(randomNum).getBytes());
            //creates an istance of the reply to be sent to the client
            authenticationReply = new AuthenticationReply(encrypted);

        } catch (NoSuchAlgorithmException |
                BadPaddingException |
                IllegalBlockSizeException |
                NoSuchPaddingException |
                InvalidKeyException e) {
            e.printStackTrace();
        }

        return authenticationReply;
    }

    /**
     * This method authenticated the client,by decrypting the encrypted challenge to check if
     * resulting number is the same as the one passed as the challenge in the AuthenticationReply instance
     * @param authenticationReply Clint's response
     * @param keyFileName filename tha holds the key
     * @return
     */
    public Boolean authenticateClint1(AuthenticationReply authenticationReply, String keyFileName){

        boolean authenticated = true;

        Key secretKey = getServerKey1(keyFileName);

        try {
            Cipher cipher = Cipher.getInstance(secretKey.getAlgorithm());
            cipher.init(Cipher.DECRYPT_MODE,secretKey);
            cipher.doFinal(authenticationReply.getEncryptedNum());

        } catch (NoSuchAlgorithmException
                | NoSuchPaddingException
                | InvalidKeyException
                | IllegalBlockSizeException e) {
                e.printStackTrace();
        } catch (BadPaddingException e) {
            authenticated = false;
        }
        return authenticated;
    }


    /**
     * loads the password from the file as a string using a given username,
     * and then generated a key using the string.
     * @param keyFileName the name of the file that holds the string value
     * @return an istance of the key
     */
    public Key getServerKey1(String keyFileName) {

        File file = new File(keyFileName.toUpperCase() + ".txt");
        BufferedReader br;
        Key secretKey = null;

        try{
            br = new BufferedReader(new FileReader(file));
            String st = br.readLine();
            String array2[] = st.split(",", 2);
            String key = array2[1];
            while(key.length()<16)
                key = key+"0";
            secretKey = new SecretKeySpec(key.getBytes(),"AES");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return secretKey;
    }

    /**
     * Returns a client given a password
     * @param password user password
     * @return
     */
    public Client getClientOnPass1(String password){

        for (Map.Entry<String, Client> entry : registeredClients.entrySet()) {
            Client client = entry.getValue();
            if(client.getUserPass().equals(password))
                return client;
        }
        return null;
    }

    /**
     * Accessor for activeAuctions
     * @return a list of all the current active auctions
     */
    public HashMap<String,Auction> getActiveAuctions1()
    {
        return activeAuctions;
    }

    /**
     * Accessor for closedAuctions
     * @return list of all the closed auctions
     */
    public HashMap<String, Auction> getClosedAuctions1(){
        return closedAuctions;
    }

    /**
     * Accessor for registedClients
     * @return list of all the registered clients
     */
    public HashMap<String,Client> getRegisteredClients1()
    {
        return registeredClients;
    }


    /**
     * Creates a channel tha is used for the dispatcher and another channel that will use
     * the cluster members to communicate with each other and share the state
     * @throws Exception
     */
    public void start() throws Exception{
        this.channelA = new JChannel();
        this.channelA.connect(CLUSTER_NAME);
        RpcDispatcher dispatcher = new RpcDispatcher(this.channelA, this);

        this.channelB = new JChannel();
        this.channelB.setReceiver(this);
        this.channelB.connect(SECOND_CLUSTER_NAME);
        this.channelB.getState(null,TIMEOUT);
    }

    /**
     * prints the view of the system.
     * @param new_view
     */
    public void viewAccepted(View new_view)
    {
        System.out.println("received view " + new_view);
    }

    /**
     * receives a message tha holds the current state and updates the
     * data in the state class
     * @param message
     */
    public void receive(Message message)
    {
        State tempState = (State)message.getObject();
        synchronized (myState)
        {
            myState.setActiveAuctions(tempState.getActiveAuctions());
            myState.setClosedAuctions(tempState.getClosedAuctions());
            myState.setRegisteredClients(tempState.getRegisteredClients());
        }
    }

    /**
     * retrieves the state of the system when entering the channel
     * @param outputStream
     */
    public void getState(OutputStream outputStream)
    {
        try
        {
            Util.objectToStream(myState,new DataOutputStream(outputStream));
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Sets the state - Set the data instatiated in this cluster member to the data held in the state
     * @param inputStream
     */
    public void setState(InputStream inputStream)
    {
        try {
            State tempState  = (State) Util.objectFromStream(new DataInputStream(inputStream));
            synchronized (myState)
            {
                this.activeAuctions = tempState.getActiveAuctions();
                this.closedAuctions = tempState.getClosedAuctions();
                this.registeredClients = tempState.getRegisteredClients();
            }
        }catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    /**
     * Sent the state as a message so that anyone that
     * enters the channel will receive the state and update the data.
     * @param channelB
     * @param activeAuctions
     * @param closedAuctions
     * @param registeredClients
     */
    public synchronized void getNewState(JChannel channelB,HashMap<String,Auction>activeAuctions,HashMap<String,Auction> closedAuctions,HashMap<String,Client>registeredClients)
    {
        this.myState.setClosedAuctions(closedAuctions);
        this.myState.setActiveAuctions(activeAuctions);
        this.myState.setRegisteredClients(registeredClients);
        //Pass the state to the other member of the clusters as a message so that all cluster memebrs will have the same view of data/
        Message message = new Message(null,this.myState);
        try {
            channelB.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close()
    {
        this.channelA.close();
        this.channelB.close();
    }



    public static void main(String args[])throws Exception
    {
        new Auction_Server_Replica().start();
    }
}
