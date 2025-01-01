/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.fonts;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TTFFile {

    private final Set<String> familyNames = new HashSet<>();
    private FontFileReader fontFile;
    private Map<TTFTableName, TTFDirTabEntry> dirTabs;
    private String postScriptName = "";
    private String fullName = "";
    private String notice = "";
    private String subFamilyName = "";

    public Set<String> getFamilyNames() {
        return familyNames;
    }

    public String getFullName() {
        return fullName;
    }

    public String getNotice() {
        return notice;
    }

    public String getPostScriptName() {
        return postScriptName;
    }

    public String getSubFamilyName() {
        return subFamilyName;
    }

    private void readDirTabs() throws IOException {
        fontFile.readTTFLong();
        int nTabs = fontFile.readTTFUShort();
        fontFile.skip(6);

        dirTabs = new HashMap<>();
        TTFDirTabEntry[] pd = new TTFDirTabEntry[nTabs];

        for (int i = 0; i < nTabs; i++) {
            pd[i] = new TTFDirTabEntry();
            String tableName = pd[i].read(fontFile);
            dirTabs.put(TTFTableName.getValue(tableName), pd[i]);
        }
        dirTabs.put(TTFTableName.TABLE_DIRECTORY, new TTFDirTabEntry(0L, fontFile.getCurrentPos()));
    }

    void readFont(FontFileReader in) throws IOException {
        fontFile = in;
        readDirTabs();
        readName();
    }

    private void readName() throws IOException {
        seekTab(fontFile);
        int i = fontFile.getCurrentPos();
        int n = fontFile.readTTFUShort();
        int j = fontFile.readTTFUShort() + i - 2;
        i += 2 * 2;

        while (n-- > 0) {
            fontFile.seekSet(i);
            int platformID = fontFile.readTTFUShort();
            int encodingID = fontFile.readTTFUShort();
            int languageID = fontFile.readTTFUShort();

            int k = fontFile.readTTFUShort();
            int l = fontFile.readTTFUShort();

            if (((platformID == 1 || platformID == 3) && (encodingID == 0 || encodingID == 1))) {
                fontFile.seekSet(j + fontFile.readTTFUShort());
                String txt = fontFile.readTTFString(l);
                switch (k) {
                    case 0:
                        if (notice.length() == 0) {
                            notice = txt;
                        }
                        break;
                    case 1:
                    case 16:
                        familyNames.add(txt);
                        break;
                    case 2:
                        if (subFamilyName.length() == 0) {
                            subFamilyName = txt;
                        }
                        break;
                    case 4:
                        if (fullName.length() == 0 || (platformID == 3 && languageID == 1033)) {
                            fullName = txt;
                        }
                        break;
                    case 6:
                        if (postScriptName.length() == 0) {
                            postScriptName = txt;
                        }
                        break;
                    default:
                        break;
                }
            }
            i += 6 * 2;
        }
    }

    private void seekTab(FontFileReader in)
            throws IOException {
        TTFDirTabEntry dt = dirTabs.get(TTFTableName.NAME);
        if (dt != null) {
            in.seekSet(dt.getOffset() + (long) 2);
        }
    }
}