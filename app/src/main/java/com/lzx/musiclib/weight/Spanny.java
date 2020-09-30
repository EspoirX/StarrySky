package com.lzx.musiclib.weight;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;

/**
 * create by lzx
 * time:2018/8/16
 */
public class Spanny extends SpannableStringBuilder {

    private int flag = Spannable.SPAN_EXCLUSIVE_EXCLUSIVE;

    public Spanny() {
        super("");
    }

    public Spanny(CharSequence text) {
        super(text);
    }

    public Spanny(CharSequence text, Object... spans) {
        super(text);
        for (Object span : spans) {
            setSpan(span, 0, length());
        }
    }

    public Spanny(CharSequence text, Object span) {
        super(text);
        setSpan(span, 0, text.length());
    }

    /**
     * Appends the character sequence {@code text} and spans {@code spans} over the appended part.
     *
     * @param text  the character sequence to append.
     * @param spans the object or objects to be spanned over the appended text.
     * @return this {@code Spanny}.
     */
    public Spanny appends(CharSequence text, Object... spans) {
        append(text);
        for (Object span : spans) {
            setSpan(span, length() - text.length(), length());
        }
        return this;
    }

    public Spanny append(CharSequence text, Object span) {
        append(text);
        setSpan(span, length() - text.length(), length());
        return this;
    }

    /**
     * Add the ImageSpan to the start of the text.
     *
     * @return this {@code Spanny}.
     */
    public Spanny append(CharSequence text, ImageSpan imageSpan) {
        text = "." + text;
        append(text);
        setSpan(imageSpan, length() - text.length(), length() - text.length() + 1);
        return this;
    }

    /**
     * Append plain text.
     *
     * @return this {@code Spanny}.
     */
    @Override
    public Spanny append(CharSequence text) {
        super.append(text);
        return this;
    }

    /**
     * @deprecated use {@link #append(CharSequence text)}
     */
    @Deprecated
    public Spanny appendText(CharSequence text) {
        append(text);
        return this;
    }

    /**
     * Change the flag. Default is SPAN_EXCLUSIVE_EXCLUSIVE.
     * The flags determine how the span will behave when text is
     * inserted at the start or end of the span's range
     *
     * @param flag see {@link Spanned}.
     */
    public void setFlag(int flag) {
        this.flag = flag;
    }

    /**
     * Mark the specified range of text with the specified object.
     * The flags determine how the span will behave when text is
     * inserted at the start or end of the span's range.
     */
    private void setSpan(Object span, int start, int end) {
        setSpan(span, start, end, flag);
    }

    /**
     * Sets a span object to all appearances of specified text in the spannable.
     * A new instance of a span object must be provided for each iteration
     * because it can't be reused.
     *
     * @param textToSpan Case-sensitive text to span in the current spannable.
     * @param getSpan    Interface to get a span for each spanned string.
     * @return {@code Spanny}.
     */
    public Spanny findAndSpan(CharSequence textToSpan, GetSpan getSpan) {
        int lastIndex = 0;
        while (lastIndex != -1) {
            lastIndex = toString().indexOf(textToSpan.toString(), lastIndex);
            if (lastIndex != -1) {
                setSpan(getSpan.getSpan(), lastIndex, lastIndex + textToSpan.length());
                lastIndex += textToSpan.length();
            }
        }
        return this;
    }

    /**
     * Interface to return a new span object when spanning multiple parts in the text.
     */
    public interface GetSpan {

        /**
         * @return A new span object should be returned.
         */
        Object getSpan();
    }

    /**
     * Sets span objects to the text. This is more efficient than creating a new instance of Spanny
     * or SpannableStringBuilder.
     *
     * @return {@code SpannableString}.
     */
    public static SpannableString spanText(CharSequence text, Object... spans) {
        SpannableString spannableString = new SpannableString(text);
        for (Object span : spans) {
            spannableString.setSpan(span, 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return spannableString;
    }

    public static SpannableString spanText(CharSequence text, Object span) {
        SpannableString spannableString = new SpannableString(text);
        spannableString.setSpan(span, 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableString;
    }
}