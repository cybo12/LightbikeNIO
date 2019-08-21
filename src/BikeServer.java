import java.io.*;
import java.util.*;
import java.net.Inet4Address;
import java.nio.*;
import java.nio.channels.*;
import java.net.*;

public class BikeServer {

    private static int RMIPortNum = 1099;
    private static int NIOPortNum = 1100;
    private static String serverIP;
    private static LinkedHashMap<String, Game> games = new LinkedHashMap<String, Game>();
    private static LinkedHashMap<String, Game> gamesActives = new LinkedHashMap<String, Game>();
    private static LinkedHashMap<SocketChannel, PlayerData> users = new LinkedHashMap<>();
    private static ServerSocketChannel serverSocket;
    private static Communication com;
    //This is a reference to the core object, which has methods for all computations.
    //Has been made public static, so that the GUI can see it and call its methods.


    public static void main(String[] args) {
        //Launching the core thread. This thread will make all computations for the gaming logic

        try {
            BikeServer bikeServer = new BikeServer();

            System.out.println("Server ready at address: " + serverIP + "/myserver");
        } catch (Exception e) {
            System.err.println("Server exception:");
            e.printStackTrace();

        }
    }

    private BikeServer() throws IOException {
        try {
            serverIP = Inet4Address.getLocalHost().getHostAddress();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Get selector
        Selector selector = Selector.open();

        System.out.println("Selector open: " + selector.isOpen());

        // Get server socket channel and register with selector
        serverSocket = ServerSocketChannel.open();
        InetSocketAddress hostAddress = new InetSocketAddress(serverIP, NIOPortNum);
        serverSocket.bind(hostAddress);
        serverSocket.configureBlocking(false);
        int ops = serverSocket.validOps();
        SelectionKey selectKy = serverSocket.register(selector, ops, null);

        while (true) {

            System.out.println("Waiting for select...");
            int noOfKeys = selector.select();

            System.out.println("Number of selected keys: " + noOfKeys);

            Set selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> iter = selectedKeys.iterator();

            while (iter.hasNext()) {

                SelectionKey ky = iter.next();

                if (ky.isAcceptable()) {

                    // Accept the new client connection
                    SocketChannel client = serverSocket.accept();
                    client.configureBlocking(false);

                    // Add the new connection to the selector
                    client.register(selector, SelectionKey.OP_READ);
                    System.out.println("Accepted new connection from client: " + client);
                } else if (ky.isReadable()) {

                    // Read the data from client

                    SocketChannel client = (SocketChannel) ky.channel();
                    ByteBuffer buffer = ByteBuffer.allocate(3);
                    client.read(buffer);
                    byte[] header = com.readFully(3, client);
                    int payloadLength = header[2];
                    int event = header[1];
                    byte[] payload = com.readFully(payloadLength, client);
                    treathEvent(event, payload, client);
                    System.out.println("Message read from client: " + header);

                }

                iter.remove();

            } // end while loop

        } // end for loop
    }
    /*
    HEHRHEHEHEHRHE
    EGRHEHEHEHE
    EHHEHEHEHE
     */

    private void treathEvent(int event, byte[] payload, SocketChannel client) throws UnsupportedEncodingException {
        switch (event) {
            case GameEvent.CONNECT:
                this.connect(client,com.getString((payload)));
                break;
            case GameEvent.REMOVEUSER:
                this.removeUser(client);
            case GameEvent.RELAUNCHUPDATEGAMELIST:
                this.relaunchUpdateGameList(client);
            default:
                System.out.println("c'est la merde");

        }
    }

    /*
     * JAVADOC is on the interface IntServer.
     */
    private void checkGame(String gameName) throws InterruptedException {
        Game game = games.get(gameName);
        boolean flag = true;
        while (flag) {
            if (game.getGameStarted() == 1) {
                System.out.println("game " + gameName + " started");
                flag = false;
                gamesActives.put(gameName, game);
                games.remove(gameName, game);
                users.forEach((key, value) -> {
                    try {
                        com.sendBytes(GameEvent.UPDATEGAMELIST,key,com.arraylistToBytes(getNames(games)));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
            Thread.sleep(1000);
        }
    }

    public void connect(SocketChannel client, String name) {
        PlayerData player = new PlayerData(client, name);
        player.setClient(client);
        users.put(client, player);
        try {
            byte[] toSend =  com.arraylistToBytes(getNames(games));
            com.sendBytes(1,client,toSend);
            com.sendBytes(GameEvent.UPDATEGAMELIST,client,com.arraylistToBytes(getNames(games)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("player connected: " + player.getName());
    }


    public ArrayList<String> getPseudosInUse() {
        ArrayList<String> names = getNames(users);
        System.out.println("pseudo lists: " + names);
        return names;
    }


    public void removeUser(SocketChannel client){
        String pseudo = users.get(client).getName();
        users.remove(client);
        removeUserFromGames(games, client);
        removeUserFromGames(gamesActives, client);

        System.out.println("remove user:" + pseudo);
    }

    public void removeUserFromGames(HashMap<String, Game> games, SocketChannel bikeUser) {
        games.forEach((k, v) -> {
            if (v.getPlayers().contains(bikeUser)) {
                    v.removePlayer(bikeUser);
                v.getPlayers().forEach((n) -> {
                    try {
                        n.updatePlayersInGameWaiting(v.getPlayersName());
                    } catch (RemoteException e) {
                        System.out.println("il n'est plus au bout du fil");
                    }
                });
            }
        });
    }

    public ArrayList<String> getGameNames() {
        ArrayList<String> names = getNames(games);
        System.out.println("gameNames list:" + names);
        return names;
    }


    public void createGame(String gameName) {
        games.put(gameName, new Game(gameName));
        users.forEach((k, v) -> {
            try {
                v.updateGameList(getNames(games));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });
        new Thread(() -> {
            try {
                checkGame(gameName);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
        System.out.println("game created: " + gameName);
    }


    public void joiningWaitingGame(String bikeUsername, String gameName){
        SocketChannel bikeUser = users.get(bikeUsername);
        Game game = games.get(gameName);
        boolean check = false;
        try {
            check = game.addPlayer(bikeUser);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!check) {
            System.out.println(bikeUsername + " try to join game complete");
        } else {
            System.out.println(game.getPlayersName());
            game.getPlayers().forEach((n) -> {
                try {
                    n.updatePlayersInGameWaiting(game.getPlayersName());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            });
            System.out.println("player " + bikeUsername + " join game: " + gameName);
        }
    }


    public void playerReadyState(String bikeUsername, String gameName, Boolean readyState) throws InterruptedException {
        games.get(gameName).setPlayerReady(readyState);
        System.out.println("player " + bikeUsername + " readiness is " + readyState);

    }


    public int getPlayerScore(String gameName)  {
        return gamesActives.get(gameName).getcCore().getScore();
    }


    public void changeDirection(String bikeUsername, String gameName, char carDirection) {
        gamesActives.get(gameName).setDirection(bikeUsername, carDirection);
    }

    public boolean getAlivePlayer(String gameName, String playerName) {
        int id = getPlayerId(gameName, playerName);
        boolean response = false;
        if (id != 99) {
            try {
                response = gamesActives.get(gameName).getcCore().getbGameInProgress()[id];
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("mais tu n'existes plus dans le jeu n'essaye pas");
            }
        }
        return response;
    }

    public void relaunchUpdateGameList(SocketChannel client){
        try {
            com.sendBytes(GameEvent.RELAUNCHUPDATEGAMELIST,client,com.arraylistToBytes(getNames(games)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getPlayerId(String gameName, String pseudo) {
        return gamesActives.get(gameName).getPlayerID(pseudo);
    }


    private ArrayList<String> getNames(HashMap hash) {
        ArrayList<String> names = new ArrayList<>();
        hash.forEach((key, value) -> names.add(key.toString()));
        return names;
    }


}
