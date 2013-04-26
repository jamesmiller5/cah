package com.cah;

import com.cah.datastructures.Card;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

class CardEater implements JsonDeserializer<Card> {
	public Card deserialize( final JsonElement jsonRaw, final java.lang.reflect.Type type, final JsonDeserializationContext context ) throws JsonParseException {
		//All messages are objects, get this out of the way
		JsonObject json = jsonRaw.getAsJsonObject();

			//it's a card
		if( json.has("Text") ) {
			Card card =  new Card( json.getAsJsonPrimitive("Text").getAsString() );
			return card;
		} else

		throw new JsonParseException("Unknown Message Delta from server");
	}
}

public class CahClient extends Thread implements JsonDeserializer<Delta>, JsonSerializer<Delta> {
	final Gson gson = new GsonBuilder().registerTypeAdapter(Delta.class, this).registerTypeAdapter(Card.class, new CardEater()).create();
	final AtomicBoolean go = new AtomicBoolean(true);
	BlockingQueue<Delta> incoming;
	BlockingQueue<Delta> outgoing;
	Socket socket;
	Thread encoder;

	public CahClient( BlockingQueue<Delta> in, BlockingQueue<Delta> out ) {
		incoming = in;
		outgoing = out;
	}

	public CahClient() {
		incoming = new ArrayBlockingQueue<Delta>( 32, true );
		outgoing = new ArrayBlockingQueue<Delta>( 32, true );
	}

	/* For Debug */
	public static void main( String args[] ) {
		try {
			System.out.println("Starting Test");

			CahClient player1 = new CahClient();
			CahClient player2 = new CahClient();

			//start player1 but not player2
			player1.start();

			//let player1 make the table
			player1.outgoing.put(new TableDelta("new", null));
			TableDelta table_reply = (TableDelta) player1.incoming.take();
			assert table_reply.Command != null;
			assert table_reply.Command.equals("ok");
			assert table_reply.Id != null;
			assert table_reply.Id.length() == 6;

			//ask for an id player 1
			player1.outgoing.put(new PlayerDelta(0, "my-id?"));
			PlayerDelta id_reply = (PlayerDelta) player1.incoming.take();
			assert id_reply.Id == 1;
			assert id_reply.Message.equals("your-id");

			//join the game
			player1.outgoing.put(new PlayerDelta(1, "join"));

			//get cards from server for player 1
			System.out.println("Player 1 should now get his/her cards");
			DeckDelta player1Deck = (DeckDelta) player1.incoming.take();
			assert player1Deck.Cards.length == 7;
			assert player1Deck.DeckFrom.equals("white-draw");
			assert player1Deck.DeckTo.equals("hand");

			//start player 2
			player2.start();

			//have player 2 join the table
			player2.outgoing.put(new TableDelta("join", table_reply.Id));
			TableDelta p2_table_reply = (TableDelta) player2.incoming.take();
			assert p2_table_reply.Command.equals("ok");
			assert p2_table_reply.Id.equals(table_reply.Id);

			//ask for an id for player 2
			player2.outgoing.put(new PlayerDelta(0, "my-id?"));
			PlayerDelta p2_id_reply = (PlayerDelta) player2.incoming.take();
			assert p2_id_reply.Id == 2;
			assert p2_id_reply.Message.equals("your-id");

			//player2 joins game
			player2.outgoing.put(new PlayerDelta(2, "join"));

			//get cards from server for player 2
			System.out.println("Player 2 should now get his/her cards");
			DeckDelta player2Deck = (DeckDelta) player2.incoming.take();
			assert player2Deck.Cards.length == 7;
			assert player2Deck.DeckFrom.equals("white-draw");
			assert player2Deck.DeckTo.equals("hand");

			// after we join, we should get information about all players at the table
			System.out.println("Player2 joins and should get info from table");
			PlayerDelta playersAtTableReply2 = (PlayerDelta) player2.incoming.take();
			assert playersAtTableReply2.Id == 1;

			// player1 should now see player 2 join
			System.out.println("player1 should now see player 2 join");
			PlayerDelta p1_joining_of_p2 = (PlayerDelta) player1.incoming.take();
			assert p1_joining_of_p2.Id == 2;
			assert p1_joining_of_p2.Message.equals("join");

			// player1 should now be the czar
			System.out.println("player 1 should see czar with id 1");
			PlayerDelta p1_is_czar = (PlayerDelta) player1.incoming.take();
			assert p1_is_czar.Message.equals("is-czar");
			assert p1_is_czar.Id == 1;

			//Player 2 should see that player 1 is czar
			System.out.println("player 2 should see czar with id 1");
			PlayerDelta p2_is_czar = (PlayerDelta) player2.incoming.take();
			assert p2_is_czar.Message.equals("is-czar");
			assert p2_is_czar.Id == 1;

			//all should get a deck delta with the black card
			System.out.println("Player 1 should get deckdelta with black card info");
			DeckDelta p1_bl_crd = (DeckDelta) player1.incoming.take();
			assert p1_bl_crd.Cards.length == 1;
			assert p1_bl_crd.DeckFrom.equals("black-draw");
			assert p1_bl_crd.DeckTo.equals("play");

			System.out.println("Player 2 should get deckdelta with black card info");
			DeckDelta p2_bl_crd = (DeckDelta) player2.incoming.take();
			assert p2_bl_crd.Cards.length == 1;
			assert p2_bl_crd.DeckFrom.equals("black-draw");
			assert p2_bl_crd.DeckTo.equals("play");

			System.out.println("Sending cards to server!");
			//our turn! send a white card we pick
			player1.outgoing.put(new DeckDelta(1, "hand", "play", new Card[] {new Card("RANDOM PLAYER 1")}));
			player2.outgoing.put(new DeckDelta(2, "hand", "play", new Card[] {new Card("RANDOM PLAYER 2")}));

			//have player 1 leave and see player 2's update
			System.out.println("Player 1 is disconnecting, should show up on player 2");
			Thread.sleep(1000*1);

			//Assert that player2 saw that player1 left the game
			player1.shutdown();
			PlayerDelta p2_leave_reply = (PlayerDelta) player2.incoming.take();
			assert p2_leave_reply.Id == 1;
			assert p2_leave_reply.Message.equals("leave");

			//Close player2 and exit
			player2.shutdown();

			System.out.println("Test Passed");
		} catch( Exception e ) {
			System.out.println("Debug failed: " + e);
		}
	}

	public void run() {
		//Keep track of our threads, they will need to interrupt each other in the event of errors or close()
		encoder = (Thread) this;
		Thread decoder = new Thread(new DecodeThread());

		String hosts[] = {
			"localhost", // Look for localhost connection first.
			"10.0.2.2",   // If that fails attempt to connect to the emulator host's loopback.
			"erictempl.in"};

		try {
			boolean connectedToServer = false;

			for( String host : hosts ) {
				socket = new Socket();
				socket.setTcpNoDelay(true);
				socket.setReuseAddress(true);

				try {
					socket.connect(new InetSocketAddress(host, 41337), 1000);
					connectedToServer = true;
					System.out.println("Connected to server at " + host);
					break;

				} catch ( IOException e ) {
					//can't connect
				}
			}

			if( connectedToServer == false ) {
				System.out.println("Can't connect to server");
				return;
			}

			decoder.start();

			encode();
		} catch (Exception e ){
			System.out.println("Unexpected Exception in CahClient.run(): " + e + "\n" + Arrays.toString(e.getStackTrace()) );
		}
	}

	//Helper thread to decode json in a blocking fashion
	private class DecodeThread implements Runnable {
		public void run() {
			try {
				decode();
			} catch( JsonSyntaxException e ) {
				//Json library throws this when connection is closed
				System.out.println("SOCKET CLOSED!");
			} catch( Exception e ) {
				System.out.println("Unexpected Decode Thread Exception: " + e + "\n" + Arrays.toString(e.getStackTrace()) );
			} finally {
				//Shutdown thread pair in case this thread encountered an error
				shutdown();
			}
		}
	}

	public void shutdown() {
		//Shutdown only once
		if( go.get() ) {
			go.set(false);

			//interrupt the encoder as it's probably waiting for a message on the incoming queue
			if( encoder != null ) {
				encoder.interrupt();
			}

			//decoder thread gets an exception from socket and will exit gracefully
			try {
				if( socket != null && !socket.isClosed() )
					socket.close();
			} catch( IOException e ) {
				//socket is already closed, continue with shutdown
			}
		}
	}

	public void encode() throws IOException {
		JsonWriter writer = new JsonWriter(	new OutputStreamWriter( socket.getOutputStream(), "UTF-8" ) );
		while( go.get() ) {
			try {
				Delta message_out = outgoing.take();
				System.out.println("Message_out: " + message_out);
				gson.toJson(message_out, Delta.class, writer);
				writer.flush(); //Flush writer to ensure message delivery asap
			} catch (InterruptedException e ) {
				//do nothing, expected exception while shutting down
			}
		}
	}

	public void decode() throws IOException {
		JsonReader reader = new JsonReader(new BufferedReader(new InputStreamReader( socket.getInputStream())));
		while( go.get() ) {
			//block and wait for messages from Socket
			Delta message_in = gson.fromJson(reader, Delta.class);
			System.out.println("Message_in: " + message_in);
			try {
				incoming.put( message_in );
			} catch (InterruptedException e ) {
				//do nothing, thread is shutting down
			}
		}
	}

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

	public JsonElement serialize(final Delta src, final java.lang.reflect.Type type, final JsonSerializationContext context ) {
		//wrap some classes in an action delta if need be
		if( src.getClass() == PlayerDelta.class ) {
			return context.serialize(new ActionDelta((PlayerDelta)src), ActionDelta.class);
		} else if( src.getClass() == DeckDelta.class ) {
			return context.serialize(new ActionDelta((DeckDelta)src), ActionDelta.class);
		} else if( src.getClass() == ActionDelta.class ) {
			//makes no sense, action delta's are a wrapper and shouldn't be used manually
			throw new IllegalStateException("Unable to encode ActionDelta, should be done automatically");
		} else {
			return context.serialize(src, src.getClass());
		}
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
	Card[] Cards;

	public DeckDelta( int Player, String DeckTo, String DeckFrom, Card[] Cards ) {
		this.Player = Player;
		this.DeckTo = DeckTo;
		this.DeckFrom = DeckFrom;
		this.Cards = Cards;
	}
}

class PlayerDelta extends Delta{
	int Id;
	String Message;

	public PlayerDelta( int id, String message ) {
		Id = id;
		Message = message;
	}
}

class ActionDelta extends Delta {
	DeckDelta Deck;
	PlayerDelta Player;
	boolean Keepalive = true;

	public ActionDelta(DeckDelta d) {
		Deck = d;
	}
	public ActionDelta(PlayerDelta p) {
		Player = p;
	}
}
