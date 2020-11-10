package com.lzx.musiclib.utils;

import com.lzx.record.utils.BytesTransUtil;

public class RecordUtils {
    public static byte[] makeFftData(byte[] pcmData) {
        if (pcmData.length < 1024) {
            return null;
        }
        double[] doubles = toHardDouble(BytesTransUtil.INSTANCE.bytes2Shorts(pcmData));
        double[] fft = fft(doubles, 0);
        return toSoftBytes(fft);
    }

    public static double[] toHardDouble(short[] shorts) {
        int length = 512;
        double[] ds = new double[length];
        for (int i = 0; i < length; i++) {
            ds[i] = shorts[i];
        }
        return ds;
    }

    public static double[] fft(double[] x, int sc) {
        int len = x.length;
        if (len == 1) {
            return x;
        }
        Complex[] cs = new Complex[len];
        double[] ds = new double[len / 2];
        for (int i = 0; i < len; i++) {
            cs[i] = new Complex(x[i], 0);
        }
        Complex[] ffts = fft(cs);

        for (int i = 0; i < ds.length; i++) {
            ds[i] = Math.sqrt(Math.pow(ffts[i].re(), 2) + Math.pow(ffts[i].im(), 2)) / x.length;
        }
        return ds;
    }

    // compute the FFT of x[], assuming its length is a power of 2
    public static Complex[] fft(Complex[] x) {
        int n = x.length;

        // base case
        if (n == 1) return new Complex[]{x[0]};

        // radix 2 Cooley-Tukey FFT
        if (n % 2 != 0) {
            throw new IllegalArgumentException("n is not a power of 2");
        }

        // fft of even terms
        Complex[] even = new Complex[n / 2];
        for (int k = 0; k < n / 2; k++) {
            even[k] = x[2 * k];
        }
        Complex[] q = fft(even);

        // fft of odd terms
        for (int k = 0; k < n / 2; k++) {
            even[k] = x[2 * k + 1];
        }
        Complex[] r = fft(even);

        // combine
        Complex[] y = new Complex[n];
        for (int k = 0; k < n / 2; k++) {
            double kth = -2 * k * Math.PI / n;
            Complex wk = new Complex(Math.cos(kth), Math.sin(kth));
            y[k] = q[k].plus(wk.times(r[k]));
            y[k + n / 2] = q[k].minus(wk.times(r[k]));
        }
        return y;
    }

    public static byte[] toSoftBytes(double[] doubles) {
        double max = getMax(doubles);

        double sc = 1f;
        if (max > 127) {
            sc = (max / 128f);
        }

        byte[] bytes = new byte[doubles.length];
        for (int i = 0; i < doubles.length; i++) {
            double item = doubles[i] / sc;
            bytes[i] = (byte) (item > 127 ? 127 : item);
        }
        return bytes;
    }

    public static double getMax(double[] data) {
        double max = 0;
        for (double datum : data) {
            if (datum > max) {
                max = datum;
            }
        }
        return max;
    }
}

