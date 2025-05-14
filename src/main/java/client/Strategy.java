package client;

import java.util.List;

public class Strategy {

    // Define player actions
    public enum Move {
        HIT, // H
        STAND, // S
        DOUBLE, // D (if not allowed, then Hit or Stand based on specific strategy rules)
        SPLIT, // P (if not allowed, then treat as hard/soft total)
        SURRENDER // R (if not allowed, then play normally)
    }

    // Basic Strategy Tables (to be fully populated)
    // Indices: Player's hand total, Dealer's up-card (2, 3, 4, 5, 6, 7, 8, 9, T, A)

    // Example for HARD table: H[player_total - 5][dealer_card_index]
    // Player totals from 5 to 17. If total < 5, always HIT. If total > 17, always
    // STAND (handled in decide).
    private static final String[][] HARD = {
            // Dealer: 2 3 4 5 6 7 8 9 T A
            /* total 5 */{ "H", "H", "H", "H", "H", "H", "H", "H", "H", "H" },
            /* total 6 */{ "H", "H", "H", "H", "H", "H", "H", "H", "H", "H" },
            /* total 7 */{ "H", "H", "H", "H", "H", "H", "H", "H", "H", "H" },
            /* total 8 */{ "H", "H", "H", "H", "H", "H", "H", "H", "H", "H" },
            /* total 9 */{ "H", "D", "D", "D", "D", "H", "H", "H", "H", "H" }, // D=Double, H=Hit
            /* total 10 */{ "D", "D", "D", "D", "D", "D", "D", "D", "H", "H" },
            /* total 11 */{ "D", "D", "D", "D", "D", "D", "D", "D", "D", "H" }, // Common rule: Double 11 vs A is H if
                                                                                // S17
            /* total 12 */{ "H", "H", "S", "S", "S", "H", "H", "H", "H", "H" }, // S=Stand
            /* total 13 */{ "S", "S", "S", "S", "S", "H", "H", "H", "H", "H" },
            /* total 14 */{ "S", "S", "S", "S", "S", "H", "H", "H", "H", "H" },
            /* total 15 */{ "S", "S", "S", "S", "S", "H", "H", "H", "R", "H" }, // R=Surrender if available, else H
            /* total 16 */{ "S", "S", "S", "S", "S", "H", "H", "R", "R", "R" }, // R=Surrender if available, else H (or
                                                                                // S for 16vT if multi-deck S17)
            /* total 17 */{ "S", "S", "S", "S", "S", "S", "S", "S", "S", "S" } // Always Stand on Hard 17+
    };

    // SOFT table: SOFT[player_ace_plus_other_cards_total - 2][dealer_card_index]
    // e.g., A,2 (total 3 for Ace+other) -> index 1. A,7 (total 8) -> index 6
    // Player totals from A,2 (13) to A,9 (20). A,A is a PAIR. A,10 is Blackjack.
    private static final String[][] SOFT = {
            // Dealer: 2 3 4 5 6 7 8 9 T A
            /* A,2 (13) */{ "H", "H", "H", "D", "D", "H", "H", "H", "H", "H" },
            /* A,3 (14) */{ "H", "H", "H", "D", "D", "H", "H", "H", "H", "H" },
            /* A,4 (15) */{ "H", "H", "D", "D", "D", "H", "H", "H", "H", "H" },
            /* A,5 (16) */{ "H", "H", "D", "D", "D", "H", "H", "H", "H", "H" },
            /* A,6 (17) */{ "H", "D", "D", "D", "D", "H", "H", "H", "H", "H" }, // Often S vs 2,7,8 if no double (S17
                                                                                // rules)
            /* A,7 (18) */{ "S", "D", "D", "D", "D", "S", "S", "H", "H", "H" }, // S=Stand, D=Double. If S17, Stand vs
                                                                                // 2,7,8. Hit vs 9,T,A. Double vs 3-6.
            /* A,8 (19) */{ "S", "S", "S", "S", "D", "S", "S", "S", "S", "S" }, // Usually Stand, Double vs 6 (S17)
            /* A,9 (20) */{ "S", "S", "S", "S", "S", "S", "S", "S", "S", "S" },
            /* A,10 (21) */{ "S", "S", "S", "S", "S", "S", "S", "S", "S", "S" } // Blackjack
    };

    // PAIR table: PAIR[card_rank_value - 1][dealer_card_index] (A=1, 2=2,... T=10)
    private static final String[][] PAIR = {
            // Dealer: 2 3 4 5 6 7 8 9 T A
            /* A,A */ { "P", "P", "P", "P", "P", "P", "P", "P", "P", "P" }, // P=Split
            /* 2,2 */ { "P", "P", "P", "P", "P", "P", "H", "H", "H", "H" }, // Split 2-7 vs dealer 2-7, else Hit
            /* 3,3 */ { "P", "P", "P", "P", "P", "P", "H", "H", "H", "H" },
            /* 4,4 */ { "H", "H", "H", "P", "P", "H", "H", "H", "H", "H" }, // Split 5,6
            /* 5,5 */ { "D", "D", "D", "D", "D", "D", "D", "D", "H", "H" }, // Never split 5s, treat as Hard 10
            /* 6,6 */ { "P", "P", "P", "P", "P", "H", "H", "H", "H", "H" }, // Split 2-6
            /* 7,7 */ { "P", "P", "P", "P", "P", "P", "H", "H", "H", "H" }, // Split 2-7
            /* 8,8 */ { "P", "P", "P", "P", "P", "P", "P", "P", "P", "P" }, // Always Split 8s (except vs A if surrender
                                                                            // preferred & allowed)
            /* 9,9 */ { "P", "P", "P", "P", "P", "S", "P", "P", "S", "S" }, // Stand vs 7, 10, A
            /* T,T */ { "S", "S", "S", "S", "S", "S", "S", "S", "S", "S" } // Never split 10s
    };

    private static int dealerCardToIndex(char dealerUpCard) {
        switch (dealerUpCard) {
            case '2':
                return 0;
            case '3':
                return 1;
            case '4':
                return 2;
            case '5':
                return 3;
            case '6':
                return 4;
            case '7':
                return 5;
            case '8':
                return 6;
            case '9':
                return 7;
            case 'T':
            case 'J':
            case 'Q':
            case 'K':
                return 8;
            case 'A':
                return 9;
            default:
                return -1; // Should not happen
        }
    }

    private static int pairRankToIndex(String pairRank) {
        if (pairRank == null || pairRank.isEmpty())
            return -1;
        char r = pairRank.charAt(0);
        if (r == 'A')
            return 0;
        if (r >= '2' && r <= '9')
            return Character.getNumericValue(r) - 1; // 2 maps to 1, ..., 9 maps to 8
        if (r == 'T' || r == 'J' || r == 'Q' || r == 'K')
            return 9; // T,J,Q,K maps to 9 (for 10s)
        return -1;
    }

    public static Move decide(int playerValue, boolean isSoft, boolean canSplit, String pairCardRank,
            char dealerUpCard) {
        // TC-deviations could be checked here first if desired.

        int dealerIdx = dealerCardToIndex(dealerUpCard);
        if (dealerIdx == -1)
            return Move.STAND; // Failsafe

        String moveChar = "";

        if (canSplit) {
            int pairIdx = pairRankToIndex(pairCardRank);
            if (pairIdx != -1) {
                // Ensure pairIdx is within bounds for PAIR table
                if (pairIdx >= 0 && pairIdx < PAIR.length && dealerIdx < PAIR[pairIdx].length) {
                    moveChar = PAIR[pairIdx][dealerIdx];
                } else {
                    // Fallback if index out of bounds, treat as non-pair
                    canSplit = false; // Clear flag and proceed to hard/soft logic
                }
            } else {
                canSplit = false; // Invalid pair rank
            }
        }

        if (!canSplit) { // If not splitting (or decided not to split from table)
            if (isSoft) {
                if (playerValue >= 21)
                    return Move.STAND; // Soft 21 (BJ) or more stands
                if (playerValue <= 12)
                    return Move.HIT; // Soft 12 or less (e.g. A,A before split) always hits if not split
                // Player value for soft hands here usually means Ace + other cards (e.g. A,7 is
                // 18)
                // Soft table index: A,2 (playerValue 13) is index 0 in a table from A,2 to A,9
                // The SOFT table above is indexed from A,2 up to A,9 (values 13 to 20)
                // playerValue 13 (A,2) -> SOFT table index 0
                // playerValue 20 (A,9) -> SOFT table index 7
                int softIdx = playerValue - 13; // A,2 (13) is index 0, A,7 (18) is index 5, A,9 (20) is index 7
                if (softIdx >= 0 && softIdx < SOFT.length && dealerIdx < SOFT[softIdx].length) {
                    moveChar = SOFT[softIdx][dealerIdx];
                } else {
                    moveChar = "S"; // Fallback e.g. A,T (21) or A,9 (20)
                    if (playerValue < 20)
                        moveChar = "H"; // Should be covered, but general fallback
                }
            } else { // Hard hand
                if (playerValue >= 17)
                    return Move.STAND;
                if (playerValue <= 8)
                    return Move.HIT; // Standard: Hit 8 or less
                // Hard table index: Hard 5 to Hard 16
                // playerValue 5 -> HARD table index 0
                // playerValue 16 -> HARD table index 11
                // playerValue 17 -> HARD table index 12
                int hardIdx = playerValue - 5;
                if (hardIdx >= 0 && hardIdx < HARD.length && dealerIdx < HARD[hardIdx].length) {
                    moveChar = HARD[hardIdx][dealerIdx];
                } else {
                    moveChar = "S"; // Fallback if out of bounds (e.g. >17 handled, <5 should be H)
                    if (playerValue < 17)
                        moveChar = "H";
                }
            }
        }

        // Parse character to Move
        switch (moveChar) {
            case "H":
                return Move.HIT;
            case "S":
                return Move.STAND;
            case "D":
                return Move.DOUBLE;
            case "P":
                return Move.SPLIT;
            case "R":
                return Move.SURRENDER;
            default:
                // This fallback should ideally not be reached if tables are correct
                // If it's a "D" but double not allowed by game rules, strategy often becomes
                // Hit or Stand.
                // This basic `decide` doesn't yet handle "D not allowed, then H" explicitly.
                // For now, if table says "D" and game doesn't allow, Autoplayer needs to adapt.
                if (playerValue < 17 && !isSoft)
                    return Move.HIT; // Default for safety on hard low totals
                return Move.STAND; // Default fallback
        }
    }

    // Helper to determine if a hand is soft
    // (Moved from Autoplayer's shouldPlayerHit and simplified for direct use)
    public static boolean isSoft(List<String> playerCards, int playerValue) {
        if (playerCards == null || playerCards.isEmpty()) {
            return false;
        }
        int numAces = 0;
        int hardTotal = 0;
        for (String card : playerCards) {
            if (card == null || card.length() < 2)
                continue;
            char rank = card.charAt(1);
            if (rank == '1' && card.length() > 2 && card.charAt(2) == '0')
                rank = 'T'; // Handle "10"

            if (rank == 'A') {
                numAces++;
                hardTotal += 1; // Count Ace as 1 for hard total initially
            } else if (rank == 'T' || rank == 'J' || rank == 'Q' || rank == 'K') {
                hardTotal += 10;
            } else if (rank >= '2' && rank <= '9') {
                hardTotal += Character.getNumericValue(rank);
            }
        }
        // If one ace is counted as 11, and the total matches playerValue, it's soft.
        return numAces > 0 && (hardTotal + 10 == playerValue);
    }
}