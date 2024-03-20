package it.octogram.android.utils;

import android.content.Context;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.messenger.ApplicationLoader;

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

public class RegistrationDateController {
    private static final DateFormat DATE_FORMAT = DateFormat.getDateInstance();
    private static final JSONObject REGISTRATION_JSON;

    private static final String[] IDS;
    private static final String FILE_NAME = "registration_id.json";
    private static final Long[] AGES;

    private static final long MIN_ID;
    private static final long MAX_ID;

    static {
        try {
            REGISTRATION_JSON = loadAgesFromJson(ApplicationLoader.applicationContext);
            Iterator<String> keysIterator = REGISTRATION_JSON.keys();
            List<String> idList = new ArrayList<>();
            while (keysIterator.hasNext()) {
                String key = keysIterator.next();
                idList.add(key);
            }
            IDS = idList.toArray(new String[0]);

            AGES = new Long[IDS.length];
            for (int i = 0; i < IDS.length; i++) {
                AGES[i] = Long.parseLong(IDS[i]);
            }

            MIN_ID = AGES[0];
            MAX_ID = AGES[AGES.length - 1];
        } catch (IOException | JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @NonNull
    public static String getRegistrationDate(long id) {
        Date dateResult = getRegistration(id);
        if (dateResult == null) {
            return "Unknown";
        }
        return String.format("~ %s", DATE_FORMAT.format(dateResult));
    }

    private static JSONObject loadAgesFromJson(Context context) throws IOException, JSONException {
        InputStream inputStream = context.getAssets().open(FILE_NAME);
        int size = inputStream.available();
        byte[] buffer = new byte[size];
        inputStream.read(buffer);
        inputStream.close();
        return new JSONObject(new String(buffer, StandardCharsets.UTF_8));
    }

    @NonNull
    private static Date getRegistration(long id) {
        try {
            if (id < MIN_ID) {
                return new Date((Long) REGISTRATION_JSON.get(IDS[0]));
            } else if (id > MAX_ID) {
                return new Date((Long) REGISTRATION_JSON.get(IDS[IDS.length - 1]));
            } else {
                int index = Arrays.binarySearch(AGES, id);
                if (index >= 0) {
                    long dateInMillis = (Long) REGISTRATION_JSON.get(IDS[index]);
                    String formattedDate = DATE_FORMAT.format(new Date(dateInMillis));
                    Date parseDate = DATE_FORMAT.parse(formattedDate);
                    if (parseDate == null) {
                        throw new ParseException("Failed to parse date", 0);
                    }
                    return parseDate;
                } else {
                    int insertionPoint = -(index + 1);
                    int lowerId = insertionPoint - 1;
                    long LOW_AGE = (Long) REGISTRATION_JSON.get(IDS[lowerId]);
                    long MAX_AGE = (Long) REGISTRATION_JSON.get(IDS[insertionPoint]);

                    double ID_RATIO = (double) (id - AGES[lowerId]) / (AGES[insertionPoint] - AGES[lowerId]);
                    long MID_DATE = (long) (ID_RATIO * (MAX_AGE - LOW_AGE) + LOW_AGE);
                    String formattedDate = DATE_FORMAT.format(new Date(MID_DATE));
                    Date parsedDate = DATE_FORMAT.parse(formattedDate);
                    if (parsedDate == null) {
                        throw new ParseException("Failed to parse date", 0);
                    }
                    return parsedDate;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
