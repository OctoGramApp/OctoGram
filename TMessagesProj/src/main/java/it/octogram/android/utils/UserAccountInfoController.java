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

public class UserAccountInfoController {

    public static UserAccountInfo getDcInfo(TLRPC.User user) {
        return getDcInfoInternal(user, null);
    }

    public static UserAccountInfo getDcInfo(TLRPC.Chat chat) {
        return getDcInfoInternal(null, chat);
    }

    private static UserAccountInfo getDcInfoInternal(TLRPC.User user, TLRPC.Chat chat) {
        int dc = 0;
        long id = 0;
        if (user != null) {
            if (UserObject.isUserSelf(user)) {
                dc = AccountInstance.getInstance(UserConfig.selectedAccount).getConnectionsManager().getCurrentDatacenterId();
            } else {
                dc = user.photo != null ? user.photo.dc_id : -1;
            }
            id = user.id;
        } else if (chat != null) {
            dc = chat.photo != null ? chat.photo.dc_id : -1;
            id = getNiceId(chat, chat.id);
        }
        dc = dc != 0 ? dc : -1;
        DcInfo dcInfo = DcInfo.getDcInfo(dc);
        return new UserAccountInfo(id, dcInfo);
    }

    public static long getNiceId(long id) {
        return getNiceId(null, id);
    }

    public static long getNiceId(TLRPC.Chat chat, long id) {
        if (OctoConfig.INSTANCE.dcIdType.getValue() == OctoConfig.DcIdType.BOT_API) {
            if (ChatObject.isChannel(chat)) {
                id = -1000000000000L - id;
            } else {
                id = -id;
            }
        }
        return id;
    }

    public static class UserAccountInfo {
        public final long userId;
        public final DcInfo dcInfo;

        UserAccountInfo(long userId, DcInfo dcInfo) {
            this.userId = userId;
            this.dcInfo = dcInfo;
        }
    }

    public enum DcInfo {
        USA_1(1, "MIA, Miami FL, USA", R.drawable.ic_pluto_datacenter),
        USA_2(3, "MIA, Miami FL, USA", R.drawable.ic_aurora_datacenter),
        AMSTERDAM_1(2, "AMS, Amsterdam, NL", R.drawable.ic_venus_datacenter),
        AMSTERDAM_2(4, "AMS, Amsterdam, NL", R.drawable.ic_vesta_datacenter),
        SINGAPORE(5, "SIN, Singapore, SG", R.drawable.ic_flora_datacenter),
        UNKNOWN(-1, LocaleController.getString("NumberUnknown", R.string.NumberUnknown), R.drawable.msg_secret_hw);

        public final int dcId;
        public final String dcName;
        public final int icon;

        DcInfo(int dcId, String dcName, int icon) {
            this.dcId = dcId;
            this.dcName = dcName;
            this.icon = icon;
        }

        public static DcInfo getDcInfo(int dcId) {
            for (DcInfo dcInfo : values()) {
                if (dcInfo.dcId == dcId) {
                    return dcInfo;
                }
            }
            return UNKNOWN;
        }
    }

}
