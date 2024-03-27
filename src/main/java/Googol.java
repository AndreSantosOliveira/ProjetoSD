import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Googol class extends JFrame and implements ActionListener.
 * This class is responsible for creating the user interface for the Googol search engine.
 * It includes a search field and a search button.
 * The search button has a hover effect and the search field accepts both enter and escape keys.
 */
public class Googol extends JFrame implements ActionListener {
    private final JTextField searchField;
    private final JButton searchButton;

    /**
     * Constructor for Googol.
     * Initializes the search field and search button, and sets up the main frame.
     */
    public Googol() {

        // Main frame
        setTitle("Googol");
        setSize(300, 100);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Initialize search field
        searchField = new JTextField(20);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(searchField, gbc);

        // Initialize search button
        searchButton = new JButton("GO");
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(searchButton, gbc);

        // Add action listener to search button
        searchButton.addActionListener(this);

        // Add hover effect to search button
        searchButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                searchButton.setForeground(Color.RED);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                searchButton.setForeground(null);
            }
        });

        // Accept enter key in search field
        searchField.addActionListener(e -> searchButton.doClick());

        // Accept escape key in search field
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

    /**
     * Handles the action event when the search button is clicked.
     *
     * @param e the action event
     */
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