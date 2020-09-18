import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Map;

/**
 * This Class  requests for input and validates it to
 * create a reference of Buyer class
 * and calls the methods.
 * @author Emmeleia Arakleitou
 */
public class BuyerDriver {


    public static void main(String [] args) throws RemoteException
    {
        Scanner in = new Scanner(System.in);
        char buyerChoice;
        Client client = new Client();
        char choice = client.displayOptions();
        Buyer myBuyer = null;

        if(choice == 'A')
        {
            String buyerName, buyerEmail,userPass;
            System.out.println("Enter your name and email to become a member and bid for active auctions or press 'D' to exit ");
            System.out.print("Name : " );

            buyerName = client.getValidName();
            if(buyerName.length() == 1 && buyerName.equalsIgnoreCase("D"))
                System.exit(0);

            System.out.println();
            System.out.println("Your email must be in this form:  name@gmail.com");
            System.out.print("Email : ");
            buyerEmail = in.nextLine();

            while (buyerEmail.length() <1 || buyerEmail.contains(" ") || !buyerEmail.contains("@gmail.com"))
            {
                System.out.println('\n' + "Please give a valid email.(Emails can not contain Space characters)");
                buyerEmail = in.nextLine();
            }
            System.out.println();

            System.out.println("Please give a password of length 6-16-characters");
            userPass = in.nextLine();
            while (userPass.length() <6 || userPass.length()> 16)
            {
                System.out.println("Please give a password of length 6-16-characters");
                userPass = in.nextLine();
            }

            buyerName = buyerName.toUpperCase();
            buyerEmail = buyerName.toUpperCase();
            userPass = userPass.toUpperCase();

            myBuyer = new Buyer(buyerName,buyerEmail,userPass);
            System.out.println("This is your ID: " + myBuyer.getUserID());

        }else if(choice == 'B')
        {
            String userName,password;
            System.out.println("Please enter your user name or press D to exit ");
            System.out.print("Name : " );
            userName  = client.validateUserName();

            boolean authenticated = false;
            while (!authenticated)
            {
                password = client.validatePass();
                if (password==null)
                    System.exit(0);
                else{
                    authenticated = client.authenticateServer(userName,password);
                    if(!authenticated)
                        System.out.println("Authentication Failed");
                    else
                        //catch the exception when a seller tries to login as a buyer.
                        try{
                            myBuyer =(Buyer) client.getRemoteService().getClientOnPass(password);
                        }catch (ClassCastException cE)
                        {
                            System.out.println('\n' + "Authentication Failed");
                            authenticated = false;
                        }
                }
            }
        }else if(choice == 'C')
            System.exit(0);

        Server_Interface remoteService = myBuyer.getRemoteService();
        buyerChoice = myBuyer.listBuyerOptions();
        int num = 0;
        while (buyerChoice!='C')
        {
            myBuyer.takeAction(buyerChoice);
            buyerChoice = myBuyer.listBuyerOptions();
            num = iAmWinner(remoteService, remoteService.getClosedAuctions(),myBuyer,num);
        }

        System.out.println();
    }

    /**
     * This method is used to validate if this buyer is the winner of a closed auction.
     * @param remoteService The remote object used to invoke methods in the interface
     * @param closedAuctions Hash<ap with the closed auctions
     * @param myBuyer The buyer to check if is the winner
     * @param previousClosedNum An int to that holds the number of the previous number of closed auctions
     * @return
     * @throws RemoteException
     */
    public static int iAmWinner(Server_Interface remoteService, HashMap<String,Auction> closedAuctions, Buyer myBuyer, int previousClosedNum) throws RemoteException
    {
        //If there one or more auction is closed
        if(  previousClosedNum  < closedAuctions.size() )
        {

            previousClosedNum = closedAuctions.size();
            for (Map.Entry<String,Auction> pair : closedAuctions.entrySet()){
                Auction auction = pair.getValue();
                String auctionID = auction.getAuctionID();
                //for every entry in the hashMap check if this buyer placed a bid in this auction
                if(myBuyer.getMyAuctions().containsKey(auctionID)) {

                    //If auction status in Active -> The auctions is closed and the winner was not announced
                    if( myBuyer.getMyAuctions().get(auctionID).getAuctionStatus().equals("Active"))
                    {
                        Buyer winner = (Buyer) remoteService.announceWinner(auctionID);

                        if (winner == null)
                            System.out.println("There is no winner because the reserved price was not met!" + '\n');
                        else if (auction.getHighestBidderID().equals(myBuyer.getUserID()) && auction.getReservedPrice().compareTo(auction.getHighestBid()) <0 )
                            System.out.println("You are the winner of auction with auctionID " + auction.getAuctionID() + '\n');
                        else
                            System.out.println("Auction " + auctionID + " is now closed but you are not the winner!! " + '\n');

                        //set status to closed so that the winner for this auction won't be announced again.
                        myBuyer.getMyAuctions().get(auctionID).setAuctionStatus("Closed");
                    }
                }
            }
        }
        return previousClosedNum;
    }
}
