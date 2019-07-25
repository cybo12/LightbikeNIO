import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.rmi.RemoteException;

/**
 * @author Sam
 */
public class LightBikesTronGUI extends JFrame {

    static final long serialVersionUID = 201906281539L;
    //Inner representation of the grid
    public static int[][] iGrid = new int[100][100];
    final IntBikeUser bikeUser;
    final String gameName;
    //Objects to store the image and the 2D environment
    BufferedImage image;
    Graphics2D g2;
    myJPanel jpBoard;
    private JButton jButton1;
    private JLabel jLabel1;
    private JTextField jYourScore;
    private BikeWaitingRoomJoined bikeWaitingRoomJoined;
    // Creates new form GUI
    public LightBikesTronGUI(IntBikeUser bikeUser, String gameName, BikeWaitingRoomJoined bikeWaitingRoomJoined) {
        this.bikeUser = bikeUser;
        this.gameName = gameName;
        this.bikeWaitingRoomJoined = bikeWaitingRoomJoined;
        initComponents();

        /* GENERATED BY NETBEANS
         * Just copied here so as to change the JPanel and use my custom one (that includes an image)
         */
        image = new BufferedImage(400, 400, BufferedImage.TYPE_INT_ARGB);
        jpBoard = new myJPanel(true, image);

        jpBoard.setMinimumSize(new Dimension(400, 400));
        jpBoard.setPreferredSize(new Dimension(400, 400));

        GroupLayout jPanel1Layout = new GroupLayout(jpBoard);
        jpBoard.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
                jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGap(0, 156, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
                jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGap(0, 396, Short.MAX_VALUE)
        );

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addGap(208, 208, 208)
                                                .addComponent(jButton1))
                                        .addGroup(layout.createSequentialGroup()
                                                .addContainerGap()
                                                .addComponent(jLabel1)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jYourScore, GroupLayout.PREFERRED_SIZE, 62, GroupLayout.PREFERRED_SIZE))
                                        .addGroup(layout.createSequentialGroup()
                                                .addGap(50, 50, 50)
                                                .addComponent(jpBoard, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                                .addContainerGap(72, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel1)
                                        .addComponent(jYourScore, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addGap(32, 32, 32)
                                .addComponent(jpBoard, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 22, Short.MAX_VALUE)
                                .addComponent(jButton1)
                                .addContainerGap())
        );

        pack();
        /* END GENERATED BY NETBEANS */

        //Resets everything
        newGrid();

        //Adds the keyboard listener
        KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        manager.addKeyEventDispatcher(new MyDispatcher());

        //Inits the image board (400x400, filled with black)
        g2 = jpBoard.image.createGraphics();
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, 400, 400);

        //Makes it visible and repainted
        jpBoard.setVisible(true);
        jpBoard.repaint();

        //To force the GUI to refresh its frame
        this.repaint();
    }

    //Resets the inner representation of the grid
    public void newGrid() {
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 100; j++) {
                iGrid[i][j] = 0;
            }
        }
    }

    //This is where we refresh the grid with the new one, given as argument
    public void refreshGrid(int[][] newGrid) {
        boolean bChanged = false;
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 100; j++) {
                if (newGrid[i][j] != iGrid[i][j]) {
                    //Detects when a tile has been changed
                    bChanged = true;
                    iGrid[i][j] = newGrid[i][j];

                    //Apply the color corresponding to the given player
                    //One tile = 4x4 px
                    if (newGrid[i][j] == 1) {
                        g2.setColor(Color.RED);
                        g2.fillRect(i * 4, j * 4, 4, 4);
                    } else if (newGrid[i][j] == 2) {
                        g2.setColor(Color.BLUE);
                        g2.fillRect(i * 4, j * 4, 4, 4);
                    } else if (newGrid[i][j] == 3) {
                        g2.setColor(Color.YELLOW);
                        g2.fillRect(i * 4, j * 4, 4, 4);
                    } else if (newGrid[i][j] == 4) {
                        g2.setColor(Color.GREEN);
                        g2.fillRect(i * 4, j * 4, 4, 4);
                    }
                }
            }
        }

        //Only repaint the frame when something has changed (efficiency)
        if (bChanged)
            this.repaint();
    }

    public void startGameGrid() throws RemoteException {
        //The play button has been pressed, we restard the game
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, 400, 400);
        this.repaint();
        newGrid();
        System.out.println("Play pressed");
        bikeWaitingRoomJoined.exitWindow();
        jButton1.setEnabled(false);
    }




    //This method updates the frame
    public void update(int[][] gameGrid) throws RemoteException {
        //Updates the score
        boolean alive = bikeUser.getServer().getAlivePlayer(gameName,bikeUser.getPseudo());
        if(alive) {
            jYourScore.setText(bikeUser.getServer().getPlayerScore(gameName) + "");// --- OK
        }
        //Refresh the image
        refreshGrid(gameGrid); // --- OK

    }
    private void formKeyPressed(KeyEvent evt) throws RemoteException {//GEN-FIRST:event_formKeyPressed
        boolean alive = bikeUser.getServer().getAlivePlayer(gameName,bikeUser.getPseudo());
        // A key has been pressed. If a game is in progress, we must warn the core - in fact it's checking the player number
        System.out.println(alive);
        if (alive == true) {
            System.out.println("I'm alive");
            switch (evt.getKeyCode()) {
                case KeyEvent.VK_LEFT:
                    bikeUser.getServer().changeDirection(bikeUser.getPseudo(),gameName,'L'); // --- OK
                    System.out.println("I'm alive L");
                    break;
                case KeyEvent.VK_RIGHT:
                    bikeUser.getServer().changeDirection(bikeUser.getPseudo(),gameName,'R'); // --- OK
                    System.out.println("I'm alive R");
                    break;
                case KeyEvent.VK_UP:
                    bikeUser.getServer().changeDirection(bikeUser.getPseudo(),gameName,'U'); // --- OK
                    System.out.println("I'm alive U");
                    break;
                case KeyEvent.VK_DOWN:
                    bikeUser.getServer().changeDirection(bikeUser.getPseudo(),gameName,'D'); // --- OK
                    System.out.println("I'm alive D");
                    break;
                default:
                    break;
            }
        }
    }

    private void initComponents() {

        jButton1 = new JButton();
        jLabel1 = new JLabel();
        jYourScore = new JTextField();

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                formWindowClosing(evt);
                try {
                    bikeUser.getServer().removeUser(bikeUser);
                    System.exit(0);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent evt) {
                try {
                    formKeyPressed(evt);
                    System.out.println("I'm pressing");
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });

        jButton1.setText("Go back to game-list");
        jButton1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                try {
                    jButton1ActionPerformed(evt);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });

        jLabel1.setText("Your Score");

        jYourScore.setText("0");
        jYourScore.setEnabled(false);

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addGap(208, 208, 208)
                                                .addComponent(jButton1))
                                        .addGroup(layout.createSequentialGroup()
                                                .addContainerGap()
                                                .addComponent(jLabel1)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jYourScore, GroupLayout.PREFERRED_SIZE, 62, GroupLayout.PREFERRED_SIZE)))
                                .addContainerGap(261, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel1)
                                        .addComponent(jYourScore, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 454, Short.MAX_VALUE)
                                .addComponent(jButton1)
                                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(ActionEvent evt) throws RemoteException {//GEN-FIRST:event_jButton1ActionPerformed
        bikeUser.createWaitingRoom(bikeUser);
        bikeUser.getServer().relaunchUpdateGameList(bikeUser.getPseudo());
        this.dispose();

    }//GEN-LAST:event_jButton1ActionPerformed

    private void formWindowClosing(WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        // When we press the red X to close the window, we warn the core thread that the program is closing.
        this.dispose();
    }//GEN-LAST:event_formWindowClosing


    public void ending(String playerName, int playerScore) {
        new Thread(()->JOptionPane.showMessageDialog(this, "GAME OVER\n" + playerName + " WINS!\nYour score : " + playerScore)).start();
        jButton1.setEnabled(true);
        bikeWaitingRoomJoined.disposeWindow();

    }

    //Key listener
    private class MyDispatcher implements KeyEventDispatcher {
        @Override
        public boolean dispatchKeyEvent(KeyEvent e) {
            if (e.getID() == KeyEvent.KEY_PRESSED) {
                //Detects when a key has been pressed
                try {
                    formKeyPressed(e);
                } catch (RemoteException e1) {
                    e1.printStackTrace();
                }
            } else if (e.getID() == KeyEvent.KEY_RELEASED) {
                //just for testing, not very usefull
            } else if (e.getID() == KeyEvent.KEY_TYPED) {
                //Ibid
            }
            return false;
        }
    }
    // End of variables declaration//GEN-END:variables

}
