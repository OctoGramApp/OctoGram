package it.octogram.android.preferences;

import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.BaseFragment;

import java.util.ArrayList;

import it.octogram.android.preferences.tgkit.preference.OctoPreferences;
import it.octogram.android.preferences.tgkit.preference.types.TGKitTextDetailRow;
import it.octogram.android.preferences.tgkit.preference.types.TGKitTextIconRow;
import it.octogram.android.preferences.tgkit.preference.types.TGKitFooterRow;

public class TestPreferences implements BasePreferencesEntry {

    @Override
    public OctoPreferences getPreferences(BaseFragment bf) {
        return OctoPreferences.builder("OctoGram Settings")
                .category("Settings", new ArrayList<>() {
                    {
                        add(new TGKitTextIconRow("General", true, R.drawable.msg_media, null));
                        add(new TGKitTextIconRow("Translator", true, R.drawable.msg_translate, null));
                        add(new TGKitTextIconRow("Appearance", true, R.drawable.msg_colors, null));
                        add(new TGKitTextIconRow("Updates", true, R.drawable.msg_photo_rotate, null));
                        add(new TGKitTextIconRow("Experiments", true, R.drawable.msg_colors, null));
                    }
                })
                .category("Info", new ArrayList<>() {
                    {
                        add(new TGKitTextIconRow("Official Channel", true, R.drawable.msg_translate, null));
                        add(new TGKitTextDetailRow("View the source code", "view source", true, null));
                        add(new TGKitTextIconRow("Translate OctoGram", true, R.drawable.msg_colors, null));
                    }
                })
                .add(new TGKitFooterRow("OctoGram", null))
                .build();
    }
}
