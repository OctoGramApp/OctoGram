/*
 * This is the source code of OctoGram for Android v.2.0.x
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023.
 */

package it.octogram.android;

import android.graphics.Color;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;

public enum Datacenter {
    USA_1(1, "MIA, Miami FL, USA", R.drawable.ic_pluto_datacenter, 0xFF329AFE, "149.154.175.50"),
    USA_2(3, "MIA, Miami FL, USA", R.drawable.ic_aurora_datacenter, 0xFFDA5653, "149.154.175.100"),
    AMSTERDAM_1(2, "AMS, Amsterdam, NL", R.drawable.ic_venus_datacenter, 0xFF8B31FD, "149.154.167.50"),
    AMSTERDAM_2(4, "AMS, Amsterdam, NL", R.drawable.ic_vesta_datacenter, 0xFFF7B139, "149.154.167.91"),
    SINGAPORE(5, "SIN, Singapore, SG", R.drawable.ic_flora_datacenter, 0xFF4BD199, "91.108.56.100"),
    UNKNOWN(-1, LocaleController.getString("NumberUnknown", R.string.NumberUnknown), R.drawable.msg_secret_hw, Color.TRANSPARENT, "");

    public final int dcId;
    public final String dcName;
    public final int icon;
    public final int color;
    public final String ip;

    Datacenter(int dcId, String dcName, int icon, int color, String ip) {
        this.dcId = dcId;
        this.dcName = dcName;
        this.icon = icon;
        this.color = color;
        this.ip = ip;
    }

    public static Datacenter getDcInfo(int dcId) {
        for (Datacenter dcInfo : values()) {
            if (dcInfo.dcId == dcId) {
                return dcInfo;
            }
        }
        return UNKNOWN;
    }

    public static int getDcIcon(int dcId) {
        for (Datacenter dcInfo : values()) {
            if (dcInfo.dcId == dcId) {
                return dcInfo.icon;
            }
        }
        return UNKNOWN.icon;
    }

}
