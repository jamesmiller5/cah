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
	static Gson gson = new Gson();

	/* For Debug */
	public static void main( String args[] ) {
		Queue<Delta> in = (Queue<Delta>) new ArrayBlockingQueue<Delta>( 32, true );
		Queue<Delta> out =(Queue<Delta>) new ArrayBlockingQueue<Delta>( 32, true );
		CahClient c = new CahClient(in, out);
		c.run();

		//debug eat messages
		while( true ) {
			Delta message_in;

			while( ( message_in = in.poll() ) != null ) {
				System.out.println("Got an incoming message");
			}

			Thread.yield();
		}
	}

	public CahClient( Queue<Delta> in, Queue<Delta> out ) {
		incoming = in;
		outgoing = out;
	}

	public void run() {

		Socket socket = null;
		try {
			socket = new Socket("localhost", 41337);
			socket.setTcpNoDelay(true);
			JsonWriter writer = new JsonWriter(	new OutputStreamWriter( socket.getOutputStream(), "UTF-8" ) );
			//JsonReader reader = new JsonReader(	new BufferedReader(new InputStreamReader( socket.getInputStream())));

			TableDelta td = new TableDelta();
			td.Command = "new";

			gson.toJson(td, TableDelta.class, writer);
			writer.flush();
			System.out.println("Written");

		} catch (UnknownHostException e) {
		} catch (IOException e) {
			System.out.println("IO Exception: "+e);
		}
	}

}
