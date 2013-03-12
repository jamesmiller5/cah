package com.cah;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.cah.customviews.CardView;

public class Cah extends Activity
{	
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
				
		LinearLayout cardContainer = (LinearLayout) findViewById(R.id.cardContainer);
		cardContainer.removeAllViews();

		for(int i = 0; i<10; i++) {
			CardView cv = new CardView(getApplicationContext());
			cv.setCardString("Dynamic card! Number = " + i + ".");
			cv.setTextColor(Color.BLACK);
			LayoutParams lp = new LayoutParams((int) (235* (this.getResources().getDisplayMetrics().densityDpi/160.)), (int) (300* (this.getResources().getDisplayMetrics().densityDpi/160.)));
			cv.setLayoutParams(lp);

			cardContainer.addView(cv);
		}

		Queue<Delta> in = new ArrayBlockingQueue<Delta>( 32, true );
		Queue<Delta> out = new ArrayBlockingQueue<Delta>( 32, true );
		performOnBackgroundThread(new CahClient((BlockingQueue<Delta>)in, (BlockingQueue<Delta>)out));
	}

	public static Thread performOnBackgroundThread(final Runnable runnable) {
		final Thread t = new Thread() {
			@Override
			public void run() {
				try {
					runnable.run();
				} finally {

				}
			}
		};
		t.start();
		return t;
	}
}
