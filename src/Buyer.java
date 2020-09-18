import java.rmi.RemoteException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Scanner;


/**
 * This class is a subclass of Client and implements the functionality
 * for the buyer of the auction.
 * @author Emmeleia Arakleitou
 */
public class Buyer extends Client {

    private final String serverURL = "rmi://localhost/AuctioningServer";
    private HashMap<String,Auction> myAuctions;
    private Server_Interface remoteService;
    private String choice;
    private char choice2;
    private Scanner in;

    /**
     * Constructor of the class that calls the constructor of the superclass Client
     * In the constructor the Buyer connects to the the server
     * @param name Name of Seller
     * @param email Email of Seller
     */
    public Buyer(String name, String email,String userPass) {

        super(name,email,userPass);
        myAuctions = new HashMap<>();
        this.remoteService = this.connectToRMI(serverURL);
        createClientID();
        this.writeKeys(this);

    }

    /**
     * This method lists all the actions that the seller can take
     * and includes a recursive call when the input is not valid
     * @return the choice made by the buyer
     */
    public char listBuyerOptions()
    {
        in = new Scanner(System.in);
        System.out.println("These are your options : " + "\n");
        System.out.println("A. See all current active auctions");
        System.out.println("B. Bid for an action");
        System.out.println("C. Exit"  + "\n" );
        System.out.println("Choose one of the above options by pressing either 'A' , 'B' or 'C'" + '\n');

        //if choice is not a valid input will recursively call the method again until is valid
        choice =in.next();
        if(choice.length() >1){
            System.out.println("Please give a valid input!" + '\n');
            listBuyerOptions();
        }
        else {
            choice2 = Character.toUpperCase(choice.charAt(0));

            //Request for choice until a valid input
            if (choice2 != 'A' && choice2 != 'B' && choice2 != 'C')
            {
                System.out.println("Please give a valid input!" + '\n');
                listBuyerOptions();
            }
        }
        return choice2;
    }

    /**
     * This method implements the action selected by the buyer
     * @param choice The choice of the buyer
     * @throws RemoteException ex
     */
    public void takeAction(char choice) throws RemoteException{

        boolean exists;
        boolean notNumber = true;
        in = new Scanner(System.in);
        String auctionID,bidMessage;
        BigDecimal bid = new BigDecimal(0);


        if(choice == 'A')
        {
            //prints the list with all the active auctions
            displayActiveAuctions(remoteService.getActiveAuctions());
        }
        else if(choice == 'B')
        {
            //check if there are any active auctions to bid
            if(remoteService.getActiveAuctions().isEmpty())
                System.out.println("There are no active auction to bid. You will be notified when an item is added.");
            else{
                displayActiveAuctions(remoteService.getActiveAuctions());
                System.out.println("To Bid for an item you must enter the auction ID and the amount that you are willing to give for the item.");
                auctionID = in.next();

                exists = remoteService.validateAuctionID(auctionID,remoteService.getActiveAuctions());

                //while the auctionID is not valid require a new action ID
                while (!exists)
                {
                    System.out.println("The auction ID provided does not belong to any of the current active auctions.");
                    System.out.println("Please give a new auctionID");
                    auctionID = in.next();
                    exists = remoteService.validateAuctionID(auctionID,remoteService.getActiveAuctions());
                }

                System.out.println("Give your bid : ");

                while (notNumber)
                {
                    //If input is bigDecimal check if input > 0 and input >= starting price
                    if(in.hasNextBigDecimal())
                    {
                        bid = in.nextBigDecimal();
                        if(bid.compareTo(BigDecimal.ZERO) <0 ||(bid.compareTo(remoteService.getActiveAuctions().get(auctionID).getStartingPrice()) < 0))
                            System.out.println("The bid must be greater than 0 and greater than the starting value. Please try again");
                        else
                            notNumber = false;
                    }else{
                        System.out.println("The bid must be a numerical value. Please try again");
                        in.next();
                    }
                }
                //make the bid.
                bidMessage = remoteService.bidFromItem(bid,auctionID,getUserID());
                System.out.println(bidMessage);
                //Add the action the buyer's action list
                myAuctions.put(auctionID,remoteService.getActiveAuctions().get(auctionID));
                System.out.println("You will be notified when the auction is closed and he winner will be announced ");
            }
        }
    }

    /**
     * Accessor for the remote object
     * @return remoteService
     */
    public Server_Interface getRemoteService()
    {
        return remoteService;
    }

    /**
     * Accessor to hashMap with the buyer's Actions
     * @return myActions
     */
    public HashMap<String, Auction> getMyAuctions() {
        return myAuctions;
    }

}
