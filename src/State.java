import java.io.Serializable;
import java.util.HashMap;


/**
 * This class is used to keep the state of the data used in the auctioning system
 * @author Emmeleia Arakleitou
 */
public class State  implements Serializable{

    private HashMap<String,Auction> activeAuctions = new HashMap<>();
    private HashMap<String,Client> registeredClients = new HashMap<>();
    private HashMap<String,Auction> closedAuctions = new HashMap<>();


    /**
     * accessor for closed auctions
     * @return
     */
    public HashMap<String, Auction> getClosedAuctions() {
        return closedAuctions;
    }

    /**
     * Accessor for current running auctions
     * @return
     */
    public HashMap<String, Auction> getActiveAuctions() {
        return activeAuctions;
    }

    /**
     * Accessor for registered clients in the system
     * @return
     */
    public HashMap<String, Client> getRegisteredClients() {
        return registeredClients;
    }

    /**
     * Mutator that updates the state of the closed auctions
     * @param closedAuctions
     */
    public void setClosedAuctions(HashMap<String, Auction> closedAuctions) {
        this.closedAuctions = closedAuctions;
    }

    /**
     * Mutator that updates the state of the active auctions
     * @param activeAuctions
     */
    public void setActiveAuctions(HashMap<String, Auction> activeAuctions) {
        this.activeAuctions = activeAuctions;
    }

    /**
     * Mutator that updates the state of the registeredClients auctions
     * @param registeredClients
     */
    public void setRegisteredClients(HashMap<String, Client> registeredClients) {
        this.registeredClients = registeredClients;
    }
}
