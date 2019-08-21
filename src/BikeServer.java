import java.io.*;
import java.rmi.RemoteException;
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
                break;
            case GameEvent.RELAUNCHUPDATEGAMELIST:
                this.relaunchUpdateGameList(client);
                break;
            case GameEvent.CHANGEDIRECTION:
                this.changeDirection(client,payload[0]);
                break;
            case GameEvent.GETALIVEPLAYER:
                this.getAlivePlayer(client);
                break;
            case GameEvent.GETPSEUDOSINUSE:
                this.getPseudosInUse(client);
                break;
            case GameEvent.JOININGWAITINGGAME:
                this.joiningWaitingGame(client,com.getString(payload));
                break;
            case GameEvent.GETGAMENAMES:
                this.getGameNames(client);
                break;
            case GameEvent.CREATEGAME:
                this.createGame(payload);
                break;
            case GameEvent.PLAYERREADYSTATE:
                this.playerReadyState(client,payload[0]!=0);
                break;
                case GameEvent.GETPLAYERSCORE:
                    this.getPlayerScore(client);
                    break;
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


    public void getPseudosInUse(SocketChannel client) {
        ArrayList<String> names = getNames(users);
        System.out.println("pseudo lists: " + names);
        try {
            com.sendBytes(GameEvent.GETPSEUDOSINUSE,client,com.arraylistToBytes(names));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void removeUser(SocketChannel client){
        String pseudo = users.get(client).getName();
        users.remove(client);
        removeUserFromGames(games, client);
        removeUserFromGames(gamesActives, client);
        try {
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("remove user:" + pseudo);
    }

    public void removeUserFromGames(HashMap<String, Game> games, SocketChannel bikeUser) {
        users.get(bikeUser).setGamename("");
        games.forEach((k, v) -> {
            if (v.getPlayers().contains(bikeUser)) {
                    v.removePlayer(bikeUser);
                v.getPlayers().forEach((n) -> {
                    try {
                        com.sendBytes(GameEvent.UPDATEPLAYERSINGAMEWAITING,n,com.arraylistToBytes(v.getPlayersName()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        });
    }

    public void getGameNames(SocketChannel client) {
        try {
            com.sendBytes(GameEvent.GETGAMENAMES,client,com.arraylistToBytes(getNames(games)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("gameNames list:" + getNames(games));
    }


    public void createGame(byte[] payload) {
        String gameName = com.getString(payload);
        games.put(gameName, new Game(gameName));
        users.forEach((k,v) -> {
            try {
                com.sendBytes(GameEvent.UPDATEGAMELIST,k,com.arraylistToBytes(getNames(games)));
            } catch (IOException e) {
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


    public void joiningWaitingGame(SocketChannel client,String gameName){
        Game game = games.get(gameName);
        PlayerData player = users.get(client);
        player.setGamename(gameName);
        boolean check = false;
            check = game.addPlayer(player);
        if (!check) {
            System.out.println(player.getName() + " try to join game complete");
        } else {
            System.out.println(game.getPlayersName());
            game.getPlayers().forEach((n) -> {
                try {
                    com.sendBytes(GameEvent.UPDATEPLAYERSINGAMEWAITING,n,com.arraylistToBytes(game.getPlayersName()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            System.out.println("player " + player.getName() + " join game: " + gameName);
        }
    }


    public void playerReadyState(SocketChannel client,Boolean readyState){
        try {
            games.get(users.get(client).getGamename()).setPlayerReady(readyState);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("player " + client.toString() + " readiness is " + readyState);

    }


    public void getPlayerScore(SocketChannel client)  {
        short score = gamesActives.get(users.get(client).getGamename()).getcCore().getScore();
        byte[] data = new byte[5];
        data[0] = (byte) (score & 0xFF);
        data[1] = (byte) ((score >> 8) & 0xFF);
        com.sendBytes(GameEvent.GETPLAYERSCORE,client,data);
    }


    public void changeDirection(SocketChannel client, byte carDirection) {
        String gameName = users.get(client).getGamename();
        gamesActives.get(gameName).setDirection(client,(char) carDirection);
    }

    public void getAlivePlayer(SocketChannel client) {
        String gameName = users.get(client).getGamename();
        int id = gamesActives.get(gameName).getPlayerID(client);
        boolean bool = false;
        if (id != 99) {
            try {
                bool = gamesActives.get(gameName).getcCore().getbGameInProgress()[id];
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("mais tu n'existes plus dans le jeu n'essaye pas");
            }
        }
        byte[] response = new byte[1];
        response[0] = (byte)(bool? 1 : 0 );
        com.sendBytes(GameEvent.GETALIVEPLAYER,client,response);
    }

    public void relaunchUpdateGameList(SocketChannel client){
        try {
            com.sendBytes(GameEvent.RELAUNCHUPDATEGAMELIST,client,com.arraylistToBytes(getNames(games)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private ArrayList<String> getNames(HashMap hash) {
        ArrayList<String> names = new ArrayList<>();
        hash.forEach((key, value) -> names.add(key.toString()));
        return names;
    }


}
