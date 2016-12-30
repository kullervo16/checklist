package kullervo16.checklist.utils;

public class StringUtils {

    public static String nullifyAndTrim(String string) {

        if (string != null) {

            string = string.trim();

            if (string.isEmpty()) {
                string = null;
            }
        }

        return string;
    }

    public static boolean isStringNullOrEmptyOrBlank(final String string) {

        if (string == null) {
            return true;
        }

        return string.trim().isEmpty();
    }
}
