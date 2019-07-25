import java.util.ArrayList;
import java.net.Inet4Address;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.Semaphore;


public class BikeServer extends UnicastRemoteObject implements IntServer {

    private static int RMIPortNum = 1099;
    private static String serverIP;
    private static LinkedHashMap<String,Game> games = new LinkedHashMap<String, Game>();
    private static LinkedHashMap<String,Game> gamesActives = new LinkedHashMap<String, Game>();
    private static LinkedHashMap<String, IntBikeUser> users = new LinkedHashMap<String, IntBikeUser>();
    //This is a reference to the core object, which has methods for all computations.
    //Has been made public static, so that the GUI can see it and call its methods.

    private BikeServer() throws RemoteException {
    super();
    try {
        serverIP = Inet4Address.getLocalHost().getHostAddress();
    } catch (Exception e) {
        e.printStackTrace();
    }
}


    public static void main(String[] args){
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
        } catch (Exception e) {
            System.err.println("Server exception:");
            e.printStackTrace();

        }
    }

    private void checkGame(String gameName) throws InterruptedException {
            Game game = games.get(gameName);
            boolean flag = true;
            while (flag) {
                if (game.getGameStarted() == 1) {
                    System.out.println("game " + gameName + " started");
                    flag = false;
                    gamesActives.put(gameName,game);
                    games.remove(gameName,game);
                    users.forEach((key, value) -> {
                        try {
                            value.updateGameList(getNames(games));
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    });
                }
                Thread.sleep(1000);
            }
    }

    /*
    * JAVADOC is on the interface IntServer.
    */

    public void Connect(IntBikeUser bikeUser) throws RemoteException{
        users.put(bikeUser.getPseudo(),bikeUser);
        bikeUser.updateGameList(getNames(games));
        System.out.println("player connected: "+bikeUser.getPseudo());
    }


    public ArrayList<String> getPseudosInUse() {
        ArrayList<String> names = getNames(users);
        System.out.println("pseudo lists: "+names);
        return names;
    }



    public void removeUser(IntBikeUser bikeUser) throws RemoteException{
        users.remove(bikeUser.getPseudo());
        removeUserFromGames(games,bikeUser);
        removeUserFromGames(gamesActives,bikeUser);

        System.out.println("remove user:"+bikeUser.getPseudo());
    }

    public void removeUserFromGames(HashMap<String,Game> games,IntBikeUser bikeUser){
        games.forEach((k,v)->{
            if(v.getPlayers().contains(bikeUser)) {
                try {
                    v.removePlayer(bikeUser);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                v.getPlayers().forEach((n)-> {
                    try {
                        n.updatePlayersInGameWaiting(v.getPlayersName());
                    } catch (RemoteException e) {
                        System.out.println("il n'est plus au bout du fil");
                    }
                });
            }
        });
    }

   public ArrayList<String> getGameNames() throws RemoteException{
       ArrayList<String> names = getNames(games);
       System.out.println("gameNames list:"+names);
       return names;
   }


    public void createGame(String gameName) throws RemoteException{
       games.put(gameName,new Game(gameName));
       users.forEach((k,v)-> {
           try {
               v.updateGameList(getNames(games));
           } catch (RemoteException e) {
               e.printStackTrace();
           }
       });
       new Thread(()-> {
            try {
                checkGame(gameName);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
        System.out.println("game created: "+gameName);
    }


    public void joiningWaitingGame(String bikeUsername, String gameName) throws RemoteException{
        IntBikeUser bikeUser = users.get(bikeUsername);
        Game game = games.get(gameName);
        boolean check = false;
        try {
            check = game.addPlayer(bikeUser);
        }catch (Exception e){
            e.printStackTrace();
        }
        if(!check){
            System.out.println(bikeUsername+" try to join game complete");
        }
        else{
            System.out.println(game.getPlayersName());
            game.getPlayers().forEach((n)-> {
                try {
                    n.updatePlayersInGameWaiting(game.getPlayersName());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            });
            System.out.println("player "+bikeUsername +" join game: "+ gameName);
        }
    }


    public void playerReadyState(String bikeUsername, String gameName, Boolean readyState) throws RemoteException, InterruptedException {
        games.get(gameName).setPlayerReady(readyState);
        System.out.println("player "+ bikeUsername +" readiness is "+readyState);

    }


    public int getPlayerScore(String gameName) throws RemoteException{
        return gamesActives.get(gameName).getcCore().getScore();
    }


    public void changeDirection(String bikeUsername, String gameName, char carDirection) throws RemoteException{
        gamesActives.get(gameName).setDirection(bikeUsername,carDirection);
    }

    public boolean getAlivePlayer(String gameName,String playerName) throws RemoteException {
        int id = getPlayerId(gameName,playerName);
        boolean response = false;
        if(id != 99) {
            try {
                response = gamesActives.get(gameName).getcCore().getbGameInProgress()[id];
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("mais tu n'existes plus dans le jeu n'essaye pas");
            }
        }
        return  response;
    }

    public void relaunchUpdateGameList(String bikeUsername) throws RemoteException {
        IntBikeUser bikeUser = users.get(bikeUsername);
        bikeUser.updateGameList(getNames(games));
    }

    public int getPlayerId(String gameName, String pseudo) throws RemoteException {
       return gamesActives.get(gameName).getPlayerID(pseudo);
    }


    private ArrayList<String> getNames(HashMap hash){
        ArrayList<String> names = new ArrayList<>();
        hash.forEach((key,value)-> names.add(key.toString()));
        return names;
    }
}
