package client;

public enum Card {
    TWO_OF_CLUBS("2C"),
    THREE_OF_CLUBS("3C"),
    FOUR_OF_CLUBS("4C"),
    FIVE_OF_CLUBS("5C"),
    SIX_OF_CLUBS("6C"),
    SEVEN_OF_CLUBS("7C"),
    EIGHT_OF_CLUBS("8C"),
    NINE_OF_CLUBS("9C"),
    TEN_OF_CLUBS("10C"),
    JACK_OF_CLUBS("JC"),
    QUEEN_OF_CLUBS("QC"),
    KING_OF_CLUBS("KC"),
    ACE_OF_CLUBS("AC"),
    TWO_OF_DIAMONDS("2D"),
    THREE_OF_DIAMONDS("3D"),
    FOUR_OF_DIAMONDS("4D"),
    FIVE_OF_DIAMONDS("5D"),
    SIX_OF_DIAMONDS("6D"),
    SEVEN_OF_DIAMONDS("7D"),
    EIGHT_OF_DIAMONDS("8D"),
    NINE_OF_DIAMONDS("9D"),
    TEN_OF_DIAMONDS("10D"),
    JACK_OF_DIAMONDS("JD"),
    QUEEN_OF_DIAMONDS("QD"),
    KING_OF_DIAMONDS("KD"),
    ACE_OF_DIAMONDS("AD"),
    TWO_OF_HEARTS("2H"),
    THREE_OF_HEARTS("3H"),
    FOUR_OF_HEARTS("4H"),
    FIVE_OF_HEARTS("5H"),
    SIX_OF_HEARTS("6H"),
    SEVEN_OF_HEARTS("7H"),
    EIGHT_OF_HEARTS("8H"),
    NINE_OF_HEARTS("9H"),
    TEN_OF_HEARTS("10H"),
    JACK_OF_HEARTS("JH"),
    QUEEN_OF_HEARTS("QH"),
    KING_OF_HEARTS("KH"),
    ACE_OF_HEARTS("AH"),
    TWO_OF_SPADES("2S"),
    THREE_OF_SPADES("3S"),
    FOUR_OF_SPADES("4S"),
    FIVE_OF_SPADES("5S"),
    SIX_OF_SPADES("6S"),
    SEVEN_OF_SPADES("7S"),
    EIGHT_OF_SPADES("8S"),
    NINE_OF_SPADES("9S"),
    TEN_OF_SPADES("10S"),
    JACK_OF_SPADES("JS"),
    QUEEN_OF_SPADES("QS"),
    KING_OF_SPADES("KS"),
    ACE_OF_SPADES("AS"),
    BACK("Back");

    private String string;
    private int value;

    Card(String card) {
        this.string = card;
        if (card.equals("Back")) {
            this.value = 0;
        } else {
            char rankChar;
            // Server sends cards like "H2", "DA", "S10"
            // The first character is the suit, the rest is the rank for single char ranks
            // For "10", the rank part is two characters.
            if (card.length() > 1 && card.substring(1).equals("10")) { // e.g., "S10", "H10"
                rankChar = 'T'; // Represent 10 as 'T' for internal logic consistency
            } else if (card.length() > 1) {
                rankChar = card.charAt(1); // e.g., '2' from "S2", 'A' from "HA", 'K' from "DK"
            } else {
                rankChar = ' '; // Invalid card string format like "S" or ""
            }

            if (rankChar == 'A') {
                this.value = 1; // Blackjack value for Ace (can also be 11, hand logic handles that)
            } else if (rankChar == 'K' || rankChar == 'Q' || rankChar == 'J' || rankChar == 'T') {
                this.value = 10;
            } else if (Character.isDigit(rankChar)) {
                // rankChar will be '2' through '9' here because '1' (from '10') is handled as
                // 'T'
                this.value = Character.getNumericValue(rankChar);
            } else {
                this.value = 0; // Default for unrecognized rankChar (e.g., from invalid card string)
            }
        }
    }

    public static Card fromString(String serverCardString) {
        if (serverCardString == null || serverCardString.trim().isEmpty()) {
            throw new IllegalArgumentException("Card string cannot be null or empty");
        }
        // Convert server string like "EIGHT of HEARTS" to enum name format
        // "EIGHT_OF_HEARTS"
        String enumName = serverCardString.toUpperCase().replace(' ', '_');

        // Special handling for "??" or similar placeholders if server sends them for
        // unknown cards
        if (enumName.equals("???") || enumName.equals("HIDDEN")) {
            // Or throw an error, or return a specific Card.UNKNOWN if you add one
            return Card.BACK; // Assuming Card.BACK is appropriate for a hidden/unknown card display
        }

        try {
            return Card.valueOf(enumName);
        } catch (IllegalArgumentException e) {
            System.err.println("[Card.fromString] Failed to find enum constant for: " + enumName
                    + ". Original server string: '" + serverCardString + "'");
            // Consider returning a default (like Card.BACK) or re-throwing a more specific
            // error
            // For now, re-throwing to make it visible where it's used.
            throw new IllegalArgumentException(
                    "Invalid card name for enum lookup: " + enumName + " (from server: " + serverCardString + ")", e);
        }
    }

    public String toString() {
        return string;
    }

    public String getFilename() {
        return string + ".png";
    }

    public int getValue() {
        return this.value;
    }
}
