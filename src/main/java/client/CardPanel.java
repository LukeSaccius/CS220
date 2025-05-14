package client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

public class CardPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private JButton hitButton;
    private JButton standButton;
    private JButton betButton;
    private JTextField betAmountField;
    private JButton playAgainButton;
    private JButton startFreshGameButton;

    private JLabel dealerValueLabel;
    private JLabel playerValueLabel;
    private JLabel gameMessageLabel;
    private JLabel balanceLabel;
    private JLabel cardsRemainingLabel;

    private List<Card> dealerCards = new ArrayList<>();
    private List<Card> playerCards = new ArrayList<>();
    private Map<Card, ImageIcon> cardImages;
    private ImageIcon cardBackImage;

    private boolean showDealerHoleCard = false;

    public CardPanel(JButton hitButton, JButton standButton, JButton betButton, JTextField betAmountField,
            JButton playAgainButton, JButton startFreshGameButton,
            Map<Card, ImageIcon> cardImages) {
        this.hitButton = hitButton;
        this.standButton = standButton;
        this.betButton = betButton;
        this.betAmountField = betAmountField;
        this.playAgainButton = playAgainButton;
        this.startFreshGameButton = startFreshGameButton;
        this.cardImages = cardImages;
        this.cardBackImage = cardImages.get(Card.BACK);

        setLayout(null);
        setBackground(new Color(0, 102, 0));

        hitButton.setBounds(50, 700, 100, 50);
        add(hitButton);

        standButton.setBounds(170, 700, 100, 50);
        add(standButton);

        betAmountField.setBounds(300, 700, 80, 50);
        add(betAmountField);

        betButton.setBounds(400, 700, 100, 50);
        add(betButton);

        playAgainButton.setBounds(520, 700, 120, 50);
        add(playAgainButton);

        startFreshGameButton.setBounds(660, 700, 160, 50);
        add(startFreshGameButton);

        dealerValueLabel = new JLabel("Dealer: ", SwingConstants.CENTER);
        dealerValueLabel.setBounds(100, 50, 200, 30);
        dealerValueLabel.setForeground(Color.WHITE);
        dealerValueLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(dealerValueLabel);

        playerValueLabel = new JLabel("Player: ", SwingConstants.CENTER);
        playerValueLabel.setBounds(100, 450, 200, 30);
        playerValueLabel.setForeground(Color.WHITE);
        playerValueLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(playerValueLabel);

        gameMessageLabel = new JLabel("Place your bet!", SwingConstants.CENTER);
        gameMessageLabel.setBounds(300, 350, 400, 40);
        gameMessageLabel.setForeground(Color.YELLOW);
        gameMessageLabel.setFont(new Font("Arial", Font.BOLD, 20));
        add(gameMessageLabel);

        balanceLabel = new JLabel("Balance: 0", SwingConstants.LEFT);
        balanceLabel.setBounds(750, 20, 200, 30);
        balanceLabel.setForeground(Color.WHITE);
        balanceLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(balanceLabel);

        cardsRemainingLabel = new JLabel("Cards Left: N/A", SwingConstants.LEFT);
        cardsRemainingLabel.setBounds(750, 50, 200, 30);
        cardsRemainingLabel.setForeground(Color.WHITE);
        cardsRemainingLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(cardsRemainingLabel);
    }

    public void setDealerValueText(String text) {
        dealerValueLabel.setText("Dealer: " + text);
    }

    public void setPlayerValueText(String text) {
        playerValueLabel.setText("Player: " + text);
    }

    public void setGameMessage(String message) {
        gameMessageLabel.setText(message);
    }

    public void setBalanceText(String text) {
        balanceLabel.setText("Balance: " + text);
    }

    public void setCardsRemainingText(String text) {
        cardsRemainingLabel.setText("Cards Left: " + text);
    }

    public void setShowDealerHoleCard(boolean show) {
        this.showDealerHoleCard = show;
    }

    public void clearCards() {
        dealerCards.clear();
        playerCards.clear();
    }

    public void addDealerCard(Card card) {
        dealerCards.add(card);
    }

    public void addPlayerCard(Card card) {
        playerCards.add(card);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        int cardWidth = (cardBackImage != null) ? cardBackImage.getIconWidth() : 71;
        int cardHeight = (cardBackImage != null) ? cardBackImage.getIconHeight() : 96;
        int xOffset = 10;

        int dealerX = 100;
        int dealerY = 100;
        for (int i = 0; i < dealerCards.size(); i++) {
            Card card = dealerCards.get(i);
            ImageIcon cardImageToDraw = null;
            if (i == 0 || showDealerHoleCard) {
                cardImageToDraw = cardImages.get(card);
            } else {
                cardImageToDraw = cardBackImage;
            }

            if (cardImageToDraw != null) {
                g.drawImage(cardImageToDraw.getImage(), dealerX, dealerY, cardWidth, cardHeight, this);
                dealerX += cardWidth + xOffset;
            } else {
                g.setColor(Color.DARK_GRAY);
                g.fillRect(dealerX, dealerY, cardWidth, cardHeight);
                g.setColor(Color.WHITE);
                g.drawString(card != null ? card.toString() : "ERR", dealerX + 10, dealerY + 20);
                dealerX += cardWidth + xOffset;
            }
        }

        int playerX = 100;
        int playerY = 500;
        for (Card card : playerCards) {
            ImageIcon cardImage = cardImages.get(card);
            if (cardImage != null) {
                g.drawImage(cardImage.getImage(), playerX, playerY, cardWidth, cardHeight, this);
                playerX += cardImage.getIconWidth() + xOffset;
            } else {
                g.setColor(Color.DARK_GRAY);
                g.fillRect(playerX, playerY, cardWidth, cardHeight);
                g.setColor(Color.WHITE);
                g.drawString(card != null ? card.toString() : "ERR", playerX + 10, playerY + 20);
                playerX += cardWidth + xOffset;
            }
        }
    }
}
