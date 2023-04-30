package it.octogram.android.utils;

import android.graphics.Color;
import android.os.SystemClock;

import org.json.JSONArray;
import org.json.JSONObject;
import org.telegram.messenger.AccountInstance;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;
import java.util.Locale;
import java.util.NoSuchElementException;

import it.octogram.android.OctoConfig;
import it.octogram.android.http.StandardHTTPRequest;

public class DCHelper {
    public static TInfo getTInfo(TLRPC.User userInfo) {
        return getTInfo(userInfo, null);
    }

    public static TInfo getTInfo(TLRPC.Chat currentChat) {
        return getTInfo(null, currentChat);
    }

    public static TInfo getTInfo(TLRPC.User user, TLRPC.Chat chat) {
        int DC = 0;
        int currentAccount = UserConfig.selectedAccount;
        int myDC = AccountInstance.getInstance(currentAccount).getConnectionsManager().getCurrentDatacenterId();
        long id = 0;
        if (user != null) {
            if (UserObject.isUserSelf(user) && myDC != -1) {
                DC = myDC;
            } else {
                DC = user.photo != null ? user.photo.dc_id : -1;
            }
            id = user.id;
        } else if (chat != null) {
            DC = chat.photo != null ? chat.photo.dc_id : -1;
            if (OctoConfig.idType == 0) {
                if (ChatObject.isChannel(chat)) {
                    id = -1000000000000L - chat.id;
                } else {
                    id = -chat.id;
                }
            } else {
                id = chat.id;
            }
        }
        DC = DC != 0 ? DC : -1;
        String DC_NAME = DCHelper.getDcName(DC);
        if (DC != -1) {
            DC_NAME = String.format(Locale.ENGLISH, "%s - DC%d", DC_NAME, DC);
        }
        return new TInfo(DC, id, DC_NAME);
    }

    public static class TInfo {
        public final int dcID;
        public final String longDcName;
        public final long tID;

        TInfo(int dcID, long tID, String longDcName) {
            this.dcID = dcID;
            this.tID = tID;
            this.longDcName = longDcName;
        }
    }

    public static int getDCColor(int dc_id) {
        switch (dc_id) {
            case 1:
                return 0xFF329AFE;
            case 2:
                return 0xFF8B31FD;
            case 3:
                return 0xFFDA5653;
            case 4:
                return 0xFFF7B139;
            case 5:
                return 0xFF4BD199;
            default:
                return Color.TRANSPARENT;
        }
    }

    public static String getDCIp(int dc_id)
        throws NoSuchElementException {

        switch (dc_id) {
            case 1:
                return "149.154.175.50";
            case 2:
                return "149.154.167.50";
            case 3:
                return "149.154.175.100";
            case 4:
                return "149.154.167.91";
            case 5:
                return "91.108.56.100";
            default:
                throw new NoSuchElementException("Unknown DC");
        }
    }

    public static String getDcName(int dc_id) {
        switch (dc_id) {
            case 1:
            case 3:
                return "MIA, Miami FL, USA";
            case 2:
            case 4:
                return "AMS, Amsterdam, NL";
            case 5:
                return "SIN, Singapore, SG";
            default:
                return LocaleController.getString("NumberUnknown", R.string.NumberUnknown);
        }
    }

    public static int getDcIcon(int dc_id) {
        switch (dc_id) {
            case 1:
                return R.drawable.ic_pluto_datacenter;
            case 2:
                return R.drawable.ic_venus_datacenter;
            case 3:
                return R.drawable.ic_aurora_datacenter;
            case 4:
                return R.drawable.ic_vesta_datacenter;
            case 5:
                return R.drawable.ic_flora_datacenter;
            default:
                return R.drawable.msg_secret_hw;
        }
    }

    public static int getDcIconLittle(int dc_id) {
        switch (dc_id) {
            case 1:
                return R.drawable.ic_pluto_datacenter_little;
            case 2:
                return R.drawable.ic_venus_datacenter_little;
            case 3:
                return R.drawable.ic_aurora_datacenter_little;
            case 4:
                return R.drawable.ic_vesta_datacenter_little;
            case 5:
                return R.drawable.ic_flora_datacenter_little;
            default:
                return R.drawable.msg_secret_hw;
        }
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
                        String url = "https://app.octogram.app/dc_status";
                        JSONObject obj = new JSONObject(new StandardHTTPRequest(url).request());
                        JSONArray listDatacenters = obj.getJSONArray("status");
                        int refreshTimeIn = obj.getInt("refresh_in_time");
                        DatacenterList infoArrayList = new DatacenterList();
                        for (int i = 0; i < listDatacenters.length(); i++) {
                            JSONObject dcInfo = listDatacenters.getJSONObject(i);
                            int dcID = dcInfo.getInt("dc_id");
                            int status = dcInfo.getInt("dc_status");
                            int ping = StandardHTTPRequest.ping(DCHelper.getDCIp(dcID));
                            infoArrayList.add(new DatacenterInfo(dcID, status, ping));
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
            void onUpdate(DatacenterList result);
        }
    }

    public static class DatacenterList extends ArrayList<DatacenterInfo> {

        public DatacenterInfo getByDc(int dcID) {
            for (int i = 0; i < size(); i++) {
                DatacenterInfo datacenterInfo = get(i);
                if (datacenterInfo.dcID == dcID) return datacenterInfo;
            }
            return null;
        }
    }


    public static class DatacenterInfo {
        public final int dcID, status, ping;

        DatacenterInfo(int dcID, int status, int ping) {
            this.dcID = dcID;
            this.status = status;
            this.ping = ping;
        }
    }
}
