package com.lzx.musiclib.weight;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhaolewei on 2018/8/17.
 */
public class AudioView extends View {

    /**
     * 频谱数量
     */
    private static final int LUMP_COUNT = 128 * 2;
    private static final int LUMP_WIDTH = 6;
    private static final int LUMP_SPACE = 2;
    private static final int LUMP_MIN_HEIGHT = LUMP_WIDTH;
    private static final int LUMP_MAX_HEIGHT = 200;//TODO: HEIGHT
    private static final int LUMP_SIZE = LUMP_WIDTH + LUMP_SPACE;
    private static final int LUMP_COLOR = Color.parseColor("#6de8fd");

    private static final int WAVE_SAMPLING_INTERVAL = 5;

    private static final float SCALE = LUMP_MAX_HEIGHT / 128;

    private ShowStyle upShowStyle = ShowStyle.STYLE_HOLLOW_LUMP;
    private ShowStyle downShowStyle = ShowStyle.STYLE_WAVE;

    private byte[] waveData;
    List<Point> pointList;

    private Paint lumpUpPaint, lumpDownPaint;
    Path wavePathUp = new Path();
    Path wavePathDown = new Path();


    public AudioView(Context context) {
        super(context);
        init();
    }

    public AudioView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AudioView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        lumpUpPaint = new Paint();
        lumpUpPaint.setAntiAlias(true);
        lumpUpPaint.setColor(LUMP_COLOR);
        lumpUpPaint.setStrokeWidth(3);
        lumpUpPaint.setStyle(Paint.Style.FILL);

        lumpDownPaint = new Paint();
        lumpDownPaint.setAntiAlias(true);
        lumpDownPaint.setColor(LUMP_COLOR);
        lumpDownPaint.setStrokeWidth(3);
        lumpDownPaint.setStyle(Paint.Style.STROKE);
    }

    public void setWaveData(byte[] data) {
        this.waveData = readyData(data);
        genSamplingPoint(data);
        invalidate();
    }

    public void setStyle(ShowStyle upShowStyle, ShowStyle downShowStyle) {
        this.upShowStyle = upShowStyle;
        this.downShowStyle = downShowStyle;
        if (upShowStyle == ShowStyle.STYLE_HOLLOW_LUMP || upShowStyle == ShowStyle.STYLE_ALL) {
            lumpUpPaint.setColor(Color.parseColor("#A4D3EE"));
        }
        if (downShowStyle == ShowStyle.STYLE_HOLLOW_LUMP || downShowStyle == ShowStyle.STYLE_ALL) {
            lumpDownPaint.setColor(Color.parseColor("#A4D3EE"));
        }
    }

    public ShowStyle getUpStyle() {
        return upShowStyle;
    }

    public ShowStyle getDownStyle() {
        return downShowStyle;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        wavePathUp.reset();
        wavePathDown.reset();

        for (int i = 0; i < LUMP_COUNT; i++) {
            if (waveData == null) {
                canvas.drawRect((LUMP_WIDTH + LUMP_SPACE) * i,
                        LUMP_MAX_HEIGHT - LUMP_MIN_HEIGHT,
                        (LUMP_WIDTH + LUMP_SPACE) * i + LUMP_WIDTH,
                        LUMP_MAX_HEIGHT,
                        lumpUpPaint);
                continue;
            }

            if (upShowStyle != null) {
                switch (upShowStyle) {
                    case STYLE_HOLLOW_LUMP:
                        drawLump(canvas, i, true);
                        break;
                    case STYLE_WAVE:
                        drawWave(canvas, i, true);
                        break;
                    case STYLE_ALL:
                        drawLump(canvas, i, true);
                        drawWave(canvas, i, true);
                    default:
                        break;
                }
            }
            if (downShowStyle != null) {
                switch (downShowStyle) {
                    case STYLE_HOLLOW_LUMP:
                        drawLump(canvas, i, false);
                        break;
                    case STYLE_WAVE:
                        drawWave(canvas, i, false);
                        break;
                    case STYLE_ALL:
                        drawLump(canvas, i, false);
                        drawWave(canvas, i, false);
                    default:
                        break;
                }
            }
        }
    }

    /**
     * 预处理数据
     *
     * @return
     */
    private static byte[] readyData(byte[] fft) {
        byte[] newData = new byte[LUMP_COUNT];
        for (int i = 0; i < Math.min(fft.length, LUMP_COUNT); i++) {
            newData[i] = (byte) Math.abs(fft[i]);
        }
        return newData;
    }

    /**
     * 绘制曲线
     *
     * @param canvas
     * @param i
     * @param reversal
     */
    private void drawWave(Canvas canvas, int i, boolean reversal) {
        if (pointList == null || pointList.size() < 2) {
            return;
        }
        float ratio = SCALE * (reversal ? 1 : -1);
        if (i < pointList.size() - 2) {
            Point point = pointList.get(i);
            Point nextPoint = pointList.get(i + 1);
            int midX = (point.x + nextPoint.x) >> 1;
            if (reversal) {
                if (i == 0) {
                    wavePathUp.moveTo(point.x, LUMP_MAX_HEIGHT - point.y * ratio);
                }
                wavePathUp.cubicTo(midX, LUMP_MAX_HEIGHT - point.y * ratio,
                        midX, LUMP_MAX_HEIGHT - nextPoint.y * ratio,
                        nextPoint.x, LUMP_MAX_HEIGHT - nextPoint.y * ratio);
                canvas.drawPath(wavePathUp, lumpDownPaint);
            } else {
                if (i == 0) {
                    wavePathDown.moveTo(point.x, LUMP_MAX_HEIGHT - point.y * ratio);
                }
                wavePathDown.cubicTo(midX, LUMP_MAX_HEIGHT - point.y * ratio,
                        midX, LUMP_MAX_HEIGHT - nextPoint.y * ratio,
                        nextPoint.x, LUMP_MAX_HEIGHT - nextPoint.y * ratio);
                canvas.drawPath(wavePathDown, lumpDownPaint);
            }

        }
    }

    /**
     * 绘制矩形条
     * reversal： true: 上
     */
    private void drawLump(Canvas canvas, int i, boolean reversal) {
        int minus = reversal ? 1 : -1;
        float top;

        if ((reversal && upShowStyle == ShowStyle.STYLE_ALL) || (!reversal && downShowStyle == ShowStyle.STYLE_ALL)) {
            top = (LUMP_MAX_HEIGHT - (LUMP_MIN_HEIGHT + waveData[i] / 4 * SCALE) * minus);
        } else {
            top = (LUMP_MAX_HEIGHT - (LUMP_MIN_HEIGHT + waveData[i] * SCALE) * minus);
        }
        canvas.drawRect(LUMP_SIZE * i,
                top,
                LUMP_SIZE * i + LUMP_WIDTH,
                LUMP_MAX_HEIGHT,
                lumpUpPaint);

    }

    /**
     * 生成波形图的采样数据，减少计算量
     *
     * @param data
     */
    private void genSamplingPoint(byte[] data) {
        if (upShowStyle != ShowStyle.STYLE_WAVE && downShowStyle != ShowStyle.STYLE_WAVE && upShowStyle != ShowStyle.STYLE_ALL && downShowStyle != ShowStyle.STYLE_ALL) {
            return;
        }
        if (pointList == null) {
            pointList = new ArrayList<>();
        } else {
            pointList.clear();
        }
        pointList.add(new Point(0, 0));
        for (int i = WAVE_SAMPLING_INTERVAL; i < LUMP_COUNT; i += WAVE_SAMPLING_INTERVAL) {
            pointList.add(new Point(LUMP_SIZE * i, waveData[i]));
        }
        pointList.add(new Point(LUMP_SIZE * LUMP_COUNT, 0));
    }


    /**
     * 可视化样式
     */
    public enum ShowStyle {
        /**
         * 空心的矩形小块
         */
        STYLE_HOLLOW_LUMP,

        /**
         * 曲线
         */
        STYLE_WAVE,

        /**
         * 不显示
         */
        STYLE_NOTHING,
        /**
         * 都显示
         */
        STYLE_ALL;

        public static ShowStyle getStyle(String name) {
            for (ShowStyle style : ShowStyle.values()) {
                if (style.name().equals(name)) {
                    return style;
                }
            }

            return STYLE_NOTHING;
        }
    }

}

