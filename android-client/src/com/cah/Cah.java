package com.cah;

import android.app.Activity;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import android.widget.ImageView;
import android.widget.QuickContactBadge;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class Cah extends Activity
{

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		Queue<Delta> in = new ArrayBlockingQueue<Delta>( 32, true );
		Queue<Delta> out = new ArrayBlockingQueue<Delta>( 32, true );
		performOnBackgroundThread(new CahClient(in, out));
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
