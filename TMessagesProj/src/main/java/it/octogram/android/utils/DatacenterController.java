/*
 * This is the source code of OctoGram for Android v.2.0.x
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2024.
 */

package it.octogram.android.utils;

import android.os.SystemClock;

import org.json.JSONObject;
import org.telegram.messenger.AndroidUtilities;

import java.util.ArrayList;

import it.octogram.android.Datacenter;
import it.octogram.android.http.StandardHTTPRequest;

public class DatacenterController {
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
                            var url = String.format("https://raw.githubusercontent.com/OctoGramApp/assets/main/DCStatus/dc_status.json?token=%s",(int) (Math.random() * 10000));
                            var obj = new JSONObject(new StandardHTTPRequest(url).request());
                            var listDatacenters = obj.getJSONArray("status");
                            int refreshTimeIn = obj.getInt("refresh_in_time");
                            var infoArrayList = new DCInfo();
                            for (int i = 0; i < listDatacenters.length(); i++) {
                                var dcInfo = listDatacenters.getJSONObject(i);
                                var dcID = dcInfo.getInt("dc_id");
                                var status = dcInfo.getInt("dc_status");
                                var lastDown = dcInfo.getInt("last_down");
                                var lastLag = dcInfo.getInt("last_lag");
                                var ping = StandardHTTPRequest.ping(Datacenter.Companion.getDcInfo(dcInfo.getInt("dc_id")).getIp());  //dcInfo.getInt("ping");
                                infoArrayList.add(new DCStatus(dcID, status, ping, lastDown, lastLag));
                                SystemClock.sleep(25);
                            }
                            if (updateCallback != null) {
                                AndroidUtilities.runOnUIThread(() -> updateCallback.onUpdate(infoArrayList));
                            }
                            SystemClock.sleep(1000L * refreshTimeIn);
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

    public static class DCInfo extends ArrayList<DCStatus> {

        public DCStatus getByDc(int dcID) {
            for (int i = 0; i < size(); i++) {
                DCStatus DCStatus = get(i);
                if (DCStatus.dcID == dcID) return DCStatus;
            }
            return null;
        }
    }

    public static class DCStatus {
        public final int dcID, status, ping, lastDown, lastLag;

        DCStatus(int dcID, int status, int ping, int lastDown, int lastLag) {
            this.dcID = dcID;
            this.status = status;
            this.ping = ping;
            this.lastDown = lastDown;
            this.lastLag = lastLag;
        }
    }
}