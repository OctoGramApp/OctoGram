/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.preferences.rows;

import android.app.Activity;
import android.view.View;

import org.telegram.ui.ActionBar.BaseFragment;

public interface Clickable {

    boolean onClick(BaseFragment fragment, Activity activity, View view, int position, float x, float y);

}
