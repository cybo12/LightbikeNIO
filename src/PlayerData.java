/**
 * Just to structure player's data.
 */
public class PlayerData {
    private  int id;
    private  IntBikeUser intBikeUser;
    private  String name;
    private  int score = 0;

    public PlayerData(int id, IntBikeUser intBikeUser,String name) {
        this.id = id;
        this.intBikeUser = intBikeUser;
        this.name = name;
    }

    @Override
    public String toString() {
        return "PlayerData: "+id+" name :"+name ;
    }

    public int getId() {
        return id;
    }


    public IntBikeUser getIntBikeUser() {
        return intBikeUser;
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


}

