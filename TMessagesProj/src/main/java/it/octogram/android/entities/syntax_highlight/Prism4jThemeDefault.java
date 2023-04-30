package it.octogram.android.entities.syntax_highlight;

import android.graphics.Typeface;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.StyleSpan;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.telegram.ui.ActionBar.Theme;

public class Prism4jThemeDefault extends Prism4jThemeBase {

    @NonNull
    public static Prism4jThemeDefault create() {
        return new Prism4jThemeDefault();
    }

    @Override
    public int textColor() {
        return 0xdd000000;
    }

    @NonNull
    @Override
    protected ColorHashMap init() {
        return new ColorHashMap()
                .add(Theme.getColor(Theme.key_code_high_light_annotation), "annotation")
                .add(Theme.getColor(Theme.key_code_high_light_atrule), "atrule")
                .add(Theme.getColor(Theme.key_code_high_light_attr_name), "attr-name")
                .add(Theme.getColor(Theme.key_code_high_light_attr_value), "attr-value")
                .add(Theme.getColor(Theme.key_code_high_light_boolean), "boolean")
                .add(Theme.getColor(Theme.key_code_high_light_builtin), "builtin")
                .add(Theme.getColor(Theme.key_code_high_light_cdata), "cdata")
                .add(Theme.getColor(Theme.key_code_high_light_char), "char")
                .add(Theme.getColor(Theme.key_code_high_light_class_name), "class-name")
                .add(Theme.getColor(Theme.key_code_high_light_comment), "comment")
                .add(Theme.getColor(Theme.key_code_high_light_constant), "constant")
                .add(Theme.getColor(Theme.key_code_high_light_deleted), "deleted")
                .add(Theme.getColor(Theme.key_code_high_light_delimiter), "delimiter")
                .add(Theme.getColor(Theme.key_code_high_light_doctype), "doctype")
                .add(Theme.getColor(Theme.key_code_high_light_entity), "entity")
                .add(Theme.getColor(Theme.key_code_high_light_function), "function")
                .add(Theme.getColor(Theme.key_code_high_light_important), "important")
                .add(Theme.getColor(Theme.key_code_high_light_inserted), "inserted")
                .add(Theme.getColor(Theme.key_code_high_light_keyword), "keyword")
                .add(Theme.getColor(Theme.key_code_high_light_number), "number")
                .add(Theme.getColor(Theme.key_code_high_light_operator), "operator")
                .add(Theme.getColor(Theme.key_code_high_light_prolog), "prolog")
                .add(Theme.getColor(Theme.key_code_high_light_property), "property")
                .add(Theme.getColor(Theme.key_code_high_light_punctuation), "punctuation")
                .add(Theme.getColor(Theme.key_code_high_light_regex), "regex")
                .add(Theme.getColor(Theme.key_code_high_light_selector), "selector")
                .add(Theme.getColor(Theme.key_code_high_light_string), "string")
                .add(Theme.getColor(Theme.key_code_high_light_symbol), "symbol")
                .add(Theme.getColor(Theme.key_code_high_light_tag), "tag")
                .add(Theme.getColor(Theme.key_code_high_light_url), "url")
                .add(Theme.getColor(Theme.key_code_high_light_variable), "variable");
    }

    @Override
    protected void applyColor(
            @NonNull String language,
            @NonNull String type,
            @Nullable String alias,
            @ColorInt int color,
            @NonNull Spannable spannable,
            int start,
            int end) {

        super.applyColor(language, type, alias, color, spannable, start, end);

        if (isOfType("important", type, alias)
                || isOfType("bold", type, alias)) {
            spannable.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE | Spanned.SPAN_COMPOSING);
        }

        if (isOfType("italic", type, alias)) {
            spannable.setSpan(new StyleSpan(Typeface.ITALIC), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE | Spanned.SPAN_COMPOSING);
        }
    }
}
