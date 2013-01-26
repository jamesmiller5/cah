package com.cah.customviews;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

public class CardScrollerLayout extends ViewGroup {

	private int maxChildWidth = 0;
	private int maxChildHeight = 0;
		
	public CardScrollerLayout(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public CardScrollerLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	
	public CardScrollerLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Measure the view and its content to determine the measured width and the measured height.
	 * This method is invoked by measure(int, int) and should be overriden by subclasses to provide accurate and efficient measurement of their contents.
	 * 
	 * CONTRACT: When overriding this method, you must call setMeasuredDimension(int, int) to store the measured width and height of this view. Failure to do so will trigger an IllegalStateException, thrown by measure(int, int). Calling the superclass' onMeasure(int, int) is a valid use.
	 * The base class implementation of measure defaults to the background size, unless a larger size is allowed by the MeasureSpec. Subclasses should override onMeasure(int, int) to provide better measurements of their content.
	 * If this method is overridden, it is the subclass's responsibility to make sure the measured height and width are at least the view's minimum height and width (getSuggestedMinimumHeight() and getSuggestedMinimumWidth()).
	 * 
	 * @param widthMeasureSpec horizontal space requirements as imposed by the parent. The requirements are encoded with View.MeasureSpec
	 * @param heightMeasureSpec vertical space requirements as imposed by the parent. The requirements are encoded with View.MeasureSpec
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		maxChildHeight = 300;
		maxChildWidth = 250;
		
		final int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(
				MeasureSpec.getSize(widthMeasureSpec), 
				MeasureSpec.AT_MOST);
		final int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
				MeasureSpec.getSize(widthMeasureSpec), 
				MeasureSpec.AT_MOST);
		
		setMeasuredDimension(
				resolveSize(maxChildWidth, widthMeasureSpec),
				resolveSize(maxChildHeight, heightMeasureSpec));
	}

	/**
	 * Called from layout when this view should assign a size and position to each of its children. 
	 * Derived classes with children should override this method and call layout on each of their children.
	 * 
	 * @param changed This is a new size or position for this view
	 * @param l Left position, relative to parent.
	 * @param t Top position, relative to parent
	 * @param r Right position, relative to parent
	 * @param b Bottom position, relative to parent
	 */
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		final int childCount = getChildCount();
		
		int visibleCount = 0;
		for(int i = 0; i<childCount; i++) {
			final View child = getChildAt(i);
			if(child.getVisibility() == GONE) {
				continue;
			}
			visibleCount++;
		}
		
		if(visibleCount == 0) {
			return;
		}
		
		for(int i = 0; i < childCount; i++) {
			final View child = getChildAt(i);
			if(child.getVisibility() == GONE) {
				continue;
			}
			LayoutParams p = child.getLayoutParams();
			child.layout(0+(i*(p.width/10)), 0, p.width+(i*(p.width/10)), p.height);
		}
	}
	
	/**
	 * Touch event handler
	 * 
	 * @param event The motion event
	 * @return True if the event was handled, false otherwise.
	 */
	public boolean onTouchEvent(MotionEvent event) {
		if(event.getActionMasked() == MotionEvent.ACTION_DOWN || event.getActionMasked() == MotionEvent.ACTION_UP){
			return true;
		}
		if(event.getActionMasked() == MotionEvent.ACTION_MOVE){
			final int childCount = getChildCount();
			
			for(int i = 0; i< childCount; i++) {
				final View child = getChildAt(i);
				if(child.getVisibility() == GONE) {
					continue;
				}
				final int childLeft = child.getLeft();
				final int childRight = child.getRight();
				child.layout(childLeft, (int)event.getY() - (child.getHeight()/2), childRight, (int)(event.getY() + child.getHeight() - (child.getHeight()/2)));
			}
			
			return true;
		}
		return false;
	}

}
