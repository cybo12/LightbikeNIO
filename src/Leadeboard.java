import javax.swing.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.stream.Collectors;

public class Leadeboard {
    private JLabel name;
    private JList leaderlist;
    private JPanel jpane;

    public Leadeboard(LinkedHashMap<Integer, String> data) {

        JFrame leaderGUI = new JFrame();
        leaderGUI.setContentPane(jpane);
        leaderGUI.setTitle("Leaderboard");
        leaderGUI.setVisible(true);
        leaderGUI.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        leaderGUI.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                e.getWindow().dispose();
            }
        });
        //change do nothing on close
        leaderGUI.pack();
        leaderGUI.setLocationRelativeTo(null);
        if (data.size()!=0) {
            DefaultListModel modelLeaderList = null;
            Map<Integer, String> result = data.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByValue())
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (oldValue, newValue) -> oldValue, LinkedHashMap::new));
            modelLeaderList = convertToModel(result);
            leaderlist.setModel(modelLeaderList);
        }
        leaderlist.repaint();

    }
    private DefaultListModel convertToModel(Map<Integer, String> leaderList) {
        DefaultListModel modelGameList = new DefaultListModel();
        for (int i = 0, n = leaderList.size(); i < n; i++) {
            modelGameList.addElement(leaderList.get(i));
        }
        return modelGameList;
    }
}
