import java.nio.channels.SocketChannel;

/**
 * Just to structure player's data.
 */
public class PlayerData {
    private  int id;
    private SocketChannel client;
    private  String name;
    private  int score = 0;
    private String gamename;
    private String pseudo;

    public PlayerData(SocketChannel client ,String pseudo) {
        this.client = client;
        this.pseudo = pseudo;
    }
    public PlayerData(SocketChannel client) {
        this.client = client;
    }
    @Override
    public String toString() {
        return "PlayerData: "+id+" name :"+name ;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public SocketChannel getClient() {
        return client;
    }

    public void setClient(SocketChannel client) {
        this.client = client;
    }

    public String getName() {
        return name;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void setName(String pseudo) { this.name = pseudo;
    }

    public String getGamename() {
        return gamename;
    }

    public void setGamename(String gamename) {
        this.gamename = gamename;
    }

    public String getPseudo() {
        return pseudo;
    }

    public void setPseudo(String pseudo) {
        this.pseudo = pseudo;
    }
}

