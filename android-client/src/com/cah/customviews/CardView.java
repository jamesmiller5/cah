package com.cah.customviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import com.cah.Cah;
import com.cah.R;

public class CardView extends View {
	private String mCardText = "Test";
	private int mTextColor = Color.BLACK;
	private int mCardColor = Color.WHITE;

	private Bitmap mCahLogo;
	private TextPaint mTextPaint;
	private StaticLayout mTextLayout;
	
	private ColorFilter mColorFilter;
	private Paint mCardPaint;
	
	private int mCardWidth;

	private final float TEXT_SIZE = this.getResources().getDimensionPixelSize(R.dimen.card_font_size);
	private final float DPI_MULTIPLIER = (float) (this.getResources().getDisplayMetrics().densityDpi/160.);
	
	private Cah gameActivity;

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
		int[] otherAttributes = {android.R.attr.layout_width};
		final TypedArray w = getContext().obtainStyledAttributes(attrs,
				otherAttributes, defStyle, 0);
		
		mCardPaint = new Paint();
		mColorFilter = new ColorFilter();

		if(this.isInEditMode() || attrs == null) {
			mCardText = "This is an example card";
		} else {
			mCardText = a.getString(R.styleable.CardView_cardString);
		}

		int backgroundColor = (attrs == null) ?  Color.WHITE : a.getColor(R.styleable.CardView_color, Color.WHITE);
		this.setCardColor(backgroundColor);
		
		mCardWidth = w.getDimensionPixelSize(0,  -1);

		a.recycle();
		w.recycle();

		// Set up a default TextPaint object
		mTextPaint = new TextPaint();
		mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
		mTextPaint.setTextAlign(Paint.Align.LEFT);
		if(this.isInEditMode() == false) {
			Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/helveticaneue.ttf");
			mTextPaint.setTypeface(typeface);
		}
		mTextPaint.setTextSize(this.TEXT_SIZE);
		// Update TextPaint and text measurements from attributes
		invalidateTextPaintAndMeasurements();
		
		if(mCardWidth > 0)
			invalidateSizeOfEverything();

	}
	
	private void invalidateSizeOfEverything() {
		// mCahLogo should be 60% of the card's width
		int goalLogoWidth = (mCardWidth / 10) * 5;
		int goalLogoHeight = (int) (goalLogoWidth/5.8);
		System.out.println("Card width = " + goalLogoWidth + ", height = " + goalLogoHeight);
		mCahLogo = Bitmap.createScaledBitmap(mCahLogo, goalLogoWidth, goalLogoHeight, false);
	}

	private void invalidateTextPaintAndMeasurements() {
		mTextPaint.setTextSize(this.TEXT_SIZE);
		mTextPaint.setColor(mTextColor);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		mCardPaint.setColorFilter(mColorFilter);
		
		if(mCardWidth < 0) {
			mCardWidth = getWidth();
			invalidateSizeOfEverything();
		}

		// TODO: consider storing these as member variables to reduce
		// code run per draw cycle.
		int paddingLeft = (int) (10. * DPI_MULTIPLIER);
		int paddingTop = getPaddingTop();
		int paddingRight = getPaddingRight();
		int paddingBottom = (int) (20. * DPI_MULTIPLIER);

		int contentWidth = getWidth() - paddingLeft - paddingRight;
		int contentHeight = getHeight() - paddingTop - paddingBottom;

		canvas.translate((float)25. * DPI_MULTIPLIER, (float)17.5 * DPI_MULTIPLIER);
		if(mTextLayout == null)
			mTextLayout = new StaticLayout(mCardText, mTextPaint, (int) (contentWidth-(47.5 * DPI_MULTIPLIER)), Alignment.ALIGN_NORMAL, 1, 10, true);
		mTextLayout.draw(canvas);

		// Draw the text.
		//canvas.drawText(mCardText, paddingLeft, paddingTop + 50, mTextPaint);
		canvas.translate((float)0, (float)0);
		canvas.drawBitmap(mCahLogo, 0, (float) (this.getHeight()-mCahLogo.getHeight()-(37.5 * DPI_MULTIPLIER)), mCardPaint);

	}
	
	protected void onCardPlayed() {
		//TODO: Send card to server.
		//gameActivity.out.add(new DeckDelta());
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

	public int getCardColor() {
		return mCardColor;
	}

	public void setCardColor(int color) {
		if(color == Color.WHITE){
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
	}
	
	/**
	 * Makes the card fade red when the hand is locked.
	 * 
	 * @param percentageRed value from 0-100 that determines how strong the red color is on the card.
	 */
	public void setRedFadePercent(int percentageRed) {
		int redValue = (int) ((((float)percentageRed)/100f) * 255f); // Value from 0-255 of the red level
		this.setBackgroundColor(Color.argb(255, 255, 255-redValue, 255-redValue));
		float[] colorMatrixArray = new float[] { 
		        1, 1, 1, 1, 1, //red
		        0, 0, 0, 0, 0, //green
		        0, 0, 0, 0, 0, //blue
		        1, 1, 1, 1, 1 //alpha;
		    };
		mColorFilter = new ColorMatrixColorFilter(colorMatrixArray);
	}

}
