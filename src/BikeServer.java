import java.io.*;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.net.Inet4Address;
import java.nio.channels.*;
import java.net.*;
import java.util.concurrent.atomic.AtomicInteger;

public class BikeServer extends UnicastRemoteObject implements IntServer {

    private static int RMIPortNum = 1099;
    private static int NIOPortNum = 1100;
    private static String serverIP;
    private static LinkedHashMap<String, Game> games = new LinkedHashMap<String, Game>();
    private static LinkedHashMap<String, Game> gamesActives = new LinkedHashMap<String, Game>();
    private static LinkedHashMap<SocketChannel, PlayerData> users = new LinkedHashMap<>();
    private static ServerSocketChannel serverSocket;
    private static Communication com = new Communication();
    private static LinkedHashMap<Integer, String> leaderboard = new LinkedHashMap<Integer, String>();
    //This is a reference to the core object, which has methods for all computations.
    //Has been made public static, so that the GUI can see it and call its methods.


    public static void main(String[] args) {
        //Launching the core thread. This thread will make all computations for the gaming logic

        try {
            BikeServer bikeServer = new BikeServer();
            try {
                Registry registry = LocateRegistry.getRegistry();
                registry.list();
            } catch (RemoteException e) {
                Registry registry = LocateRegistry.createRegistry(RMIPortNum);
            }
            String registryURL = "rmi://" + serverIP + "/myserver";
            Naming.rebind(registryURL, bikeServer);
            System.out.println("Server ready at address: " + serverIP + "/myserver");
            bikeServer.lauchNIOServer();

        } catch (Exception e) {
            System.err.println("Server exception:");
            e.printStackTrace();

        }
    }

    private BikeServer() throws IOException {
        super();
        try {
            serverIP = Inet4Address.getLocalHost().getHostAddress();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private  void lauchNIOServer()throws IOException{
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

            //System.out.println("Waiting for select...");
            int noOfKeys = selector.select();

            //System.out.println("Number of selected keys: " + noOfKeys);

            Set selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> iter = selectedKeys.iterator();

            while (iter.hasNext()) {
                //System.out.println("loop");
                SelectionKey ky = iter.next();

                if (ky.isAcceptable()) {

                    // Accept the new client connection
                    SocketChannel client = serverSocket.accept();
                    client.configureBlocking(false);

                    // Add the new connection to the selector
                    client.register(selector, SelectionKey.OP_READ);
                    System.out.println("Accepted new connection from client: " + client);
                } else if (ky.isReadable()) {
                    //System.out.println("read");
                    // Read the data from client

                    SocketChannel client = (SocketChannel) ky.channel();
                    byte[] header = com.readFully(3, client);
                    int payloadLength = header[2];
                    int event = header[1];
                    byte[] payload = {0};
                    if (payloadLength != 0) {
                        payload = com.readFully(payloadLength, client);
                    }
                    treatEvent(event, payload, client);

                } else if (ky.isWritable()) {
                    ky.interestOps(SelectionKey.OP_READ);
                }

                iter.remove();

            } // end while loop

        } // end for loop
    }


    private void treatEvent(int event, byte[] payload, SocketChannel client) throws UnsupportedEncodingException {
        if (event != 20 && event != 11 && event !=18) {
            System.out.println("Message read from  " + event);
        }
        switch (event) {
            case GameEvent.HELLO:
                System.out.println("hello ");
                break;
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
                System.out.println(event);
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
                        System.out.println(getNames(games));
                        com.sendBytes(GameEvent.UPDATEGAMELIST,key,com.arraylistToBytes(getNames(games)));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
            Thread.sleep(1000);
        }
    }

    public void checkFinishGame(){
        gamesActives.forEach((k,v) ->{
        if (v.getGameStarted() == 2){
            ArrayList<PlayerData> scores = v.getPlayersData();
            scores.forEach(playerData -> {
                if(!leaderboard.isEmpty()) {
                    AtomicInteger min = new AtomicInteger();
                    leaderboard.forEach((key, value) -> {
                        if (min.get() < playerData.getScore()) {
                            min.set(playerData.getScore());
                        }
                    });
                    if (playerData.getScore() > min.get()) {
                        if (leaderboard.size() <= 10) {
                            leaderboard.remove(leaderboard.remove(min));
                        }
                    }
                }
                    leaderboard.put(playerData.getScore(),playerData.getPseudo());
                    System.out.println("leadeboard : "+leaderboard);

            });
            System.out.println("remove game : "+ v);
            gamesActives.remove(v);
        }
        });
    }
    public void connect(SocketChannel client, String name) {
        PlayerData player = new PlayerData(client);
        System.out.println(player);
        player.setPseudo(name);
        users.put(client, player);
        try {
            com.sendBytes(GameEvent.UPDATEGAMELIST,client,com.arraylistToBytes(getNames(games)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("player connected: " + player.getPseudo());
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
        String pseudo = users.get(client).getPseudo();
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
            com.sendBytes(GameEvent.UPDATEGAMELIST,client,com.arraylistToBytes(getNames(games)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("gameNames list:" + getNames(games));
    }


    public void createGame(byte[] payload) {
        String gameName = com.getString(payload);
        games.put(gameName, new Game(gameName));
        System.out.println(games);
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
        System.out.print("players in game:");
        System.out.println(game.getPlayersName());
        if (!check) {
            System.out.println(player.getPseudo() + " try to join game complete");
        } else {
            System.out.println(game.getPlayersName());
            game.getPlayers().forEach((n) -> {
                try {
                    System.out.println(game);
                    if(game.getPlayersName() != null) {
                        com.sendBytes(GameEvent.UPDATEPLAYERSINGAMEWAITING, n, com.arraylistToBytes(game.getPlayersName()));
                    }else{
                        com.sendBytes(GameEvent.UPDATEPLAYERSINGAMEWAITING, n);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            System.out.println("player " + player.getPseudo() + " join game: " + gameName);
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
        int score = gamesActives.get(users.get(client).getGamename()).getcCore().getScore();
        byte[] data = new byte[] {
                (byte)(score >>> 24),
                (byte)(score >>> 16),
                (byte)(score >>> 8),
                (byte)score};
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


    @Override
    public LinkedHashMap<Integer, String> getLeaderbord() throws RemoteException {
        this.checkFinishGame();
        return leaderboard;
    }
}
