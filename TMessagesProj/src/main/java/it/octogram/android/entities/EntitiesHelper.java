package it.octogram.android.entities;


import android.graphics.Typeface;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.LocaleSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;
import android.view.View;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.Emoji;
import org.telegram.messenger.MediaDataController;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AnimatedEmojiSpan;
import org.telegram.ui.Components.TextStyleSpan;
import org.telegram.ui.Components.URLSpanMono;
import org.telegram.ui.Components.URLSpanNoUnderline;
import org.telegram.ui.Components.URLSpanReplacement;
import org.telegram.ui.Components.URLSpanUserMention;

import java.util.ArrayList;

public class EntitiesHelper {

    public static Spanned getSpannableString(String text, ArrayList<TLRPC.MessageEntity> entities, int offset) {
        for (int a = 0; a < entities.size(); a++) {
            TLRPC.MessageEntity entity = entities.get(a);
            entity.offset = entity.offset + offset;
        }
        return getSpannableString(text, entities, false);
    }

    public static Spanned getSpannableString(String text, ArrayList<TLRPC.MessageEntity> entities) {
        return getSpannableString(text, entities, false);
    }

    public static Spanned getSpannableString(String text, ArrayList<TLRPC.MessageEntity> entities, boolean includeLinks) {
        Editable messSpan = new SpannableStringBuilder(text);
        MediaDataController.addTextStyleRuns(entities, messSpan, messSpan, -1);
        MediaDataController.addAnimatedEmojiSpans(entities, messSpan, null);
        applySpansToSpannable(-1, -1, messSpan, 0, text.length(), includeLinks);
        return messSpan;
    }

    public static void applySpansToSpannable(int rS, int rE, Editable spannableString, int startSpan, int endSpan, boolean includeLinks) {
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

    private static void applyTelegramSpannable(Editable outputSpannable, Editable spannableString, int endSpan) {
        CharacterStyle[] mSpans = spannableString.getSpans(0, endSpan, CharacterStyle.class);
        RawSpannableInfo rawSpannableInfo = null;
        for (CharacterStyle mSpan : mSpans) {
            int start = spannableString.getSpanStart(mSpan);
            int end = spannableString.getSpanEnd(mSpan);
            CharacterStyle result = null;
            if (mSpan instanceof URLSpan) {
                URLSpan urlSpan = (URLSpan) mSpan;
                if (urlSpan.getURL() != null) {
                    if (urlSpan.getURL().startsWith("tg://user?id=")) {
                        String id = urlSpan.getURL().replace("tg://user?id=", "");
                        result = new URLSpanUserMention(id, 1);
                    } else {
                        result = new URLSpanReplacement(urlSpan.getURL());
                    }
                }
            } else if (mSpan instanceof StyleSpan) {
                StyleSpan styleSpan = (StyleSpan) mSpan;
                switch (styleSpan.getStyle()) {
                    case Typeface.BOLD:
                        TextStyleSpan.TextStyleRun run = new TextStyleSpan.TextStyleRun();
                        run.flags |= TextStyleSpan.FLAG_STYLE_BOLD;
                        result = new TextStyleSpan(run);
                        break;
                    case Typeface.ITALIC:
                        TextStyleSpan.TextStyleRun run2 = new TextStyleSpan.TextStyleRun();
                        run2.flags |= TextStyleSpan.FLAG_STYLE_ITALIC;
                        result = new TextStyleSpan(run2);
                        break;
                    case Typeface.BOLD_ITALIC:
                        TextStyleSpan.TextStyleRun run3 = new TextStyleSpan.TextStyleRun();
                        run3.flags |= TextStyleSpan.FLAG_STYLE_ITALIC;
                        run3.flags |= TextStyleSpan.FLAG_STYLE_BOLD;
                        result = new TextStyleSpan(run3);
                        break;
                }
            } else if (mSpan instanceof TypefaceSpan) {
                TextStyleSpan.TextStyleRun run = new TextStyleSpan.TextStyleRun();
                run.flags |= TextStyleSpan.FLAG_STYLE_MONO;
                LocaleSpan[] localeSpans = spannableString.getSpans(start, end, LocaleSpan.class);
                if (localeSpans != null && localeSpans.length > 0 && localeSpans[0].getLocale() != null && "og".equalsIgnoreCase(localeSpans[0].getLocale().getCountry())) {
                    TLRPC.TL_messageEntityPre entity = new TLRPC.TL_messageEntityPre();
                    entity.offset = start;
                    entity.length = end - entity.offset;
                    entity.language = localeSpans[0].getLocale().getLanguage();
                    run.urlEntity = entity;
                }
                result = new TextStyleSpan(run);
            } else if (mSpan instanceof UnderlineSpan) {
                TextStyleSpan.TextStyleRun run = new TextStyleSpan.TextStyleRun();
                run.flags |= TextStyleSpan.FLAG_STYLE_UNDERLINE;
                result = new TextStyleSpan(run);
            } else if (mSpan instanceof StrikethroughSpan) {
                TextStyleSpan.TextStyleRun run = new TextStyleSpan.TextStyleRun();
                run.flags |= TextStyleSpan.FLAG_STYLE_STRIKE;
                result = new TextStyleSpan(run);
            } else if ((mSpan instanceof ForegroundColorSpan && ((ForegroundColorSpan) mSpan).getForegroundColor() == Theme.getColor(Theme.key_chat_messagePanelText)) || mSpan instanceof BackgroundColorSpan) {
                if (mSpan instanceof BackgroundColorSpan) {
                    ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(Theme.getColor(Theme.key_chat_messagePanelText));
                    outputSpannable.removeSpan(mSpan);
                    outputSpannable.setSpan(foregroundColorSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                TextStyleSpan.TextStyleRun run = new TextStyleSpan.TextStyleRun();
                run.flags |= TextStyleSpan.FLAG_STYLE_SPOILER;
                result = new TextStyleSpan(run);
            } else if (mSpan instanceof ForegroundColorSpan) {
                outputSpannable.removeSpan(mSpan);
                continue;
            } else if (mSpan instanceof AnimatedEmojiSpan) {
                AnimatedEmojiSpan[] spans = outputSpannable.getSpans(start, end, AnimatedEmojiSpan.class);
                if (spans != null && spans.length > 0) {
                    continue;
                }
                result = mSpan;
            }
            if (result != null) {
                if (result instanceof TextStyleSpan) {
                    if (rawSpannableInfo != null) {
                        if (rawSpannableInfo.start == start && rawSpannableInfo.end == end) {
                            rawSpannableInfo.textStyleSpan.getTextStyleRun().merge(((TextStyleSpan) result).getTextStyleRun());
                        } else {
                            outputSpannable.setSpan(rawSpannableInfo.textStyleSpan, rawSpannableInfo.start, rawSpannableInfo.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            rawSpannableInfo = new RawSpannableInfo((TextStyleSpan) result, start, end);
                        }
                    } else {
                        rawSpannableInfo = new RawSpannableInfo((TextStyleSpan) result, start, end);
                    }
                } else {
                    if (rawSpannableInfo != null) {
                        outputSpannable.setSpan(rawSpannableInfo.textStyleSpan, rawSpannableInfo.start, rawSpannableInfo.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        rawSpannableInfo = null;
                    }
                    outputSpannable.setSpan(result, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        }
        if (rawSpannableInfo != null) {
            outputSpannable.setSpan(rawSpannableInfo.textStyleSpan, rawSpannableInfo.start, rawSpannableInfo.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
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

    public static boolean containsMarkdown(CharSequence text) {
        text = AndroidUtilities.getTrimmedString(text);
        CharSequence[] message = new CharSequence[]{AndroidUtilities.getTrimmedString(text)};
        return MediaDataController.getInstance(UserConfig.selectedAccount).getEntities(message, true, false).size() > 0;
    }

    public static boolean isEmoji(String message) {
        return Emoji.fullyConsistsOfEmojis(message);
    }

    static class RawSpannableInfo {
        public final TextStyleSpan textStyleSpan;
        public final int start;
        public final int end;

        public RawSpannableInfo(TextStyleSpan textStyleSpan, int start, int end) {
            this.textStyleSpan = textStyleSpan;
            this.start = start;
            this.end = end;
        }
    }
}
