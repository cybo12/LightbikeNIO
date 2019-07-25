import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.rmi.*;
import java.util.ArrayList;


public class BikeWaitingRoom {
    private JButton createAGameButton;
    private JList gameList;
    private JTextField gameNameTextField;
    private JPanel mainPanel;
    private String selectedGameName;

    public BikeWaitingRoom(IntBikeUser bikeUser) throws RemoteException {
        JFrame waitingGUI = new JFrame();
        gameNameTextField.setText(BikeUserCredGUI.randomString(6));
        waitingGUI.setContentPane(mainPanel);
        waitingGUI.setTitle(bikeUser.getPseudo());
        waitingGUI.setVisible(true);
        waitingGUI.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        waitingGUI.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                try {
                    bikeUser.getServer().removeUser(bikeUser);
                } catch (RemoteException e1) {
                    e1.printStackTrace();
                }
                e.getWindow().dispose();
                System.exit(0);
                System.out.println("JFrame Closed!");
            }
        });
        //change do nothing on close
        waitingGUI.pack();
        waitingGUI.setLocationRelativeTo(null);

        createAGameButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String gameName = gameNameTextField.getText();
                if (gameName.length() >= 3) {
                    try {
                        ArrayList<String> gameNamesInUse = bikeUser.getServer().getGameNames();
                        if (gameNamesInUse.contains(gameName)) {
                            JOptionPane.showMessageDialog(null, "This game name is already in use, please change");
                            gameNameTextField.setText("Please no");
                        } else {
                            bikeUser.createGame(gameName);
                        }
                    } catch (RemoteException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
        gameList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (e.getClickCount() == 2) {
                    selectedGameName = gameList.getSelectedValue().toString();
                    try {
                        bikeUser.createWaitingRoomJoined(bikeUser,selectedGameName);
                        bikeUser.getServer().joiningWaitingGame(bikeUser.getPseudo(),selectedGameName);
                        System.out.println(selectedGameName);
                        waitingGUI.dispose();
                    } catch (RemoteException e1) {
                        e1.printStackTrace();
                    }

                }
            }
        });

    }


    /**
     * This class is only used to test the display dont delete it
     * @throws RemoteException
     */
    public BikeWaitingRoom() {
        JFrame waitingGUI = new JFrame();
        waitingGUI.setContentPane(mainPanel);
        waitingGUI.setTitle("TEST PURPOSE");
        waitingGUI.setVisible(true);
        waitingGUI.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        waitingGUI.pack();
        waitingGUI.setLocationRelativeTo(null);
    }





    public void updateGameListGUI(DefaultListModel gameListModel) {
        gameList.setModel(gameListModel);
        gameList.repaint();
    }

}


