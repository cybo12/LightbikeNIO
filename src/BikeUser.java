import javax.swing.*;
import java.io.IOException;
import java.nio.*;
import java.nio.channels.*;
import java.net.*;
import java.rmi.RemoteException;
import java.util.ArrayList;

public class BikeUser implements Runnable {
    private static BikeUserCredGUI prompt;
    protected BikeWaitingRoom bikeWaitingRoom;
    protected BikeWaitingRoomJoined bikeWaitingRoomJoined;
    protected LightBikesTronGUI lightBikesTronGUI;
    private PlayerData player;
    private Communication com;
    private static int NIOPortNum = 1100;


    public BikeUser(String ipAddress) {
        connectToServer(ipAddress, NIOPortNum);
    }

    public void connectToServer(String address, int port)
    {
        //open a TCP connection to the server
        try
        {
            player = new PlayerData(SocketChannel.open(new InetSocketAddress(address, port)));

            //start a thread to receive data
            new Thread(this).start();
        }
        catch(Exception e)
        {
            System.out.println("Error connecting to server: " + e);
        }
    }

    public void run()
    {
        while (true)
        {
            try
            {
                //read the next header
                byte[] header = com.readFully(3,player.getClient());
                //get the payload length from the header
                int payloadLength = header[2];
                byte[] message = com.readFully(payloadLength,player.getClient());
                treathEvent(header[1],message,player.getClient());
            }
            catch(Exception e)
            {
                System.out.println("Error receiving data: " + e);
            }
        }
    }
    private void treathEvent(int event, byte[] payload, SocketChannel client) throws IOException {
        switch(event){
            case GameEvent.GETPSEUDOSINUSE:
                prompt.setInUse(com.bytesToArraylist(payload));
                break;
        }
    }

    public static void main(String[] args) throws RemoteException, UnknownHostException {
        prompt = new BikeUserCredGUI();
    }

    /**
     * @return the pseudo of the chatuser
     * @throws RemoteException
     */
    public String getPseudo(){
        return player.getName();
    }

    public void connect(){
        com.sendBytes(GameEvent.CONNECT,player.getClient(),this.getPseudo().getBytes());
    }

    public void createWaitingRoom(){
        bikeWaitingRoom = new BikeWaitingRoom(this);
    }

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

    public void createGame(String gameName) throws RemoteException {
        com.sendBytes(GameEvent.CREATEGAME,player.getClient(),gameName.getBytes());
    }

    public void createWaitingRoomJoined(BikeUser bikeUser, String gameName) throws RemoteException {
        bikeWaitingRoomJoined = new BikeWaitingRoomJoined(bikeUser, gameName);
    }

    public void updatePlayersInGameWaiting(ArrayList<String> updatePlayersList) throws RemoteException {
        if (updatePlayersList.size() < 4) {
            for (int i = updatePlayersList.size(); i < 4; i++) {
                updatePlayersList.add("IA");
            }
        }
        DefaultListModel modelPlayersWaiting = convertToModel(updatePlayersList);
        bikeWaitingRoomJoined.updatePlayerListGUI(modelPlayersWaiting);
    }

    public void startGame(ArrayList<String> playerList, String gameName) throws RemoteException {
        lightBikesTronGUI = new LightBikesTronGUI(this,gameName,bikeWaitingRoomJoined);
        lightBikesTronGUI.setSize(500, 550);
        lightBikesTronGUI.setLocation(100, 100);
        lightBikesTronGUI.setVisible(true);
    }

    public void endGame(String playerName, int playerScore) throws RemoteException {
        lightBikesTronGUI.ending(playerName, playerScore);

    }

    public void startGameGrid() throws RemoteException {


        lightBikesTronGUI.startGameGrid();
    }

    public void updateGameGrid(int[] change) throws RemoteException {
        lightBikesTronGUI.update(change);
    }


    public void setPseudo(String pseudo) {
        player.setName(pseudo);
    }

    public void getPseudoInUse() {
        com.sendBytes(GameEvent.GETPSEUDOSINUSE,player.getClient());
    }

    public void removeUser() {
        com.sendBytes(GameEvent.REMOVEUSER,player.getClient());
    }

    public void getGameNames() {
        com.sendBytes(GameEvent.GETGAMENAMES,player.getClient());
    }

    public boolean getAlivePlayer() {
        com.sendBytes(GameEvent.GETALIVEPLAYER,player.getClient();
    }
}
