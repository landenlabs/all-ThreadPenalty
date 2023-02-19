package com.landenlabs.allThreadPenalty.util;

import android.content.SharedPreferences;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Helper methods to store/load a bundle in a SharedPreferences
 */
public class SaveUtil {
    private static final String SAVED_PREFS_BUNDLE_KEY_SEPARATOR = ",";
    private static final String KEY_ARRAY = "array_";
    private static final String KEY_ARRAY_LEN = "0len_";
    private static final String KEY_ARRAY_DATA = "1data_";

    /**
     * Save a Bundle object to SharedPreferences.
     * <p>
     * NOTE: The editor must be writable, and this function does not commit.
     *
     * @param editor      SharedPreferences Editor
     * @param key         SharedPreferences key under which to store the bundle data. Note this key must
     *                    not contain '§§' as it's used as a delimiter
     * @param preferences Bundled preferences
     */
    public static void savePreferencesBundle(SharedPreferences.Editor editor, String key, Bundle preferences) {
        Set<String> keySet = preferences.keySet();
        Iterator<String> it = keySet.iterator();
        String prefKeyPrefix = key + SAVED_PREFS_BUNDLE_KEY_SEPARATOR;

        while (it.hasNext()) {
            String bundleKey = it.next();
            Object o = preferences.get(bundleKey);
            String itemKey = prefKeyPrefix + bundleKey;
            if (o == null) {
                editor.remove(itemKey);
            } else if (o instanceof Integer) {
                editor.putInt(itemKey, (Integer) o);
            } else if (o instanceof Long) {
                editor.putLong(itemKey, (Long) o);
            } else if (o instanceof Float) {
                editor.putFloat(itemKey, (Float) o);
            } else if (o instanceof Boolean) {
                editor.putBoolean(itemKey, (Boolean) o);
            } else if (o instanceof CharSequence) {
                editor.putString(itemKey, ((CharSequence) o).toString());
            } else if (o instanceof Bundle) {
                savePreferencesBundle(editor, itemKey, ((Bundle) o));
            } else if (o instanceof float[]) {
                float[] values = (float[]) o;
                editor.putInt(itemKey + KEY_ARRAY + KEY_ARRAY_LEN, values.length);
                for (int idx = 0; idx < values.length; idx++) {
                    editor.putFloat(itemKey + KEY_ARRAY + KEY_ARRAY_DATA + idx, values[idx]);
                }
            } else {
                throw new UnsupportedOperationException(o.getClass().getSimpleName());
            }
        }
    }

    /**
     * Load a Bundle object from SharedPreferences.
     * (that was previously stored using savePreferencesBundle())
     * <p>
     * NOTE: The editor must be writable, and this function does not commit.
     *
     * @param sharedPreferences SharedPreferences
     * @param key               SharedPreferences key under which to store the bundle data. Note this key must
     *                          not contain '§§' as it's used as a delimiter
     * @return bundle loaded from SharedPreferences
     */
    public static Bundle loadPreferencesBundle(SharedPreferences sharedPreferences, String key) {
        Bundle bundle = new Bundle();
        Map<String, ?> all = sharedPreferences.getAll();
        List<String> sortedList = new ArrayList<>(all.keySet());
        Collections.sort(sortedList);
        Iterator<String> it = sortedList.iterator();
        String prefKeyPrefix = key + SAVED_PREFS_BUNDLE_KEY_SEPARATOR;
        Set<String> subBundleKeys = new HashSet<String>();
        while (it.hasNext()) {

            String prefKey = it.next();

            if (prefKey.startsWith(prefKeyPrefix)) {
                String bundleKey = prefKey.substring(prefKeyPrefix.length());

                if (bundleKey.contains(KEY_ARRAY + KEY_ARRAY_LEN)) {
                    int len = (Integer) all.get(prefKey);
                    float[] fArray = new float[len];
                    String arrayKey = KEY_ARRAY + KEY_ARRAY_DATA;
                    int pos = prefKey.indexOf(KEY_ARRAY) + arrayKey.length();
                    for (int idx = 0; idx < len; idx++) {
                        prefKey = it.next();
                        if (prefKey.contains(KEY_ARRAY + KEY_ARRAY_DATA)) {
                            float fValue = (Float) all.get(prefKey);
                            Integer arrayIdx = Integer.parseInt(prefKey.substring(pos));
                            fArray[arrayIdx] = fValue;
                        } else {
                            throw new UnsupportedOperationException(prefKey + " array data explected");
                        }
                    }
                    bundle.putFloatArray(bundleKey.replace(KEY_ARRAY + KEY_ARRAY_LEN, ""), fArray);

                } else if (!bundleKey.contains(SAVED_PREFS_BUNDLE_KEY_SEPARATOR)) {
                    Object o = all.get(prefKey);
                    if (o == null) {
                        // Ignore null keys
                    } else if (o instanceof Integer) {
                        bundle.putInt(bundleKey, (Integer) o);
                    } else if (o instanceof Long) {
                        bundle.putLong(bundleKey, (Long) o);
                    } else if (o instanceof Float) {
                        bundle.putFloat(bundleKey, (Float) o);
                    } else if (o instanceof Boolean) {
                        bundle.putBoolean(bundleKey, (Boolean) o);
                    } else if (o instanceof CharSequence) {
                        bundle.putString(bundleKey, ((CharSequence) o).toString());
                    }
                } else {
                    // Key is for a sub bundle
                    // String subBundleKey = StringUtils.substringBefore(bundleKey, SAVED_PREFS_BUNDLE_KEY_SEPARATOR);
                    String subBundleKey = bundleKey.substring(0, bundleKey.indexOf(SAVED_PREFS_BUNDLE_KEY_SEPARATOR));
                    subBundleKeys.add(subBundleKey);
                }
            } else {
                // Key is not related to this bundle.
            }
        }

        // Recursively process the sub-bundles
        for (String subBundleKey : subBundleKeys) {
            Bundle subBundle = loadPreferencesBundle(sharedPreferences, prefKeyPrefix + subBundleKey);
            bundle.putBundle(subBundleKey, subBundle);
        }

        return bundle;
    }
}
