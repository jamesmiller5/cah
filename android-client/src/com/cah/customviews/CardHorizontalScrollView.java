package com.cah.customviews;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;

public class CardHorizontalScrollView extends HorizontalScrollView {
	
	Context context;
	Point startingPoint;
	Point dxdy;
	boolean verticalSwipePossible;
	boolean verticalSwipeHappening = false;
	View cardToMove;
	Rect originalCardRect = new Rect();

	public CardHorizontalScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		// TODO Auto-generated constructor stub
	}

	public boolean onTouchEvent(MotionEvent event) {
		if(event.getAction() == MotionEvent.ACTION_DOWN){
			startingPoint = new Point((int)event.getRawX(), (int)event.getRawY());
			System.out.println(startingPoint.toString());
			verticalSwipePossible = true;
			return super.onTouchEvent(event);
		} else if (event.getAction() == MotionEvent.ACTION_UP){
			verticalSwipePossible = false;
			verticalSwipeHappening = false;

			if(cardToMove != null) {
				// We need to figure out what to do with the card that was dropped.
				DisplayMetrics displayMetrics = new DisplayMetrics();
				((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(displayMetrics);
				int windowHalfwayPoint = displayMetrics.heightPixels / 2;

				if(event.getRawY() < windowHalfwayPoint) {
					// Dropped on top half
					cardToMove.setVisibility(View.GONE);
					// TODO: Animate card into table.
					// TODO: Call function telling app that the user has played a card.s
				} else {
					// Dropped on bottom half
					cardToMove.layout(originalCardRect.left, originalCardRect.top, originalCardRect.right, originalCardRect.bottom);
					// TODO: Spring back
				}
			}

			startingPoint = null;
			dxdy = null;
			cardToMove = null;
		}

		if(verticalSwipePossible) {
			dxdy = new Point((int)event.getX()-startingPoint.x, (int)event.getY()-startingPoint.y);
			if(Math.abs(dxdy.x)> 15) {
				// We can no longer try to swipe up.
				verticalSwipePossible = false;
				return super.onTouchEvent(event);
			}
			if(dxdy.y< -20) {
				// We're going to push the card upward.
				verticalSwipePossible = false;
				
				// Let's figure out which/if the user is touching a card.
				ViewGroup child = (ViewGroup) this.getChildAt(0);
				for(int i = 0; i<child.getChildCount(); i++) {
					View currentChild = child.getChildAt(i);
					Rect childRect = new Rect();
					currentChild.getHitRect(childRect);
					if(childRect.contains(this.getScrollX()+startingPoint.x, startingPoint.y)) {
						System.out.println(i);
						cardToMove = currentChild;
						cardToMove.getHitRect(originalCardRect);
						break;
					}
				}
				
				// Check to make sure that we actually have touched a card
				if(cardToMove == null) {
					return super.onTouchEvent(event);
				}
				
				verticalSwipeHappening = true;
				//Toast.makeText(getContext(), "Vertical swipe happening", Toast.LENGTH_SHORT).show();
			}
		}
		if(verticalSwipeHappening && event.getAction() == MotionEvent.ACTION_MOVE) {
			dxdy = new Point((int)event.getRawX(), (int)event.getRawY());
			System.out.println("dx=" + dxdy.x + " dy=" + dxdy.y);
			cardToMove.layout(cardToMove.getLeft(), (dxdy.y) - (startingPoint.y - originalCardRect.top), cardToMove.getRight(), (dxdy.y) -(startingPoint.y - originalCardRect.top) + cardToMove.getMeasuredHeight());
			return true;
		}
		return super.onTouchEvent(event);
	}

}
