package com.cah;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import com.cah.customviews.CardHorizontalScrollView;
import com.cah.customviews.CardView;
import com.cah.customviews.GameTable;
import com.cah.datastructures.Card;

//import android.graphics.Bitmap;
//import android.widget.QuickContactBadge;


public class Cah extends Activity
{
	CahClient client;
	public static CahPlayer player;

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
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
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
	
	public void playerCanPlayCard(boolean status) {
		CardHorizontalScrollView handScrollView = (CardHorizontalScrollView) findViewById(R.id.handScrollView);
		handScrollView.handLocked = !status;
	}

	//for testing
	private void addDummyPlayersAndCards() {
		
		// Dynamically add 7 cards to the game.
		String dummyCards[] = {"Licking things to claim them as your own",
				"Leaving an awkward voicemail.",
				"50,000 volts straight to the nipples.",
				"Panda Sex.",
				"Fabricating Statistics.",
				"Finding Waldo.",
				"Old-people smell."};
	
		for(int i = 0; i<dummyCards.length; i++) {
			this.addCardToHand(new Card(i%2 == 0 ? Card.Color.WHITE : Card.Color.BLACK, dummyCards[i]));
		}

		// Dynamically add 7 players to the game table.
		// for testing
		for(int i = 0; i<10; i++) {
			GameTable table = (GameTable)this.findViewById(R.id.gameTable);
			LayoutInflater inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View playerCard = inflater.inflate(R.layout.player_card, null);
			ImageView crownImageView = (ImageView)playerCard.findViewById(R.id.playerCrown);
			if(i == 7)
				crownImageView.setVisibility(View.VISIBLE);
	//		if(i==1){
	//			TempContactBadge playerPicture = (TempContactBadge)playerCard.findViewById(R.id.playerBadge);
	//		}else{
//				QuickContactBadge playerPicture = (QuickContactBadge)playerCard.findViewById(R.id.playerBadge);
	//		}
			table.addView(playerCard);
		}
	}

	public static Thread performOnBackgroundThread(final Runnable runnable) {
		Thread t = new Thread(runnable);
		t.start();
		return t;
	}
}
