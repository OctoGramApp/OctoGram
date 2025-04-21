/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.preferences.rows.impl;

import android.app.Activity;
import android.view.View;

import androidx.annotation.Nullable;

import org.telegram.ui.ActionBar.BaseFragment;

import java.util.function.Consumer;

import it.octogram.android.ConfigProperty;
import it.octogram.android.preferences.PreferenceType;
import it.octogram.android.preferences.rows.BaseRow;
import it.octogram.android.preferences.rows.BaseRowBuilder;
import it.octogram.android.preferences.rows.Clickable;

public class CustomAIModelRow extends BaseRow implements Clickable {

    private final String modelID;
    private final Runnable onClick;
    private final Consumer<View> onShowOptions;


    public CustomAIModelRow(@Nullable String modelID, boolean divider, ConfigProperty<Boolean> showIf, boolean showIfReverse, Runnable onClick, Consumer<View> onShowOptions) {
        super(modelID, null, false, showIf, showIfReverse, divider, PreferenceType.CUSTOM_AI_MODEL);
        this.modelID = modelID;
        this.onClick = onClick;
        this.onShowOptions = onShowOptions;
    }

    public String getModelID() {
        return modelID;
    }

    public Consumer<View> getOnShowOptions() {
        return onShowOptions;
    }

    @Override
    public boolean onClick(BaseFragment fragment, Activity activity, View view, int position, float x, float y) {
        if (onClick != null) {
            onClick.run();
            return true;
        }
        return false;
    }

    public static class CustomAIModelRowBuilder extends BaseRowBuilder<CustomAIModelRow> {
        private String modelID;
        private Runnable onClick;
        private Consumer<View> onShowOptions;

        public CustomAIModelRowBuilder modelID(String modelID) {
            this.modelID = modelID;
            return this;
        }

        public CustomAIModelRowBuilder onClick(Runnable onClick) {
            this.onClick = onClick;
            return this;
        }

        public CustomAIModelRowBuilder onShowOptions(Consumer<View> onShowOptions) {
            this.onShowOptions = onShowOptions;
            return this;
        }

        @Override
        public CustomAIModelRow build() {
            return new CustomAIModelRow(modelID, divider, showIf, showIfReverse, onClick, onShowOptions);
        }
    }
}
