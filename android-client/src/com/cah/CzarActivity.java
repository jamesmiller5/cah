package com.cah;

//import java.util.ArrayList;
//import java.util.Collection;

import android.app.Activity;
import android.content.Context;
//import android.content.Intent;
import android.graphics.Color;
//import android.os.Bundle;
//import android.text.SpannableString;
//import android.util.Pair;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
//import android.view.View.OnClickListener;
//import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.cah.customviews.CardView;
import com.cah.datastructures.Card;

public class CzarActivity extends Activity {

/*	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if(this.getIntent().hasExtra("WHITE_CARDS")) {
			setContentView(R.layout.sample_scrollview);
			ArrayList<String> whiteCards = this.getIntent().getExtras().getStringArrayList("WHITE_CARDS");
//			int index = 0;
			for(String cardText : whiteCards) {
				Card card = new Card(Card.Color.WHITE, cardText);
				//this.addChoosableCardToHand(card, index++);
			}
		} else {
			setContentView(R.layout.activity_czar);

			FrameLayout cardContainer = (FrameLayout) findViewById(R.id.czarCardViewContainer);

			CardView card = new CardView(this);
			card.setCardColor(Color.BLACK);
			card.setCardString(((SpannableString)this.getIntent().getExtras().get("BLACK_CARD_TEXT")).toString());

			LayoutParams lp = new LayoutParams(this.getResources().getDisplayMetrics().widthPixels, this.getResources().getDisplayMetrics().heightPixels);
			card.setLayoutParams(lp);
			cardContainer.addView(card);
		}
	}
*/
	
	//grabs the black card view to display on czar's screen
	//adds it to the card container
	public static View getBlackCardView(Card black_card, Context context) {
		LinearLayout cardContainer = new LinearLayout(context);
		cardContainer.setGravity(Gravity.CENTER);

		CardView card = new CardView(context);
		card.setCardColor(Color.BLACK);
		card.setCardString(black_card.text);

		LayoutParams lp = new LayoutParams((int) (235* (context.getResources().getDisplayMetrics().densityDpi/160.)), (int) (300* (context.getResources().getDisplayMetrics().densityDpi/160.)));
		card.setLayoutParams(lp);

		cardContainer.addView(card);
		return cardContainer;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.czar, menu);
		return true;
	}

}
