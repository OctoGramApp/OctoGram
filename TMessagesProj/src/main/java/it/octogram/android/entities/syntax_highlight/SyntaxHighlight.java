package it.octogram.android.entities.syntax_highlight;

import android.graphics.Color;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextUtils;

import org.telegram.tgnet.TLRPC;
import org.telegram.ui.Components.TextStyleSpan;

import io.noties.prism4j.DefaultGrammarLocator;
import io.noties.prism4j.Prism4j;

public class SyntaxHighlight {

    private static final Prism4jThemeDefault theme = Prism4jThemeDefault.create();
    private static Prism4jSyntaxHighlight highlight;

    public static void highlight(TextStyleSpan.TextStyleRun run, Spannable spannable) {
        if (run.urlEntity instanceof TLRPC.TL_messageEntityHashtag) {
            var length = run.end - run.start;
            if (length == 7 || length == 9) {
                try {
                    int color = Color.parseColor(spannable.subSequence(run.start, run.end).toString());
                    spannable.setSpan(new ColorHighlightSpan(color, run), run.end - 1, run.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                } catch (IllegalArgumentException ignore) {
                }
            }
        } else if (!TextUtils.isEmpty(run.urlEntity.language)) {
            if (highlight == null) {
                highlight = Prism4jSyntaxHighlight.create(new Prism4j(new DefaultGrammarLocator()), theme);
            }
            highlight.highlight(run.urlEntity.language, spannable, run.start, run.end);
        }
    }

    public static void highlight(String language, int start, int end, Spannable spannable) {
        if (!TextUtils.isEmpty(language)) {
            if (highlight == null) {
                highlight = Prism4jSyntaxHighlight.create(new Prism4j(new DefaultGrammarLocator()), theme);
            }
            highlight.highlight(language, spannable, start, end);
        }
    }

    public static void updateColors() {
        theme.updateColors();
    }
}
