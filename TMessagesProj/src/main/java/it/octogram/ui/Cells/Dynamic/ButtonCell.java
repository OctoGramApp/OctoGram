package it.octogram.ui.Cells.Dynamic;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;

import org.telegram.ui.ActionBar.Theme;

import it.octogram.android.OctoConfig;

public class ButtonCell {
    public static BaseButtonCell getCurrentButtonCell(Context context, Theme.ResourcesProvider resourcesProvider, String text, int iconId, int color) {
        switch (OctoConfig.buttonStyleType) {
            case 1:
                return new RoundedButtonCell(context, resourcesProvider, text, iconId, color);
            case 2:
                return new IceledButtonCell(context, resourcesProvider, text, iconId, color);
            case 3:
                return new PillsButtonCell(context, resourcesProvider, text, iconId, color);
            case 4:
                return new LinearButtonCell(context, resourcesProvider, text, iconId, color);
            default:
                return new SquaredButtonCell(context, resourcesProvider, text, iconId, color);
        }
    }

    public static void drawFlickerPreview(Canvas canvas, int x, int y, int w, int pos, Context context, Paint paint) {
        drawButtonPreview(canvas, x, y, w, pos, OctoConfig.buttonStyleType, context, paint);
    }

    public static void drawButtonPreview(Canvas canvas, int x, int y, int w, int pos, int style, Context context) {
        drawButtonPreview(canvas, x, y, w, pos, style, context, null);
    }

    private static void drawButtonPreview(Canvas canvas, int x, int y, int w, int pos, int style, Context context, Paint paint) {
        switch (style) {
            case 1:
                RoundedButtonCell.getPreview(canvas, x, y, w, paint);
                break;
            case 2:
                IceledButtonCell.getPreview(canvas, x, y, w, paint);
                break;
            case 3:
                PillsButtonCell.getPreview(canvas, x, y, w, paint);
                break;
            case 4:
                LinearButtonCell.getPreview(canvas, x, y, w, pos, context, paint);
                break;
            default:
                SquaredButtonCell.getPreview(canvas, x, y, w, paint);
                break;
        }
    }

    public static int getPreviewHeight() {
        switch (OctoConfig.buttonStyleType) {
            case 1:
                return RoundedButtonCell.getPreviewHeight();
            case 2:
                return IceledButtonCell.getPreviewHeight();
            case 3:
                return PillsButtonCell.getPreviewHeight();
            case 4:
                return LinearButtonCell.getPreviewHeight();
            default:
                return SquaredButtonCell.getPreviewHeight();
        }
    }
}
