import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface IntServer extends Remote {

    /**
     * Used to connect a bikeUser and adding it to the users table in the server
     * also doing some refreshes of userlists
     *
     * @param bikeUser
     * @throws RemoteException
     */
    void Connect(IntBikeUser bikeUser) throws RemoteException;

    /**
     * Used to avoid double pseudos
     *
     * @return
     * @throws RemoteException
     */

    ArrayList<String> getPseudosInUse() throws RemoteException;

    /**
     * @param bikeUser
     * @throws RemoteException
     * @see IntBikeUser
     */

    void removeUser(IntBikeUser bikeUser) throws RemoteException;

    /**
     * Get all games name
     */
    ArrayList<String> getGameNames() throws RemoteException;

    /**
     * Create a game, be aware, the server is supposed  to iterate the users to call (updateGameList) with the updated list
     */
    void createGame(String gameName) throws RemoteException;


    /**
     * Method used when a player double click on the waiting room, to join the waiting room of the selected game
     * the server has to do the same thing than createGame, iterate the users in the same game waiting room to update the defaultmodellist
     *
     * @param bikeUsername
     * @param gameName
     * @throws RemoteException
     * @see IntBikeUser
     */
    void joiningWaitingGame(String bikeUsername, String gameName) throws RemoteException;

    /**
     * Method used when a user in a game is ready or not, he's not ready at the beginning
     * @param bikeUsername
     * @param gameName
     * @param readyState
     *
     */
    void playerReadyState(String bikeUsername, String gameName, Boolean readyState) throws RemoteException, InterruptedException;


    /**
     * get score of player (it's the same for all players)
     * @param gameName
     * @return
     * @throws RemoteException
     */
    int getPlayerScore(String gameName) throws RemoteException;


    /**
     * for change the direction of your bike
     * @param bikeUsername
     * @param gameName
     * @param carDirection
     * @throws RemoteException
     */
    void changeDirection(String bikeUsername, String gameName, char carDirection) throws RemoteException;

    /**
     * Get players still in game (alive)
     * @param gameName
     * @return boolean[]
     */
    boolean getAlivePlayer(String gameName,String playerName) throws RemoteException;
    /**
     * Relaunch, give me gamelist (server does it with the intbikeuser.updategamelist)
     * @param
     */
      void relaunchUpdateGameList(String bikeUsername) throws RemoteException;
}