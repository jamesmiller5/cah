package com.cah;

import android.util.Log;
import java.util.Queue;
import java.util.LinkedList;
import com.google.gson.*;
import com.google.gson.stream.*;
import java.net.*;
import java.io.*;

abstract class Delta {
}

class TableDelta extends Delta{

	public String Command;
	public String Id; 

}

class DeckDelta extends Delta {

	int Player;
	String DeckTo;
	String DeckFrom;
	int Amount;

}

class PlayerDelta extends Delta{

	int Id;
	String Message;

}

class ActionDelta extends Delta {

	DeckDelta Deck;
	PlayerDelta Player;

}

public class CahClient implements Runnable{

	static Queue<Delta> incoming;
	static Queue<Delta> outgoing;


	public void run() {
		Log.d("Cah", "schlemschlemschlem");

		Socket socket = null;
		try {
			socket = new Socket("sslab00.cs.purdue.edu", 41337);
			JsonWriter jw = new JsonWriter(	new OutputStreamWriter( socket.getOutputStream(), "UTF-8" ) );
			JsonReader jr = new JsonReader(	new BufferedReader(new InputStreamReader( socket.getInputStream())));

			Log.d("Cah", "here is some json");
			
			socket.close();
		} catch (UnknownHostException e) {
			Log.d("Cah", "Bad Host");
		} catch (IOException e) {
			Log.d("Cah", "Bad IO");
		}
	}

}
