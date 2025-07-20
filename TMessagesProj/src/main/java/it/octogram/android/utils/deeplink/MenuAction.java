/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.utils.deeplink;

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
        MenuActionDef.BROWSER_HOME_ID,
        MenuActionDef.DATA_AND_STORAGE,
        MenuActionDef.AI_FEATURE
})
public @interface MenuAction {
}
