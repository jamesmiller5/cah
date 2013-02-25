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

public class CahClient implements Runnable {

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

	Socket socket;

	public Encode() {
		JsonWriter writer = new JsonWriter(	new OutputStreamWriter( socket.getOutputStream(), "UTF-8" ) );
		while( true ) {
			Delta message_out;
			while( ( message_out = out.poll() ) != null ) {
				gson.toJson(message_out, message_out.getClass(), writer);
				writer.flush();
			}
			Thread.yield();
		}
	}

	private class Decoder extends Thread {
		public run() {
			JsonReader reader = new JsonReader(	new BufferedReader(new InputStreamReader( socket.getInputStream())));

			while( true ) {
				Delta message_in;

				reader.beginObject();

				String name = reader.nextName();
				if( name.equals("name") ) {
					username = reader.nextString();
				} else if( name.equals("followers_count") ) {
					followersCount = reader.nextInt();
				} else {
					//error
				}

				JsonElement jelement = new JsonParser().parse(jsonLine);
				JsonObject  jobject = jelement.getAsJsonObject();
				jobject = jobject.getAsJsonObject("data");
				JsonArray jarray = jobject.getAsJsonArray("translations");
				jobject = jarray.get(0).getAsJsonObject();
				String result = jobject.get("translatedText").toString();

				reader.endObject();
			}
		}
	}

	public void run() {
		try {
			Decode d = new Decode();
			socket = new Socket("localhost", 41337);
			socket.setTcpNoDelay(true);
			d.start();
			Encode();
		} catch (UnknownHostException e) {
			System.out.println("Unknown Host: "+e);
		} catch (IOException e) {
			System.out.println("IO Exception: "+e);
		}
	}

}
