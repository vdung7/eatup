package vn.momo.eatup.provider;

import android.net.Uri;
import android.provider.BaseColumns;

import vn.momo.eatup.BuildConfig;

/**
 * Convenience definitions for NotePadProvider
 */
public final class EatUpProviderAPI {
    public static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".provider.eatwhat";

    // This class cannot be instantiated
    private EatUpProviderAPI() {
    }

    public static final class EatWhatColumn implements BaseColumns {
        private EatWhatColumn() {
        }

        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/eatwhat");
        public static final String CONTENT_TYPE = "vn.momo.eatwhat.cursor.dir/" + BuildConfig.APPLICATION_ID + ".eatwhat";
        public static final String CONTENT_ITEM_TYPE = "vn.momo.eatwhat.cursor.item/" + BuildConfig.APPLICATION_ID + ".eatwhat";

        public static final String NAME = "name";
        public static final String LAST_EAT_DATE = "last_eat_date";
        public static final String EAT_TIMES = "eat_times";
        public static final String EAT_FOR = "eat_for";
        public static final String LOCATION = "location";
    }
}
