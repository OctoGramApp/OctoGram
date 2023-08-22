/*
 * This is the source code of OctoGram for Android v.2.0.x
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023.
 */

package it.octogram.android.utils;

import it.octogram.android.OctoConfig;
import org.telegram.messenger.*;
import org.telegram.tgnet.TLRPC;

import java.util.WeakHashMap;

public class DCUtils {

    private static final WeakHashMap<Integer, String> dcNames = new WeakHashMap<>();
    static {
        dcNames.put(1, "MIA, Miami FL, USA");
        dcNames.put(3, "MIA, Miami FL, USA");
        dcNames.put(2, "AMS, Amsterdam, NL");
        dcNames.put(4, "AMS, Amsterdam, NL");
        dcNames.put(5, "SIN, Singapore, SG");
    }

    public static DCInfo getDcInfo(TLRPC.User user) {
        return getDcInfoInternal(user, null);
    }

    public static DCInfo getDcInfo(TLRPC.Chat chat) {
        return getDcInfoInternal(null, chat);
    }

    private static DCInfo getDcInfoInternal(TLRPC.User user, TLRPC.Chat chat) {
        int dc = 0;
        int currentAccount = UserConfig.selectedAccount;
        int myDC = AccountInstance.getInstance(currentAccount).getConnectionsManager().getCurrentDatacenterId();
        long id = 0;
        if (user != null) {
            if (UserObject.isUserSelf(user) && myDC != -1) {
                dc = myDC;
            } else {
                dc = user.photo != null ? user.photo.dc_id : -1;
            }
            id = user.id;
        } else if (chat != null) {
            dc = chat.photo != null ? chat.photo.dc_id : -1;
            if (OctoConfig.INSTANCE.dcIdType.getValue() == OctoConfig.DcIdType.BOT_API) {
                if (ChatObject.isChannel(chat)) {
                    id = -1000000000000L - chat.id;
                } else {
                    id = -chat.id;
                }
            } else {
                id = chat.id;
            }
        }
        dc = dc != 0 ? dc : -1;
        String dcName = dcNames.getOrDefault(dc, LocaleController.getString("NumberUnknown", R.string.NumberUnknown));
        return new DCInfo(dc, id, dcName);
    }


    public static class DCInfo {
        public final int dcId;
        public final String dcNameType;
        public final long userId;

        DCInfo(int dcId, long userId, String dcNameType) {
            this.dcId = dcId;
            this.userId = userId;
            this.dcNameType = dcNameType;
        }
    }

}
