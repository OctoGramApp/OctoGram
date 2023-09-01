/*
 * This is the source code of OctoGram for Android v.2.0.x
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023.
 */

package it.octogram.android.utils;

import android.os.SystemClock;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import org.telegram.messenger.AndroidUtilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class DatacenterController {

    private static int token = 1;
    private static final Gson GSON = new Gson();
    private static final Object lock = new Object();

    private static DCInfo fetchDCStatus() {
        synchronized (lock) {
            try {
                String assetsUrl = "https://raw.githubusercontent.com/OctoGramApp/assets/main/DCStatus/dc_status.json?token=" + ++token;
                URL url = new URL(assetsUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line);
                    }
                    reader.close();

                    String json = stringBuilder.toString();
                    // Parse the JSON string and create an instance of DCInfo
                    return parseJson(json);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static class DatacenterStatusChecker {
        private UpdateCallback updateCallback;
        private boolean isRunning = false;
        private boolean doneStopRunning = true;
        private Thread thread;

        public void setOnUpdate(UpdateCallback updateCallback) {
            this.updateCallback = updateCallback;
        }

        public void runListener() {
            if (isRunning) return;
            isRunning = true;
            if (!doneStopRunning) return;
            thread = new Thread() {
                @Override
                public void run() {
                    while (isRunning) {
                        try {
                            DCInfo currentDCStatus = fetchDCStatus();
                            if (currentDCStatus != null) {
                                if (updateCallback != null) {
                                    AndroidUtilities.runOnUIThread(() -> updateCallback.onUpdate(currentDCStatus));
                                }
                                SystemClock.sleep(1000L * currentDCStatus.refresh_in_time);
                            }
                            SystemClock.sleep(1000L);
                        } catch (Exception ignored) {
                            SystemClock.sleep(1000L);
                        }
                    }
                    doneStopRunning = true;
                }
            };
            thread.start();
        }

        public void stop(boolean forced) {
            isRunning = false;
            doneStopRunning = forced;
            if (forced) {
                thread.interrupt();
            }
        }

        public interface UpdateCallback {
            void onUpdate(DCInfo result);
        }
    }

    private static DCInfo parseJson(String json) {
        return GSON.fromJson(json, DCInfo.class);
    }

    public static class DCInfo {
        @SerializedName("status")
        public List<DCStatus> status;

        @SerializedName("last_refresh")
        public int last_refresh;

        @SerializedName("refresh_in_time")
        public int refresh_in_time;

        @SerializedName("is_refreshing")
        public boolean is_refreshing;

        public DCInfo(List<DCStatus> dc_status, int last_refresh, int refresh_in_time, boolean is_refreshing) {
            this.status = dc_status;
            this.last_refresh = last_refresh;
            this.refresh_in_time = refresh_in_time;
            this.is_refreshing = is_refreshing;
        }

        public DCStatus getByDc(int id) {
            for (DCStatus datacenterInfo : status) {
                if (datacenterInfo.dc_id == id) return datacenterInfo;
            }
            return null;
        }
    }

    public static class DCStatus {
        @SerializedName("dc_id")
        public int dc_id;

        @SerializedName("ping")
        public int ping;

        @SerializedName("dc_status")
        public int dc_status;

        @SerializedName("last_down")
        public int last_down;

        @SerializedName("last_lag")
        public int last_lag;

        public DCStatus(int dc_id, int ping, int dc_status, int last_down, int last_lag) {
            this.dc_id = dc_id;
            this.ping = ping;
            this.dc_status = dc_status;
            this.last_down = last_down;
            this.last_lag = last_lag;
        }
    }
}
