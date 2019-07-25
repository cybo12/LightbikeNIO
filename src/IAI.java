/**
 * This is the interface, shared by all IA, which has only one method to be called to get a decision
 * @author Sam
 */
public interface IAI {
    
    /**
      * Method to be implemented
      * @param dir the current direction of the player (0=Up, 1=Right, 2=Down, 3=Left)
      * @param x the x position of the player on the grid (column; 0 = left)
      * @param y the y position of the player on the grid (line; 0 = top)
      * @param grid a copy of the current grid representation
      * @param timer a copy of the current freshness information
      * @return a character as a movement decision ('U', 'R', 'L', 'D').
      */
    char getNewDir(char dir, int x, int y, int[][] grid, int[][] timer);
}
