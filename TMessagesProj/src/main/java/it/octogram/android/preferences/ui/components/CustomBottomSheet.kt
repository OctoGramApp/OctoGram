package it.octogram.android.preferences.ui.components

import android.content.Context
import org.telegram.ui.ActionBar.BottomSheet

open class CustomBottomSheet(
    context: Context,
    needFocus: Boolean
) : BottomSheet(context, needFocus) {

    companion object {
        private var shown = false
    }

    override fun show() {
        if (shown) {
            return
        }
        shown = true
        super.show()
    }
}
