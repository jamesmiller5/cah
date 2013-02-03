package com.cah;

import android.app.Activity;
import java.util.Queue;
import java.util.LinkedList;
import android.widget.ImageView;
import android.widget.QuickContactBadge;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class Cah extends Activity
{

	Queue<Delta> incoming;
	Queue<Delta> outgoing;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		incoming = new LinkedList<Delta>();
		outgoing = new LinkedList<Delta>();
		performOnBackgroundThread(new CahClient(incoming, outgoing)); 
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
