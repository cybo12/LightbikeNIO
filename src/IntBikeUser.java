import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface IntBikeUser extends Remote {
    /**
     * @return the pseudo of the chatuser
     * @throws RemoteException
     */
    String getPseudo() throws RemoteException;

    IntServer getServer() throws RemoteException;

    /**
     * Will create the waiting room (GUI)
     *
     * @param bikeUser
     * @throws RemoteException
     */

    void createWaitingRoom(IntBikeUser bikeUser) throws RemoteException;

    /**
     * Will ask the GUI to update the gamelist
     *
     * @param gameList
     * @throws RemoteException
     */

    void updateGameList(ArrayList<String> gameList) throws RemoteException;

    /**
     * Will create a game, and ask to the server if the name already exist, before creating it
     *
     * @param gameName
     * @throws RemoteException
     */
    void createGame(String gameName) throws RemoteException;

    /**
     * Will create the GUI for the waiting room when you've joined a game and you're waiting for the launch
     *
     * @param gameName
     * @param bikeUser
     */
    void createWaitingRoomJoined(IntBikeUser bikeUser, String gameName) throws RemoteException;

    /**
     * Method to update current players in the room
     */
    void updatePlayersInGameWaiting(ArrayList<String> updatePlayersWaiting) throws RemoteException;

    /**
     * This method is used to inform the player that the game will start because all players are ready (with or without IA)
     */
    void startGame(ArrayList<String> playerList, String gameName) throws RemoteException;

    /**
     * send by server when a player is game over
     *
     * @param playerName
     * @param playerScore
     * @throws RemoteException
     */
    void endGame(String playerName,int playerScore) throws RemoteException;

    /**
     * Will start the real game and launch the grid + disable the play button
     */
    void startGameGrid() throws RemoteException;

    /**
     * update the game grid
     * @param gameGrid
     */
void updateGameGrid(int [][] gameGrid) throws RemoteException;
}