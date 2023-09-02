package it.octogram.android.preferences;

import android.content.Context;

import it.octogram.android.preferences.fragment.PreferencesFragment;
import org.telegram.ui.ActionBar.BaseFragment;

public interface PreferencesEntry {

    OctoPreferences getPreferences(PreferencesFragment fragment, Context context);

}
