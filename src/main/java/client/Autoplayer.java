package client;

import java.util.HashMap; // Added for HashMap
import java.util.List;
import java.util.Map;

public class Autoplayer {

    // Hi-Lo Card Values (Re-introduced)
    private static final Map<Character, Double> cardValues = new HashMap<>();
    static {
        cardValues.put('2', 1.0);
        cardValues.put('3', 1.0);
        cardValues.put('4', 1.0);
        cardValues.put('5', 1.0);
        cardValues.put('6', 1.0);
        cardValues.put('7', 0.0);
        cardValues.put('8', 0.0);
        cardValues.put('9', 0.0);
        cardValues.put('T', -1.0);
        cardValues.put('J', -1.0);
        cardValues.put('Q', -1.0);
        cardValues.put('K', -1.0);
        cardValues.put('A', -1.0);
    }

    // Counting variables (Re-introduced)
    private static double runningCount = 0.0;
    private static Map<String, Integer> seenCards = new HashMap<>();

    public static void main(String[] args) throws Exception {
        String baseUrl = "http://euclid.knox.edu:8080/api/blackjack";
        String username = "tle"; // Using your username
        String password = "24b3790"; // Using your password

        ClientConnecter clientConnecter = new ClientConnecter(baseUrl, username, password);
        GameState state = clientConnecter.startGame();

        int numGames = 10000; // Keeping 10k games for better assessment
        int numWins = 0;
        int numLosses = 0;
        int numPushes = 0;
        int numBlackjacks = 0;
        int numDealerBlackjacks = 0;
        int baseBet = 10;

        // Initialize counts at the start of the session
        runningCount = 0.0;
        seenCards.clear();

        for (int i = 0; i < numGames; i++) {
            // Handle reshuffle: reset counts
            if (state.reshuffled) {
                System.out.println("--- RESHUFFLE ---");
                runningCount = 0.0;
                seenCards.clear();
                // Count initial cards after reshuffle
                updateCountFromList(state.playerCards, true);
                updateCountFromList(state.dealerCards, true);
            }

            // Calculate True Count for betting and playing strategy
            double decksRemaining = estimateDecksRemaining(state.cardsRemaining);
            double trueCount = calculateTrueCount(runningCount, decksRemaining);

            // Variable Betting Strategy based on True Count:
            int betAmount = decideBet(trueCount, baseBet);

            state = clientConnecter.placeBet(state.sessionId, betAmount);

            // Update counts with newly dealt cards after bet
            updateCountFromList(state.playerCards, false);
            updateCountFromList(state.dealerCards, false);

            // Handle reshuffle immediately after bet (if it occurred)
            if (state.reshuffled) {
                System.out.println("--- RESHUFFLE (Post-Bet) ---");
                runningCount = 0.0;
                seenCards.clear();
                updateCountFromList(state.playerCards, true);
                updateCountFromList(state.dealerCards, true);
            }

            // Immediate win/loss logic
            if (state.phase.equals("RESOLVED")) {
                if (state.outcome.equals("PLAYER_BLACKJACK")) {
                    numBlackjacks++;
                    numWins++;
                } else if (state.outcome.equals("DEALER_WINS")) {
                    if (state.dealerValue != null && state.dealerValue == 21 && state.dealerCards != null
                            && state.dealerCards.size() == 2) {
                        numDealerBlackjacks++;
                    }
                    numLosses++;
                } else if (state.outcome.equals("PUSH")) {
                    numPushes++;
                }
                state = clientConnecter.newGame(state.sessionId);
                continue;
            }

            // Playing Strategy: Hi-Lo deviations + Basic Strategy
            char dealerUpCardRank = ' ';
            if (state.dealerCards != null && !state.dealerCards.isEmpty()) {
                dealerUpCardRank = extractRank(state.dealerCards.get(0));
            }

            while (state.canHit && shouldPlayerHit(state, trueCount, dealerUpCardRank)) { // Pass trueCount
                List<String> cardsBeforeHit = state.playerCards;
                state = clientConnecter.hit(state.sessionId);
                updateCountForNewCards(cardsBeforeHit, state.playerCards); // Update count for hit card

                if (state.reshuffled) { // Check for reshuffle after hit
                    System.out.println("--- RESHUFFLE (Post-Hit) ---");
                    runningCount = 0.0;
                    seenCards.clear();
                    updateCountFromList(state.playerCards, true);
                    updateCountFromList(state.dealerCards, true);
                }
            }

            if (!state.phase.equals("RESOLVED") && state.canStand) {
                List<String> dealerCardsBeforeStand = state.dealerCards;
                state = clientConnecter.stand(state.sessionId);
                updateCountForNewCards(dealerCardsBeforeStand, state.dealerCards); // Update for dealer revealed cards

                if (state.reshuffled) { // Check for reshuffle after stand
                    System.out.println("--- RESHUFFLE (Post-Stand) ---");
                    runningCount = 0.0;
                    seenCards.clear();
                    updateCountFromList(state.playerCards, true);
                    updateCountFromList(state.dealerCards, true);
                }
            }

            // Final outcome processing
            if (state.phase.equals("RESOLVED")) {
                if (state.outcome.equals("PLAYER_WINS")) {
                    numWins++;
                } else if (state.outcome.equals("DEALER_WINS")) {
                    numLosses++;
                } else if (state.outcome.equals("PUSH")) {
                    numPushes++;
                }
            }
            state = clientConnecter.newGame(state.sessionId);
            if ((i + 1) % 1000 == 0) {
                System.out.printf("Sau %d ván  Balance: %d RC: %.1f TC: %.2f%n", (i + 1), state.balance, runningCount,
                        trueCount);
            }
        }
        clientConnecter.finishGame(state.sessionId);
        System.out.println("Number of games played: " + numGames);
        System.out.println("Number of wins: " + numWins);
        System.out.println("Number of losses: " + numLosses);
        System.out.println("Number of pushes: " + numPushes);
        System.out.println("Number of blackjacks: " + numBlackjacks);
        System.out.println("Number of dealer blackjacks: " + numDealerBlackjacks);
        System.out.println("Final balance: " + state.balance);
    }

    /* tách rank từ chuỗi lá bài ("D10" → 'T', "H9" → '9') */
    private static char extractRank(String card) {
        if (card == null || card.length() < 2)
            return ' '; // Return a blank or throw error
        if (card.length() >= 3 && card.charAt(1) == '1' && card.charAt(2) == '0')
            return 'T';
        return card.charAt(1);
    }

    /* xác định hand soft (có Ace đếm 11) */
    private static boolean isSoft(List<String> playerCards, int playerValue) {
        if (playerCards == null)
            return false;
        int hardTotal = 0;
        int numAces = 0;
        for (String card : playerCards) {
            if (card == null || card.length() < 2)
                continue;
            char rank = extractRank(card); // Use extractRank here
            if (rank == 'A') {
                numAces++;
                hardTotal += 1;
            } else if (rank == 'T' || rank == 'J' || rank == 'Q' || rank == 'K') {
                hardTotal += 10;
            } else if (rank >= '2' && rank <= '9') {
                hardTotal += Character.getNumericValue(rank);
            }
        }
        return numAces > 0 && (hardTotal + 10) == playerValue;
    }

    // Re-introduced Counting Helper Methods
    private static double getCardValue(String card) {
        if (card == null || card.length() < 2)
            return 0.0;
        char rank = extractRank(card);
        return cardValues.getOrDefault(rank, 0.0);
    }

    private static void updateCountFromList(List<String> cards, boolean forceUpdate) {
        if (cards == null)
            return;
        for (String card : cards) {
            if (card == null)
                continue;
            // Use a unique identifier for seenCards if cards can be identical across hands
            // but are physically different
            // For now, simple string seen check is okay for one shoe
            if (forceUpdate || !seenCards.containsKey(card) || seenCards.get(card) == 0) {
                double value = getCardValue(card);
                runningCount += value;
                seenCards.put(card, seenCards.getOrDefault(card, 0) + 1);
            }
        }
    }

    private static void updateCountForNewCards(List<String> oldCards, List<String> newCards) {
        if (newCards == null)
            return;
        Map<String, Integer> oldCardCounts = new HashMap<>();
        if (oldCards != null) {
            for (String card : oldCards) {
                if (card == null)
                    continue;
                oldCardCounts.put(card, oldCardCounts.getOrDefault(card, 0) + 1);
            }
        }
        for (String card : newCards) {
            if (card == null)
                continue;
            int oldCount = oldCardCounts.getOrDefault(card, 0);
            if (oldCount > 0) {
                oldCardCounts.put(card, oldCount - 1);
            } else {
                if (!seenCards.containsKey(card) || seenCards.get(card) == 0) {
                    double value = getCardValue(card);
                    runningCount += value;
                    seenCards.put(card, seenCards.getOrDefault(card, 0) + 1);
                } else {
                    // If already seen (e.g. duplicate card string like two "H5"), still mark
                    // another instance as seen.
                    seenCards.put(card, seenCards.getOrDefault(card, 0) + 1);
                }
            }
        }
    }

    private static double estimateDecksRemaining(int cardsRemainingInApi) {
        if (cardsRemainingInApi <= 0)
            return 0.1;
        return (double) cardsRemainingInApi / 52.0;
    }

    private static double calculateTrueCount(double currentRunningCount, double decksRemaining) {
        if (decksRemaining < 0.25)
            return currentRunningCount * 4; // Simplified for very few cards
        return currentRunningCount / decksRemaining;
    }

    private static int decideBet(double trueCount, int baseBet) {
        int betMultiplier = 1;
        if (trueCount >= 1)
            betMultiplier = (int) Math.floor(trueCount);
        if (trueCount < 1 && trueCount > 0)
            betMultiplier = 1; // Don't drop bet if TC is like 0.5
        if (trueCount <= -1)
            betMultiplier = 1; // Or consider not betting min, but for now min bet

        int bet = baseBet * betMultiplier;
        return Math.max(baseBet, Math.min(bet, baseBet * 10)); // Cap bet at 10x base, min baseBet
    }

    // Re-introduce isTenValueOrAce helper
    private static boolean isTenValueOrAce(char rank) {
        return rank == 'T' || rank == 'J' || rank == 'Q' || rank == 'K' || rank == 'A';
    }

    // Modified shouldPlayerHit to include True Count for Deviations
    private static boolean shouldPlayerHit(GameState state, double trueCount, char dealerUpCardRank) {
        int playerValue = state.playerValue;
        boolean softHand = isSoft(state.playerCards, playerValue);
        int upVal = (dealerUpCardRank == 'A') ? 11
                : ((dealerUpCardRank >= 'T' && dealerUpCardRank <= 'K') || dealerUpCardRank == 'X' ? 10
                        : dealerUpCardRank - '0');
        if (dealerUpCardRank == ' ')
            upVal = 0;

        // === Hi-Lo Strategy Deviations ===
        // Player 16 vs. Dealer 10 (T, J, Q, K, A)
        if (playerValue == 16 && isTenValueOrAce(dealerUpCardRank)) {
            return trueCount < 0; // Hit if TC < 0, Stand if TC >= 0
        }
        // Player 15 vs. Dealer 10 (T, J, Q, K, A)
        if (playerValue == 15 && isTenValueOrAce(dealerUpCardRank)) {
            return trueCount < 4; // Hit if TC < 4, Stand if TC >= 4
        }
        // Player 13 vs. Dealer 2
        if (playerValue == 13 && dealerUpCardRank == '2') {
            return trueCount > -1; // Stand if TC <= -1 (Basic stands, this is a deviation to hit more if TC
                                   // positive)
        }
        // Player 13 vs. Dealer 3
        if (playerValue == 13 && dealerUpCardRank == '3') {
            return trueCount > -2; // Stand if TC <= -2
        }
        // Player 12 vs. Dealer 2
        if (playerValue == 12 && dealerUpCardRank == '2') {
            return trueCount < 3; // Hit if TC < 3, Stand if TC >= 3
        }
        // Player 12 vs. Dealer 3
        if (playerValue == 12 && dealerUpCardRank == '3') {
            return trueCount < 2; // Hit if TC < 2, Stand if TC >= 2
        }
        // Player 12 vs. Dealer 4
        if (playerValue == 12 && dealerUpCardRank == '4') {
            return trueCount < 0; // Hit if TC < 0, Stand if TC >= 0
        }
        // Player 12 vs. Dealer 5
        if (playerValue == 12 && dealerUpCardRank == '5') {
            return trueCount < -2; // Hit if TC < -2, Stand if TC >= -2
        }
        // Player 12 vs. Dealer 6
        if (playerValue == 12 && dealerUpCardRank == '6') {
            return trueCount < -1; // Hit if TC < -1, Stand if TC >= -1
        }

        // --- Basic Strategy Fallback (from previous version) ---
        if (softHand) {
            if (playerValue >= 19)
                return false;
            if (playerValue == 18) {
                return !((upVal >= 2 && upVal <= 8));
            }
            return true;
        } else {
            if (playerValue >= 17)
                return false;
            if (playerValue <= 8)
                return true;
            if (playerValue >= 13 && playerValue <= 16) {
                return !((upVal >= 2 && upVal <= 6));
            }
            if (playerValue == 12) {
                return !((upVal >= 4 && upVal <= 6));
            }
            return true;
        }
    }

}