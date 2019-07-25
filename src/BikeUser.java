import javax.swing.*;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class BikeUser extends UnicastRemoteObject implements IntBikeUser {
    protected BikeWaitingRoom bikeWaitingRoom;
    protected BikeWaitingRoomJoined bikeWaitingRoomJoined;
    protected LightBikesTronGUI lightBikesTronGUI;
    private IntServer server;
    private String pseudo;
    private IntBikeUser bikeUserLocal;

    /**
     * bikeUser is a class only client-side. It's used by the server to call method from the clients because we won't store any datas on the client's side
     * Exemple ; when a client login, the server must update the user's list for all users (for all users, chatclient.updateuserlist etc...
     *
     * @param server server created from the GUI
     * @param pseudo
     * @throws RemoteException
     */
    public BikeUser(IntServer server, String pseudo) throws RemoteException {
        this.server = server;
        this.pseudo = pseudo;
        ArrayList<String> test = server.getPseudosInUse();
        System.out.println(test);
    }

    public static void main(String[] args) throws RemoteException, UnknownHostException {
        BikeUserCredGUI prompt = new BikeUserCredGUI();
    }

    /**
     * @return the pseudo of the chatuser
     * @throws RemoteException
     */
    public String getPseudo() throws RemoteException {
        return pseudo;
    }

    public IntServer getServer() throws RemoteException {
        return server;
    }

    @Override
    public void createWaitingRoom(IntBikeUser bikeUser) throws RemoteException {
        bikeWaitingRoom = new BikeWaitingRoom(bikeUser);
    }

    @Override
    public void updateGameList(ArrayList<String> gameList) throws RemoteException {
        DefaultListModel modelGameList = convertToModel(gameList);
        bikeWaitingRoom.updateGameListGUI(modelGameList);
    }

    private DefaultListModel convertToModel(ArrayList<String> gameList) {
        DefaultListModel modelGameList = new DefaultListModel();
        for (int i = 0, n = gameList.size(); i < n; i++) {
            modelGameList.addElement(gameList.get(i));
        }
        return modelGameList;
    }

    @Override
    public void createGame(String gameName) throws RemoteException {
        server.createGame(gameName);
    }

    @Override
    public void createWaitingRoomJoined(IntBikeUser bikeUser, String gameName) throws RemoteException {
        bikeWaitingRoomJoined = new BikeWaitingRoomJoined(bikeUser, gameName);
    }

    @Override
    public void updatePlayersInGameWaiting(ArrayList<String> updatePlayersList) throws RemoteException {
        if (updatePlayersList.size() < 4) {
            for (int i = updatePlayersList.size(); i < 4; i++) {
                updatePlayersList.add("IA");
            }
        }
        DefaultListModel modelPlayersWaiting = convertToModel(updatePlayersList);
        bikeWaitingRoomJoined.updatePlayerListGUI(modelPlayersWaiting);
    }

    @Override
    public void startGame(ArrayList<String> playerList, String gameName) throws RemoteException {
        lightBikesTronGUI = new LightBikesTronGUI(bikeUserLocal, gameName, bikeWaitingRoomJoined);
        lightBikesTronGUI.setSize(500, 550);
        lightBikesTronGUI.setLocation(100, 100);
        lightBikesTronGUI.setVisible(true);
    }

    @Override
    public void endGame(String playerName, int playerScore) throws RemoteException {
        lightBikesTronGUI.ending(playerName, playerScore);

    }

    @Override
    public void startGameGrid() throws RemoteException {


        lightBikesTronGUI.startGameGrid();
    }

    @Override
    public void updateGameGrid(int[][] gameGrid) throws RemoteException {
        lightBikesTronGUI.update(gameGrid);
    }

    public void setBikeUserLocal(IntBikeUser bikeUserLocal) {
        this.bikeUserLocal = bikeUserLocal;
    }
}
