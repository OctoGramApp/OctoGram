package it.octogram.android.utils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import it.octogram.android.WebPages;

public class WebPingController {
    public static final int WAITING_FOR_USER = 0;
    public static final int PINGING = 1;
    public static final int CONNECTED = 2;
    public static final int FAILED = 3;
    public static final int INTERRUPTED = 4;
    private UpdateCallback callback;
    private Thread thread;
    private final ArrayList<Integer> parsedPageIds = new ArrayList<>();

    public void setCallback(UpdateCallback callback) {
        this.callback = callback;
    }

    public void pingSites(int specificSite) {
        if (callback == null) {
            return;
        }

        if (thread != null) {
            thread.interrupt();
        }

        parsedPageIds.clear();

        thread = new Thread() {
            @Override
            public void run() {
                for (WebPages page : WebPages.getEntries()) {
                    if (specificSite == 0 || specificSite == page.getId()) {
                        callback.onUpdate(page.getId(), PINGING);
                    }
                }

                for (WebPages page : WebPages.getEntries()) {
                    if (Thread.currentThread().isInterrupted()) {
                        return;
                    }

                    if (specificSite != 0 && specificSite != page.getId()) {
                        continue;
                    }

                    try {
                        long dnsResolved = System.currentTimeMillis();
                        URL url = new URL(page.getWebsite());
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("GET");
                        connection.setConnectTimeout(8000);
                        connection.setReadTimeout(8000);
                        connection.connect();
                        int diff = Math.round(System.currentTimeMillis() - dnsResolved);
                        if (connection.getResponseCode() == 200) {
                            callback.onUpdate(page.getId(), CONNECTED, diff);
                        } else {
                            callback.onUpdate(page.getId(), FAILED, connection.getResponseCode());
                        }
                    } catch (IOException e) {
                        callback.onUpdate(page.getId(), FAILED);
                    }
                    parsedPageIds.add(page.getId());
                }

                if (Thread.currentThread().isInterrupted()) {
                    return;
                }

                if (specificSite == 0) {
                    callback.onCompletedCycle();
                }
            }
        };
        thread.start();
    }

    public void pingSites() {
        pingSites(0);
    }

    public void stopMonitor() {
        if (thread != null) {
            thread.interrupt();
        }

        for (WebPages page : WebPages.getEntries()) {
            if (!parsedPageIds.contains(page.getId())) {
                callback.onUpdate(page.getId(), INTERRUPTED);
            }
        }

        parsedPageIds.clear();
    }

    public interface UpdateCallback {
        void onUpdate(int pageId, int status, int parameter);
        default void onUpdate(int pageId, int status) {
            onUpdate(pageId, status, 0);
        }
        void onCompletedCycle();
    }
}
