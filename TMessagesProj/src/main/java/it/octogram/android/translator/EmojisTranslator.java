/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.translator;

import org.telegram.tgnet.TLRPC;

import java.util.HashMap;
import java.util.List;

public class EmojisTranslator {

    private static final List<String> targetLanguages = List.of("en", "it", "fr");

    public static void executeTranslation(String text, SingleTranslationManager.OnTranslationResultCallback callback) {
        TLRPC.TL_textWithEntities finalText = new TLRPC.TL_textWithEntities();
        finalText.text = convertToEmoji(text);

        callback.onResponseReceived();
        callback.onSuccess(finalText);
    }

    private static String convertToEmoji(String input) {
        HashMap<Character, String> emojiMap = new HashMap<>();
        emojiMap.put('a', "\uD83C\uDDE6"); // 🅰️
        emojiMap.put('b', "\uD83C\uDDE7"); // 🅱️
        emojiMap.put('c', "\uD83C\uDDE8"); // 🅲
        emojiMap.put('d', "\uD83C\uDDE9"); // 🅳
        emojiMap.put('e', "\uD83C\uDDEA"); // 🅴
        emojiMap.put('f', "\uD83C\uDDEB"); // 🅵
        emojiMap.put('g', "\uD83C\uDDEC"); // 🅶
        emojiMap.put('h', "\uD83C\uDDED"); // 🅷
        emojiMap.put('i', "\uD83C\uDDEE"); // 🅸
        emojiMap.put('j', "\uD83C\uDDEF"); // 🅹
        emojiMap.put('k', "\uD83C\uDDF0"); // 🅺
        emojiMap.put('l', "\uD83C\uDDF1"); // 🅻
        emojiMap.put('m', "\uD83C\uDDF2"); // 🅼
        emojiMap.put('n', "\uD83C\uDDF3"); // 🅽
        emojiMap.put('o', "\uD83C\uDDF4"); // 🅾️
        emojiMap.put('p', "\uD83C\uDDF5"); // 🅿️
        emojiMap.put('q', "\uD83C\uDDF6"); // 🆀
        emojiMap.put('r', "\uD83C\uDDF7"); // 🆁
        emojiMap.put('s', "\uD83C\uDDF8"); // 🆂
        emojiMap.put('t', "\uD83C\uDDF9"); // 🆃
        emojiMap.put('u', "\uD83C\uDDFA"); // 🆄
        emojiMap.put('v', "\uD83C\uDDFB"); // 🆅
        emojiMap.put('w', "\uD83C\uDDFC"); // 🆆
        emojiMap.put('x', "\uD83C\uDDFD"); // 🆇
        emojiMap.put('y', "\uD83C\uDDFE"); // 🆈
        emojiMap.put('z', "\uD83C\uDDFF"); // 🆉

        StringBuilder result = new StringBuilder();
        for (char ch : input.toLowerCase().toCharArray()) {
            if (emojiMap.containsKey(ch)) {
                result.append(emojiMap.get(ch));
            } else {
                result.append(ch);
            }
        }

        return result.toString();
    }

    public static boolean isUnsupportedLanguage(String completeLanguage) {
        return !targetLanguages.contains(completeLanguage);
    }
}