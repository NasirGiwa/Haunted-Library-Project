package scene_creation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

// GameIntroPopup is a class that displays a starting popup window with the game description and controls.
public class GameIntroPopup extends JDialog {

    // Constructor for the GameIntroPopup.
    public GameIntroPopup(JFrame parent, Runnable onStartGame) {
        super(parent, "Welcome to Haunted Leddy", true); // Modal dialog to block the parent window.
        setDefaultCloseOperation(DISPOSE_ON_CLOSE); // Dispose the dialog when closed.
        setLayout(new BorderLayout());
        setSize(800, 400); // Set the size of the popup window.
        setLocationRelativeTo(null); // Center the popup on the screen.

        // Create the main panel with padding.
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(new Color(240, 240, 240)); // Light gray background.

        // Game title label.
        JLabel titleLabel = new JLabel("Haunted Leddy", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 24));
        titleLabel.setForeground(new Color(139, 69, 19)); // Dark brown color for the title.
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Game description and controls panel.
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(new Color(240, 240, 240));

        // Game description.
        JLabel descriptionLabel = new JLabel("<html>" +
                "<b>Game Description:</b><br>" +
                "Welcome to the Haunted Leddy Library! You have 5 minutes to solve all 8 book-ordering puzzles on the shelves.<br>" +
                "Click a book to begin a puzzle and arrange the books in the correct order.<br>" +
                "A mysterious gas sphere blocks the door until you complete all puzzles. Solve them all to make it disappear and escape!<br>" +
                "If time runs out before you finish, the ghost will appear, ending your game.<br>" +
                "</html>");
        descriptionLabel.setFont(new Font("Serif", Font.PLAIN, 14));
        descriptionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(descriptionLabel);
        contentPanel.add(Box.createVerticalStrut(15)); // Add spacing.

        // Game controls.
        JLabel controlsLabel = new JLabel("<html><b>Controls:</b><br>" +
                "- <b>W, A, S, D or Arrow keys :</b> Move forward, left, backward, right<br>" +
                "- <b>Mouse:</b> Look around (move to rotate camera)<br>" +
                "- <b>Shift + Mouse:</b> Adjust camera height<br>" +
                "- <b>Left Click:</b> Interact with books (start puzzle) <br>" +
                "- <b>Space:</b> Jump <br>" +
                "- <b>Please Wait While Game is Loading and Start game by mouse movement </b></html>");
        controlsLabel.setFont(new Font("Serif", Font.PLAIN, 14));
        controlsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(controlsLabel);

        // Add the content panel to a scroll pane in case the content is too long.
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Start game button panel.
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(240, 240, 240));
        JButton startButton = new JButton("Start Game");
        startButton.setFont(new Font("Serif", Font.BOLD, 16));
        startButton.setBackground(new Color(34, 139, 34)); // Forest green background.
        startButton.setForeground(Color.BLACK);
        startButton.setFocusPainted(false);
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose(); // Close the popup.
                onStartGame.run(); // Run the callback to start the game.
            }
        });
        buttonPanel.add(startButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Add the main panel to the dialog.
        add(mainPanel);
    }

    // Method to display the popup.
    public void showPopup() {
        setVisible(true);
    }
}