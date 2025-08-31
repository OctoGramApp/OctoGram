/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android;

import static org.telegram.messenger.LocaleController.formatString;
import static org.telegram.messenger.LocaleController.getString;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;
import android.provider.DocumentsContract;
import android.text.TextPaint;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.Emoji;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.Bulletin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import it.octogram.android.app.ui.cells.EmojiSetBulletinLayout;
import it.octogram.android.utils.network.FileDownloader;
import it.octogram.android.utils.network.StandardHTTPRequest;
import it.octogram.android.utils.OctoLogging;
import it.octogram.android.utils.data.FileUnzip;
import it.octogram.android.utils.fonts.FontFileReader;

public class CustomEmojiController {
    private static Typeface systemEmojiTypeface;
    private static boolean loadSystemEmojiFailed = false;
    private static final String EMOJI_FONT_AOSP = "NotoColorEmoji.ttf";

    public static final int FAILED = -1;
    public static final int LOADING = 0;
    public static final int LOADED_LOCAL = 1;
    public static final int LOADED_REMOTE = 2;
    private static int statusLoading = FAILED;
    private static String pendingDeleteEmojiPackId;
    private static final ArrayList<EmojiPackBase> emojiPacksInfo = new ArrayList<>();
    private static final SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("customEmojiCache", Activity.MODE_PRIVATE);
    private static Bulletin emojiPackBulletin;

    private final static String EMOJI_PACKS_CACHE_DIR = AndroidUtilities.getCacheDir().getAbsolutePath() + "/emojis/";
    private final static String EMOJI_PACKS_FILE_DIR;
    private final static String EMOJI_PACKS_TMP_DIR;
    private static final Runnable invalidateUiRunnable = () -> NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.emojiLoaded);

    static {
        var files = ApplicationLoader.applicationContext.getExternalFilesDir(null);
        var cache = ApplicationLoader.applicationContext.getExternalCacheDir();
        if (files != null && cache != null) {
            EMOJI_PACKS_FILE_DIR = files.getAbsolutePath() + "/emojis/";
            EMOJI_PACKS_TMP_DIR = cache.getAbsolutePath() + "/emojis/tmp/";
        } else {
            EMOJI_PACKS_FILE_DIR = ApplicationLoader.applicationContext.getFilesDir().getAbsolutePath() + "/emojis/";
            EMOJI_PACKS_TMP_DIR = ApplicationLoader.applicationContext.getCacheDir().getAbsolutePath() + "/emojis/tmp/";
        }
    }

    private static final String[] previewEmojis = {
            "\uD83D\uDE00",
            "\uD83D\uDE09",
            "\uD83D\uDE14",
            "\uD83D\uDE28"
    };

    public static boolean isLoading() {
        return statusLoading >= LOADING && statusLoading < LOADED_REMOTE;
    }

    public static Typeface getCurrentTypeface() {
        if (OctoConfig.INSTANCE.useSystemEmoji.getValue()) return getSystemEmojiTypeface();
        return getSelectedTypeface();
    }

    private static Typeface getSystemEmojiTypeface() {
        if (!loadSystemEmojiFailed && systemEmojiTypeface == null) {
            try {
                Pattern p = Pattern.compile(">(.*emoji.*)</font>", Pattern.CASE_INSENSITIVE);
                BufferedReader br = new BufferedReader(new FileReader("/system/etc/fonts.xml"));
                String line;
                while ((line = br.readLine()) != null) {
                    Matcher m = p.matcher(line);
                    if (m.find()) {
                        systemEmojiTypeface = Typeface.createFromFile("/system/fonts/" + m.group(1));
                        OctoLogging.d("emoji font file fonts.xml = " + m.group(1));
                        break;
                    }
                }
                br.close();
            } catch (Exception e) {
                OctoLogging.e(e);
            }
            if (systemEmojiTypeface == null) {
                try {
                    systemEmojiTypeface = Typeface.createFromFile("/system/fonts/" + EMOJI_FONT_AOSP);
                    OctoLogging.d("emoji font file = " + EMOJI_FONT_AOSP);
                } catch (Exception e) {
                    OctoLogging.e(e);
                    loadSystemEmojiFailed = true;
                }
            }
        }
        return systemEmojiTypeface;
    }

    private static Typeface getSelectedTypeface() {
        return getEmojiCustomPacksInfo()
                .stream()
                .filter(emojiPackInfo -> emojiPackInfo.packId.equals(OctoConfig.INSTANCE.selectedEmojiPack.getValue()))
                .map(emojiPackInfo -> {
                    File emojiFile = new File(emojiPackInfo.fileLocation);
                    if (emojiFile.exists()) {
                        return Typeface.createFromFile(emojiFile);
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    public static String getSelectedPackName() {
        if (OctoConfig.INSTANCE.useSystemEmoji.getValue()) return "System";
        synchronized (emojiPacksInfo) {
            return emojiPacksInfo
                    .stream()
                    .filter(e -> {
                        if (e instanceof EmojiPackInfo) {
                            return emojiDir(e.packId, ((EmojiPackInfo) e).versionWithMd5).exists();
                        }
                        return true;
                    })
                    .filter(emojiPackInfo -> Objects.equals(emojiPackInfo.packId, OctoConfig.INSTANCE.selectedEmojiPack.getValue()))
                    .findFirst()
                    .map(e -> e.packName)
                    .orElse("Apple");
        }
    }

    public static String getSelectedEmojiPackId() {
        return getAllEmojis()
                .stream()
                .map(File::getName)
                .anyMatch(name -> name.startsWith(OctoConfig.INSTANCE.selectedEmojiPack.getValue()) || name.endsWith(OctoConfig.INSTANCE.selectedEmojiPack.getValue()))
                ? OctoConfig.INSTANCE.selectedEmojiPack.getValue() : "default";
    }

    public static int getLoadingStatus() {
        return statusLoading;
    }

    public static void loadEmojisInfo() {
        loadEmojisInfo(() -> NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.emojiLoaded));
    }

    private static void invalidateCache(boolean isRemotePack) {
        new ArrayList<>(emojiPacksInfo).stream()
                .filter(emojiPackInfo -> (emojiPackInfo instanceof EmojiPackInfo) == isRemotePack)
                .forEach(emojiPacksInfo::remove);
    }

    public static void loadEmojisInfo(EmojiPackListener listener) {
        if (isLoading()) {
            return;
        }
        statusLoading = LOADING;

        new Thread() {
            @Override
            public void run() {
                ArrayList<EmojiPackBase> tmp = loadCustomEmojiPacks();
                invalidateCache(false);
                statusLoading = LOADED_LOCAL;

                synchronized (emojiPacksInfo) {
                    emojiPacksInfo.addAll(tmp);
                    AndroidUtilities.runOnUIThread(listener::onLoaded);

                    if (ApplicationLoader.isNetworkOnline()) {
                        try {
                            String json = new StandardHTTPRequest.Builder("https://raw.githubusercontent.com/OctoGramApp/assets/emojiPacks/EmojiPacks/emoji_packs.json").build().request();
                            preferences.edit().putString("emoji_packs", json).apply();
                            invalidateCache(true);
                            emojiPacksInfo.addAll(loadFromJson(json));
                        } catch (Exception e) {
                            try {
                                invalidateCache(true);
                                emojiPacksInfo.addAll(loadFromJson(preferences.getString("emoji_packs", "[]")));
                            } catch (JSONException ignored) {
                                statusLoading = FAILED;
                            }
                            OctoLogging.e("Error loading emoji packs", e);
                        }
                    } else {
                        try {
                            invalidateCache(true);
                            emojiPacksInfo.addAll(loadFromJson(preferences.getString("emoji_packs", "[]")));
                        } catch (JSONException e) {
                            statusLoading = FAILED;
                            OctoLogging.e("Error loading emoji packs from cache", e);
                        }
                    }

                    statusLoading = LOADED_REMOTE;
                    AndroidUtilities.runOnUIThread(listener::onLoaded);
                }
            }
        }.start();
    }

    private static ArrayList<EmojiPackInfo> loadFromJson(String json) throws JSONException {
        ArrayList<EmojiPackInfo> packs = new ArrayList<>();
        JSONArray array = new JSONArray(json);
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            packs.add(new EmojiPackInfo(
                    obj.getString("name"),
                    obj.getString("file"),
                    obj.getString("preview"),
                    obj.getString("id"),
                    obj.getLong("file_size"),
                    obj.getInt("version"),
                    obj.getInt("emoji_count"),
                    obj.getString("md5")
            ));
        }
        return packs.stream()
                .sorted(Comparator.comparing(e -> e.packName))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public static ArrayList<EmojiPackBase> getEmojiPacks() {
        return emojiPacksInfo;
    }

    public static ArrayList<EmojiPackBase> getEmojiPacksInfo() {
        synchronized (emojiPacksInfo) {
            return emojiPacksInfo.stream()
                    .filter(e -> e instanceof EmojiPackInfo)
                    .filter(e -> e.getEmojiCount() > 0)
                    .collect(Collectors.toCollection(ArrayList::new));
        }
    }

    public static ArrayList<EmojiPackBase> getEmojiCustomPacksInfo() {
        synchronized (emojiPacksInfo) {
            return emojiPacksInfo.stream()
                    .filter(e -> !(e instanceof EmojiPackInfo))
                    .filter(e -> !e.getPackId().equals(pendingDeleteEmojiPackId))
                    .collect(Collectors.toCollection(ArrayList::new));
        }
    }

    public static class EmojiPackBase {
        protected String packName;
        protected String packId;
        protected String fileLocation;
        protected String preview;
        protected int emojiCount;
        protected long fileSize;

        public EmojiPackBase() {
            this(null, null, null, null, 0, 0);
        }

        public void loadFromFile(File file) {
            String fileName = file.getName();
            packName = fileName;
            int versionSep = packName.lastIndexOf("_v");
            packName = packName.substring(0, versionSep);
            packId = fileName.substring(versionSep);
            File fileFont = new File(file, packName + ".ttf");
            fileLocation = fileFont.getAbsolutePath();
            preview = file.getAbsolutePath() + "/preview.png";
            fileSize = fileFont.length();
        }

        public EmojiPackBase(String packName, String packId, String fileLocation, String preview, int emojiCount, long fileSize) {
            this.packName = packName;
            this.packId = packId;
            this.fileLocation = fileLocation;
            this.preview = preview;
            this.emojiCount = emojiCount;
            this.fileSize = fileSize;
        }

        public String getPackName() {
            return packName;
        }

        public String getPackId() {
            return packId;
        }

        public String getFileLocation() {
            return fileLocation;
        }

        public String getPreview() {
            return preview;
        }

        public Long getFileSize() {
            return fileSize;
        }

        public int getEmojiCount() {
            return emojiCount;
        }
    }

    public static class EmojiPackInfo extends EmojiPackBase {
        private final String versionWithMd5;

        public EmojiPackInfo(String packName, String fileLink, String previewLink, String packId, Long packSize, Integer packVersion, int emojiCount, String md5) {
            super(packName, Objects.equals(packId, "apple") ? "default" : packId, fileLink, previewLink, emojiCount, packSize);
            this.versionWithMd5 = String.format(Locale.US, "%s_%s", packVersion, md5);
        }

        public String getVersionWithMd5() {
            return versionWithMd5;
        }
    }

    public static boolean isInstalledOldVersion(String emojiID, String versionWithMd5) {
        return !getAllVersions(emojiID, versionWithMd5).isEmpty();
    }

    public static boolean isInstalledOffline(String emojiID) {
        return !getAllVersions(emojiID, null).isEmpty();
    }

    public static ArrayList<File> getAllVersions(String emojiID) {
        return getAllVersions(emojiID, null);
    }

    public static File getCurrentEmojiPackOffline() {
        return getAllVersions(OctoConfig.INSTANCE.selectedEmojiPack.getValue())
                .stream()
                .findFirst()
                .orElse(null);
    }

    private static ArrayList<File> getAllEmojis() {
        ArrayList<File> emojis = new ArrayList<>();
        emojis.addAll(getAllEmojis(EMOJI_PACKS_CACHE_DIR));
        emojis.addAll(getAllEmojis(EMOJI_PACKS_FILE_DIR));
        return emojis;
    }

    private static ArrayList<File> getAllEmojis(String path) {
        ArrayList<File> emojis = new ArrayList<>();
        File emojiDir = new File(path);
        if (emojiDir.exists()) {
            File[] files = emojiDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        emojis.add(file);
                    }
                }
            }
        }
        return emojis;
    }

    public static ArrayList<File> getAllVersions(String emojiID, String versionWithMd5) {
        return getAllEmojis().stream()
                .filter(file -> file.getName().startsWith(emojiID))
                .filter(file -> TextUtils.isEmpty(versionWithMd5) || !file.getName().endsWith("_v" + versionWithMd5))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public static Long getEmojiSize() {
        return getAllEmojis().stream()
                .filter(file -> !file.getName().startsWith(OctoConfig.INSTANCE.selectedEmojiPack.getValue()))
                .filter(file -> !isValidCustomPack(file))
                .map(CustomEmojiController::calculateFolderSize)
                .reduce(0L, Long::sum);
    }

    private static long calculateFolderSize(File directory) {
        long length = 0;
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    length += file.length();
                } else {
                    length += calculateFolderSize(file);
                }
            }
        }
        return length;
    }

    public static void deleteAll() {
        getAllEmojis().stream()
                .filter(file -> !file.getName().startsWith(OctoConfig.INSTANCE.selectedEmojiPack.getValue()))
                .filter(file -> !isValidCustomPack(file))
                .forEach(FileUnzip::deleteFolder);
    }

    public static boolean isValidCustomPack(File file) {
        String packName = file.getName();
        int lastIndexOf = packName.lastIndexOf("_v");
        if (lastIndexOf == -1) {
            return false;
        }
        packName = packName.substring(0, lastIndexOf);
        return new File(file, packName + ".ttf").exists() && new File(file, "preview.png").exists();
    }

    public static void deleteOldVersions(String emojiID, String versionWithMd5) {
        for (File oldVersion : getAllVersions(emojiID, versionWithMd5)) {
            FileUnzip.deleteFolder(oldVersion);
        }
    }

    public static File emojiDir(String emojiID, String versionWithMd5) {
        return new File(EMOJI_PACKS_CACHE_DIR + emojiID + "_v" + versionWithMd5);
    }

    public static File emojiTmp(String emojiID) {
        return new File(EMOJI_PACKS_CACHE_DIR + emojiID + ".zip");
    }

    public static boolean emojiTmpDownloaded(String id) {
        boolean isCorrupted = true;
        try {
            long neededLength = getEmojiPacksInfo()
                    .stream()
                    .filter(emojiPackInfo -> Objects.equals(emojiPackInfo.packId, id))
                    .findFirst()
                    .map(e -> e.fileSize)
                    .orElse(0L);
            if (emojiTmp(id).length() == neededLength && neededLength != 0) {
                isCorrupted = false;
            }
        } catch (Exception ignored) {
        }
        return emojiTmp(id).exists() && !FileDownloader.isRunningDownload(id) && !isCorrupted;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void mkDirs() {
        File emojiDir = new File(EMOJI_PACKS_CACHE_DIR);
        if (!emojiDir.exists()) {
            emojiDir.mkdirs();
        }
    }

    public interface EmojiPackListener {
        void onLoaded();
    }

    public static void checkEmojiPacks() {
        loadEmojisInfo(() -> {
            if (getSelectedEmojiPackId().equals("default")) return;
            synchronized (emojiPacksInfo) {
                if (emojiPacksInfo.isEmpty()) {
                    if (!isInstalledOffline(OctoConfig.INSTANCE.selectedEmojiPack.getValue())) {
                        OctoConfig.INSTANCE.selectedEmojiPack.updateValue("default");
                    }
                    Emoji.reloadEmoji();
                    AndroidUtilities.cancelRunOnUIThread(invalidateUiRunnable);
                    AndroidUtilities.runOnUIThread(invalidateUiRunnable);
                    return;
                }
                for (EmojiPackBase emojiPackBase : emojiPacksInfo) {
                    if (emojiPackBase instanceof EmojiPackInfo) {
                        EmojiPackInfo emojiPackInfo;
                        emojiPackInfo = (EmojiPackInfo) emojiPackBase;
                        boolean isUpdate = isInstalledOldVersion(emojiPackInfo.packId, emojiPackInfo.versionWithMd5);
                        if (OctoConfig.INSTANCE.selectedEmojiPack.getValue().equals(emojiPackInfo.packId)) {
                            if (!emojiDir(emojiPackInfo.packId, emojiPackInfo.versionWithMd5).exists()) {
                                CustomEmojiController.mkDirs();
                                FileDownloader.downloadFile(ApplicationLoader.applicationContext, emojiPackInfo.packId, CustomEmojiController.emojiTmp(emojiPackInfo.packId), emojiPackInfo.fileLocation);
                                FileDownloader.addListener(emojiPackInfo.packId, "checkListener", new FileDownloader.FileDownloadListener() {
                                    @Override
                                    public void onPreStart(String id) {
                                    }

                                    @Override
                                    public void onProgressChange(String id, int percentage, long downBytes, long totBytes) {
                                    }

                                    @SuppressWarnings("ResultOfMethodCallIgnored")
                                    @Override
                                    public void onFinished(String id, boolean isFailed) {
                                        if (CustomEmojiController.emojiTmpDownloaded(id)) {
                                            FileUnzip.unzipFile(ApplicationLoader.applicationContext, id, CustomEmojiController.emojiTmp(id), CustomEmojiController.emojiDir(id, emojiPackInfo.versionWithMd5));
                                            FileUnzip.addListener(id, "checkListener", id1 -> {
                                                CustomEmojiController.emojiTmp(id).delete();
                                                if (CustomEmojiController.emojiDir(id, emojiPackInfo.versionWithMd5).exists()) {
                                                    deleteOldVersions(emojiPackInfo.packId, emojiPackInfo.versionWithMd5);
                                                }
                                                Emoji.reloadEmoji();
                                                AndroidUtilities.cancelRunOnUIThread(invalidateUiRunnable);
                                                AndroidUtilities.runOnUIThread(invalidateUiRunnable);
                                            });
                                        } else if (isFailed) {
                                            CustomEmojiController.emojiTmp(id).delete();
                                            if (!isUpdate) {
                                                OctoConfig.INSTANCE.selectedEmojiPack.updateValue("default");
                                            }
                                            Emoji.reloadEmoji();
                                            AndroidUtilities.cancelRunOnUIThread(invalidateUiRunnable);
                                            AndroidUtilities.runOnUIThread(invalidateUiRunnable);
                                        }
                                    }
                                });
                            } else {
                                Emoji.reloadEmoji();
                                AndroidUtilities.cancelRunOnUIThread(invalidateUiRunnable);
                                AndroidUtilities.runOnUIThread(invalidateUiRunnable);
                            }
                            break;
                        }
                    }
                }
            }
        });
    }

    public static boolean isValidEmojiPack(File path) {
        Typeface typeface;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            typeface = new Typeface.Builder(path)
                    .build();
        } else {
            typeface = Typeface.createFromFile(path);
        }
        return typeface != null && !typeface.equals(Typeface.DEFAULT);
    }

    public static EmojiPackBase installEmoji(File emojiFile) throws Exception {
        return installEmoji(emojiFile, true);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static EmojiPackBase installEmoji(File emojiFile, boolean checkInstallation) throws Exception {
        String fontName = emojiFile.getName();
        int dotIndex = fontName.lastIndexOf('.');
        if (dotIndex != -1) {
            fontName = fontName.substring(0, dotIndex);
        }
        MessageDigest md = MessageDigest.getInstance("MD5");
        try (FileInputStream fis = new FileInputStream(emojiFile)) {
            byte[] dataBytes = new byte[4 * 1024];
            int nread;
            while ((nread = fis.read(dataBytes)) != -1) {
                md.update(dataBytes, 0, nread);
            }
        }

        byte[] mdBytes = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte mdByte : mdBytes) {
            sb.append(Integer.toString((mdByte & 0xff) + 0x100, 16).substring(1));
        }
        try {
            String tmpFontName = FontFileReader.readTTF(emojiFile.getAbsolutePath()).getFullName();
            if (tmpFontName != null) {
                fontName = tmpFontName;
            }
        } catch (IOException ignored) {
        }
        File emojiDir = new File(EMOJI_PACKS_FILE_DIR + fontName + "_v" + sb);
        boolean isAlreadyInstalled = getAllEmojis().stream()
                .filter(CustomEmojiController::isValidCustomPack)
                .anyMatch(file -> file.getName().endsWith(sb.toString()));
        if (isAlreadyInstalled) {
            if (checkInstallation) {
                return null;
            } else {
                EmojiPackBase emojiPackBase = new EmojiPackBase();
                emojiPackBase.loadFromFile(emojiDir);
                return emojiPackBase;
            }
        }
        emojiDir.mkdirs();
        File emojiFont = new File(emojiDir, fontName + ".ttf");
        FileInputStream inputStream = new FileInputStream(emojiFile);
        FileOutputStream outputStream = new FileOutputStream(emojiFont);
        byte[] buffer = new byte[4 * 1024];
        int length;
        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }
        int emojiSize = 73;
        Bitmap bitmap = Bitmap.createBitmap(emojiSize * 2, emojiSize * 2, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Typeface typeface = Typeface.createFromFile(emojiFont);
        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 2; y++) {
                int xPos = x * emojiSize;
                int yPos = y * emojiSize;
                String emoji = previewEmojis[x + y * 2];
                CustomEmojiController.drawEmojiFont(
                        canvas,
                        xPos,
                        yPos,
                        typeface,
                        emoji,
                        emojiSize
                );
            }
        }
        File emojiPreview = new File(emojiDir, "preview.png");
        FileOutputStream out = new FileOutputStream(emojiPreview);
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        out.close();
        inputStream.close();
        outputStream.close();
        EmojiPackBase emojiPackBase = new EmojiPackBase();
        emojiPackBase.loadFromFile(emojiDir);
        synchronized (emojiPacksInfo) {
            emojiPacksInfo.add(emojiPackBase);
        }
        return emojiPackBase;
    }

    public static void drawEmojiFont(Canvas canvas, int x, int y, Typeface typeface, String emoji, int emojiSize) {
        int fontSize = (int) (emojiSize * 0.85f);
        Rect areaRect = new Rect(0, 0, emojiSize, emojiSize);
        TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTypeface(Typeface.create(typeface, Typeface.NORMAL));
        textPaint.setTextSize(fontSize);
        textPaint.setTextAlign(Paint.Align.CENTER);
        Rect textRect = new Rect();
        textPaint.getTextBounds(emoji, 0, emoji.length(), textRect);
        canvas.drawText(emoji, areaRect.centerX() + x, -textRect.top + y, textPaint);
    }

    private static ArrayList<EmojiPackBase> loadCustomEmojiPacks() {
        return getAllEmojis().stream()
                .filter(CustomEmojiController::isValidCustomPack)
                .sorted(Comparator.comparingLong(File::lastModified))
                .map(file -> {
                    EmojiPackBase emojiPackBase = new EmojiPackBase();
                    emojiPackBase.loadFromFile(file);
                    return emojiPackBase;
                })
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public static boolean isSelectedCustomEmojiPack() {
        return getAllEmojis().stream()
                .filter(CustomEmojiController::isValidCustomPack)
                .anyMatch(file -> file.getName().endsWith(OctoConfig.INSTANCE.selectedEmojiPack.getValue()));
    }

    public static void cancelableDelete(BaseFragment fragment, EmojiPackBase emojiPackBase, OnBulletinAction onUndoBulletinAction) {
        if (emojiPackBulletin != null && pendingDeleteEmojiPackId != null) {
            AlertDialog progressDialog = new AlertDialog(fragment.getParentActivity(), 3);
            emojiPackBulletin.hide(false, 0);
            new Thread() {
                @Override
                public void run() {
                    do {
                        SystemClock.sleep(50);
                    } while (pendingDeleteEmojiPackId != null);
                    AndroidUtilities.runOnUIThread(() -> {
                        progressDialog.dismiss();
                        cancelableDelete(fragment, emojiPackBase, onUndoBulletinAction);
                    });
                }
            }.start();
            progressDialog.setCanCancel(false);
            progressDialog.showDelayed(150);
            return;
        }
        pendingDeleteEmojiPackId = emojiPackBase.getPackId();
        onUndoBulletinAction.onPreStart();
        boolean wasSelected = emojiPackBase.getPackId().equals(OctoConfig.INSTANCE.selectedEmojiPack.getValue());
        if (wasSelected) {
            OctoConfig.INSTANCE.selectedEmojiPack.updateValue("default");
        }
        EmojiSetBulletinLayout bulletinLayout = new EmojiSetBulletinLayout(
                fragment.getParentActivity(),
                getString(R.string.EmojiSetRemoved),
                formatString(R.string.EmojiSetRemovedInfo, emojiPackBase.getPackName()),
                emojiPackBase,
                null
        );
        Bulletin.UndoButton undoButton = new Bulletin.UndoButton(fragment.getParentActivity(), false).setUndoAction(() -> {
            if (wasSelected) {
                OctoConfig.INSTANCE.selectedEmojiPack.updateValue(pendingDeleteEmojiPackId);
            }
            pendingDeleteEmojiPackId = null;
            onUndoBulletinAction.onUndo();
        }).setDelayedAction(() -> new Thread() {
            @Override
            public void run() {
                deleteEmojiPack(emojiPackBase);
                Emoji.reloadEmoji();
                AndroidUtilities.runOnUIThread(() -> NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.emojiLoaded));
                pendingDeleteEmojiPackId = null;
            }
        }.start());
        bulletinLayout.setButton(undoButton);
        emojiPackBulletin = Bulletin.make(fragment, bulletinLayout, Bulletin.DURATION_LONG).show();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void deleteEmojiPack(EmojiPackBase emojiPackBase) {
        File emojiDir = new File(emojiPackBase.getFileLocation()).getParentFile();
        if (emojiDir != null && emojiDir.exists()) {
            File[] files = emojiDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    file.delete();
                }
            }
            emojiDir.delete();
        }
        synchronized (emojiPacksInfo) {
            emojiPacksInfo.remove(emojiPackBase);
        }
        if (emojiPackBase.getPackId().equals(OctoConfig.INSTANCE.selectedEmojiPack.getValue())) {
            OctoConfig.INSTANCE.selectedEmojiPack.updateValue("default");
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static ArrayList<File> getFilesFromActivityResult(Intent intentResult) {
        File dir = new File(EMOJI_PACKS_TMP_DIR);
        if (dir.exists()) {
            FileUnzip.deleteFolder(dir);
        }
        dir.mkdirs();
        ArrayList<File> files = new ArrayList<>();
        Uri data = intentResult.getData();
        ClipData clipData = intentResult.getClipData();
        if (data != null) {
            File file = getFileFromUri(data);
            if (file != null) {
                files.add(file);
            }
        } else if (clipData != null) {
            for (int i = 0; i < clipData.getItemCount(); i++) {
                File file = getFileFromUri(clipData.getItemAt(i).getUri());
                if (file != null) {
                    files.add(file);
                }
            }
        }
        return files;
    }

    private static File getFileFromUri(Uri uri) {
        String docId = DocumentsContract.getDocumentId(uri);
        String path = AndroidUtilities.getPath(uri);
        if (docId.startsWith("msf:") && path == null) {
            File file = new File(EMOJI_PACKS_TMP_DIR, docId.substring(4));
            try {
                final InputStream inputStream = ApplicationLoader.applicationContext.getContentResolver().openInputStream(uri);
                if (inputStream != null) {
                    OutputStream outputStream = new FileOutputStream(file);
                    final byte[] buffer = new byte[4 * 1024];
                    int read;
                    while ((read = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, read);
                    }
                    inputStream.close();
                    outputStream.flush();
                    outputStream.close();
                    return file;
                }
            } catch (IOException e) {
                OctoLogging.e(e);
            }
        } else if (path != null) {
            return new File(path);
        }
        return null;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void deleteTempFile(File file) {
        if (file.getAbsolutePath().startsWith(EMOJI_PACKS_TMP_DIR)) file.delete();
    }

    public interface OnBulletinAction {
        void onPreStart();

        void onUndo();
    }
}
