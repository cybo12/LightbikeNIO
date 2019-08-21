import javax.swing.*;
import java.io.IOException;
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
            case GameEvent.GETALIVEPLAYER:
                lightBikesTronGUI.setAlive(payload[0]!=0);
                break;
            case GameEvent.GETPLAYERSCORE:
                this.setScore(payload[0]);
                break;
            case GameEvent.UPDATEGAMEGRID:
                lightBikesTronGUI.update(payload);
                break;
            case GameEvent.STARTGAMEGRID:
                lightBikesTronGUI.startGameGrid();
                break;
            case GameEvent.ENDGAME:
                lightBikesTronGUI.ending(com.getString(payload));
            case GameEvent.UPDATEGAMELIST:
                DefaultListModel modelGameList = convertToModel(com.bytesToArraylist(payload));
                bikeWaitingRoom.updateGameListGUI(modelGameList);
                break;
            case GameEvent.STARTGAME:
                this.startGame(com.getString(payload));
                break;
            case GameEvent.UPDATEPLAYERSINGAMEWAITING:
                this.updatePlayersInGameWaiting(com.bytesToArraylist(payload));
                break;
        }
    }

    public static void main(String[] args) throws UnknownHostException {
        prompt = new BikeUserCredGUI();
    }


    public void connect(){
        com.sendBytes(GameEvent.CONNECT,player.getClient(),this.getPseudo().getBytes());
    }

    public void createWaitingRoom(){
        bikeWaitingRoom = new BikeWaitingRoom(this);
    }


    private DefaultListModel convertToModel(ArrayList<String> gameList) {
        DefaultListModel modelGameList = new DefaultListModel();
        for (int i = 0, n = gameList.size(); i < n; i++) {
            modelGameList.addElement(gameList.get(i));
        }
        return modelGameList;
    }

    public void createGame(String gameName) {
        com.sendBytes(GameEvent.CREATEGAME,player.getClient(),gameName.getBytes());
    }

    public void createWaitingRoomJoined(BikeUser bikeUser, String gameName) {
        bikeWaitingRoomJoined = new BikeWaitingRoomJoined(bikeUser, gameName);
    }

    public void updatePlayersInGameWaiting(ArrayList<String> updatePlayersList){
        if (updatePlayersList.size() < 4) {
            for (int i = updatePlayersList.size(); i < 4; i++) {
                updatePlayersList.add("IA");
            }
        }
        DefaultListModel modelPlayersWaiting = convertToModel(updatePlayersList);
        bikeWaitingRoomJoined.updatePlayerListGUI(modelPlayersWaiting);
    }

    public void startGame(String gameName){
        lightBikesTronGUI = new LightBikesTronGUI(this,gameName,bikeWaitingRoomJoined);
        lightBikesTronGUI.setSize(500, 550);
        lightBikesTronGUI.setLocation(100, 100);
        lightBikesTronGUI.setVisible(true);
    }



    /**
     * @return the pseudo of the chatuser
     * @throws RemoteException
     */
    public String getPseudo(){
        return player.getName();
    }

    public int getScore() {return player.getScore();}

    public void setScore(int score) {player.setScore(score);}

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

    public void getAlivePlayer() {
        com.sendBytes(GameEvent.GETALIVEPLAYER,player.getClient());
    }

    public void changeDirection(char l) {
        byte[] send = {(byte)l};
        com.sendBytes(GameEvent.CHANGEDIRECTION,player.getClient(),send);
    }

    public void playerReadyState(boolean readyState){

    }

    public void relaunchUpdateGameList() {
        com.sendBytes(GameEvent.RELAUNCHUPDATEGAMELIST,player.getClient());

    }
}
