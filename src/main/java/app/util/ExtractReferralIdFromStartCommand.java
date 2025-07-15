package app.util;

public class ExtractReferralIdFromStartCommand {

    public static Long extract(String input) {
        if (input == null || input.isEmpty()) {
            return 0L;
        }

        final String prefix = "/start ";
        if (!input.startsWith(prefix)) {
            return 0L;
        }

        String idPart = input.substring(prefix.length()).trim();
        try {
            return Long.parseLong(idPart);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}
