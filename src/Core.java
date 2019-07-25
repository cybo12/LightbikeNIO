import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This is the core class. This class is responsible for keeping the state of the game as well
 * as asking each AI for its input.
 * @author Sam
 */
public class Core {

    public  int score = 0;																//Player score (~= number of seconds played)
    private int runTime = 0;																		//Number of "game ticks"
    private int gameRunTime = 0;																//Unused. Used to make a sync every X game ticks
    private int gameMaxRunTime = 1;															//Ibid.
    public int[] ixCarPos = new int[4];													//Position of the players on the x axis (columns; 0 = left)
    public int[] iyCarPos = new int[4];													//Position of the players on the y axis (lines  ; 0 = top)
    public boolean[] bIsHuman = new boolean[4];									//Are the players human or not?
    public char[] cCarDir = new char[4]; 								//Current player orientation; 'U'p  'D'own 'L'eft 'R'ight
    public boolean bGameInProgress[] = new boolean[4];		//Are players still in play or are they eliminated?
    public AtomicBoolean bGameQuit = new AtomicBoolean(false);										//Is the program about to be closed
    public String sWinnerName = "NOBODY";								//Current winner of the game
    public IAI[] aiOpponents = new IAI[4];											//AI for non-human players

    private int[][] iGrid = new int[100][100];									//Inner grid representation (0 = empty, any = player id)
    private int[][] iTimer = new int[100][100];									//Memorizes the "freshness" of paths, for AI to use.



    //main method called by the handling thread
    public void runGame()
    {
        //Resets everything
        newGrid();


        //Initialization
        bGameQuit.set(false);
        bGameInProgress[0] = true;
        bGameInProgress[1] = true;
        bGameInProgress[2] = true;
        bGameInProgress[3] = true;

        System.out.println("bGameInProgress: "+ Arrays.toString(bGameInProgress));
        System.out.println("bIsHuman: "+ Arrays.toString(bIsHuman));
        System.out.println("cCardir: "+ Arrays.toString(cCarDir));


        aiOpponents[0] = (IAI)new EasyAI();   //Won't be used, since player 0 is human
        aiOpponents[1] = (IAI)new EasyAI();
        aiOpponents[2] = (IAI)new EasyAI();
        aiOpponents[3] = (IAI)new EasyAI();

        //While the program hasn't been requested to quit
        while(!bGameQuit.get())
        {
            try
            {
                //Increase a game tick (one tick = 50ms; game plays at about 20fps)
                Thread.sleep(50);
                runTime++;
                gameRunTime++;
                
                //If any player is still "alive"
                if(gameRunTime%gameMaxRunTime == 0 && (bGameInProgress[0] == true || bGameInProgress[1] == true || bGameInProgress[2] == true || bGameInProgress[3] == true))
                {
                	  //unused, don't bother
                    gameMaxRunTime = 1;
                    if(gameMaxRunTime < 1)
                        gameMaxRunTime = 1;
                        
                    //Update the freshness of paths (0 = quite old path)
                    for(int i = 0; i < 100; i++)
                    {
                        for(int j = 0; j < 100; j++)
                        {
                            if(iTimer[i][j] > 0)
                                iTimer[i][j]--;
                        }
                    }
                    
                    //Update position of each player
                    for(int i = 0; i < 4; i++)
                    {

                        //If this AI is still in play
                        if(!bIsHuman[i] && bGameInProgress[i])
                        {
                            //Request a decision from the AI
                            cCarDir[i] = aiOpponents[i].getNewDir(cCarDir[i], ixCarPos[i], iyCarPos[i], iGrid, iTimer);
                        }
                        
                        //If the player is still in play
                        if(bGameInProgress[i] == true)
                        {
                            //Update its position based on the previous position and the current direction
                            //If we hit the wall or the path of any player, it's game over
                            switch(cCarDir[i])
                            {
                                case 'L' : if(ixCarPos[i] > 0 && iGrid[ixCarPos[i]-1][iyCarPos[i]] == 0)
                                {
                                    ixCarPos[i]--;
                                }
                                else
                                {
                                    bGameInProgress[i] = false;
                                }
                                    break;
                                case 'R' : if(ixCarPos[i] < 99 && iGrid[ixCarPos[i]+1][iyCarPos[i]] == 0)
                                {
                                    ixCarPos[i]++;
                                }
                                else
                                {
                                    bGameInProgress[i] = false;
                                }
                                    break;
                                case 'U' : if(iyCarPos[i] > 0 && iGrid[ixCarPos[i]][iyCarPos[i]-1] == 0)
                                {
                                    iyCarPos[i]--;
                                }
                                else
                                {
                                    bGameInProgress[i] = false;
                                }
                                    break;
                                case 'D' : if(iyCarPos[i] < 99 && iGrid[ixCarPos[i]][iyCarPos[i]+1] == 0)
                                {
                                    iyCarPos[i]++;
                                }
                                else
                                {
                                    bGameInProgress[i] = false;
                                }
                                    break;
                            }
                            
                            //This particular tile is now no longer available
                            iGrid[ixCarPos[i]][iyCarPos[i]] = (i+1);
                            
                            //This tile gets a freshness of 10
                            iTimer[ixCarPos[i]][iyCarPos[i]] = 10;


                        }
                    }
                    
                    //If "highlander" (there's only one left), this one wins, and the current game ends.
                    if(bGameInProgress[0] == true && bGameInProgress[1] == false && bGameInProgress[2] == false && bGameInProgress[3] == false)
                    {
                        sWinnerName = "RED"; bGameInProgress[0] = false;
                    }
                    if(bGameInProgress[1] == true && bGameInProgress[0] == false && bGameInProgress[2] == false && bGameInProgress[3] == false)
                    {
                        sWinnerName = "BLUE"; bGameInProgress[1] = false;
                    }
                    if(bGameInProgress[2] == true && bGameInProgress[0] == false && bGameInProgress[1] == false && bGameInProgress[3] == false)
                    {
                        sWinnerName = "YELLOW"; bGameInProgress[2] = false;
                    }
                    if(bGameInProgress[3] == true && bGameInProgress[0] == false && bGameInProgress[1] == false && bGameInProgress[2] == false)
                    {
                        sWinnerName = "GREEN"; bGameInProgress[3] = false;
                    }

                }
                
                //Update the score of the player if it's been 1 second (20 fps) since the last update
                if(runTime == 20)
                {
                    runTime = 0;
                    if(bGameInProgress[0] == true)
                        score++;
                }

            }
            catch(Exception e)
            {
                //Handle this properly
                e.printStackTrace();
            }
        }
        System.out.println("game thread close");

    }

    //Returns the representation of the grid
    public int[][] getGrid()
    {
        return iGrid;
    }

    //Resets everything
    public void newGrid()
    {
        gameRunTime = 0;
        gameMaxRunTime = 1;
        for(int i = 0; i < 100; i++)
        {
            for (int j = 0; j < 100; j++)
            {
                iGrid[i][j] = 0;
                iTimer[i][j] = 0;
            }
        }
        
        //Every player starts in the middle of a border segment
        ixCarPos[0] = 50;
        iyCarPos[0] = 99;
        cCarDir[0] = 'U';
        ixCarPos[1] = 0;
        iyCarPos[1] = 50;
        cCarDir[1] = 'R';
        ixCarPos[2] = 50;
        iyCarPos[2] = 0;
        cCarDir[2] = 'D';
        ixCarPos[3] = 99;
        iyCarPos[3] = 50;
        cCarDir[3] = 'L';
        for(int i = 0; i < 4; i++)
        {
            iGrid[ixCarPos[i]][iyCarPos[i]] = (i+1);
            iTimer[ixCarPos[i]][iyCarPos[i]] = 10;
        }
    }
    public void stop(){
        bGameQuit.set(true);
    }


    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public boolean[] getbIsHuman() {
        return bIsHuman;
    }


    public char[] getcCarDir() {
        return cCarDir;
    }

    public void setcCarDir(char[] cCarDir) {
        this.cCarDir = cCarDir;
    }

    public boolean[] getbGameInProgress() {
        return bGameInProgress;
    }

    public void setbGameInProgress(boolean[] bGameInProgress) {
        this.bGameInProgress = bGameInProgress;
    }

    public String getsWinnerName() {
        return sWinnerName;
    }

}
