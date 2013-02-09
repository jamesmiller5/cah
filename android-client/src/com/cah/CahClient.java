package com.cah;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.Queue;
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

	/* For Debug */
	public static void main( String args[] ) {
		Queue<Delta> in = (Queue<Delta>) new ArrayBlockingQueue<Delta>( 32, true );
		Queue<Delta> out =(Queue<Delta>) new ArrayBlockingQueue<Delta>( 32, true );
		CahClient c = new CahClient(in, out);
		c.run();
	}

	public CahClient( Queue<Delta> in, Queue<Delta> out ) {
		incoming = in;
		outgoing = out;
	}

	public void run() {

		Socket socket = null;
		try {
			socket = new Socket("sslab00.cs.purdue.edu", 41337);
			JsonWriter jw = new JsonWriter(	new OutputStreamWriter( socket.getOutputStream(), "UTF-8" ) );
			JsonReader jr = new JsonReader(	new BufferedReader(new InputStreamReader( socket.getInputStream())));

			socket.close();
		} catch (UnknownHostException e) {
		} catch (IOException e) {
		}
	}

}
