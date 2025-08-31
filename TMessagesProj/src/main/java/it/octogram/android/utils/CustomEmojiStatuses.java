package it.octogram.android.utils;

import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class CustomEmojiStatuses {
    private final static long OCTO_EMOJI_ID = 5868373829826386101L;
    private final static HashMap<Long, Long> assoc = new HashMap<>();
    private final static ArrayList<Long> replaceBackground = new ArrayList<>();
    static {
        assoc.put(1966997491L, 5819078828017849357L); // mainchannel
        assoc.put(1927074211L, 5891243564309942507L); // chat
        assoc.put(1980359671L, 5988023995125993550L); // beta
        assoc.put(1973191602L, 5879883461711367869L); // apk
        assoc.put(2563682223L, 5785326857587003471L); // models
        assoc.put(1733655252L, 5807868868886009920L); // internal
        replaceBackground.add(1733655252L);
    }

    public static boolean hasCustomEmojiId(TLRPC.Chat chat) {
        return chat != null && assoc.containsKey(chat.id);
    }

    public static boolean hasCustomEmojiId(long id) {
        return assoc.containsKey(id);
    }

    public static long getCustomEmojiId(TLRPC.Chat chat) {
        if (hasCustomEmojiId(chat)) {
            return Objects.requireNonNull(assoc.get(chat.id));
        }
        return 0;
    }

    public static long getCustomEmojiId(long id) {
        if (hasCustomEmojiId(id)) {
            return Objects.requireNonNull(assoc.get(id));
        }
        return 0;
    }

    public static long getBackgroundEmojiId(TLRPC.Chat chat) {
        if (hasCustomEmojiId(chat)) {
            return OCTO_EMOJI_ID;
        }
        return 0;
    }

    public static long getBackgroundEmojiId(long id) {
        if (hasCustomEmojiId(id)) {
            return OCTO_EMOJI_ID;
        }
        return 0;
    }

    public static int getCustomColorId(TLRPC.Chat chat) {
        if (chat != null && replaceBackground.contains(chat.id)) {
            return 10;
        }
//        if (chat.profile_color != null && (chat.profile_color.flags & 1) != 0){
//            OctoLogging.d("MainChannelsWrapper", chat.id+" - "+chat.profile_color.color+" - "+chat.profile_color.flags+" - "+chat.profile_color.background_emoji_id);
//        }
        return 0;
    }
}
