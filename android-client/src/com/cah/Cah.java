package com.cah;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
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
import com.cah.datastructures.Card;

public class Cah extends Activity
{

	CahClient client;
	CahPlayer player;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		final Intent recievedIntent = getIntent();

		LinearLayout cardContainer = (LinearLayout) findViewById(R.id.cardContainer);
		cardContainer.removeAllViews();

		if(recievedIntent.hasExtra("COMMAND")) {
			//spawn a new client to the server
			client = new CahClient();
			performOnBackgroundThread(client);
			
			player = new CahPlayer(this, client, recievedIntent.getStringExtra("TABLE_ID"));
			client.player = player; //TODO: DON'T DO THIS HERE. CahClient could crash as soon as it receives a message.
		} else {
			this.addDummyPlayersAndCards();
		}
	}
	
	@Override
	public void onStop() {
		if(client != null)
			client.shutdown();
		
		super.onStop(); // Required!
	}

	public void addPlayerToTable(Bitmap playerBitmap, boolean isCzar) {
		GameTable table = (GameTable)this.findViewById(R.id.gameTable);

		//TODO: Decide if this inflater should be a private member variable of Cah
		LayoutInflater inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View playerCard = inflater.inflate(R.layout.player_card, null);
		ImageView crownImageView = (ImageView)playerCard.findViewById(R.id.playerCrown);
		if(isCzar) {
			crownImageView.setVisibility(View.VISIBLE);
		}
		QuickContactBadge playerPicture = (QuickContactBadge)playerCard.findViewById(R.id.playerBadge);
		if(playerBitmap!=null) {
			playerPicture.setImageBitmap(playerBitmap);
		}
		table.addView(playerCard);
	}

	public void addCardToHand(Card card) {
		LinearLayout cardContainer = (LinearLayout) findViewById(R.id.cardContainer);
		CardView cv = new CardView(getApplicationContext());
		cv.setCardString(card.text);
		if(card.color == Card.Color.WHITE){
			cv.setTextColor(Color.BLACK);
			cv.setCardColor(Color.WHITE);
		} else {
			cv.setTextColor(Color.WHITE);
			cv.setCardColor(Color.BLACK);
		}
		LayoutParams lp = new LayoutParams((int) (235* (this.getResources().getDisplayMetrics().densityDpi/160.)), (int) (300* (this.getResources().getDisplayMetrics().densityDpi/160.)));
		cv.setLayoutParams(lp);
		cardContainer.addView(cv);
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
		for(int i = 0; i<dummyCards.length; i++) {
			this.addCardToHand(new Card(i%2 == 0 ? Card.Color.WHITE : Card.Color.BLACK, dummyCards[i]));
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
		Thread t = new Thread(runnable);
		t.start();
		return t;
	}
}
