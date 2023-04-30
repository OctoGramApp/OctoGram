package it.octogram.android.translator;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import it.octogram.android.OctoConfig;

public class AutoTranslateConfig {
    private static final SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("OctoDialogConfig", Context.MODE_PRIVATE);

    public static boolean isAutoTranslateEnabled(long dialogId, int topicId) {
        if (hasAutoTranslateConfig(dialogId, topicId)) {
            return preferences.getBoolean(getExceptionsKey(dialogId, topicId), OctoConfig.autoTranslate);
        } else {
            return preferences.getBoolean(getExceptionsKey(dialogId, 0), OctoConfig.autoTranslate);
        }
    }

    public static boolean hasAutoTranslateConfig(long dialogId, int topicId) {
        return preferences.contains(getExceptionsKey(dialogId, topicId));
    }

    public static void setEnabled(long dialogId, int topicId, boolean enable) {
        preferences.edit().putBoolean(getExceptionsKey(dialogId, topicId), enable).apply();
        if (isAllTopicEnabledOrDisabled(dialogId, enable) || topicId == 0) {
            preferences.edit().putBoolean(getExceptionsKey(dialogId, 0), enable).apply();
            deleteAllTopicExceptions(dialogId);
        }
    }

    public static boolean isDefault(long dialogId, int topicId) {
        return !hasAutoTranslateConfig(dialogId, topicId);
    }

    public static void removeGroupException(long dialogId) {
        preferences.edit().remove(getExceptionsKey(dialogId, 0)).apply();
        deleteAllTopicExceptions(dialogId);
    }

    private static void deleteAllTopicExceptions(long dialogId) {
        getAllExceptions().stream()
                .filter(e -> e.dialogId == dialogId)
                .filter(e -> e.topicId != 0)
                .forEach(e -> preferences.edit().remove(getExceptionsKey(e.dialogId, e.topicId)).apply());
    }

    public static void removeAllTypeExceptions(boolean isAllowed) {
        getAllExceptions().stream()
                .filter(e -> e.isAllow == isAllowed)
                .forEach(e -> preferences.edit().remove(getExceptionsKey(e.dialogId, e.topicId)).apply());
    }

    private static boolean isAllTopicEnabledOrDisabled(long dialogId, boolean enabled) {
        List<TLRPC.TL_forumTopic> topics = MessagesController.getInstance(UserConfig.selectedAccount).getTopicsController().getTopics(-dialogId);
        if (topics != null) {
            return topics.stream().allMatch(t -> isAutoTranslateEnabled(dialogId, t.id) == enabled);
        } else {
            return true;
        }
    }

    public static boolean isLastTopicAvailable(long dialogId, int topicId, boolean enabled) {
        List<TLRPC.TL_forumTopic> topics = MessagesController.getInstance(UserConfig.selectedAccount).getTopicsController().getTopics(-dialogId);
        if (topics != null) {
            return topics.stream().filter(t -> t.id != topicId).anyMatch(t -> isAutoTranslateEnabled(dialogId, t.id) == enabled);
        } else {
            return false;
        }
    }

    public static void setDefault(long dialogId, int topicId) {
        preferences.edit().remove(getExceptionsKey(dialogId, topicId)).apply();
    }

    private static String getExceptionsKey(long dialogId, int topicId) {
        return "exceptions_" + UserConfig.selectedAccount + "_" + dialogId + (topicId != 0 ? "_" + topicId : "");
    }

    public static void migrate() {
        preferences.getAll().entrySet().stream()
                .filter(entry -> entry.getKey().startsWith("autoTranslate_"))
                .forEach(entry -> {
                    String key = entry.getKey();
                    String[] parts = key.split("_");
                    long dialogId = Long.parseLong(parts[1]);
                    int topicId = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;
                    boolean value = (boolean) entry.getValue();
                    setEnabled(dialogId, topicId, value);
                    preferences.edit().remove(key).apply();
                });
    }

    public static class AutoTranslateException {
        public final long dialogId;
        public final int topicId;
        public final boolean isAllow;
        public TLObject chat;

        public AutoTranslateException(long dialogId, int topicId, boolean isAllow) {
            this.dialogId = dialogId;
            this.topicId = topicId;
            this.isAllow = isAllow;
            if (dialogId > 0) {
                this.chat = MessagesController.getInstance(UserConfig.selectedAccount).getUser(dialogId);
            } else {
                this.chat = MessagesController.getInstance(UserConfig.selectedAccount).getChat(-dialogId);
            }
        }
    }

    public static List<AutoTranslateException> getAllExceptions() {
        return preferences.getAll().entrySet().stream()
                .filter(entry -> entry.getKey().startsWith("exceptions_" + UserConfig.selectedAccount))
                .map(entry -> new AutoTranslateException(
                        Long.parseLong(entry.getKey().split("_")[2]),
                        entry.getKey().split("_").length > 3 ? Integer.parseInt(entry.getKey().split("_")[3]) : 0,
                        (boolean) entry.getValue()))
                .filter(exception -> exception.chat != null)
                .filter(o1 -> !TextUtils.isEmpty(o1.chat instanceof TLRPC.User ? ((TLRPC.User) o1.chat).first_name : ((TLRPC.Chat) o1.chat).title))
                .sorted((Comparator.comparing(o1 -> o1.chat instanceof TLRPC.User ?
                        ((TLRPC.User) o1.chat).first_name :
                        ((TLRPC.Chat) o1.chat).title)))
                .collect(Collectors.toList());
    }

    public static List<AutoTranslateException> getExceptions(boolean isAllow) {
        return getAllExceptions().stream()
                .filter(exception -> exception.isAllow == isAllow)
                .filter(distinctByKey(exception -> exception.dialogId))
                .filter(exception -> isAllTopicEnabledOrDisabled(exception.dialogId, isAllow) || isAllow)
                .collect(Collectors.toList());
    }

    public static boolean getExceptionsById(boolean allow, long dialogId) {
        return getExceptions(allow).stream().anyMatch(e -> e.dialogId == dialogId);
    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        final Set<Object> seen = new HashSet<>();
        return t -> seen.add(keyExtractor.apply(t));
    }

    public static void resetExceptions() {
        preferences.edit().clear().apply();
    }
}