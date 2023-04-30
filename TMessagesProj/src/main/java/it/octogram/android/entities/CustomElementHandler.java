package it.octogram.android.entities;

import android.text.Editable;
import android.text.Spannable;
import android.text.Spanned;

import org.telegram.ui.Components.AnimatedEmojiSpan;
import org.xml.sax.Attributes;

public class CustomElementHandler implements HTMLTagAttributesHandler.TagHandler {

    @Override
    public boolean handleTag(boolean opening, String tag, Editable output, Attributes attributes) {
        if (tag.equalsIgnoreCase("tg-emoji")) {
            if (opening) {
                String emojiIdString = HTMLTagAttributesHandler.getValue(attributes, "emoji-id");
                if (emojiIdString != null) {
                    long documentId = Long.parseLong(emojiIdString);
                    output.setSpan(new AnimatedEmojiSpan(documentId, null), output.length(), output.length(), Spanned.SPAN_MARK_MARK);
                    return true;
                }
            } else {
                AnimatedEmojiSpan obj = getLast(output, AnimatedEmojiSpan.class);
                if (obj != null) {
                    int where = output.getSpanStart(obj);
                    output.removeSpan(obj);
                    if (where != output.length()) {
                        output.setSpan(obj, where, output.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private <T> T getLast(Editable text, Class<T> kind) {
        T[] objs = text.getSpans(0, text.length(), kind);
        if (objs.length != 0) {
            for (int i = objs.length; i > 0; i--) {
                if (text.getSpanFlags(objs[i - 1]) == Spannable.SPAN_MARK_MARK) {
                    return objs[i - 1];
                }
            }
        }
        return null;
    }
}