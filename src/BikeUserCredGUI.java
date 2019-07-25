import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.Inet4Address;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
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
    private JButton conButton;
    private JPanel promptPanel;
    private IntServer server = null;

    public BikeUserCredGUI() throws UnknownHostException {
        ipTextField.setText(Inet4Address.getLocalHost().getHostAddress());
        pseudoTextField.setText(randomString(6));
        JFrame promptCredits = new JFrame();
        promptCredits.setContentPane(promptPanel);
        promptCredits.setTitle("Login prompt");
        promptCredits.setVisible(true);
        promptCredits.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        promptCredits.pack();
        promptCredits.setLocationRelativeTo(null);
        conButton.addActionListener(new ActionListener() {
            @Override
            /**
             * Check if IP is correct, pseudo > 3 chars, then creating the stub.
             * Once the stub is created ; checking if the pseudo is not already in use (=register on the server)
             */
            public void actionPerformed(ActionEvent e) {
                if ((pseudoTextField.getText().length() > 3) && (ValidIP.validate(ipTextField.getText())) || ipTextField.getText().equals("localhost")) {
                    try {
                        if (server == null) {
                            server = (IntServer) Naming.lookup("rmi://" + ipTextField.getText() + "/myserver"); //We don't want an error, the server will anyways be assigned once(stub)
                        }
                        System.out.println("yes");
                        ArrayList<String> inUse = server.getPseudosInUse();
                        if (inUse.contains(pseudoTextField.getText())) {
                            JOptionPane.showMessageDialog(null, "This pseudo is already in use, please change");
                            pseudoTextField.setText("");
                        } else {
                            IntBikeUser bikeUser = new BikeUser(server, pseudoTextField.getText());
                            bikeUser.createWaitingRoom(bikeUser);
                            server.Connect(bikeUser);
                            ((BikeUser) bikeUser).setBikeUserLocal(bikeUser);
                            promptCredits.dispose();
                        }
                    } catch (RemoteException e1) {
                        e1.printStackTrace();
                        JOptionPane.showMessageDialog(null, "The IP address may be wrong.");
                    } catch (NotBoundException e1) {
                        e1.printStackTrace();
                    } catch (MalformedURLException e1) {
                        e1.printStackTrace();
                    }
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
}
