import java.lang.*;
import java.nio.channels.SocketChannel;
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
    private Communication com;


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
              players.forEach((n)-> com.sendBytes(GameEvent.STARTGAME,n.getClient(),name.getBytes()));
              System.out.println("send preparing game start");
              gameStarted = 1;
              Thread.sleep(2000);
              players.forEach((n)-> com.sendBytes(GameEvent.STARTGAMEGRID,n.getClient()));
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


    public boolean addPlayer(PlayerData player){
        if(players.size()<maxPlayers && gameStarted == 0) {
            players.add(player);
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
    public ArrayList<SocketChannel> getPlayers() {
        ArrayList<SocketChannel> intBikeUsers = new ArrayList<>();
        for(PlayerData p :this.players){
            intBikeUsers.add(p.getClient());
        }
        return intBikeUsers;
    }

    /**
     * Take the time to correctly delete à player from a game. Launch by BikeServer.removeUser
     * @param intBikeUser
     */
    public void removePlayer(SocketChannel intBikeUser){
        for(int x=0;x<players.size();x++){
            PlayerData p = players.get(x);
            if(p.getClient().equals(intBikeUser)){
                System.out.println("game "+ name +" remove player: "+p.getName());
                cCore.getbGameInProgress()[p.getId()]=false;
                players.remove(p);
            }
        }

    }

    /**
     * Take all position's input from player in a game and after verifications modify it in the core.
     * @param client
     * @param carDirection
     */
    public void setDirection(SocketChannel client, char carDirection) {
        ArrayList<Character> checkDirections = new ArrayList<>(Arrays.asList('U','L','D','R'));
        //System.out.println("checkDirections :"+checkDirections);
        char[] directions = cCore.getcCarDir();
        for(PlayerData p : players){
            if(p.getClient().equals(client)){
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
     * Get the ID of a player whand you give his SocketChannel
     * @param client
     * @return
     */
    public int getPlayerID(SocketChannel client){
        int returnId = 99;
        for(PlayerData p : players){
            if (p.getClient().equals(client)) {
                returnId = p.getId();
            }
        }
        return returnId;
    }

    /**
     * Thread who check who is alive for blocking input from dead players and when you have to end the game properly.
     * @throws InterruptedException
     */
    private void checkThread() throws InterruptedException {
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


    public void updateGameGrid(int[] change){
        byte[] data = new byte[5];
        data[0] = (byte) (change[0] & 0xFF);
        data[1] = (byte) ((change[0] >> 8) & 0xFF);
        data[2] = (byte) (change[1] & 0xFF);
        data[3] = (byte) ((change[2] >> 8) & 0xFF);
        data[4] = (byte) change[3];
        for(PlayerData p : players){
            com.sendBytes(GameEvent.UPDATEGAMEGRID,p.getClient(),data);
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
            com.sendBytes(GameEvent.ENDGAME,n.getClient(),sWinnerName.getBytes());
        });
    }
}
