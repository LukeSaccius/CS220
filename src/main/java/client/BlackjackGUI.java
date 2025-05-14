package client;

// import java.awt.Component; // Removed as unused
import java.awt.Dialog;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Objects;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
// import javax.swing.JLabel; // Removed as unused
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
// import javax.swing.JPanel; // Removed as unused
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

public class BlackjackGUI extends JFrame {
    private static final long serialVersionUID = 1L;

    private JButton hitButton;
    private JButton standButton;
    private JButton betButton;
    private JTextField betAmountField;
    private JButton playAgainButton;
    private JButton startFreshGameButton;

    private String BASE_URL = "http://euclid.knox.edu:8080/api/blackjack";
    private String USERNAME = "tle";
    private String PASSWORD = "24b3790";
    private ClientConnecter clientConnecter;

    private CardPanel cardPanel;
    private Map<Card, ImageIcon> cardImages;
    private UUID sessionId;

    public BlackjackGUI() {
        setTitle("Blackjack Game");
        setSize(1000, 800);
        loadCards();
        // create and pass the buttons to the card panel
        // it will resize them and add them to the panel
        hitButton = new JButton("Hit");
        standButton = new JButton("Stand");
        betButton = new JButton("Bet");
        betAmountField = new JTextField(5);
        playAgainButton = new JButton("Play Again");
        playAgainButton.setVisible(false);
        startFreshGameButton = new JButton("Start Fresh Game");
        startFreshGameButton.setVisible(false);

        cardPanel = new CardPanel(hitButton, standButton, betButton, betAmountField, playAgainButton,
                startFreshGameButton, cardImages);
        setContentPane(cardPanel);

        // now set the action listeners for the hit/stand buttons
        hitButton.addActionListener(e -> {
            System.out.println("[BlackjackGUI] Hit button clicked.");
            if (sessionId == null) {
                JOptionPane.showMessageDialog(this, "Please start a new game first.", "Game Not Started",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                GameState newState = clientConnecter.hit(sessionId);
                System.out.println("[BlackjackGUI] State after hit: " + newState);
                updateUiWithGameState(newState);

                // updateUiWithGameState should handle button states based on phase
                // If phase is RESOLVED (e.g., player busted), hit/stand will be disabled by it.

            } catch (Throwable t) {
                System.err.println("[BlackjackGUI] Throwable caught in Hit button ActionListener:");
                t.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error during Hit: " + t.getMessage(), "Hit Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        standButton.addActionListener(e -> {
            System.out.println("[BlackjackGUI] Stand button clicked.");
            if (sessionId == null) {
                JOptionPane.showMessageDialog(this, "Please start a new game first.", "Game Not Started",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Disable buttons immediately, as player's turn is ending.
            hitButton.setEnabled(false);
            standButton.setEnabled(false);

            try {
                GameState newState = clientConnecter.stand(sessionId);
                System.out.println("[BlackjackGUI] State after stand: " + newState);

                // Ensure dealer's hole card is revealed before updating UI if phase is RESOLVED
                // or DEALER_TURN
                // updateUiWithGameState handles this based on phase, but explicitly setting
                // here is fine for clarity
                if ("RESOLVED".equals(newState.phase) || "DEALER_TURN".equals(newState.phase)) {
                    cardPanel.setShowDealerHoleCard(true);
                }
                updateUiWithGameState(newState);
                // updateUiWithGameState should handle button states and final UI based on
                // RESOLVED phase

            } catch (Throwable t) {
                System.err.println("[BlackjackGUI] Throwable caught in Stand button ActionListener:");
                t.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error during Stand: " + t.getMessage(), "Stand Error",
                        JOptionPane.ERROR_MESSAGE);
                // Re-enable buttons if stand failed? Or rely on New Game to reset.
                // For now, they remain disabled. New Game will re-enable bet button.
            }
        });

        betButton.addActionListener(e -> {
            String betText = betAmountField.getText();
            int betAmountInt;
            try {
                betAmountInt = Integer.parseInt(betText);
                if (betAmountInt <= 0) {
                    JOptionPane.showMessageDialog(this, "Bet amount must be a positive integer.", "Invalid Bet",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid number for the bet amount.", "Invalid Bet",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (sessionId == null) {
                JOptionPane.showMessageDialog(this, "Please start a new game first.", "Game Not Started",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                System.out.println("[BlackjackGUI] Bet button clicked. Amount (verified): " + betAmountInt);

                System.out.println("TESTING 123"); // Simplest possible print statement

                if (clientConnecter == null) {
                    System.err.println("[BlackjackGUI] CRITICAL ERROR: clientConnecter is NULL before Checkpoint 1!");
                    JOptionPane.showMessageDialog(this, "Critical error: ClientConnecter is null before bet call.",
                            "Internal Error", JOptionPane.ERROR_MESSAGE);
                    return; // Stop if clientConnecter is null
                }
                System.out.println("[BlackjackGUI] DEBUG: clientConnecter instance is not null.");

                System.out.println("[BlackjackGUI] DEBUG: Checkpoint 1");
                String sid = null;
                System.out.println("[BlackjackGUI] DEBUG: Checkpoint 2");
                if (sessionId != null) {
                    System.out.println("[BlackjackGUI] DEBUG: Checkpoint 3 (sessionId is not null)");
                    sid = sessionId.toString();
                    System.out
                            .println("[BlackjackGUI] DEBUG: Checkpoint 4 (sessionId.toString() assigned: " + sid + ")");
                } else {
                    System.out.println("[BlackjackGUI] DEBUG: Checkpoint 3 (sessionId IS NULL - THIS IS A PROBLEM)");
                }

                System.out.println("[BlackjackGUI] DEBUG: Checkpoint 5 (About to call placeBet)");
                GameState stateAfterBet = clientConnecter.placeBet(sessionId, betAmountInt);

                System.out.println(
                        "[BlackjackGUI] Returned from clientConnecter.placeBet(). stateAfterBet: " + stateAfterBet);

                updateUiWithGameState(stateAfterBet);

                // If game is resolved immediately after bet (e.g. Blackjack)
                if ("RESOLVED".equals(stateAfterBet.phase)) {
                    // New Game button already handles resetting for the next game.
                    // Here, we just ensure buttons are set correctly for a resolved state.
                    betButton.setEnabled(false); // Keep bet disabled until new game
                    betAmountField.setEnabled(false);
                    hitButton.setEnabled(false);
                    standButton.setEnabled(false);
                    playAgainButton.setVisible(true);
                    playAgainButton.setEnabled(true);
                    startFreshGameButton.setVisible(true);
                    startFreshGameButton.setEnabled(true);
                    // Show dealer's hole card if not already shown
                    cardPanel.setShowDealerHoleCard(true);
                    // update dealer's hand to show the hole card and final value
                    updateDealerHandPostResolution(stateAfterBet);

                } else if ("PLAYER_TURN".equals(stateAfterBet.phase)) {
                    hitButton.setEnabled(stateAfterBet.canHit);
                    standButton.setEnabled(stateAfterBet.canStand);
                    betButton.setEnabled(false);
                    betAmountField.setEnabled(false);
                    playAgainButton.setVisible(false);
                    playAgainButton.setEnabled(false);
                    startFreshGameButton.setVisible(false);
                    startFreshGameButton.setEnabled(false);
                } else {
                    // Handle other phases if necessary, for now, same as resolved
                    betButton.setEnabled(false);
                    betAmountField.setEnabled(false);
                    hitButton.setEnabled(false);
                    standButton.setEnabled(false);
                    playAgainButton.setVisible(false);
                    playAgainButton.setEnabled(false);
                    startFreshGameButton.setVisible(false);
                    startFreshGameButton.setEnabled(false);
                }
                repaint();

            } catch (Throwable t) {
                System.err.println("[BlackjackGUI] Throwable caught in Bet button ActionListener:");
                t.printStackTrace(); // Print stack trace for any Throwable
                JOptionPane.showMessageDialog(this, "Error placing bet: " + t.getMessage(), "Bet Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        playAgainButton.addActionListener(e -> {
            System.out.println("[BlackjackGUI] Play Again button clicked.");
            if (sessionId == null) {
                JOptionPane.showMessageDialog(this, "Cannot 'Play Again' without an active game session.", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                GameState state = clientConnecter.newGame(sessionId); // Use newGame to continue session
                // sessionId remains the same
                updateUiWithGameState(state);
            } catch (Throwable t) {
                System.err.println("[BlackjackGUI] Throwable caught in Play Again button ActionListener:");
                t.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error starting new game: " + t.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        startFreshGameButton.addActionListener(e -> {
            System.out.println("[BlackjackGUI] Start Fresh Game button clicked.");
            try {
                GameState state = clientConnecter.startGame(); // Starts a brand new session
                System.out.println(state);
                sessionId = state.sessionId; // Get new session ID
                updateUiWithGameState(state);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error starting new game: " + ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        // Initial button states
        hitButton.setEnabled(false);
        standButton.setEnabled(false);
        betButton.setEnabled(true);
        betAmountField.setEnabled(true);
        playAgainButton.setEnabled(false);
        startFreshGameButton.setEnabled(false);

        // client connecter to make API calls on the server
        clientConnecter = new ClientConnecter(BASE_URL, USERNAME, PASSWORD);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        addMenuBar();
        // TODO: keyboard shortcuts
        // TODO: mouse events
    }

    private void addMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        this.setJMenuBar(menuBar);

        JMenu fileMenu = new JMenu("File");

        menuBar.add(fileMenu);
        addMenuItem(fileMenu, "Reconnect", () -> {
            System.out.println("Load clicked");
            try {
                List<SessionSummary> sessionSummaryList = clientConnecter.listSessions();
                for (SessionSummary session : sessionSummaryList) {
                    System.out.println("Session ID: " + session.sessionId + ", Balance: " + session.balance);
                }

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error loading game: " + e.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        addMenuItem(fileMenu, "New Game", () -> {
            System.out.println("New Game clicked");
            try {
                GameState state = clientConnecter.startGame();
                System.out.println(state); // Keep for debugging for now
                sessionId = state.sessionId;

                updateUiWithGameState(state); // Use the helper method

                // Set button states for betting phase (already in updateUiWithGameState if it's
                // BETTING phase)
                // but explicitly set here to ensure correct state after new game
                if ("BETTING".equals(state.phase) || state.phase == null) { // state.phase can be null initially
                    betButton.setEnabled(true);
                    betAmountField.setEnabled(true);
                    hitButton.setEnabled(false);
                    standButton.setEnabled(false);
                    playAgainButton.setVisible(false);
                    playAgainButton.setEnabled(false);
                    startFreshGameButton.setVisible(false);
                    startFreshGameButton.setEnabled(false);
                } else { // If new game starts in a different phase (e.g. straight to player turn if bet
                         // is auto)
                    betButton.setEnabled(false);
                    betAmountField.setEnabled(false);
                    hitButton.setEnabled(state.canHit);
                    standButton.setEnabled(state.canStand);
                    playAgainButton.setVisible(false);
                    playAgainButton.setEnabled(false);
                    startFreshGameButton.setVisible(false);
                    startFreshGameButton.setEnabled(false);
                }

                repaint(); // Repaint the frame to show changes

            } catch (Exception e) {
                e.printStackTrace(); // Print stack trace for better debugging
                JOptionPane.showMessageDialog(this, "Error starting new game: " + e.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

    }

    private void updateUiWithGameState(GameState state) {
        cardPanel.clearCards();
        cardPanel.setShowDealerHoleCard(false); // Default for start of hand or if dealer's turn not yet complete

        if (state.playerCards != null) {
            for (String cardStr : state.playerCards) {
                Card card = getCardFromServerString(cardStr);
                if (card != null) {
                    cardPanel.addPlayerCard(card);
                }
            }
        }
        cardPanel.setPlayerValueText(String.valueOf(Objects.isNull(state.playerValue) ? 0 : state.playerValue));

        if (state.dealerCards != null && !state.dealerCards.isEmpty()) {
            Card dealerUpCard = getCardFromServerString(state.dealerCards.get(0));
            if (dealerUpCard != null) {
                cardPanel.addDealerCard(dealerUpCard);
                if (state.dealerCards.size() > 1) {
                    // If phase is resolved, or it's dealer's turn and cards are revealed
                    if ("RESOLVED".equals(state.phase) || "DEALER_TURN".equals(state.phase)) {
                        cardPanel.setShowDealerHoleCard(true);
                        Card holeCard = getCardFromServerString(state.dealerCards.get(1));
                        if (holeCard != null) {
                            cardPanel.addDealerCard(holeCard); // Add the actual hole card
                        } else {
                            cardPanel.addDealerCard(Card.BACK); // Fallback if string not parsable, though unusual
                        }
                        // Display final dealer value
                        cardPanel.setDealerValueText(String.valueOf(state.dealerValue != null ? state.dealerValue : 0));
                    } else {
                        // Hole card is present but not yet revealed
                        cardPanel.addDealerCard(Card.BACK); // Add placeholder for the back of the hole card

                        // Display value of the dealer's upcard. dealerUpCard is guaranteed not null
                        // here.
                        if (Objects.isNull(state.dealerValue)) {
                            cardPanel.setDealerValueText(String.valueOf(dealerUpCard.getValue()));
                        } else {
                            cardPanel.setDealerValueText(String.valueOf(state.dealerValue));
                        }
                    }
                } else { // Only one dealer card dealt so far. dealerUpCard is guaranteed not null here.
                    cardPanel.setDealerValueText(
                            String.valueOf(state.dealerValue != null ? state.dealerValue : dealerUpCard.getValue()));
                }
            } else { // Outer else: dealerUpCard is null (first dealer card couldn't be parsed)
                cardPanel.setDealerValueText("0"); // Or perhaps "?"
            }
        } else {
            cardPanel.setDealerValueText("0");
        }

        cardPanel.setBalanceText(String.valueOf(state.balance));
        cardPanel.setCardsRemainingText(
                String.valueOf(Objects.isNull(state.cardsRemaining) ? "N/A" : state.cardsRemaining));
        cardPanel.setGameMessage(state.phase != null ? state.phase : "Place your bet!");

        // Default button states, can be overridden by caller if needed
        if ("BETTING".equals(state.phase) || state.phase == null) {
            betButton.setEnabled(true);
            betAmountField.setEnabled(true);
            hitButton.setEnabled(false);
            standButton.setEnabled(false);
            playAgainButton.setVisible(false);
            playAgainButton.setEnabled(false);
            startFreshGameButton.setVisible(false);
            startFreshGameButton.setEnabled(false);
        } else if ("PLAYER_TURN".equals(state.phase)) {
            betButton.setEnabled(false);
            betAmountField.setEnabled(false);
            hitButton.setEnabled(state.canHit);
            standButton.setEnabled(state.canStand);
            playAgainButton.setVisible(false);
            playAgainButton.setEnabled(false);
            startFreshGameButton.setVisible(false);
            startFreshGameButton.setEnabled(false);
        } else if ("RESOLVED".equals(state.phase)) {
            betButton.setEnabled(false);
            betAmountField.setEnabled(false);
            hitButton.setEnabled(false);
            standButton.setEnabled(false);
            playAgainButton.setVisible(true);
            playAgainButton.setEnabled(true);
            startFreshGameButton.setVisible(true);
            startFreshGameButton.setEnabled(true);
            System.out.println("[BlackjackGUI] Play Again Button - isVisible: " + playAgainButton.isVisible()
                    + ", isEnabled: " + playAgainButton.isEnabled() + ", Parent: " + playAgainButton.getParent());
            cardPanel.setShowDealerHoleCard(true);
            updateDealerHandPostResolution(state);
        } else {
            betButton.setEnabled(false);
            betAmountField.setEnabled(false);
            hitButton.setEnabled(false);
            standButton.setEnabled(false);
            playAgainButton.setVisible(false);
            playAgainButton.setEnabled(false);
            startFreshGameButton.setVisible(false);
            startFreshGameButton.setEnabled(false);
        }
        repaint(); // Added repaint here to ensure panel updates after any state change
    }

    private void updateDealerHandPostResolution(GameState state) {
        // This method ensures the dealer's hand in the UI accurately reflects the final
        // state.
        // It assumes cardPanel.clearCards() was NOT called if we want to preserve
        // player cards.
        // For now, it just re-adds dealer cards assuming they might need full update.
        // A more optimized version might just update the hole card image.

        // Clear only dealer cards from cardPanel before re-adding, if CardPanel
        // supports it.
        // cardPanel.clearDealerCards(); // Assuming such a method exists or can be
        // added

        // For now, let's rely on updateUiWithGameState to have added the cards
        // correctly
        // based on showDealerHoleCard being true.
        // This method will specifically ensure the dealer's final value is displayed.
        if (state.dealerCards != null && !state.dealerCards.isEmpty()) {
            // Logic within updateUiWithGameState already handles showing hole card if phase
            // is RESOLVED.
            // We just need to ensure dealer value is the final one.
            cardPanel.setDealerValueText(String.valueOf(state.dealerValue != null ? state.dealerValue : 0));
        }
        // Ensure the card panel repaints if changes were made to card
        // visibility/values.
        cardPanel.repaint();
    }

    // convert server card string (e.g., "SA", "D10") to Card enum
    private Card getCardFromServerString(String serverCardString) {
        // The server sends card strings like "S2", "HA", "D10"
        // Card.fromString expects this format directly.
        try {
            return Card.fromString(serverCardString);
        } catch (IllegalArgumentException e) {
            System.err.println("Error parsing card string from server: " + serverCardString + " - " + e.getMessage());
            // Optionally, return a default/error card or handle differently
            return null; // Or throw, or have a Card.UNKNOWN
        }
    }

    private void addMenuItem(JMenu menu, String name, Runnable action) {
        JMenuItem menuItem = new JMenuItem(name);
        menuItem.addActionListener(e -> action.run());
        menu.add(menuItem);
    }

    private void loadCards() {
        // Load card images and add them to the main panel
        // This is where you would implement the logic to load and display cards
        cardImages = new HashMap<>();
        for (Card card : Card.values()) {
            ImageIcon cardImage = new ImageIcon(getClass().getResource("/assets/" + card.getFilename()));
            cardImages.put(card, cardImage);
        }
    }

    public void showListPopup(String title, java.util.List<String> items) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), title,
                Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(300, 200);
        dialog.setLocationRelativeTo(this);

        JList<String> list = new JList<>(new DefaultListModel<>());
        DefaultListModel<String> model = (DefaultListModel<String>) list.getModel();
        for (String item : items) {
            model.addElement(item);
        }

        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) { // double click to select
                    String selected = list.getSelectedValue();
                    System.out.println("Selected: " + selected);
                    dialog.dispose();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(list);
        dialog.add(scrollPane);
        dialog.setVisible(true);
    }

    public static void main(String[] args) {
        BlackjackGUI gui = new BlackjackGUI();
        gui.setVisible(true);
    }

}
