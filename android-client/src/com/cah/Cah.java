package com.cah;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.cah.customviews.CardView;

public class Cah extends Activity
{	
	
	CahClient client;
	Queue<Delta> in;
	Queue<Delta> out;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		Intent recievedIntent = getIntent();
				
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

		in = new ArrayBlockingQueue<Delta>( 32, true );
		out = new ArrayBlockingQueue<Delta>( 32, true );
		client = new CahClient((BlockingQueue<Delta>)in, (BlockingQueue<Delta>)out);
		performOnBackgroundThread(client);
		
		if(recievedIntent.hasExtra("COMMAND")) {
			try {
				// This is probably running on the UI thread. TODO: Move this off of the UI thread if it's running there. UI will hang if there's a slow internet connection.

				// Make/Join new table
				client.outgoing.put(new TableDelta(recievedIntent.getStringExtra("COMMAND"), recievedIntent.getStringExtra("TABLE_ID")));

				// Get the server's reply.
				TableDelta table_reply = (TableDelta) client.incoming.take();
				AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
				alertBuilder.setTitle("New table reply");
				alertBuilder.setMessage("Command \"" + table_reply.Command + "\", Id \"" + table_reply.Id + "\"");
				alertBuilder.setPositiveButton("Close", null);
				alertBuilder.show();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
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
