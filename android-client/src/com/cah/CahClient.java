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

	public TableDelta( String command, String id ) {
		Command = command;
		Id = id;
	}
}

class DeckDelta extends Delta {
	int Player;
	String DeckTo;
	String DeckFrom;
	int Amount; //TODO: This should be an array if card indicators
	
	public DeckDelta( int Player, String DeckTo, String DeckFrom, int Amount ) {
		this.Player = Player;
		this.DeckTo = DeckTo;
		this.DeckFrom = DeckFrom;
		this.Amount = Amount;
	}
}

class PlayerDelta extends Delta{
	int Id;
	String Message;
	
	public PlayerDelta( int id, String message ) {
		this.Id = id;
		this.Message = message;
	}
}

class ActionDelta extends Delta {
	DeckDelta Deck;
	PlayerDelta Player;
}

public class CahClient extends Thread implements JsonDeserializer<Delta> {
	BlockingQueue<Delta> incoming;
	BlockingQueue<Delta> outgoing;
	Socket socket;
	final Gson gson = new GsonBuilder().registerTypeAdapter(Delta.class, this).create();
	final AtomicBoolean go = new AtomicBoolean(true);
	Thread encoder, decoder;

	/* For Debug */
	public static void main( String args[] ) {
		try {
			CahClient player1 = new CahClient();
			CahClient player2 = new CahClient();

			//Make Player1 the host
			player1.start();

			player1.outgoing.put(new TableDelta("new", null));

			TableDelta table_reply = (TableDelta) player1.incoming.take();
			System.out.println("Reply: "+ table_reply);

			assert table_reply.Command != null;
			assert table_reply.Command.equals("ok");
			assert table_reply.Id != null;
			assert table_reply.Id.length() == 6;

			System.out.println("Starting player 2");
			player2.start();
			player2.outgoing.put(new TableDelta("join", table_reply.Id));

			System.out.println("Player1 : " + player1.incoming.take());
			System.out.println("Player2 : " + player2.incoming.take());

		} catch( Exception e ) {
			System.out.println("Debug failed: " + e);
		}
	}

	public CahClient( BlockingQueue<Delta> in, BlockingQueue<Delta> out ) {
		incoming = in;
		outgoing = out;
	}

	public CahClient() {
		incoming = (BlockingQueue<Delta>) new ArrayBlockingQueue<Delta>( 32, true );
		outgoing = (BlockingQueue<Delta>) new ArrayBlockingQueue<Delta>( 32, true );
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

			socket = new Socket("10.0.2.2", 41337);
			socket.setTcpNoDelay(true);
			socket.setReuseAddress(true);

			Thread.sleep(3000);

			decoder.start();
			this.encode();
		} catch (UnknownHostException e) {
			System.out.println("Unknown Host: "+e);
		} catch (IOException e) {
			System.out.println("IO Exception: "+e);
		} catch (Exception e ) {
			System.out.println("Unexpected Exception: " + e);
		} finally {
			cleanup();
		}
	}

	public void encode() throws IOException {
		JsonWriter writer = new JsonWriter(	new OutputStreamWriter( socket.getOutputStream(), "UTF-8" ) );

		//encode and send messages until go is false
		while( go.get() ) {
			try {
				//block and wait for messages from Message-Queue
				Delta message_out = outgoing.take();
				System.out.println("Message_out: " + message_out);
				gson.toJson(message_out, message_out.getClass(), writer);
				writer.flush(); //Flush writer to ensure message delivery asap
			} catch (InterruptedException e ) {
				System.out.println("InterruptedException: " + e);
			}
		}
	}

	public void decode() throws IOException {
		JsonReader reader = new JsonReader(new BufferedReader(new InputStreamReader( socket.getInputStream())));

		try {
			//recieve and decode messages until go is false
			while( go.get() ) {
				try {
					//block and wait for messages from Socket
					Delta message_in = gson.fromJson(reader, Delta.class);
					incoming.put( message_in );
					System.out.println("Message_in: " + message_in);
				} catch (InterruptedException e ) {
					System.out.println("InterruptedException: " + e);
				}
			}
		} catch (JsonParseException e) {
			System.out.println("JsonParseException: " + e);
			cleanup();
		}
	}

	//Helper thread to decode json in a blocking fashion
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
