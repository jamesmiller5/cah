package com.cah;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.QuickContactBadge;

import com.cah.customviews.CardView;
import com.cah.customviews.GameTable;

public class Cah extends Activity
{	
	
	CahClient client;
	Queue<Delta> in;
	public Queue<Delta> out;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		final Intent recievedIntent = getIntent();

		in = new ArrayBlockingQueue<Delta>( 32, true );
		out = new ArrayBlockingQueue<Delta>( 32, true );
		client = new CahClient((BlockingQueue<Delta>)in, (BlockingQueue<Delta>)out);
		performOnBackgroundThread(client);
		
		if(recievedIntent.hasExtra("COMMAND")) {
			Runnable joinGame = new Runnable() {
				@Override
				public void run() {
					try {
						// Make/Join new table
						client.outgoing.put(new TableDelta(recievedIntent.getStringExtra("COMMAND"), recievedIntent.getStringExtra("TABLE_ID")));

						// Get the server's reply.
						TableDelta table_reply = (TableDelta) client.incoming.take();

						if(table_reply.Command.equals("ok")) {
							Log.d("CAH", "Settings.Secure.ANDROID_ID=" + Settings.Secure.ANDROID_ID);
							// Create a player
							client.outgoing.put(new PlayerDelta((int)(Math.random()*Integer.MAX_VALUE), "connect"));
						}
					} catch (InterruptedException ex) {
						ex.printStackTrace();
					}
				}
			};
			performOnBackgroundThread(joinGame);

		} else {
			this.addDummyPlayersAndCards();
		}
		
		
		
	}
	
	private void addDummyPlayersAndCards() {
		// Dynamically add 10 cards to the game.
		String dummyCards[] = {"Pretending to be happy",
				"Test, Test, Test, Test, Test, Test, Test, Test, Test!",
				"This card should hang off the right side of the screen!",
				"Bla bla bla",
				"greetings cheese popsicle the number you have dialed is currently out of porkchops",
				"friends dont let friends let scientific progress go boink",
				"oh yeah alright were gonna shake it up with the party bear tonight",
				"f of two equals f of one equals one",
				"oh hai im in ur computer eating your cheezburgers and CAHing your textz",
				"how are you holding up because im a potato"};
		LinearLayout cardContainer = (LinearLayout) findViewById(R.id.cardContainer);
		cardContainer.removeAllViews();
		for(int i = 0; i<dummyCards.length; i++) {
			CardView cv = new CardView(getApplicationContext());
			cv.setCardString(dummyCards[i]);
			cv.setTextColor(Color.BLACK);
			LayoutParams lp = new LayoutParams((int) (235* (this.getResources().getDisplayMetrics().densityDpi/160.)), (int) (300* (this.getResources().getDisplayMetrics().densityDpi/160.)));
			cv.setLayoutParams(lp);
			cardContainer.addView(cv);
		}

		// Dynamically add 10 players to the game table.
		for(int i = 0; i<10; i++) {
			GameTable table = (GameTable)this.findViewById(R.id.gameTable);
			LayoutInflater inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View playerCard = inflater.inflate(R.layout.player_card, null);
			ImageView crownImageView = (ImageView)playerCard.findViewById(R.id.playerCrown);
			if(i == 7)
				crownImageView.setVisibility(View.VISIBLE);
			QuickContactBadge playerPicture = (QuickContactBadge)playerCard.findViewById(R.id.playerBadge);
			table.addView(playerCard);
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
