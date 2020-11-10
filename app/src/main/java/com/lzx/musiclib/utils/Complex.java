package com.lzx.musiclib.utils;

import java.util.Objects;

/**
 * 复数
 */
public class Complex {

    /**
     * 实数部分
     */
    private final double real;

    /**
     * 虚数部分 imaginary
     */
    private final double im;

    public Complex(double real, double imag) {
        this.real = real;
        im = imag;
    }

    @Override
    public String toString() {
        return String.format("hypot: %s, complex: %s+%si", hypot(), real, im);
    }

    public double hypot() {
        return Math.hypot(real, im);
    }

    public double phase() {
        return Math.atan2(im, real);
    }

    /**
     * 复数求和
     */
    public Complex plus(Complex b) {
        double real = this.real + b.real;
        double imag = this.im + b.im;
        return new Complex(real, imag);
    }

    // return a new Complex object whose value is (this - b)
    public Complex minus(Complex b) {
        double real = this.real - b.real;
        double imag = this.im - b.im;
        return new Complex(real, imag);
    }

    // return a new Complex object whose value is (this * b)
    public Complex times(Complex b) {
        Complex a = this;
        double real = a.real * b.real - a.im * b.im;
        double imag = a.real * b.im + a.im * b.real;
        return new Complex(real, imag);
    }

    // return a new object whose value is (this * alpha)
    public Complex scale(double alpha) {
        return new Complex(alpha * real, alpha * im);
    }

    // return a new Complex object whose value is the conjugate of this
    public Complex conjugate() {
        return new Complex(real, -im);
    }

    // return a new Complex object whose value is the reciprocal of this
    public Complex reciprocal() {
        double scale = real * real + im * im;
        return new Complex(real / scale, -im / scale);
    }

    // return the real or imaginary part
    public double re() {
        return real;
    }

    public double im() {
        return im;
    }

    // return a / b
    public Complex divides(Complex b) {
        Complex a = this;
        return a.times(b.reciprocal());
    }

    // return a new Complex object whose value is the complex exponential of this
    public Complex exp() {
        return new Complex(Math.exp(real) * Math.cos(im), Math.exp(real) * Math.sin(im));
    }

    // return a new Complex object whose value is the complex sine of this
    public Complex sin() {
        return new Complex(Math.sin(real) * Math.cosh(im), Math.cos(real) * Math.sinh(im));
    }

    // return a new Complex object whose value is the complex cosine of this
    public Complex cos() {
        return new Complex(Math.cos(real) * Math.cosh(im), -Math.sin(real) * Math.sinh(im));
    }

    // return a new Complex object whose value is the complex tangent of this
    public Complex tan() {
        return sin().divides(cos());
    }


    // a static version of plus
    public static Complex plus(Complex a, Complex b) {
        double real = a.real + b.real;
        double imag = a.im + b.im;
        Complex sum = new Complex(real, imag);
        return sum;
    }

    // See Section 3.3.
    @Override
    public boolean equals(Object x) {
        if (x == null) return false;
        if (this.getClass() != x.getClass()) return false;
        Complex that = (Complex) x;
        return (this.real == that.real) && (this.im == that.im);
    }

    // See Section 3.3.
    @Override
    public int hashCode() {
        return Objects.hash(real, im);
    }

}