package com.cah;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import com.google.gson.*;
import com.google.gson.stream.*;
import java.net.*;
import java.io.*;

abstract class Delta {
	/* Print all fields for sub-classes */
	public String toString() {
		StringBuilder result = new StringBuilder();
		String newLine = System.getProperty("line.separator");

		result.append( this.getClass().getName() );
		result.append( " Object {" );
		result.append(newLine);

		//determine fields declared in this class only (no fields of superclass)
		java.lang.reflect.Field[] fields = this.getClass().getDeclaredFields();

		//print field names paired with their values
		for ( java.lang.reflect.Field field : fields  ) {
			result.append("  ");
			try {
				result.append( field.getName() );
				result.append(": ");
				//requires access to private field:
				result.append( field.get(this) );
			} catch ( IllegalAccessException ex ) {
				System.out.println(ex);
			}
			result.append(newLine);
		}
		result.append("}");

		return result.toString();
	}
}

class TableDelta extends Delta{
	String Command;
	String Id;
}

class DeckDelta extends Delta {
	int Player;
	String DeckTo;
	String DeckFrom;
	int Amount; //TODO: This should be an array if card indicators
}

class PlayerDelta extends Delta{
	int Id;
	String Message;
}

class ActionDelta extends Delta {
	DeckDelta Deck;
	PlayerDelta Player;
}

public class CahClient extends Thread implements JsonDeserializer<Delta> {
	static BlockingQueue<Delta> incoming;
	static BlockingQueue<Delta> outgoing;
	Socket socket;
	final Gson gson = new GsonBuilder().registerTypeAdapter(Delta.class, this).create();
	final AtomicBoolean go = new AtomicBoolean(true);
	Thread encoder, decoder;

	/* For Debug */
	public static void main( String args[] ) {
		try {
			BlockingQueue<Delta> in = (BlockingQueue<Delta>) new ArrayBlockingQueue<Delta>( 32, true );
			BlockingQueue<Delta> out =(BlockingQueue<Delta>) new ArrayBlockingQueue<Delta>( 32, true );
			(new CahClient(in, out)).start();

			TableDelta table_new = new TableDelta();
			table_new.Command = "new";
			out.put(table_new);

			TableDelta table_reply = (TableDelta) in.take();
			System.out.println("Reply: "+ table_reply);

			assert table_reply.Command != null;
			assert table_reply.Command.equals("ok");
			assert table_reply.Id != null;
			assert table_reply.Id.length() == 6;

		} catch( Exception e ) {
			System.out.println("Debug failed: " + e);
		}
	}

	public CahClient( BlockingQueue<Delta> in, BlockingQueue<Delta> out ) {
		incoming = in;
		outgoing = out;
	}

	public void cleanup() {
		//cleanup threads
		go.set(false);
		encoder.isInterrupted();
		decoder.isInterrupted();
	}

	public void run() {
		try {
			//Keep track of our threads, they will need to interrupt each other
			encoder = (Thread) this;
			decoder = new Thread(new DecodeThread());

			socket = new Socket("localhost", 41337);
			socket.setTcpNoDelay(true);
			socket.setReuseAddress(true);

			decoder.start();
			this.encode();
		} catch (UnknownHostException e) {
			System.out.println("Unknown Host: "+e);
		} catch (IOException e) {
			System.out.println("IO Exception: "+e);
		}
	}

	public void encode() throws IOException {
		JsonWriter writer = new JsonWriter(	new OutputStreamWriter( socket.getOutputStream(), "UTF-8" ) );

		//encode and send messages forever
		while( go.get() ) {
			try {
				//block and wait for messages
				Delta message_out = outgoing.take();
				System.out.println("Message_out: " + message_out);
				gson.toJson(message_out, message_out.getClass(), writer);
				writer.flush();
			} catch (InterruptedException e ) {
				System.out.println("InterruptedException: " + e);
			}
		}
	}

	public void decode() throws IOException {
		JsonReader reader = new JsonReader(new BufferedReader(new InputStreamReader( socket.getInputStream())));

		try {
			while( go.get() ) {
				try {
					Delta message_in = gson.fromJson(reader, Delta.class);
					incoming.put( message_in );
					System.out.println("Message_in: " + message_in.getClass().getName());
				} catch (InterruptedException e ) {
					System.out.println("InterruptedException: " + e);
				}
			}
		} catch (JsonParseException e) {
			System.out.println("JsonParseException: " + e);
			cleanup();
		}
	}

	private class DecodeThread implements Runnable {
		public void run() {
			try {
				decode();
			} catch( Exception e ) {
				System.out.println("Deocde Thread Exception: " + e );
				cleanup();
			}
		}
	}

	@Override
	public Delta deserialize( final JsonElement jsonRaw, final java.lang.reflect.Type type, final JsonDeserializationContext context ) throws JsonParseException {
		//All messages are objects, get this out of the way
		JsonObject json = jsonRaw.getAsJsonObject();

		//Guess message type based on fields
		if( json.has("Command") ) {
			return context.deserialize(json, TableDelta.class );
		} else if( json.has("DeckTo") ) {
			return context.deserialize(json, DeckDelta.class );
		} else if( json.has("Id") ) {
			return context.deserialize(json, PlayerDelta.class );
		} else if( json.has("Deck") ) {
			return context.deserialize(json, ActionDelta.class );
		}

		throw new JsonParseException("Unknown Message Delta from server");
	}
}
