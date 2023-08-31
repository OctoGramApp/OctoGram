package it.octogram.android.fonts;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class FontFileReader {

    private int fSize;
    private int current;
    private byte[] file;

    public FontFileReader(InputStream in) throws IOException {
        init(in);
    }

    public FontFileReader(String fileName) throws IOException {
        File f = new File(fileName);
        try (InputStream in = new FileInputStream(f)) {
            init(in);
        }
    }

    public static TTFFile readTTF(String path) throws IOException {
        TTFFile ttfFile = new TTFFile();
        ttfFile.readFont(new FontFileReader(path));
        return ttfFile;
    }

    public byte[] getAllBytes() {
        return file;
    }

    public int getCurrentPos() {
        return current;
    }

    public int getFileSize() {
        return fSize;
    }

    private void init(InputStream in) throws IOException {
        file = IOUtils.toByteArray(in);
        fSize = file.length;
        current = 0;
    }

    private byte read() throws IOException {
        if (current >= fSize) {
            throw new EOFException("Reached EOF, file size=" + fSize);
        }

        return file[current++];
    }

    public byte readTTFByte() throws IOException {
        return read();
    }

    public void readTTFLong() throws IOException {
        readTTFUByte();
        readTTFUByte();
        readTTFUByte();
        readTTFUByte();
    }

    public String readTTFString(int len) throws IOException {
        if ((len + current) > fSize) {
            throw new EOFException("Reached EOF, file size=" + fSize);
        }

        byte[] tmp = new byte[len];
        System.arraycopy(file, current, tmp, 0, len);
        current += len;
        Charset encoding;
        if ((tmp.length > 0) && (tmp[0] == 0)) {
            encoding = StandardCharsets.UTF_16BE;
        } else {
            encoding = StandardCharsets.ISO_8859_1;
        }
        return new String(tmp, encoding);
    }

    public int readTTFUByte() throws IOException {
        byte buf = read();

        if (buf < 0) {
            return 256 + buf;
        } else {
            return buf;
        }
    }

    public long readTTFULong() throws IOException {
        long ret = readTTFUByte();
        ret = (ret << 8) + readTTFUByte();
        ret = (ret << 8) + readTTFUByte();
        ret = (ret << 8) + readTTFUByte();

        return ret;
    }

    public int readTTFUShort() throws IOException {
        return (readTTFUByte() << 8) + readTTFUByte();
    }

    public void seekSet(long offset) throws IOException {
        if (offset > fSize || offset < 0) {
            throw new EOFException("Reached EOF, file size=" + fSize + " offset=" + offset);
        }
        current = (int) offset;
    }

    public void skip(long add) throws IOException {
        seekSet(current + add);
    }
}