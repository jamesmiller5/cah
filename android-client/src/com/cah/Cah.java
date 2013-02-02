package com.cah;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class Cah extends Activity
{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game);
        
        Button button = (Button) this.findViewById(R.id.testButton);
        button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				//Toast.makeText(getBaseContext(), "Button clicked!", Toast.LENGTH_LONG).show();
				TextView tv = (TextView) findViewById(R.id.textView);
				tv.setText("Button was clicked!");
				((Button) v).setText("Click me again, please!");
			}
        	
        });
    }
}
