import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/***
 * Interface of googol search engine
 * TO RUN:
 * Execute ServidorRMI + ClienteRMI
 *  ***/

public class Googol extends JFrame implements ActionListener {
    private final JTextField searchField;
    private final JButton searchButton;

    public Googol() {

        // Main frame
        setTitle("Googol");
        setSize(300, 100);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        searchField = new JTextField(20);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(searchField, gbc);

        searchButton = new JButton("GO");
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(searchButton, gbc);

        searchButton.addActionListener(this);

        // Efeito Hover
        searchButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                searchButton.setForeground(Color.RED);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                searchButton.setForeground(null);
            }
        });

        // Aceita enters
        searchField.addActionListener(e -> searchButton.doClick());

        // Aceita escapes
        this.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                searchField.requestFocusInWindow();
            }
        });
        searchField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    dispose();
                }
            }
        });

        add(panel);

        setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == searchButton) {
            String searchTerm = searchField.getText();

            // Protection against empty searches
            if (searchTerm.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a search query!", "Warning", JOptionPane.WARNING_MESSAGE);
            } else {
                System.out.println("Searching for: " + searchTerm);
            }
        }
    }
}
