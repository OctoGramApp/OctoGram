package it.octogram.android.preferences.rows;

import android.app.Activity;
import android.view.View;

import org.telegram.ui.ActionBar.BaseFragment;

public interface Clickable {

    boolean onClick(BaseFragment fragment, Activity activity, View view, int position, float x, float y);

}
