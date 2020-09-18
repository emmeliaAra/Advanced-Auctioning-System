import java.rmi.RemoteException;
import java.math.BigDecimal;
import java.util.Scanner;

/**
 * This class is a subclass of Client and implements the functionality
 * for the seller of the auction.
 * @author Emmeleia Arakleitou
 */
public class Seller extends Client
{
    private final String serverURL ="rmi://localhost/AuctioningServer";
    private BigDecimal startingPrice,reservedPrice;
    private Server_Interface remoteService;
    private String choice;
    private char choice2;
    private Scanner in;

    /**
     * Constructor of the class that calls the constructor of the superclass Client
     * In the constructor the Seller connects to the the server
     * @param sellerName Name of Seller
     * @param selleEmail Email of Seller
     */
    public Seller(String sellerName,String selleEmail,String userPass) {

        super(sellerName, selleEmail,userPass);
        this.remoteService = this.connectToRMI(serverURL);
        createClientID();
        this.writeKeys(this);
    }

    /**
     * This method lists all the actions that the seller can take
     * and includes a recursive call when the input is not valid
     * @return the choice made by the seller
     */
    public char listSellerOptions()
    {
        in = new Scanner(System.in);
        System.out.println('\n' + "These are your options : " + '\n');
        System.out.println("A. Create a new Auction");
        System.out.println("B. View all current active Auctions");
        System.out.println("C. Terminate an active Auction");
        System.out.println("D. Exit" + '\n');
        System.out.println("Choose one of the above options by pressing either 'A' , 'B' , 'C' or 'D' " + '\n');

        //if choice is not a valid input will recursively call the method again until is valid
        choice = in.next();
        if(choice.length() > 1){
            System.out.println("Please give a valid input!" + '\n');
            listSellerOptions();
        }
        else{
            choice2 = Character.toUpperCase(choice.charAt(0));
            if(choice2!= 'A'  && choice2 != 'B' && choice2 != 'C' && choice2 != 'D')
            {
                System.out.println("Please give a valid input!" + '\n');
                listSellerOptions();
            }
        }
        return choice2;
    }

    /**
     * This method implements the action selected by the seller
     * @param choice The choice of the seller
     * @throws RemoteException ex
     */
    public void takeAction(char choice) throws RemoteException {
        String itemDesc, auctionID;
        in = new Scanner(System.in);
        boolean isEmpty ;
        int isClosed;

        //Takes a different action according to the value of variable choice
        if(choice == 'A')
        {
            System.out.println("To create a new Auction you must give the following information: " + '\n');
            System.out.print("Description of the item : " );
            itemDesc = in.nextLine();
            in = new Scanner(System.in );

            //Get user input for reserved, starting value and description of the item.
            this.getValues();

            //create a new action using the remote object
            auctionID = remoteService.createNewAuction(itemDesc,startingPrice,reservedPrice,this.getUserID());
            System.out.println('\n' + "An Auction has been created successfully with ID : " + auctionID +  '\n');
        }
        else if(choice == 'B')
            //prints the list with the active options
            displayActiveAuctions(remoteService.getActiveAuctions());

        else if(choice == 'C')
        {
            //Checks if there are no active auctions.
            isEmpty = displayActiveAuctions(remoteService.getActiveAuctions());
            if(!isEmpty)
            {
                //Checks if the ID provided belongs to an active action
                System.out.println("Enter the ID of the auction you want to close");
                auctionID = in.next();
                isClosed = remoteService.closeAuction(auctionID, this.getUserID());

                if(isClosed == -1 )
                    System.out.println("The auction ID provided does not belong to any of the current active auctions");
                else if (isClosed == 0)
                    System.out.println("You can not close this auction because you are not the creator of the auction");

                else{
                    //If id is valid looks for the winner and prints the appropriate message
                    Client winner = remoteService.announceWinner(auctionID);
                    if (winner == null)
                        System.out.println("There was no winner because the reserved price was not met");
                    else
                        System.out.println("The winner of this auction is: " + winner.getName() + " With userID: " + winner.getUserID());

                    System.out.println("Action Closed");

                }
            }
        }
    }

    /**
     * This method is used to validate the user input.
     */
    public void getValues()
    {
        boolean notNumber = true;
        boolean aNotSet = true;
        boolean bNotSet = true;

        System.out.print('\n' + "Reserved price: ");
        while(notNumber && (aNotSet || bNotSet)) {

            //checks if input is a BigDecimal value
            if(in.hasNextBigDecimal() )
            {
                //Checks if the reserved value is set. if not ask for input.
                //Checks if value given is greater that 0 and if not ask for input again
                if(aNotSet)
                {
                    reservedPrice =  in.nextBigDecimal();
                    if ( reservedPrice.compareTo(BigDecimal.ZERO) > 0)
                    {
                        aNotSet = false;
                        System.out.println();
                        System.out.print("Starting Price: ");
                    }else {
                        System.out.println("Reserved price must be a greater than zero. Please try again");
                        System.out.print("Reserved Price: ");
                    }

                }
                //if reserved price is set does the same for the starting price
                else{
                    startingPrice = in.nextBigDecimal();
                    if(startingPrice.compareTo(BigDecimal.ZERO) >0)
                        notNumber = false;
                    else{
                        System.out.println("Starting Price must be a positive number. Please try again");
                        System.out.print("Starting Price: ");
                    }
                }
            } else{ //if not a BigDecimal value require for input.
                if(aNotSet)
                {
                    System.out.println("Reserved price must be numerical values. Please give a new input." + '\n');
                    System.out.print("Reserved price: ");
                    in.next();
                }
                else{
                    System.out.println("Starting price must be numerical value. Please give a new input." + '\n');
                    System.out.print("Starting price: ");
                    in.next();
                }
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


}