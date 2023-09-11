/*
 * This is the source code of OctoGram for Android v.2.0.x
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023.
 */

package it.octogram.android.utils;

import it.octogram.android.Datacenter;
import org.telegram.messenger.AccountInstance;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.tgnet.TLRPC;

import it.octogram.android.OctoConfig;

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
        Datacenter dcInfo = Datacenter.getDcInfo(dc);
        return new UserAccountInfo(id, dcInfo);
    }

    public static long getNiceId(long id) {
        return getNiceId(null, id);
    }

    public static long getNiceId(TLRPC.Chat chat, long id) {
        if (OctoConfig.INSTANCE.dcIdType.getValue() == OctoConfig.DcIdType.BOT_API) {
            if (ChatObject.isChannel(chat)) {
                id = -1000000000000L - id;
            }
        }
        return id;
    }

    public static class UserAccountInfo {
        public final long userId;
        public final Datacenter dcInfo;

        UserAccountInfo(long userId, Datacenter dcInfo) {
            this.userId = userId;
            this.dcInfo = dcInfo;
        }
    }

}
