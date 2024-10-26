package it.octogram.android.deeplink;

import androidx.annotation.IntDef;

@IntDef({
        MenuActionDef.PREFERENCES_ID,
        MenuActionDef.SEARCH_DIALOGS_ID,
        MenuActionDef.SESSIONS_ID,
        MenuActionDef.DIALOGS_FOLDER_ID,
        MenuActionDef.DATACENTER_ID,
        MenuActionDef.QR_LOGIN_ID,
        MenuActionDef.LITE_MODE_ID,
        MenuActionDef.PROXY_LIST_ID,
        MenuActionDef.BROWSER_HOME_ID
})
public @interface MenuAction {
}
