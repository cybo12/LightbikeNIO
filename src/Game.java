import java.lang.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Semaphore;


public class Game implements java.io.Serializable {
    private String name;
    private Core cCore;
    private int gameStarted = 0;
    private int maxPlayers = 4;
    private Semaphore sem = new Semaphore(1);
    private int playersReady = 0;
    private ArrayList<PlayerData> players = new ArrayList<PlayerData>();
    private int playerID = 0;


    public Game(String name){
        cCore = new Core(this);
        this.name = name;
    }

    /**
     * readiness's player gestion. When they are all ready it launch the game.
     * @param choice
     * @throws InterruptedException
     */
    public void setPlayerReady(boolean choice) throws InterruptedException {
      if(choice) {
          playersReady += 1;
          System.out.println("players size: "+players.size());
          System.out.println("players ready: "+playersReady);
          if(players.size() == playersReady){
              for(int x=0;x<maxPlayers;x++){
                if(x<players.size()) {
                    players.get(x).setId(x);
                    cCore.getbIsHuman()[x] = true;
                }else{
                    cCore.getbIsHuman()[x] = false;
                }
              }
              //LAUNCH TIME
              players.forEach((n)-> {
                  try {
                      n.getIntBikeUser().startGame(getPlayersName(),name);
                  } catch (RemoteException e) {
                      e.printStackTrace();
                  }
              });
              System.out.println("send preparing game start");
              gameStarted = 1;
              Thread.sleep(2000);
              players.forEach((n)-> {
                  try {
                      n.getIntBikeUser().startGameGrid();
                  } catch (RemoteException e) {
                      e.printStackTrace();
                  }
              });
              System.out.println("send game start");
              Thread gameThread = new Thread(() -> cCore.runGame());
              gameThread.start();
              System.out.println("core run");
              Thread checkThread = new Thread(() -> {
                  try {
                      checkThread();
                  } catch (Exception e) {
                      e.printStackTrace();
                  }
              });
              checkThread.start();
              System.out.println("thread started");
          }
      }
      else{
          playersReady -= 1;
      }
    }

    /**
     * Add player to a game.
     * @param user
     * @return
     * @throws RemoteException
     */
    public boolean addPlayer(IntBikeUser user) throws RemoteException {
        if(players.size()<maxPlayers && gameStarted == 0) {
            players.add(new PlayerData(user,user.getPseudo()));
            return true;
        }
        else{
            System.out.println("max players nb in this game: "+ players.size());
            return false;
        }
    }


    /**
     * Get the Core of a game.
     * @return
     */
    public Core getcCore() {
        return cCore;
    }

    /**
     * get a arraylist of core's players
     * @return
     */
    public ArrayList<IntBikeUser> getPlayers() {
        ArrayList<IntBikeUser> intBikeUsers = new ArrayList<>();
        for(PlayerData p :this.players){
            intBikeUsers.add(p.getIntBikeUser());
        }
        return intBikeUsers;
    }

    /**
     * Take the time to correctly delete à player from a game. Launch by BikeServer.removeUser
     * @param intBikeUser
     */
    public void removePlayer(IntBikeUser intBikeUser) throws RemoteException {
        for(int x=0;x<players.size();x++){
            PlayerData p = players.get(x);
            if(p.getIntBikeUser().getPseudo().equals(intBikeUser.getPseudo())){
                System.out.println("game "+ name +" remove player: "+p.getName());
                cCore.getbGameInProgress()[p.getId()]=false;
                players.remove(p);
            }
        }

    }

    /**
     * Take all position's input from player in a game and after verifications modify it in the core.
     * @param bikeUsername
     * @param carDirection
     */
    public void setDirection(String bikeUsername, char carDirection) {
        ArrayList<Character> checkDirections = new ArrayList<>(Arrays.asList('U','L','D','R'));
        //System.out.println("checkDirections :"+checkDirections);
        char[] directions = cCore.getcCarDir();
        for(PlayerData p : players){
            if(p.getName().equals(bikeUsername)){
                char initialDirection = directions[p.getId()];
                int x = Math.abs(checkDirections.indexOf(initialDirection)-checkDirections.indexOf(carDirection));
                //System.out.println("direction actuelle : "+initialDirection);
                //System.out.println("direction voulue:" + carDirection);
                //System.out.println("direction x: "+x);
                if(x==1 || x==3) {
                    cCore.setcCarDir(p.getId(),carDirection);
                }
            }
        }
    }

    /**
     * Get a arraylist of players name
     * @return
     */
    public ArrayList<String> getPlayersName(){
        ArrayList<String> names = new ArrayList<String>();
        players.forEach((n)-> names.add(n.getName()));
        return names;
    }


    /**
     * Get the ID of a player whand you give his name
     * @param name
     * @return
     */
    public int getPlayerID(String name){
        int returnId = 99;
        for(PlayerData p : players){
            if (p.getName().equals(name)) {
                returnId = p.getId();
            }
        }
        return returnId;
    }

    /**
     * Thread who check who is alive for blocking input from dead players and when you have to end the game properly.
     * @throws InterruptedException
     */
    private void checkThread() throws InterruptedException, RemoteException {
        boolean flag = true;
        boolean first = false;
        while (flag) {
            Thread.sleep(50);
            //System.out.println(players.get(0).getName()+" est en x: "
            // +cCore.getIxCarPos()[players.get(0).getId()]+" y: "+cCore.getIyCarPos()[players.get(0).getId()]);
            for(int x=0;x<maxPlayers;x++){
                if(x<players.size()){
                    PlayerData p = players.get(x);
                    if(!cCore.getbGameInProgress()[p.getId()]){
                        if(p.getScore()==0) {
                            p.setScore(cCore.getScore());
                            System.out.println("end game for: " + p.getName());
                        }
                        boolean[] check = cCore.getbGameInProgress();
                        int nb = maxPlayers;
                        for (boolean b : check) {
                            if (!b) {
                                nb--;
                            }
                        }
                        //System.out.println("nb restants :"+nb);
                        if (nb == 0) {
                            if(!first) {
                                first = true;
                                new Thread(() -> endgame(cCore.getsWinnerName())).start();
                                System.out.println("game " + name + " is over.");
                                cCore.stop();
                                flag = false;
                                gameStarted = 2;
                                playerID = 0;
                                playersReady = 0;
                                System.out.println("endgame close");
                            }
                        }

                    }
                }
            }


        }
        System.out.println("checkThread close");
    }

    /**
     * for delete game from server
     * @return
     */
    public int getGameStarted() {
        return gameStarted;
    }


    public void updateGameGrid(int[] change) throws RemoteException {
        for(PlayerData p : players){
                p.getIntBikeUser().updateGameGrid(change);
        }

    }
    /**
     * launch by the Check Thread for reset the room and notify all players.
     * @param sWinnerName
     */
    private void endgame(String sWinnerName) {
        players.forEach(n -> {
            //System.out.println("endgame");
            //System.out.println(n.getName());
            try {
                n.getIntBikeUser().endGame(sWinnerName,n.getScore());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });
    }
}
