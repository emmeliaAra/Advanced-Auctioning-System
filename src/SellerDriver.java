import java.rmi.RemoteException;
import java.util.Scanner;

/**
 * This Class is creates a reference of Seller class
 * and calls the methods.
 * @author Emmeleia Arakleitou
 */
public class SellerDriver {

    /**
     * Main Class that requests and validates the input to create an new Seller.
     * @param args
     * @throws RemoteException ex
     */
    public static void main(String [] args) throws RemoteException {

        Client client = new Client();
        char choice = client.displayOptions();
        char sellerChoice;
        Seller mySeller = null;

        if(choice == 'A'){
            String sellerName,sellerEmail,userPass;
            Scanner in = new Scanner(System.in);

            System.out.println("Enter your user name, email  to become a member and sell your items or press 'D' to exit");
            System.out.print("User Name : " );

            //While seller name is empty ask for the name again
            sellerName = client.getValidName();

            //If the user press character D the program stops
            if(sellerName.length() == 1 && sellerName.equalsIgnoreCase("D"))
                System.exit(0);

            System.out.println();
            System.out.println("Your email must be in this form:  name@gmail.com");
            System.out.print("Email :");
            sellerEmail = in.nextLine();

            //Ask for email as long as the email format given does not match the one required.
            while(sellerEmail.length()<1 || sellerEmail.contains(" ") || !sellerEmail.contains("@gmail.com"))
            {
                System.out.println('\n' + "Please give a valid email.(Emails can not contain Space characters)");
                sellerEmail = in.nextLine();
            }
            System.out.println();
            System.out.println("Please give a password of length 6-16-characters");
            userPass = in.nextLine();
            while (userPass.length() <6 || userPass.length()> 16)
            {
                System.out.println("Please give a password of length 6-16-characters");
                userPass = in.nextLine();
            }

            sellerName = sellerName.toUpperCase();
            sellerEmail = sellerEmail.toUpperCase();
            userPass = userPass.toUpperCase();
            //creates the seller.
            mySeller = new Seller(sellerName,sellerEmail,userPass);

            System.out.println("This is you ID: " + mySeller.getUserID());
        }
        else if(choice == 'B')
        {
            String userName;
            System.out.println("Please enter your user name ");
            System.out.print("Name : " );

            userName = client.validateUserName();

            boolean authenticated = false;
            while (!authenticated)
            {
                String password= client.validatePass();

                if(password == null)
                    System.exit(0);
                else
                {
                    //authenticates server and its self
                    authenticated = client.authenticateServer(userName,password);
                    if(!authenticated)
                        System.out.println("Authentication Failed");
                    else
                        try
                        {
                            mySeller =(Seller) client.getRemoteService().getClientOnPass(password);
                            //Catch exception if a buyer tries to login as a seller.
                        }catch (ClassCastException cE)
                        {
                            System.out.println('\n' + "Authentication Failed");
                            authenticated = false;
                        }
                }
            }
        }else if(choice == 'C')
            System.exit(0);

        sellerChoice = mySeller.listSellerOptions();
        // list the options and make actions as long as the user does not terminate the program
       while(sellerChoice!= 'D'){
            mySeller.takeAction(sellerChoice);
            sellerChoice = mySeller.listSellerOptions();
        }
    }
}
