/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.app.rows.impl;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

import org.telegram.ui.ActionBar.BaseFragment;

import it.octogram.android.ConfigProperty;
import it.octogram.android.app.PreferenceType;
import it.octogram.android.app.rows.BaseRow;
import it.octogram.android.app.rows.BaseRowBuilder;
import it.octogram.android.app.rows.Clickable;

public class CustomCellRow extends BaseRow implements Clickable {

    private final Runnable onClick;
    private final View layout;
    private final String propertySelectionTag;
    private final boolean avoidReDraw;
    private final boolean isEnabled;
    private boolean bound = false;

    private CustomCellRow(@Nullable CharSequence title, @Nullable String summary, boolean requiresRestart, boolean avoidReDraw, boolean isEnabled, String propertySelectionTag, ConfigProperty<Boolean> showIf, boolean showIfReverse, boolean divider, Runnable onClick, View layout) {
        super(title, summary, requiresRestart, showIf, showIfReverse, divider, PreferenceType.CUSTOM);
        this.onClick = onClick;
        this.layout = layout;
        this.propertySelectionTag = propertySelectionTag;
        this.avoidReDraw = avoidReDraw;
        this.isEnabled = isEnabled;
    }

    @Override
    public boolean onClick(BaseFragment fragment, Activity activity, View view, int position, float x, float y) {
        if (onClick != null)
            onClick.run();
        return true;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public String getPropertySelectionTag() {
        return propertySelectionTag;
    }

    public void bind(FrameLayout frameLayout) {
        if (!avoidReDraw || !bound) {
            bound = true;

            // remove the current view if it exists
            if (frameLayout.getChildCount() > 0) {
                frameLayout.removeAllViews();
            }

            if (layout != null) {
                ViewGroup parent = (ViewGroup) layout.getParent();
                if (parent != null) {
                    parent.removeView(layout);
                }
                frameLayout.addView(layout);

                if (isEnabled) {
                    frameLayout.setClickable(true);
                    frameLayout.setOnClickListener((v) -> {

                    });
                }
            }
        }
    }

    public static class CustomCellRowBuilder extends BaseRowBuilder<CustomCellRow> {

        private Runnable onClick;
        private View layout;
        private String propertySelectionTag;
        private boolean avoidReDraw = false;
        private boolean isEnabled = false;

        public CustomCellRowBuilder layout(View layout) {
            this.layout = layout;
            return this;
        }

        public CustomCellRowBuilder onClick(Runnable onClick) {
            this.onClick = onClick;
            return this;
        }

        public CustomCellRowBuilder avoidReDraw(boolean avoidReDraw) {
            this.avoidReDraw = avoidReDraw;
            return this;
        }

        public CustomCellRowBuilder propertySelectionTag(String propertySelectionTag) {
            this.propertySelectionTag = propertySelectionTag;
            return this;
        }

        public CustomCellRowBuilder isEnabled(boolean isEnabled) {
            this.isEnabled = isEnabled;
            return this;
        }

        @Override
        public CustomCellRow build() {
            return new CustomCellRow(title, description, requiresRestart, avoidReDraw, isEnabled, propertySelectionTag, showIf, showIfReverse, divider, onClick, layout);
        }
    }
}
