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
    private Communication com = new Communication();
    private static int NIOPortNum = 1100;


    public BikeUser(String ipAddress) {
        connectToServer( ipAddress, NIOPortNum);
    }

    public void connectToServer(String address, int port )
    {
        //open a TCP connection to the server
        try
        {
            player = new PlayerData(SocketChannel.open(new InetSocketAddress(address, port)));
            com.sendBytes(GameEvent.HELLO,player.getClient());
            System.out.print("my client :");
            System.out.println(player.getClient());
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
                System.exit(0);
            }
        }
    }
    private void treathEvent(int event, byte[] payload, SocketChannel client) throws IOException {
        if (event != 20 && event != 11 && event !=18) {
            System.out.print("from server event:");
            System.out.println(event);
        }
        switch(event){
            case GameEvent.GETPSEUDOSINUSE:
                prompt.setInUse(com.bytesToArraylist(payload));
                break;
            case GameEvent.GETALIVEPLAYER:
                lightBikesTronGUI.setAlive(payload[0]);
                break;
            case GameEvent.GETPLAYERSCORE:
                this.setScore(payload);
                break;
            case GameEvent.UPDATEGAMEGRID:
                lightBikesTronGUI.update(payload);
                break;
            case GameEvent.STARTGAMEGRID:
                lightBikesTronGUI.startGameGrid();
                break;
            case GameEvent.ENDGAME:
                lightBikesTronGUI.ending(com.getString(payload));
                player.setGamename("");
            case GameEvent.UPDATEGAMELIST:
                this.updateGameList(payload);
                break;
            case GameEvent.STARTGAME:
                this.startGame(com.getString(payload));
                break;
            case GameEvent.UPDATEPLAYERSINGAMEWAITING:
                this.updatePlayersInGameWaiting(com.bytesToArraylist(payload));
                break;
            case GameEvent.RELAUNCHUPDATEGAMELIST:
                this.updateGameList(payload);
                break;
            default:
                System.out.println("c'est la merde");
                System.out.println(event);
                System.out.println(payload);
        }
    }


    public void connect(){
        com.sendBytes(GameEvent.CONNECT,player.getClient(),this.getPseudo().getBytes());
    }

    public void createWaitingRoom(){
        bikeWaitingRoom = new BikeWaitingRoom(this);
    }

    public void updateGameList(byte[] payload){
        DefaultListModel modelGameList = null;
        try {
            ArrayList<String> load = com.bytesToArraylist(payload);
            if (load !=null) {
                modelGameList = convertToModel(com.bytesToArraylist(payload));
                bikeWaitingRoom.updateGameListGUI(modelGameList);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        return player.getPseudo();
    }

    public int getScore() {
        com.sendBytes(GameEvent.GETPLAYERSCORE,player.getClient());
        return  player.getScore();
    }

    public void setScore(byte[] data) {
        int score = (data[0] << 24)
                + ((data[1] & 0xFF) << 16)
                + ((data[2] & 0xFF) << 8)
                + (data[3] & 0xFF);
        player.setScore(score);}

    public void setPseudo(String pseudo) {
        player.setPseudo(pseudo);
    }

    public void getPseudoInUse() {
        com.sendBytes(GameEvent.GETPSEUDOSINUSE,player.getClient());
    }

    public void removeUser() {
        com.sendBytes(GameEvent.REMOVEUSER,player.getClient());
        try {
            player.getClient().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        byte[] send = {(byte) (readyState?1:0)};
        com.sendBytes(GameEvent.PLAYERREADYSTATE,player.getClient(),send);
    }

    public void relaunchUpdateGameList() {
        com.sendBytes(GameEvent.RELAUNCHUPDATEGAMELIST,player.getClient());

    }

    public void joiningWaitingGame(String selectedGameName) {
        com.sendBytes(GameEvent.JOININGWAITINGGAME,player.getClient(),selectedGameName.getBytes());
        player.setGamename(selectedGameName);
    }


    public static void main(String[] args) throws UnknownHostException {
        prompt = new BikeUserCredGUI();
    }
}
