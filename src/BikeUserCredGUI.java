import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.Inet4Address;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Random;

/**
 * Ask the user for creditentials like pseudo and the server ip
 */
public class BikeUserCredGUI {
    static final String SOURCE = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private JTextField pseudoTextField;
    private JTextField ipTextField;
    private JButton conButton ;
    private JPanel promptPanel;
    private BikeUser bikeUser;
    private ArrayList<String> inUse;


    public BikeUserCredGUI() throws UnknownHostException {
        JFrame promptCredits = new JFrame();
        ipTextField.setText(Inet4Address.getLocalHost().getHostAddress());
        pseudoTextField.setText(randomString(6));
        promptCredits.setContentPane(promptPanel);
        promptCredits.setTitle("Login prompt");
        promptCredits.setVisible(true);
        promptCredits.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        promptCredits.pack();
        promptCredits.setLocationRelativeTo(null);
        /**
         * Check if IP is correct, pseudo > 3 chars, then creating the stub.
         * Once the stub is created ; checking if the pseudo is not already in use (=register on the server)
         */conButton.addActionListener(e -> {
             if ((pseudoTextField.getText().length() > 3) && (ValidIP.validate(ipTextField.getText())) || ipTextField.getText().equals("localhost")) {
                     if(bikeUser ==null) {
                         bikeUser = new BikeUser(ipTextField.getText());
                     }
                     System.out.println("yes");
                     bikeUser.getPseudoInUse();
                     if (inUse.contains(pseudoTextField.getText())) {
                         JOptionPane.showMessageDialog(null, "This pseudo is already in use, please change");
                         pseudoTextField.setText("");
                     } else {
                         bikeUser.setPseudo(pseudoTextField.getText());
                         bikeUser.createWaitingRoom();
                         bikeUser.connect();
                         promptCredits.dispose();
                     }
             }
         });
    }
    public static String randomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        Random rand = new Random();
        for (int i = 0; i < length; i++)
            sb.append(SOURCE.charAt(rand.nextInt(SOURCE.length())));
        return sb.toString();
    }

    public void setInUse(ArrayList<String> inUse) {
        this.inUse = inUse;
    }
}
