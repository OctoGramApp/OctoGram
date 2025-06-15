package org.telegram.messenger;

import android.app.Activity;
import android.content.Context;
import android.view.ViewGroup;

import org.telegram.messenger.regular.BuildConfig;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.IUpdateButton;
import org.telegram.ui.IUpdateLayout;

import it.octogram.android.tgastandaloneexport.UpdateAppAlertDialog;
import it.octogram.android.tgastandaloneexport.UpdateButton;
import it.octogram.android.tgastandaloneexport.UpdateLayout;

public class ApplicationLoaderImpl extends ApplicationLoader {
    @Override
    protected String onGetApplicationId() {
        return BuildConfig.APPLICATION_ID;
    }

    @Override
    public boolean showUpdateAppPopup(Context context, TLRPC.TL_help_appUpdate update, int account) {
        try {
            (new UpdateAppAlertDialog(context, update, account)).show();
        } catch (Exception e) {
            FileLog.e(e);
        }
        // UpdateAppAlertDialog IS PART OF TGA STANDALONE BUILD
        // THE OCTOGRAM BEHAVIOR IS MUCH DIFFERENT THAN TGA ONE
        return true;
    }

    @Override
    public IUpdateLayout takeUpdateLayout(Activity activity, ViewGroup sideMenu, ViewGroup sideMenuContainer) {
        // UpdateLayout IS PART OF TGA STANDALONE BUILD
        // THE OCTOGRAM BEHAVIOR IS MUCH DIFFERENT THAN TGA ONE
        return new UpdateLayout(activity, sideMenu, sideMenuContainer);
    }

    @Override
    public IUpdateButton takeUpdateButton(Context context) {
        // UpdateButton IS PART OF TGA STANDALONE BUILD
        // THE OCTOGRAM BEHAVIOR IS MUCH DIFFERENT THAN TGA ONE
        return new UpdateButton(context);
    }
}
