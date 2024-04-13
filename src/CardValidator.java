public class CardValidator {

    public static boolean validateCardNumber(String cardNumber) {
        // Remove any spaces and dashes from the card number
        cardNumber = cardNumber.replaceAll("[\\s-]+", "");

        // Check if the card number contains only digits
        if (!cardNumber.matches("\\d+")) {
            return false;
        }

        int sum = 0;
        boolean alternate = false;
        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int digit = Integer.parseInt(cardNumber.substring(i, i + 1));
            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit = (digit % 10) + 1;
                }
            }
            sum += digit;
            alternate = !alternate;
        }

        return (sum % 10 == 0);
    }
}
