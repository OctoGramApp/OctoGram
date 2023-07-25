package it.octogram.android.preferences;

import android.content.Context;

import org.telegram.ui.ActionBar.BaseFragment;

public interface PreferencesEntry {

    OctoPreferences getPreferences(BaseFragment fragment, Context context);

}
