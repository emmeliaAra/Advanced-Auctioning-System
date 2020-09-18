import java.security.NoSuchAlgorithmException;
import java.security.InvalidKeyException;
import javax.crypto.spec.SecretKeySpec;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.security.Key;
import java.rmi.Naming;
import javax.crypto.*;
import java.util.*;
import java.io.*;

/**
 * This class creates a local reference to the remote object
 * Can invoke methods of the remote service.
 * @author Emmeleia Arakleitou
 */
public class Client implements Serializable{

    private String name, email,userID,userPass;
    private Server_Interface remoteService;
    private String serverURL = "rmi://localhost/AuctioningServer";

    /**
     * Constructor of the class
     * @param name Client name
     * @param email Client Email
     */
    public Client(String name, String email, String userPass) {
        this.name = name;
        this.email = email;
        this.userPass = userPass;
    }

    /**
     * Constructor with no parameters used to invoke methods from client to validate the client's information
     */
    public Client() {
        connectToRMI(serverURL);
    }

    /**
     * This method connects the client to the remote object
     * @param serverURL The URL to the remoteServer.
     * @return a reference to the remote object.
     */
    public Server_Interface connectToRMI(String serverURL) {

        try {
            //Lookup the remote service in the registry.
            remoteService = (Server_Interface) Naming.lookup(serverURL);
        } catch (NotBoundException nBe) {
            System.out.println("The name you try to lookup has no bind in the registry");
            nBe.printStackTrace();

        } catch (MalformedURLException
                 | RemoteException mURLe) {
            mURLe.printStackTrace();
        }
        return remoteService;
    }

    /**
     * Creates and returns a random ID for the user
     * @return User's ID
     * @throws RemoteException ex
     */
    public String createClientID(){
        Random rand = new Random();
        this.userID =  Integer.toString(rand.nextInt(1000) + 1);
        //checks if user id already assigned.
        try {
            while (remoteService.validateClientID(userID,this))
                this.userID =  Integer.toString(rand.nextInt(1000) + 1);
        }catch (RemoteException re) {
                re.printStackTrace();
        }
        return this.userID;
    }

    /**
     * Prints the list of the active actions
     * @param activeAuctions HashMap with the active actions
     * @return
     */
    public boolean displayActiveAuctions(HashMap<String,Auction> activeAuctions )
    {
        boolean isEmpty = false;
        if(activeAuctions.isEmpty())
        {
            System.out.println("There are no active Auctions at the moment");
            isEmpty = true;
        }
        else{
            System.out.println("These are the current active Auctions" + '\n');
            System.out.format("%-25s%-60s%-25s%-25s%n","AuctionID" , "Item Description" ,"Starting Price" , "Highest Bid" );
            for (Map.Entry<String,Auction> pair : activeAuctions.entrySet()){
                Auction a = pair.getValue();
                System.out.format("%-25s%-60s%-25s%-25s%n", pair.getKey() , a.getItemDesc() , a.getStartingPrice() ,  a.getHighestBid() );
                isEmpty = false;
            }
            System.out.println();
        }
        return isEmpty;
    }

    /**
     * Provides clients a menu to login or create an account!
     * @return user's choice
     */
    public char displayOptions()
    {
        Scanner in = new Scanner(System.in);
        System.out.println("These are your options: ");
        System.out.println("A. Register  ");
        System.out.println("B. Log in ");
        System.out.println("C. Exit");

        char choice2 = 0;
        boolean itsOk = false;

        while (!itsOk)
        {
            String choice = in.next();
            if(choice.length() >1 )
                System.out.println("Please type 'A', 'B' or 'C'");
            else {
                choice2 = Character.toUpperCase(choice.charAt(0));
                if(choice2!= 'A'  && choice2 != 'B' && choice2!='C')
                    System.out.println("Please give a valid input!" + '\n');
                else
                    itsOk = true;
            }
        }
        return choice2;
    }

    /**
     * Checks that the user name provided is valid. -Not used by another user not empty spaces/
     * @return the user name
     * @throws RemoteException
     */
    public  String getValidName() throws RemoteException {
        boolean itsOk=false;
        Scanner in = new Scanner(System.in);
        String sellerName = in.nextLine();
        while (!itsOk)
        {
            if(sellerName.replace(" ","").length()==0) {
                System.out.println("Please give your user name  or press 'D' to exit.");
                System.out.print("User Name : " );
                sellerName = in.nextLine();
            }
            else if(this.getRemoteService().validateUserName(sellerName.toUpperCase())) {
                System.out.println("This userName is already taken by another user! Please try again or press 'D' to exit");
                System.out.print("User Name : " );
                sellerName = in.nextLine();
            }
            else if(sellerName.length() ==1 && sellerName.equalsIgnoreCase("D"))
                itsOk = true;
            else
                itsOk = true;
        }
        return sellerName;
    }

    /**
     * Checks if password is in the right format in order to be authenticated.
     * @return String password, null if not in right format
     */
    public String validatePass()
    {
        String password=null;
        Scanner in = new Scanner(System.in);
        boolean itsOk = false;
        while (!itsOk)
        {
            System.out.println("Please provide password to login! Or press 'D' to exit");
            System.out.print("Password: " );
            password = in.nextLine();

            //If the user press character D the program stops
            if(password.length() == 1 && password.equalsIgnoreCase("D")) {
                password = null;
                itsOk = true;
            }

            else if (password.length()< 6 || password.length() > 16)
            {
                System.out.println("Password must be 16 characters");
            }
            else
                itsOk = true;
        }
        return password;
    }

    /**
     * Checks that the userName provided to login is valid.
     * @return username if valid, null if not
     * @throws RemoteException
     */
    public String validateUserName() throws RemoteException {
        Scanner in = new Scanner(System.in);
        String  userName = in.nextLine();

        while (!this.getRemoteService().validateUserName(userName.toUpperCase())){

            //If the user press character D the program stops
            if(userName.length() == 1 && userName.equalsIgnoreCase("D"))
                System.exit(0);
            System.out.println("The user name does not exits");
            System.out.println("Please try again or press 'D' to exit");
            userName = in.nextLine();
        }
        return userName;
    }

    /**
     * Function that writes the secret keys(passwords) to a file.
     * @param client
     */
    public void writeKeys(Client client)
    {
        try{
            //initialize a BuffeWriter and write in the file the username and password. The File is named after the user name
            //for later identification
            BufferedWriter writer = new BufferedWriter(new FileWriter(client.getName()+ ".txt"));
            writer.write(client.getName() + "," + client.getUserPass());
            writer.newLine();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method used to authenticate the server.
     * @param userName the user name used to login
     * @param userPass the password that will be used as the key.
     * @return true if both user and client are authenticated
     */
    public boolean authenticateServer(String userName,String userPass)
    {
        boolean authenticated = true;
        Random rand = new Random();
        int randomNum =  rand.nextInt(1000) + 1;
        AuthenticationReply authenticationReply = null;

        try {
            /*calls remote method to authenticate the server and get the response.
             * (The server challenge and the challenge for the client*/
            authenticationReply = remoteService.serverAuthentication(userName,randomNum);

            //calls a method to generate a key using the password form the file
            Key secretKey = getClientSecretKey(userPass);

            /*Decrypts the server challenge using his own key which is the password used to login
             * if Either server or client not how they claim to be will lead to a badPaddingException
             * AES decryption with an incorrect key will produce garbage data as an output -> instead of decrypting it
             * produces an error*/
            Cipher cipher = Cipher.getInstance(secretKey.getAlgorithm());
            cipher.init(Cipher.DECRYPT_MODE,secretKey);
            cipher.doFinal(authenticationReply.getEncryptedNum());

        } catch (RemoteException
                | NoSuchPaddingException
                | NoSuchAlgorithmException
                | InvalidKeyException
                | IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            authenticated = false;
        }
        /*if the server is authenticated the client must now authenticate
         * it's self by solving the random number generated by the server*/
        if(authenticated)
        {
            int myChallenge = authenticationReply.getRandomNumForYou();
            try {
                Key secretKey = getClientSecretKey(userPass);
                Cipher cipher = Cipher.getInstance(secretKey.getAlgorithm());
                cipher.init(Cipher.ENCRYPT_MODE,secretKey);

                byte[] encrypted = cipher.doFinal(Integer.toString(myChallenge).getBytes());
                authenticationReply = new AuthenticationReply(encrypted);
                authenticated = remoteService.authenticateClint(authenticationReply,userName);

            } catch (NoSuchAlgorithmException
                    | NoSuchPaddingException
                    | BadPaddingException
                    | InvalidKeyException
                    | RemoteException
                    | IllegalBlockSizeException e) {
                e.printStackTrace();
            }
        }
        return authenticated;
    }

    /**
     * Generates public key from password stored in tge file as string
     * @param userPass the user password used when account was created
     * @return the Key to encrypt and decrypt the data
     */
    public Key getClientSecretKey(String userPass)
    {
        Key secretKey;
        //adds some characters to the string so that an 128 key will result
        while(userPass.length()<16)
            userPass = userPass+"0";
        secretKey = new SecretKeySpec(userPass.getBytes(),"AES");
        return secretKey;
    }

    public Server_Interface getRemoteService() {
        return remoteService;
    }

    /**
     * Accessor to name variable
     * @return Client name
     */
    public String getName() {
        return name;
    }

    /**
     * Accessor to UserID variable
     * @return Client ID
     */
    public String getUserID() {
        return userID;
    }

    /**
     * Accessor for user password
     * @return string
     */
    public String getUserPass() {
        return userPass;
    }
}
