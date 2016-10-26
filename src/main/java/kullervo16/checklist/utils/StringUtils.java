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
}
