/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.fonts;


import androidx.annotation.NonNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class TTFDirTabEntry {

    private final byte[] tag = new byte[4];

    private long offset;

    private long length;

    public TTFDirTabEntry() {
    }

    public TTFDirTabEntry(long offset, long length) {
        this.offset = offset;
        this.length = length;
    }

    public long getLength() {
        return length;
    }

    public long getOffset() {
        return offset;
    }

    public byte[] getTag() {
        return tag;
    }

    public String getTagString() {
        return new String(tag, StandardCharsets.ISO_8859_1);
    }

    public String read(FontFileReader in) throws IOException {
        tag[0] = in.readTTFByte();
        tag[1] = in.readTTFByte();
        tag[2] = in.readTTFByte();
        tag[3] = in.readTTFByte();

        in.skip(4);

        offset = in.readTTFULong();
        length = in.readTTFULong();

        return new String(tag, StandardCharsets.ISO_8859_1);
    }

    @NonNull
    @Override
    public String toString() {
        return "Read dir tab [" + tag[0] + " " + tag[1] + " " + tag[2] + " " + tag[3] + "]"
                + " offset: " + offset + " bytesToUpload: " + length + " name: " + Arrays.toString(tag);
    }

}
