/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.ConnectionsManager;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.MessageFormat;
import java.util.Locale;

import it.octogram.android.logs.OctoLogging;

public class CustomDevicePerformanceManager {
    /**
     * List of device hardware IDs (probably based on some system property or build characteristics)
     * that are known to report inaccurate or unreliable State of Charge (SOC) values when the battery
     * level is low.
     * <p>
     * Devices with these IDs might require special handling or filtering of SOC data to prevent
     * unexpected behavior or incorrect battery level indications.
     * <p>
     * Each element in the array represents a specific device hardware ID, likely obtained through
     * system properties or build characteristics.
     * <p>
     *  -1775228513: EXYNOS 850
     *  802464304:  EXYNOS 7872
     *  802464333:  EXYNOS 7880
     *  802464302:  EXYNOS 7870
     *  2067362118: MSM8953
     *  2067362060: MSM8937
     *  2067362084: MSM8940
     *  2067362241: MSM8992
     *  2067362117: MSM8952
     *  2067361998: MSM8917
     *  -1853602818: SDM439
     */
    private static final int[] LOW_SOC = {
            -1775228513, // EXYNOS 850
            802464304,  // EXYNOS 7872
            802464333,  // EXYNOS 7880
            802464302,  // EXYNOS 7870
            2067362118, // MSM8953
            2067362060, // MSM8937
            2067362084, // MSM8940
            2067362241, // MSM8992
            2067362117, // MSM8952
            2067361998, // MSM8917
            -1853602818 // SDM439
    };

    /**
     * Measures the device's performance class based on various factors like Android version, CPU count,
     * memory class, RAM size, and CPU frequency.
     * <p>
     * The method evaluates the device's hardware capabilities and assigns a performance class:
     * - {@link SharedConfig#PERFORMANCE_CLASS_LOW}: For devices with low-performance SoCs.
     * - {@link SharedConfig#PERFORMANCE_CLASS_AVERAGE}: For devices with moderate performance.
     * - {@link SharedConfig#PERFORMANCE_CLASS_HIGH}: For devices with high performance.
     * <p>
     * The performance class is determined by considering a combination of factors, including:
     * - Android version: Newer versions are generally considered more performant.
     * - CPU count: More cores generally indicate better performance.
     * - Memory class: Higher memory class indicates more available memory for apps.
     * - RAM size: Larger RAM capacity contributes to better performance.
     * - Maximum CPU frequency: Higher frequency indicates faster processing capabilities.
     * <p>
     * The method also logs the performance information for debugging purposes.
     *
     * @return The performance class of the device, which can be one of the following:
     *         {@link SharedConfig#PERFORMANCE_CLASS_LOW},
     *         {@link SharedConfig#PERFORMANCE_CLASS_AVERAGE},
     *         {@link SharedConfig#PERFORMANCE_CLASS_HIGH}.
     */
    public static int measureDevicePerformanceClass() {
        int androidVersion = Build.VERSION.SDK_INT;
        int cpuCount = ConnectionsManager.CPU_COUNT;
        int memoryClass = getMemoryClass();
        long ram = getTotalMemory();
        int maxCpuFreq = getMaxCpuFrequency(cpuCount);

        if (isLowPerformanceSoC()) {
            return SharedConfig.PERFORMANCE_CLASS_LOW;
        }

        int performanceClass = evaluatePerformanceClass(androidVersion, cpuCount, memoryClass, maxCpuFreq, ram);

        logPerformanceInfo(performanceClass, cpuCount, maxCpuFreq, memoryClass, androidVersion, ram);

        return performanceClass;
    }

    private static int getMemoryClass() {
        var activityManager = (ActivityManager) ApplicationLoader.applicationContext.getSystemService(Context.ACTIVITY_SERVICE);
        return activityManager.getMemoryClass();
    }

    private static long getTotalMemory() {
        try {
            ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
            ActivityManager activityManager = (ActivityManager) ApplicationLoader.applicationContext.getSystemService(Context.ACTIVITY_SERVICE);
            activityManager.getMemoryInfo(memoryInfo);
            return memoryInfo.totalMem;
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Retrieves the maximum CPU frequency across all available CPU cores.
     * <p>
     * This method iterates through each CPU core, reading the 'cpuinfo_max_freq' file
     * from the sysfs filesystem to obtain the maximum frequency for that core.
     * It then calculates the average maximum frequency across all cores.
     *
     * @param cpuCount The total number of CPU cores available.
     * @return The average maximum CPU frequency in MHz, or -1 if no frequency information
     *         could be retrieved for any core.
     */
    private static int getMaxCpuFrequency(int cpuCount) {
        int totalCpuFreq = 0;
        int freqResolved = 0;
        for (int i = 0; i < cpuCount; i++) {
            try (RandomAccessFile reader = new RandomAccessFile(String.format(Locale.ENGLISH, "/sys/devices/system/cpu/cpu%d/cpufreq/cpuinfo_max_freq", i), "r")) {
                String line = reader.readLine();
                if (line != null) {
                    totalCpuFreq += Utilities.parseInt(line) / 1000;
                    freqResolved++;
                }
            } catch (IOException ignore) {
            }
        }
        return freqResolved == 0 ? -1 : (int) Math.ceil(totalCpuFreq / (float) freqResolved);
    }

    /**
     * Determines if the device is running on a low-performance SoC (System on a Chip).
     * <p>
     * This method checks the device's SoC model against a predefined list of low-performance SoCs.
     * It is only supported on devices running Android S (API level 31) or higher.
     * On older devices, it always returns false.
     *
     * @return True if the device is running on a low-performance SoC, false otherwise.
     */
    private static boolean isLowPerformanceSoC() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            int hash = Build.SOC_MODEL.toUpperCase().hashCode();
            for (int lowSocHash : LOW_SOC) {
                if (lowSocHash == hash) {
                    return true;
                }
            }
        }
        return false;
    }

    private static int evaluatePerformanceClass(int androidVersion, int cpuCount, int memoryClass, int maxCpuFreq, long ram) {
        if (androidVersion < 21 || cpuCount <= 2 || memoryClass <= 100 ||
                (cpuCount <= 4 && maxCpuFreq <= 1250) ||
                (cpuCount <= 4 && maxCpuFreq <= 1600 && memoryClass <= 128 && androidVersion == 21) ||
                (cpuCount <= 4 && maxCpuFreq <= 1300 && memoryClass <= 128 && androidVersion <= 24) ||
                (ram != -1 && ram < 2L * 1024L * 1024L * 1024L)) {
            return SharedConfig.PERFORMANCE_CLASS_LOW;
        } else if (cpuCount < 8 || memoryClass <= 160 || maxCpuFreq <= 2055) {
            return SharedConfig.PERFORMANCE_CLASS_AVERAGE;
        } else {
            return SharedConfig.PERFORMANCE_CLASS_HIGH;
        }
    }

    /**
     * Logs device performance information to the debug log.
     * This information includes the selected performance class, CPU count, maximum CPU frequency,
     * memory class, Android version, and available RAM.
     *
     * @param performanceClass The selected performance class.
     * @param cpuCount         The number of CPU cores.
     * @param maxCpuFreq       The maximum CPU frequency in kHz.
     * @param memoryClass      The memory class of the device.
     * @param androidVersion   The Android version of the device.
     * @param ram             The total RAM available in bytes.
     */
    private static void logPerformanceInfo(int performanceClass, int cpuCount, int maxCpuFreq, int memoryClass, int androidVersion, long ram) {
        if (BuildVars.LOGS_ENABLED) {
            OctoLogging.d(MessageFormat.format("Device performance info selected_class = {0} (cpu_count = {1}, freq = {2}, memoryClass = {3}, android version {4}, RAM = {5} GB)", performanceClass, cpuCount, maxCpuFreq, memoryClass, androidVersion, ram / (1024L * 1024L * 1024L)));
        }
    }
}
