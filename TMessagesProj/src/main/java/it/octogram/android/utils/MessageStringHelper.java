/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.utils;

import android.graphics.Typeface;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.LocaleSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;

import org.telegram.messenger.MediaDataController;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.TextStyleSpan;
import org.telegram.ui.Components.URLSpanMono;
import org.telegram.ui.Components.URLSpanNoUnderline;
import org.telegram.ui.Components.URLSpanReplacement;
import org.telegram.ui.Components.URLSpanUserMention;

import java.util.ArrayList;
import java.util.Locale;

public class MessageStringHelper {

    public static Spanned getSpannableString(String text, ArrayList<TLRPC.MessageEntity> entities, boolean includeLinks) {
        Editable messSpan = new SpannableStringBuilder(text);
        MediaDataController.addTextStyleRuns(entities, messSpan, messSpan, -1);
        MediaDataController.addAnimatedEmojiSpans(entities, messSpan, null);
        applySpansToSpannable(-1, -1, messSpan, 0, text.length(), includeLinks);
        return messSpan;
    }

    private static void applySpansToSpannable(int rS, int rE, Editable spannableString, int startSpan, int endSpan, boolean includeLinks) {
        if (endSpan - startSpan <= 0) {
            return;
        }
        if (rS >= 0 && rE >= 0) {
            CharacterStyle[] mSpansDelete = spannableString.getSpans(rS, rE, CharacterStyle.class);
            for (CharacterStyle mSpan : mSpansDelete) {
                spannableString.removeSpan(mSpan);
            }
        }
        CharacterStyle[] mSpans = spannableString.getSpans(startSpan, endSpan, CharacterStyle.class);
        for (CharacterStyle mSpan : mSpans) {
            int start = spannableString.getSpanStart(mSpan);
            int end = spannableString.getSpanEnd(mSpan);
            if (mSpan instanceof URLSpanMono) {
                TextStyleSpan.TextStyleRun run = new TextStyleSpan.TextStyleRun();
                run.flags |= TextStyleSpan.FLAG_STYLE_MONO;
                mSpan = new TextStyleSpan(run);
            }
            if (mSpan instanceof URLSpanUserMention) {
                TextStyleSpan.TextStyleRun run = new TextStyleSpan.TextStyleRun();
                run.flags |= TextStyleSpan.FLAG_STYLE_MENTION;
                TLRPC.TL_messageEntityMentionName entityMention = new TLRPC.TL_messageEntityMentionName();
                entityMention.user_id = Long.parseLong(((URLSpanUserMention) mSpan).getURL());
                run.urlEntity = entityMention;
                mSpan = new TextStyleSpan(run);
            }
            if (mSpan instanceof URLSpanReplacement) {
                TextStyleSpan.TextStyleRun run = new TextStyleSpan.TextStyleRun();
                run.flags = ((URLSpanReplacement) mSpan).getTextStyleRun().flags;
                run.urlEntity = ((URLSpanReplacement) mSpan).getTextStyleRun().urlEntity;
                mSpan = new TextStyleSpan(run);
            }
            if (mSpan instanceof TextStyleSpan) {
                boolean isBold = (((TextStyleSpan) mSpan).getStyleFlags() & TextStyleSpan.FLAG_STYLE_BOLD) > 0;
                boolean isItalic = (((TextStyleSpan) mSpan).getStyleFlags() & TextStyleSpan.FLAG_STYLE_ITALIC) > 0;
                if (isBold && !isItalic) {
                    spannableString.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                if (!isBold && isItalic) {
                    spannableString.setSpan(new StyleSpan(Typeface.ITALIC), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                if (isBold && isItalic) {
                    spannableString.setSpan(new StyleSpan(Typeface.BOLD_ITALIC), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                if ((((TextStyleSpan) mSpan).getStyleFlags() & TextStyleSpan.FLAG_STYLE_MONO) > 0) {
                    TextStyleSpan.TextStyleRun runner = ((TextStyleSpan) mSpan).getTextStyleRun();
                    if (runner.urlEntity != null && !TextUtils.isEmpty(runner.urlEntity.language)) {
                        spannableString.setSpan(new LocaleSpan(Locale.forLanguageTag(runner.urlEntity.language + "-og")), runner.urlEntity.offset, runner.urlEntity.offset + runner.urlEntity.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    spannableString.setSpan(new TypefaceSpan("monospace"), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                if ((((TextStyleSpan) mSpan).getStyleFlags() & TextStyleSpan.FLAG_STYLE_UNDERLINE) > 0) {
                    spannableString.setSpan(new UnderlineSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                if ((((TextStyleSpan) mSpan).getStyleFlags() & TextStyleSpan.FLAG_STYLE_STRIKE) > 0) {
                    spannableString.setSpan(new StrikethroughSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                if (((((TextStyleSpan) mSpan).getStyleFlags() & TextStyleSpan.FLAG_STYLE_SPOILER) > 0)) {
                    spannableString.setSpan(new ForegroundColorSpan(Theme.getColor(Theme.key_chat_messagePanelText)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                if ((((TextStyleSpan) mSpan).getStyleFlags() & TextStyleSpan.FLAG_STYLE_URL) > 0) {
                    String url = ((TextStyleSpan) mSpan).getTextStyleRun().urlEntity.url;
                    String urlEntity = spannableString.subSequence(start, end).toString();
                    if (url != null && urlEntity.endsWith("/") && !url.endsWith("/")) {
                        urlEntity = urlEntity.substring(0, urlEntity.length() - 1);
                    }
                    if (url != null && (includeLinks || (!url.equals(urlEntity) && !url.equals(String.format("http://%s", urlEntity)) && !url.equals(String.format("https://%s", urlEntity))))) {
                        spannableString.setSpan(new URLSpan(url), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
                if ((((TextStyleSpan) mSpan).getStyleFlags() & TextStyleSpan.FLAG_STYLE_MENTION) > 0) {
                    TLRPC.MessageEntity urlEntity = ((TextStyleSpan) mSpan).getTextStyleRun().urlEntity;
                    long id;
                    if (urlEntity instanceof TLRPC.TL_inputMessageEntityMentionName) {
                        id = ((TLRPC.TL_inputMessageEntityMentionName) urlEntity).user_id.user_id;
                    } else {
                        id = ((TLRPC.TL_messageEntityMentionName) urlEntity).user_id;
                    }
                    spannableString.setSpan(new URLSpan("tg://user?id=" + id), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            } else if (mSpan instanceof URLSpan) {
                spannableString.removeSpan(mSpan);
            }
        }
    }

    public static CharSequence getUrlNoUnderlineText(CharSequence charSequence) {
        Spannable spannable = new SpannableString(charSequence);
        URLSpan[] spans = spannable.getSpans(0, charSequence.length(), URLSpan.class);
        for (URLSpan urlSpan : spans) {
            URLSpan span = urlSpan;
            int start = spannable.getSpanStart(span);
            int end = spannable.getSpanEnd(span);
            spannable.removeSpan(span);
            span = new URLSpanNoUnderline(span.getURL()) {
                @Override
                public void onClick(View widget) {
                    super.onClick(widget);
                }
            };
            spannable.setSpan(span, start, end, 0);
        }
        return spannable;
    }

    public static String getUrlNoUnderlineText(String stringSequence) {
        CharSequence result = getUrlNoUnderlineText((CharSequence) stringSequence);
        return result.toString();
    }

    public static Spanned fromHtml(@NonNull String source) {
        return fromHtml(source, null);
    }

    public static Spanned fromHtml(@NonNull String source, Html.TagHandler tagHandler) {
        return HtmlCompat.fromHtml(source, HtmlCompat.FROM_HTML_MODE_LEGACY,null, tagHandler);
    }
}
