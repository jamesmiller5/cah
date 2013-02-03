package com.cah.customviews;

import com.cah.R;
import com.cah.R.styleable;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

/**
 * TODO: document your custom view class.
 */
public class CardView extends View {
	private String mCardText = "Test"; // TODO: use a default from R.string...
	private int mTextColor = Color.BLACK; // TODO: Set this based on background color
	
	private float mExampleDimension = 0; // TODO: use a default from R.dimen...
	
	private Bitmap mCahLogo;

	private TextPaint mTextPaint;
	private StaticLayout mTextLayout;
	private float mTextWidth;
	private float mTextHeight;

	public CardView(Context context) {
		super(context);
		init(null, 0);
	}

	public CardView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs, 0);
	}

	public CardView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(attrs, defStyle);
	}

	private void init(AttributeSet attrs, int defStyle) {
		// Load attributes
		final TypedArray a = getContext().obtainStyledAttributes(attrs,
				R.styleable.CardView, defStyle, 0);

		if(this.isInEditMode()) {
			mCardText = "This is an example card";
		} else {
			mCardText = a.getString(R.styleable.CardView_exampleString);
		}

		// Use getDimensionPixelSize or getDimensionPixelOffset when dealing
		// with
		// values that should fall on pixel boundaries.
		mExampleDimension = a.getDimension(
				R.styleable.CardView_exampleDimension, mExampleDimension);

		a.recycle();

		// Set up a default TextPaint object
		mTextPaint = new TextPaint();
		mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
		mTextPaint.setTextAlign(Paint.Align.LEFT);
		Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/helveticaneue.ttf");
		mTextPaint.setTypeface(typeface);
		mTextPaint.setTextSize((float)16.0);
		
		// Update TextPaint and text measurements from attributes
		invalidateTextPaintAndMeasurements();
		
		// Get bitmap image for icon
		mCahLogo = BitmapFactory.decodeResource(this.getContext().getResources(), R.drawable.icon_w);
	}

	private void invalidateTextPaintAndMeasurements() {
		mTextPaint.setTextSize(mExampleDimension);
		mTextPaint.setColor(mTextColor);
		
		mTextWidth = mTextPaint.measureText(mCardText);

		Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
		mTextHeight = fontMetrics.bottom;
	}

	@SuppressLint("DrawAllocation")
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		// TODO: consider storing these as member variables to reduce
		// allocations per draw cycle.
		int paddingLeft = getPaddingLeft();
		int paddingTop = getPaddingTop();
		int paddingRight = getPaddingRight();
		int paddingBottom = getPaddingBottom();

		int contentWidth = getWidth() - paddingLeft - paddingRight;
		int contentHeight = getHeight() - paddingTop - paddingBottom;
		
		canvas.translate((float)50, (float)35);
		mTextLayout = new StaticLayout(mCardText, mTextPaint, contentWidth-95, Alignment.ALIGN_NORMAL, 1, 10, true);
		mTextLayout.draw(canvas);
		
		// Draw the text.
		//canvas.drawText(mCardText, paddingLeft, paddingTop + 50, mTextPaint);
		canvas.translate((float)0, (float)0);
		canvas.drawBitmap(mCahLogo, 0, this.getHeight()-90, null);

	}

	/**
	 * Gets the example string attribute value.
	 * 
	 * @return The example string attribute value.
	 */
	public String getExampleString() {
		return mCardText;
	}

	/**
	 * Sets the view's example string attribute value. In the example view, this
	 * string is the text to draw.
	 * 
	 * @param exampleString
	 *            The example string attribute value to use.
	 */
	public void setExampleString(String exampleString) {
		mCardText = exampleString;
		invalidateTextPaintAndMeasurements();
	}

	/**
	 * Gets the example color attribute value.
	 * 
	 * @return The example color attribute value.
	 */
	public int getTextColor() {
		return mTextColor;
	}

	/**
	 * Sets the view's example color attribute value. In the example view, this
	 * color is the font color.
	 * 
	 * @param exampleColor
	 *            The example color attribute value to use.
	 */
	public void setTextColor(int exampleColor) {
		mTextColor = exampleColor;
		invalidateTextPaintAndMeasurements();
	}

	/**
	 * Gets the example dimension attribute value.
	 * 
	 * @return The example dimension attribute value.
	 */
	public float getExampleDimension() {
		return mExampleDimension;
	}

	/**
	 * Sets the view's example dimension attribute value. In the example view,
	 * this dimension is the font size.
	 * 
	 * @param exampleDimension
	 *            The example dimension attribute value to use.
	 */
	public void setExampleDimension(float exampleDimension) {
		mExampleDimension = exampleDimension;
		invalidateTextPaintAndMeasurements();
	}

}
