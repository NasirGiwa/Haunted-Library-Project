package scene_creation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.sound.sampled.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BookOrderingGame extends JFrame {
    private JPanel bookPanel;
    private List<BookButton> bookButtons;
    private JButton checkButton;
    private BookButton selectedBook = null;
    private int moves = 0;
    private JLabel movesLabel;
    private int shelfNumber;
    private String shelfId; // Store the shelf identifier
    private MainClass mainClass;
    private boolean puzzleSolved = false;

    private class BookButton extends JButton {
        private String correctTitle;
        private int bookSize;

        public BookButton(String title, int size) {
            super(title);
            this.correctTitle = title;
            this.bookSize = size;
            setFont(new Font("Serif", Font.PLAIN, 16));
            setBackground(new Color(139, 69, 19));
            setForeground(Color.BLACK);
            updateSize(size);
            System.out.println("Book '" + title + "' created with height: " + size);
        }

        public void updateSize(int size) {
            Dimension dim = new Dimension(150, size);
            setPreferredSize(dim);
            setMinimumSize(dim);
            setMaximumSize(dim);
        }

        public String getCorrectTitle() {
            return correctTitle;
        }

        public int getBookSize() {
            return bookSize;
        }
    }

    public BookOrderingGame(int shelfNumber, String shelfId, MainClass mainClass) {
        super("Organize Books - Shelf " + shelfNumber);
        this.shelfNumber = shelfNumber;
        this.shelfId = shelfId; // Store shelfId
        this.mainClass = mainClass;

        setSize(800, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        bookButtons = new ArrayList<>();
        initializeBooks();

        bookPanel = new JPanel();
        setGameLayoutAndLogic();

        JPanel controlPanel = new JPanel();
        controlPanel.setBackground(new Color(50, 30, 20));

        movesLabel = new JLabel("Moves: 0");
        movesLabel.setForeground(Color.WHITE);
        checkButton = new JButton("Check Order");
        checkButton.addActionListener(e -> checkOrder());

        controlPanel.add(movesLabel);
        controlPanel.add(checkButton);

        add(getInstructionLabel(), BorderLayout.NORTH);
        add(bookPanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);

        // Update bookshelfUsage when window closes
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (puzzleSolved) {
                    mainClass.incrementPoints();
                    MainClass.bookshelfUsage.put(shelfId, true); // Mark shelf as used only if solved
                    System.out.println("Shelf " + shelfId + " marked as used (puzzle solved)");
                } else {
                    System.out.println("Shelf " + shelfId + " not marked as used (puzzle not solved)");
                }
            }
        });

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JLabel getInstructionLabel() {
        String instruction;
        switch (shelfNumber) {
            case 1: instruction = "Sort books alphabetically (A-Z)"; break;
            case 2: instruction = "Sort books reverse alphabetically (Z-A)"; break;
            case 3: instruction = "Sort by length (Shortest to Longest)"; break;
            case 4: instruction = "Sort by length (Longest to Shortest)"; break;
            case 5: instruction = "Sort by vowel count (Fewest to Most)"; break;
            case 6: instruction = "Sort by consonant count (Fewest to Most)"; break;
            case 7: instruction = "Sort by book height (Shortest to Tallest)"; break;
            case 8: instruction = "Sort by book height (Tallest to Shortest)"; break;
            default: instruction = "Unknown game"; break;
        }
        return new JLabel(instruction, SwingConstants.CENTER);
    }

    private void initializeBooks() {
        List<String> bookTitles = new ArrayList<>();
        List<Integer> sizes = new ArrayList<>();
        switch (shelfNumber) {
            case 1: case 2:
                bookTitles.add("Boo");
                bookTitles.add("Fear");
                bookTitles.add("Ghost");
                bookTitles.add("Witch");
                bookTitles.add("Dracula");
                break;
            case 3: case 4:
                bookTitles.add("Boo");
                bookTitles.add("Fear");
                bookTitles.add("Ghost");
                bookTitles.add("Witches");
                bookTitles.add("Darkness");
                break;
            case 5:
                bookTitles.add("Sky");
                bookTitles.add("Fear");
                bookTitles.add("Ghost");
                bookTitles.add("Witches");
                bookTitles.add("Audio");
                break;
            case 6:
                bookTitles.add("Ape");
                bookTitles.add("Boo");
                bookTitles.add("Fear");
                bookTitles.add("Ghost");
                bookTitles.add("Witch");
                break;
            case 7: case 8:
                bookTitles.add("Boo");
                bookTitles.add("Fear");
                bookTitles.add("Ghost");
                bookTitles.add("Witch");
                bookTitles.add("Dracula");
                sizes.add(30);
                sizes.add(40);
                sizes.add(50);
                sizes.add(60);
                sizes.add(70);
                break;
        }

        if (shelfNumber == 7 || shelfNumber == 8) {
            Collections.shuffle(sizes);
            for (int i = 0; i < bookTitles.size(); i++) {
                bookButtons.add(new BookButton(bookTitles.get(i), sizes.get(i)));
            }
        } else {
            Collections.shuffle(bookTitles);
            for (String title : bookTitles) {
                bookButtons.add(new BookButton(title, 40));
            }
        }
    }

    private void setGameLayoutAndLogic() {
        if (shelfNumber == 7 || shelfNumber == 8) {
            bookPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        } else {
            bookPanel.setLayout(new GridLayout(1, 5, 5, 5));
        }
        bookPanel.setBackground(new Color(50, 30, 20));
        for (BookButton button : bookButtons) {
            button.updateSize(button.getBookSize());
            bookPanel.add(button);
            button.addActionListener(new SwapBookClickListener());
            System.out.println("Book '" + button.getText() + "' actual size after adding: " +
                    button.getWidth() + "x" + button.getHeight());
        }
        bookPanel.revalidate();
        bookPanel.repaint();
    }

    private class SwapBookClickListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            playClickSound();
            BookButton clicked = (BookButton) e.getSource();

            if (selectedBook == null) {
                selectedBook = clicked;
                clicked.setBackground(new Color(184, 134, 11));
            } else if (selectedBook != clicked) {
                int index1 = bookButtons.indexOf(selectedBook);
                int index2 = bookButtons.indexOf(clicked);

                bookButtons.set(index1, clicked);
                bookButtons.set(index2, selectedBook);

                selectedBook.setBackground(new Color(139, 69, 19));
                selectedBook = null;

                moves++;
                movesLabel.setText("Moves: " + moves);

                bookPanel.removeAll();
                for (BookButton button : bookButtons) {
                    button.updateSize(button.getBookSize());
                    bookPanel.add(button);
                    System.out.println("Book '" + button.getText() + "' size after swap: " +
                            button.getWidth() + "x" + button.getHeight());
                }
                bookPanel.revalidate();
                bookPanel.repaint();
            }
        }
    }

    private void playClickSound() {
        try {
            URL soundURL = BookOrderingGame.class.getResource("click.wav");
            if (soundURL == null) {
                System.err.println("click.wav not found!");
                return;
            }
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundURL);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
        } catch (Exception ex) {
            System.err.println("Error playing click sound: " + ex.getMessage());
        }
    }

    private void checkOrder() {
        switch (shelfNumber) {
            case 1: checkAlphabeticalOrder(true); break;
            case 2: checkAlphabeticalOrder(false); break;
            case 3: checkLengthOrder(true); break;
            case 4: checkLengthOrder(false); break;
            case 5: checkVowelCountOrder(); break;
            case 6: checkConsonantCountOrder(); break;
            case 7: checkSizeOrder(true); break;
            case 8: checkSizeOrder(false); break;
        }
    }

    private void checkAlphabeticalOrder(boolean ascending) {
        List<String> currentOrder = new ArrayList<>();
        List<String> correctOrder = new ArrayList<>();

        for (BookButton button : bookButtons) {
            currentOrder.add(button.getText());
            correctOrder.add(button.getCorrectTitle());
        }

        Collections.sort(correctOrder);
        if (!ascending) Collections.reverse(correctOrder);

        boolean correct = currentOrder.equals(correctOrder);

        if (correct) {
            puzzleSolved = true;
            JOptionPane.showMessageDialog(this,
                    "Congratulations! Books on Shelf " + shelfNumber + " are in correct order!\nMoves: " + moves,
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Books on Shelf " + shelfNumber + " are not in " + (ascending ? "alphabetical" : "reverse alphabetical") + " order yet.",
                    "Try Again",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void checkLengthOrder(boolean ascending) {
        List<String> currentOrder = new ArrayList<>();
        for (BookButton button : bookButtons) {
            currentOrder.add(button.getText());
        }

        boolean correct = true;
        for (int i = 0; i < currentOrder.size() - 1; i++) {
            int len1 = currentOrder.get(i).length();
            int len2 = currentOrder.get(i + 1).length();
            if (ascending ? len1 > len2 : len1 < len2) {
                correct = false;
                break;
            }
        }

        if (correct) {
            puzzleSolved = true;
            JOptionPane.showMessageDialog(this,
                    "Congratulations! Books on Shelf " + shelfNumber + " are in correct length order!\nMoves: " + moves,
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Books on Shelf " + shelfNumber + " are not in " + (ascending ? "ascending" : "descending") + " length order yet.",
                    "Try Again",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void checkVowelCountOrder() {
        List<String> currentOrder = new ArrayList<>();
        for (BookButton button : bookButtons) {
            currentOrder.add(button.getText());
        }

        boolean correct = true;
        for (int i = 0; i < currentOrder.size() - 1; i++) {
            int vowels1 = countVowels(currentOrder.get(i));
            int vowels2 = countVowels(currentOrder.get(i + 1));
            if (vowels1 > vowels2) {
                correct = false;
                break;
            }
        }

        if (correct) {
            puzzleSolved = true;
            JOptionPane.showMessageDialog(this,
                    "Congratulations! Books on Shelf " + shelfNumber + " are in correct vowel count order!\nMoves: " + moves,
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Books on Shelf " + shelfNumber + " are not in ascending vowel count order yet.",
                    "Try Again",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void checkConsonantCountOrder() {
        List<String> currentOrder = new ArrayList<>();
        for (BookButton button : bookButtons) {
            currentOrder.add(button.getText());
        }

        boolean correct = true;
        for (int i = 0; i < currentOrder.size() - 1; i++) {
            int cons1 = countConsonants(currentOrder.get(i));
            int cons2 = countConsonants(currentOrder.get(i + 1));
            if (cons1 > cons2) {
                correct = false;
                break;
            }
        }

        if (correct) {
            puzzleSolved = true;
            JOptionPane.showMessageDialog(this,
                    "Congratulations! Books on Shelf " + shelfNumber + " are in correct consonant count order!\nMoves: " + moves,
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Books on Shelf " + shelfNumber + " are not in ascending consonant count order yet.",
                    "Try Again",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void checkSizeOrder(boolean ascending) {
        List<BookButton> currentOrder = new ArrayList<>(bookButtons);

        System.out.println("Expected heights for Shelf " + shelfNumber + ":");
        List<Integer> expectedSizes = new ArrayList<>();
        for (BookButton button : currentOrder) {
            expectedSizes.add(button.getBookSize());
        }
        System.out.println(expectedSizes);

        boolean correct = true;
        for (int i = 0; i < currentOrder.size() - 1; i++) {
            int size1 = currentOrder.get(i).getBookSize();
            int size2 = currentOrder.get(i + 1).getBookSize();
            if (ascending ? size1 > size2 : size1 < size2) {
                correct = false;
                break;
            }
        }

        if (correct) {
            puzzleSolved = true;
            JOptionPane.showMessageDialog(this,
                    "Congratulations! Books on Shelf " + shelfNumber + " are in correct height order!\nMoves: " + moves,
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Books on Shelf " + shelfNumber + " are not in " + (ascending ? "ascending" : "descending") + " height order yet.",
                    "Try Again",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private int countVowels(String text) {
        return (int) text.toLowerCase().chars()
                .filter(ch -> "aeiouy".indexOf(ch) != -1)
                .count();
    }

    private int countConsonants(String text) {
        return (int) text.toLowerCase().chars()
                .filter(ch -> Character.isLetter(ch) && "aeiouy".indexOf(ch) == -1)
                .count();
    }
    public boolean isGameWon() {
        return puzzleSolved;
    }
}