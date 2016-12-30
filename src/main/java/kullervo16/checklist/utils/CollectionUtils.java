package kullervo16.checklist.utils;

import java.util.Collection;

public class CollectionUtils {

    public static boolean isCollectionNullOrEmpty(final Collection collection) {

        if (collection == null) {
            return true;
        }

        return collection.isEmpty();
    }
}
