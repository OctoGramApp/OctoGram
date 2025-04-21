/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.utils.network;

import android.os.SystemClock;

import org.telegram.messenger.Utilities;

import java.net.SocketException;

import it.octogram.android.Datacenter;
import it.octogram.android.http.StandardHTTPRequest;
import it.octogram.android.preferences.ui.custom.DatacenterStatus;

public class DatacenterController {
    public static class DatacenterStatusChecker {
        private UpdateCallback updateCallback;
        private Thread thread;

        public void setOnUpdate(UpdateCallback updateCallback) {
            this.updateCallback = updateCallback;
        }

        public void runListener() {
            if (updateCallback == null) {
                return;
            }

            if (thread != null) {
                thread.interrupt();
            }

            thread = new Thread() {
                @Override
                public void run() {
                    while (!Thread.currentThread().isInterrupted()) {
                        updateCallback.onNewCycle();

                        long timeToSleep = 60 * 1000L;
                        for (int i = 1; i <= 5; i++) {
                            Datacenter dcInfo = Datacenter.Companion.getDcInfo(i);

                            try {
                                var ping = StandardHTTPRequest.ping(dcInfo.getIp());
                                updateCallback.onUpdate(i, DatacenterStatus.AVAILABLE, ping);
                            } catch (SocketException e) {
                                updateCallback.onUpdate(i, DatacenterStatus.UNAVAILABLE);
                            }

                            SystemClock.sleep(500L);
                            timeToSleep -= 500L;
                            // handle SLOW dc status && better timing
                        }

                        SystemClock.sleep(Utilities.clamp(timeToSleep, 60 * 1000L, 5 * 1000L));
                    }
                }
            };
            thread.start();
        }

        public void stop() {
            thread.interrupt();
        }

        public interface UpdateCallback {
            void onUpdate(int dcId, int status, int parameter);
            default void onUpdate(int dcId, int status) {
                onUpdate(dcId, status, 0);
            }
            void onNewCycle();
        }
    }
}