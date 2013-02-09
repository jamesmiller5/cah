package com.cah.customviews;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import com.cah.R;

/**
 * TODO: document your custom view class.
 */
public class CardView extends View {
	private String mCardText = "Test"; // TODO: use a default from R.string...
	private int mTextColor = Color.BLACK; // TODO: Set this based on background color
		
	private Bitmap mCahLogo;

	private TextPaint mTextPaint;
	private StaticLayout mTextLayout;

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
			mCardText = a.getString(R.styleable.CardView_cardString);
		}
		
		int backgroundColor = a.getColor(R.styleable.CardView_color, Color.WHITE);
		if(backgroundColor == Color.WHITE){
			mTextColor = Color.BLACK;
			this.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.card_background_white));
			// Get bitmap image for icon
			mCahLogo = BitmapFactory.decodeResource(this.getContext().getResources(), R.drawable.icon_w);
		} else {
			mTextColor = Color.WHITE;
			this.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.card_background_black));
			// Get bitmap image for icon
			mCahLogo = BitmapFactory.decodeResource(this.getContext().getResources(), R.drawable.icon_b);
		}
		
		a.recycle();

		// Set up a default TextPaint object
		mTextPaint = new TextPaint();
		mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
		mTextPaint.setTextAlign(Paint.Align.LEFT);
		if(this.isInEditMode() == false) {
			Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/helveticaneue.ttf");
			mTextPaint.setTypeface(typeface);
		}
		mTextPaint.setTextSize((float)45);
		
		// Update TextPaint and text measurements from attributes
		invalidateTextPaintAndMeasurements();
		
		
	}

	private void invalidateTextPaintAndMeasurements() {
		mTextPaint.setTextSize((float)45);
		mTextPaint.setColor(mTextColor);
		
		Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
	}

	@SuppressLint("DrawAllocation")
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		// TODO: consider storing these as member variables to reduce
		// allocations per draw cycle.
		int paddingLeft = 20;
		int paddingTop = getPaddingTop();
		int paddingRight = getPaddingRight();
		int paddingBottom = 40;

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
	public String getCardString() {
		return mCardText;
	}

	/**
	 * Sets the view's example string attribute value. In the example view, this
	 * string is the text to draw.
	 * 
	 * @param exampleString
	 *            The example string attribute value to use.
	 */
	public void setCardString(String exampleString) {
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


}
