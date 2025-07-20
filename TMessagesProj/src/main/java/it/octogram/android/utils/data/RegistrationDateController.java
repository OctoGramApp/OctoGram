/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.utils.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.Cells.UserInfoCell;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import it.octogram.android.utils.OctoLogging;

public class RegistrationDateController {
    private static final String TAG = "RegistrationDateCtrl";
    private static final DateFormat dateFormat = DateFormat.getDateInstance();
    private static final JSONObject registrationData;

    private static final String[] idKeys;
    private static final String REGISTRATION_FILE_NAME = "registration_id.json";
    private static final Long[] sortedIds;

    private static final long minSortedId;
    private static final long maxSortedId;

    static {
        JSONObject loadedRegistrationData = null;
        String[] loadedIdKeys = new String[0];
        Long[] loadedSortedIds = new Long[0];
        long loadedMinSortedId = 0;
        long loadedMaxSortedId = 0;

        Context context = ApplicationLoader.applicationContext;
        if (context == null) {
            OctoLogging.e(TAG, "Application context is null, RegistrationDateController will not be initialized properly.");
            registrationData = new JSONObject();
            idKeys = loadedIdKeys;
            sortedIds = loadedSortedIds;
            minSortedId = loadedMinSortedId;
            maxSortedId = loadedMaxSortedId;
        } else {
            try {
                loadedRegistrationData = loadRegistrationDataFromJson(context);
                Iterator<String> keysIterator = loadedRegistrationData.keys();
                List<String> idKeyList = new ArrayList<>();
                while (keysIterator.hasNext()) {
                    idKeyList.add(keysIterator.next());
                }
                loadedIdKeys = idKeyList.toArray(new String[0]);

                loadedSortedIds = new Long[loadedIdKeys.length];
                for (int i = 0; i < loadedIdKeys.length; i++) {
                    loadedSortedIds[i] = Long.parseLong(loadedIdKeys[i]);
                }
                Arrays.sort(loadedSortedIds);

                if (loadedSortedIds.length > 0) {
                    loadedMinSortedId = loadedSortedIds[0];
                    loadedMaxSortedId = loadedSortedIds[loadedSortedIds.length - 1];
                } else {
                    OctoLogging.w(TAG, "No registration IDs found in JSON file.");
                }


            } catch (IOException | JSONException e) {
                OctoLogging.e(TAG, "Error loading registration data from JSON", e);
                loadedRegistrationData = new JSONObject();
                loadedIdKeys = new String[0];
                loadedSortedIds = new Long[0];
            } finally {
                registrationData = loadedRegistrationData != null ? loadedRegistrationData : new JSONObject();
                idKeys = loadedIdKeys;
                sortedIds = loadedSortedIds;
                minSortedId = loadedMinSortedId;
                maxSortedId = loadedMaxSortedId;
            }
        }
    }

    @NonNull
    public static String getRegistrationDate(long id) {
        return getRegistrationDate(id, null);
    }

    @NonNull
    public static String getRegistrationDate(long id, TLRPC.PeerSettings settings) {
        if (settings != null && settings.registration_month != null) {
            return String.format(Locale.US, "%s", UserInfoCell.displayDate(settings.registration_month));
        }
        Date registrationDateResult = getRegistration(id);
        if (registrationDateResult == null) {
            return "Unknown";
        }
        return String.format(Locale.US, "~ %s", dateFormat.format(registrationDateResult));
    }

    @NonNull
    private static JSONObject loadRegistrationDataFromJson(Context context) throws IOException, JSONException {
        try (InputStream inputStream = context.getAssets().open(REGISTRATION_FILE_NAME)) {
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            var ignore = inputStream.read(buffer);
            return new JSONObject(new String(buffer, StandardCharsets.UTF_8.name()));
        }
    }

    @Nullable
    private static Date getRegistration(long id) {
        if (registrationData == null || idKeys == null || sortedIds == null || sortedIds.length == 0) {
            return null;
        }

        try {
            if (id < minSortedId) {
                if (idKeys.length > 0 && registrationData.has(idKeys[0])) {
                    return getDateFromMillis(registrationData.optLong(idKeys[0]));
                } else {
                    return null;
                }
            } else if (id > maxSortedId) {
                if (idKeys.length > 0 && registrationData.has(idKeys[idKeys.length - 1])) {
                    return getDateFromMillis(registrationData.optLong(idKeys[idKeys.length - 1]));
                } else {
                    return null;
                }
            } else {
                int searchIndex = Arrays.binarySearch(sortedIds, id);
                if (searchIndex >= 0) {
                    if (registrationData.has(idKeys[searchIndex])) {
                        return getDateFromMillis(registrationData.optLong(idKeys[searchIndex]));
                    } else {
                        return null;
                    }
                } else {
                    int insertionIndex = -(searchIndex + 1);
                    if (insertionIndex <= 0 || insertionIndex >= sortedIds.length) {
                        return null;
                    }
                    int lowerIndex = insertionIndex - 1;

                    if (!registrationData.has(idKeys[lowerIndex]) || !registrationData.has(idKeys[insertionIndex])) {
                        return null;
                    }

                    long lowerDateInMillis = registrationData.optLong(idKeys[lowerIndex]);
                    long upperDateInMillis = registrationData.optLong(idKeys[insertionIndex]);

                    long lowerSortedId = sortedIds[lowerIndex];
                    long upperSortedId = sortedIds[insertionIndex];

                    if (upperSortedId - lowerSortedId == 0) {
                        return getDateFromMillis(lowerDateInMillis);
                    }

                    double idRatio = (double) (id - lowerSortedId) / (upperSortedId - lowerSortedId);
                    long interpolatedDateInMillis = (long) (idRatio * (upperDateInMillis - lowerDateInMillis) + lowerDateInMillis);
                    return getDateFromMillis(interpolatedDateInMillis);
                }
            }
        } catch (Exception e) {
            OctoLogging.e(TAG, "Error getting registration date for id: " + id, e);
            return null;
        }
    }

    @Nullable
    private static Date getDateFromMillis(long milliseconds) {
        if (milliseconds <= 0) {
            return null;
        }
        Date date = new Date(milliseconds);
        String formattedDate = dateFormat.format(date);
        try {
            return dateFormat.parse(formattedDate);
        } catch (ParseException e) {
            OctoLogging.e(TAG, "Error parsing formatted date: " + formattedDate, e);
            return date;
        }
    }
}