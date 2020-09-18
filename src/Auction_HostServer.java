import java.rmi.registry.LocateRegistry;
import java.rmi.Naming;

/**
 * This class provides a server that will host the remote object services.
 * @author Emmeleia Arakleitou.
 */
public class Auction_HostServer {

    /**
     * Constructor of the class. Creates an instance of the remote object
     * and binds it to the naming service-RMIRegistry.
     */
    public Auction_HostServer(){

        String urlName = "rmi://localhost/AuctioningServer";
        try {
            //Create an instance of the remote UnicastObject and locate
            //the Registry to bind the the object to the remote naming service
            Server_Interface auction = new Auction_RemoteService();
            LocateRegistry.createRegistry(1090);
            Naming.rebind(urlName,auction);
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     *Main method that instantiates the hosting server.
     */
    public static void main (String[] args)
    {
        new Auction_HostServer();
    }
}
