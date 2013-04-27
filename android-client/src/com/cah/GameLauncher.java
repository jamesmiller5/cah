package com.cah;

import android.app.Activity;
//import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class GameLauncher extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game_launcher);
		
		final TextView titleTextView = (TextView) findViewById(R.id.titleTextView);
		Typeface typeface = Typeface.createFromAsset(this.getAssets(), "fonts/helveticaneue.ttf");
		titleTextView.setTypeface(typeface);
		
		final Button createNewTableButton = (Button) findViewById(R.id.buttonCreateNewTable);
		final Button joinExistingTableButton = (Button) findViewById(R.id.buttonJoinExistingTable);
		final EditText tableIdEditText = (EditText) findViewById(R.id.editTextTableId);
		
		//action event listener for the join exiting table button on the GUI
		createNewTableButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View button) {
				Intent intent = new Intent(button.getContext(), Cah.class);
				intent.putExtra("COMMAND", "new");
				startActivity(intent);
			}
		});
		
		//action event listener for the join exiting table button on the GUI
		joinExistingTableButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(v.getContext(), Cah.class);
				intent.putExtra("COMMAND", "join");
				intent.putExtra("TABLE_ID", tableIdEditText.getText().toString());
				startActivity(intent);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_game_launcher, menu);
		return true;
	}
}
