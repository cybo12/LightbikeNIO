import javax.swing.*;

public class BikeWaitingRoomJoined {
    private JPanel mainPanel;
    private JList playersList;
    private JButton notReadyPressForButton;
    private Boolean readyState = false;
    public JFrame waitingJoinedGUI;
    public BikeWaitingRoomJoined(BikeUser bikeUser, String gameName) {
        waitingJoinedGUI = new JFrame();
        waitingJoinedGUI.setContentPane(mainPanel);
        waitingJoinedGUI.setTitle(bikeUser.getPseudo() + " in game : " + gameName);
        waitingJoinedGUI.setVisible(true);
        waitingJoinedGUI.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        waitingJoinedGUI.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                    bikeUser.removeUser();
                    System.exit(0);
                e.getWindow().dispose();
                System.out.println("JFrame Closed!");
            }
        });
        waitingJoinedGUI.pack();
        waitingJoinedGUI.setLocationRelativeTo(null);
        notReadyPressForButton.addActionListener(e -> {
            if(notReadyPressForButton.getToolTipText().equals("no")){
                readyState = true;
                try {
                    System.out.println("Player ready " + bikeUser.getPseudo());
                    bikeUser.getServer().playerReadyState(bikeUser.getPseudo(), gameName, readyState);
                } catch (RemoteException | InterruptedException e1) {
                    e1.printStackTrace();
                }
                notReadyPressForButton.setToolTipText("Rady or not");
                notReadyPressForButton.setText("You're ready (click for not)");
            }else{
                readyState = false;
                notReadyPressForButton.setToolTipText("no");
                notReadyPressForButton.setText("Not ready (press for Ready)");
                try {
                    bikeUser.getServer().playerReadyState(bikeUser.getPseudo(), gameName, readyState);
                } catch (RemoteException | InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    public void disposeWindow()
    {
        waitingJoinedGUI.dispose();
    }

    /**
     * This class is only used to test the display, don't delete it
     *
     * @throws RemoteException
     */
    public BikeWaitingRoomJoined() {
        JFrame waitingJoinedGUI = new JFrame();
        waitingJoinedGUI.setContentPane(mainPanel);
        waitingJoinedGUI.setTitle("TEST PURPOSE");
        waitingJoinedGUI.setVisible(true);
        waitingJoinedGUI.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        waitingJoinedGUI.pack();
        waitingJoinedGUI.setLocationRelativeTo(null);


    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }

    public void updatePlayerListGUI(DefaultListModel playersWaitingModel){
        playersList.setModel(playersWaitingModel);
        playersList.repaint();
    }
    public void exitWindow(){
        waitingJoinedGUI.setVisible(false);
    }



}
