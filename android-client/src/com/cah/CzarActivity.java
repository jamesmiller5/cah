package com.cah;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout.LayoutParams;

import com.cah.customviews.CardView;

public class CzarActivity extends Activity {

	CardView card;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_czar);
		
		FrameLayout cardContainer = (FrameLayout) findViewById(R.id.czarCardViewContainer);
		
		card = new CardView(this);
		card.setCardColor(Color.BLACK);
		card.setCardString(((SpannableString)this.getIntent().getExtras().get("CARD_TEXT")).toString());
		
		LayoutParams lp = new LayoutParams(this.getResources().getDisplayMetrics().widthPixels, this.getResources().getDisplayMetrics().heightPixels);
		card.setLayoutParams(lp);
		
		cardContainer.addView(card);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.czar, menu);
		return true;
	}

}
