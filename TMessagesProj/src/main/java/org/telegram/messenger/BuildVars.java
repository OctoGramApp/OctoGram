/*
 * This is the source code of Telegram for Android v. 7.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2020.
 */

package org.telegram.messenger;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import com.android.billingclient.api.ProductDetails;

import java.util.Objects;

import it.octogram.android.StoreUtils;

public class BuildVars {

    public static boolean DEBUG_VERSION = false; //BuildConfig.DEBUG_PRIVATE_VERSION;
    public static boolean LOGS_ENABLED = false; //BuildConfig.DEBUG_PRIVATE_VERSION;
    public static boolean FIREBASE_CRASHLYTICS_ENABLED = false;
    public static boolean DEBUG_PRIVATE_VERSION = BuildConfig.DEBUG_PRIVATE_VERSION;
    public static boolean USE_CLOUD_STRINGS = true;
    public static boolean CHECK_UPDATES = false;
    public static boolean NO_SCOPED_STORAGE = Build.VERSION.SDK_INT <= 29;
    public static String BUILD_VERSION_STRING = BuildConfig.BUILD_VERSION_STRING;
    public static int TELEGRAM_BUILD_VERSION = BuildConfig.TELEGRAM_BUILD_VERSION;
    public static String TELEGRAM_VERSION_STRING = BuildConfig.TELEGRAM_VERSION_STRING;
    public static int APP_ID = 14565251;
    public static String APP_HASH = "9952ccf3124c8fa7835d9b8e091dfb4a";

    // SafetyNet key for Google Identity SDK, set it to empty to disable
    public static String SAFETYNET_KEY = "";
    public static String PLAYSTORE_APP_URL = "https://play.google.com/store/apps/details?id=it.octogram.android";
    public static String HUAWEI_STORE_URL = "https://appgallery.huawei.com/app/C101184875";
    public static String GOOGLE_AUTH_CLIENT_ID = "760348033671-81kmi3pi84p11ub8hp9a1funsv0rn2p9.apps.googleusercontent.com";

    public static String HUAWEI_APP_ID = "0";

    // You can use this flag to disable Google Play Billing (If you're making fork and want it to be in Google Play)
    public static boolean IS_BILLING_UNAVAILABLE = StoreUtils.isFromPlayStore();

    static {
        if (ApplicationLoader.applicationContext != null) {
            SharedPreferences sharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("systemConfig", Context.MODE_PRIVATE);
            LOGS_ENABLED = DEBUG_VERSION || sharedPreferences.getBoolean("logsEnabled", DEBUG_VERSION);
            if (LOGS_ENABLED) {
                final Thread.UncaughtExceptionHandler pastHandler = Thread.getDefaultUncaughtExceptionHandler();
                Thread.setDefaultUncaughtExceptionHandler((thread, exception) -> {
                    FileLog.fatal(exception, false);
                    //OctoLogging.fatal(exception);
                    if (pastHandler != null) {
                        pastHandler.uncaughtException(thread, exception);
                    }
                });
            }
        }
    }

    public static boolean useInvoiceBilling() {
        // return BillingController.billingClientEmpty || DEBUG_VERSION || isStandaloneApp() || isBetaApp() || isHuaweiStoreApp() || hasDirectCurrency();
        return BillingController.billingClientEmpty || DEBUG_VERSION && false || ApplicationLoader.isStandaloneBuild() || isBetaApp() && false || isHuaweiStoreApp() || hasDirectCurrency();
    }

    private static boolean hasDirectCurrency() {
        if (!BillingController.getInstance().isReady() || (BillingController.PREMIUM_PRODUCT_DETAILS == null || BillingController.PREMIUM_PRODUCT_DETAILS.getSubscriptionOfferDetails() == null)) {
            return false;
        }
        for (ProductDetails.SubscriptionOfferDetails offerDetails : BillingController.PREMIUM_PRODUCT_DETAILS.getSubscriptionOfferDetails()) {
            for (ProductDetails.PricingPhase phase : offerDetails.getPricingPhases().getPricingPhaseList()) {
                for (String cur : MessagesController.getInstance(UserConfig.selectedAccount).directPaymentsCurrency) {
                    if (Objects.equals(phase.getPriceCurrencyCode(), cur)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static Boolean standaloneApp;

    public static boolean isStandaloneApp() {
        if (standaloneApp == null) {
            standaloneApp = ApplicationLoader.applicationContext != null && "it.octogram.android".equals(ApplicationLoader.applicationContext.getPackageName());
        }
        return standaloneApp;
    }

    private static Boolean betaApp;
    public static boolean isBetaApp() {
        if (betaApp == null) {
            betaApp = (ApplicationLoader.applicationContext != null && "it.octogram.android.beta".equals(ApplicationLoader.applicationContext.getPackageName())) || BuildConfig.BUILD_VERSION_STRING.toLowerCase().contains("beta");
        }
        return betaApp;
    }


    public static boolean isHuaweiStoreApp() {
        return ApplicationLoader.isHuaweiStoreBuild();
    }

    public static String getSmsHash() {
        return isStandaloneApp() ? "w0lkcmTZkKh" : (DEBUG_VERSION ? "O2P2z+/jBpJ" : "oLeq9AcOZkT");
    }
}
