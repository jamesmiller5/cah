package com.cah.customviews;

import com.cah.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.QuickContactBadge;

public class GameTable extends ViewGroup {

	Paint paint;
	Context context;
	
	public GameTable(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		this.setWillNotDraw(false);
		paint = new Paint();
		paint.setColor(Color.LTGRAY);
		paint.setStrokeWidth(1);
	}
	
	public void addPlayerToTable(Bitmap playerBitmap, boolean isCzar) {
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View playerCard = inflater.inflate(R.layout.player_card, null);
		ImageView crownImageView = (ImageView)playerCard.findViewById(R.id.playerCrown);
		if(isCzar) {
			crownImageView.setVisibility(View.VISIBLE);
		}
		QuickContactBadge playerPicture = (QuickContactBadge)playerCard.findViewById(R.id.playerBadge);
		if(playerBitmap!=null) {
			playerPicture.setImageBitmap(playerBitmap);
		}
		this.addView(playerCard);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
		int mMaxChildWidth = 0;
        int mMaxChildHeight = 0;
 
        final int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(
                MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.AT_MOST);
        final int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
                MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.AT_MOST);
 
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() == GONE) {
                continue;
            }
 
            child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
 
            mMaxChildWidth = Math.max(mMaxChildWidth, child.getMeasuredWidth());
            mMaxChildHeight = Math.max(mMaxChildHeight, child.getMeasuredHeight());
        }
 
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		int radius = (this.getWidth()<this.getHeight()) ? this.getWidth()/3 : this.getHeight()/3;
		//radius /=3;
		int numberOfChildren = this.getChildCount();
		for(int i = 0; i<numberOfChildren; i++) {
			View toPosition = this.getChildAt(i);
			int x =(int) (Math.cos(((2.0*Math.PI)/ (float)numberOfChildren)*(float)i)*(float)radius + this.getLeft()+(this.getWidth()/2));
			int y = (int) (Math.sin(((2.0*Math.PI)/(float)numberOfChildren)*(float)i)*(float)radius + this.getTop()+(this.getHeight()/2));
			toPosition.layout(x-(toPosition.getMeasuredWidth()/2), y-(toPosition.getMeasuredHeight()/2), x+(toPosition.getMeasuredWidth()/2), y+(toPosition.getMeasuredHeight()/2));
		}		
	}
	
	protected void onDraw(Canvas canvas) {
		
		//Set shading for table
		paint.setShader(new RadialGradient(this.getLeft()+(this.getWidth()/2), this.getTop()+(this.getHeight()/2), (this.getWidth()<this.getHeight()) ? this.getWidth()/3 : this.getHeight()/3, Color.LTGRAY,Color.GRAY, Shader.TileMode.MIRROR));

		canvas.drawCircle(this.getLeft()+(this.getWidth()/2), this.getTop()+(this.getHeight()/2), (this.getWidth()<this.getHeight()) ? this.getWidth()/3 : this.getHeight()/3, paint);
	}

}
