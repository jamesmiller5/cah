package com.cah;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.cah.customviews.CardView;
import com.cah.datastructures.Card;

public class CzarActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if(this.getIntent().hasExtra("WHITE_CARDS")) {
			setContentView(R.layout.sample_scrollview);
			ArrayList<String> whiteCards = this.getIntent().getExtras().getStringArrayList("WHITE_CARDS");
			int index = 0;
			for(String cardText : whiteCards) {
				Card card = new Card(Card.Color.WHITE, cardText);
				this.addChoosableCardToHand(card, index++);
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
	
	public static View getBlackCardView(String cardText, Context context) {
		LinearLayout cardContainer = new LinearLayout(context);
		cardContainer.setGravity(Gravity.CENTER);
		
		CardView card = new CardView(context);
		card.setCardColor(Color.BLACK);
		card.setCardString(cardText);
		
		LayoutParams lp = new LayoutParams((int) (235* (context.getResources().getDisplayMetrics().densityDpi/160.)), (int) (300* (context.getResources().getDisplayMetrics().densityDpi/160.)));
		card.setLayoutParams(lp);		

		cardContainer.addView(card);
		return cardContainer;
	}

	public void addChoosableCardToHand(Card card, final int index) {
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
		cv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Bundle bundle = new Bundle();
				bundle.putInt("CHOSEN_CARD", index);
				Intent intent = new Intent();
				intent.putExtras(bundle);
				if(getParent() == null) {
					setResult(Activity.RESULT_OK, intent);
				} else {
					getParent().setResult(Activity.RESULT_OK, intent);
				}
				finish();
			}
		});
		cardContainer.addView(cv);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.czar, menu);
		return true;
	}

}
