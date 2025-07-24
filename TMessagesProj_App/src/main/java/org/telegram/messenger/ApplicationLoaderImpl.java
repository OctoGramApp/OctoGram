package org.telegram.messenger;

import android.content.Context;

import org.telegram.messenger.regular.BuildConfig;
import org.telegram.tgnet.TLRPC;

public class ApplicationLoaderImpl extends ApplicationLoader {
    @Override
    protected String onGetApplicationId() {
        return BuildConfig.APPLICATION_ID;
    }

    @Override
    public boolean showUpdateAppPopup(Context context, TLRPC.TL_help_appUpdate update, int account) {
        return true;
    }
}
