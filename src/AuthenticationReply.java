import java.io.Serializable;
import java.util.Random;

/**
 * This class represents the reply that both the server and the client users
 * to respont to an authentication request.
 * It contains the encrypted challenge and a random number that will be used as
 * a challenge from the receiver if not already solved one.
 * @author Emmeleia Arakleitou
 */
public class AuthenticationReply implements Serializable {

    private byte[] encryptedNum;
    private int randomNumForYou;

    /**
     * Constructor of the class that generated the random number
     * @param encryptedNum the encrypted challenge.
     */
    public AuthenticationReply(byte[] encryptedNum) {
        Random rand = new Random();
        this.encryptedNum = encryptedNum;
        this.randomNumForYou = rand.nextInt(1000) + 1;
    }
    /**
     * Accessor for the encrypted challenge
     * @return byte[]
     */
    public byte[] getEncryptedNum() {
        return encryptedNum;
    }

    /**
     * Accessor for the random number that repserents the challenge.
     * @return int
     */
    public int getRandomNumForYou()
    {
        return randomNumForYou;
    }
}
