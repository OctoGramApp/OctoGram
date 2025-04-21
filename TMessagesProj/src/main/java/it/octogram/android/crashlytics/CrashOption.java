/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.crashlytics;

/**
 * Defines the available options for handling crashes in OctoGram.
 * <p>
 * These options are used to specify actions that can be taken when a crash occurs, such as opening the log file,
 * sending the log, copying the crash line, or opening a report URL.
 */
public enum CrashOption {
    /**
     * Option to open the crash log file for viewing.
     */
    OPEN_LOG(0),

    /**
     * Option to send the crash log for reporting.
     */
    SEND_LOG(1),

    /**
     * Option to copy the line where the crash occurred to the clipboard.
     */
    COPY_CRASH_LINE(2),

    /**
     * Option to open a URL related to the crash report, potentially for more detailed information online.
     */
    OPEN_REPORT_URL(3),

    /**
     * Option to save the crash log file for later reference.
     */
    SAVE_LOG(4);

    private final int value;

    CrashOption(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    /**
     * Creates a {@link CrashOption} from an integer value.
     *
     * @param value The integer value representing a crash option.
     * @return The corresponding {@link CrashOption} enum value.
     * @throws IllegalArgumentException if the provided value does not correspond to a valid {@link CrashOption}.
     */
    public static CrashOption fromValue(int value) {
        for (CrashOption option : CrashOption.values()) {
            if (option.value == value) {
                return option;
            }
        }
        throw new IllegalArgumentException("Invalid CrashOption value: " + value);
    }
}
