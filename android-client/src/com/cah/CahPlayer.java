package com.cah;

import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import android.app.AlertDialog;
import android.widget.TextView;

import com.cah.datastructures.Card;
import com.cah.datastructures.Player;

/**
 * Class that handles all gameplay interactions with the UI
 *
 */
public class CahPlayer {
	
	final AtomicBoolean go = new AtomicBoolean(true);
	final Cah cahActivity;
	final CahClient client;
	String tableID;
	int playerId;
	Thread messageHandler;

	/**
	 * 
	 * @param cahActivity
	 * @param client
	 * @param tableToJoin Table ID to join. Creates a new table if null
	 */
	public CahPlayer(Cah cahActivity, CahClient client, String tableToJoin) {
		this.cahActivity = cahActivity;
		this.client = client;
		this.tableID = tableToJoin;
		
		Cah.performOnBackgroundThread(new Runnable() {

			@Override
			public void run() {
				try {
					messageHandler = new Thread(new HandleMessageThread());
					messageHandler.start();
					
					// Go ahead and join/create table at this point.
					if(CahPlayer.this.tableID != null) {
						// Join table
						CahPlayer.this.client.outgoing.put(new TableDelta("join", CahPlayer.this.tableID));
					} else {
						// Create new table.
						CahPlayer.this.client.outgoing.put(new TableDelta("new", null));
					}
				} catch (InterruptedException e) {
					//TODO: Die gracefully
					e.printStackTrace();
				}
			}

		}); // End performOnBackgroundThread

	}
	
	public void handleIncomingMessages(BlockingQueue<Delta> incoming, BlockingQueue<Delta> outgoing) throws InterruptedException {
		while( go.get() ) {
			Delta incoming_message = incoming.take(); // This will block until something comes in.
			System.out.println("in handleIncomingMessages(): " + incoming_message.toString());
			this.showDebugText(incoming_message.toString());
			Class<? extends Delta> c = incoming_message.getClass();
			if(c == TableDelta.class){
				//TODO: Implement this type of delta.
				TableDelta delta = (TableDelta) incoming_message;
				if(delta.Command.equals("ok")) {
					this.tableID = delta.Id;
					//ask for an id
					outgoing.put(new PlayerDelta(0, "my-id?"));
				}
			} else if (c == DeckDelta.class){
				//TODO: Implement this type of delta.
				DeckDelta delta = (DeckDelta) incoming_message;
				if(delta.DeckTo.equals("hand") && delta.DeckFrom.equals("draw")) {
					// Add the card to our hand
					for(String cardText : delta.Cards) {
						addCardToHand(new Card(Card.Color.WHITE, cardText));
					}
				}
			} else if (c == PlayerDelta.class) {
				//TODO: Implement this type of delta.
				PlayerDelta delta = (PlayerDelta) incoming_message;
				if(delta.Message.equals("your-id")) {
					this.playerId = delta.Id;
					outgoing.put(new PlayerDelta(this.playerId, "join"));
				} else if (delta.Id != this.playerId){
					//TODO: Unlock the player's hand when the server tells us that the round has started.
					cahActivity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							cahActivity.playerCanPlayCard(true);
						}
					});
				}
				// When joining table, client should send a player delta with a 0 Id and message "join".
				// Server should send a reply delta with next Id and message "you"
				// followed by zero or more player deltas that are the other people
			} else if (c == ActionDelta.class) {
				//TODO: Implement this type of delta.
			}
		}
	}
	//Helper thread to handle incoming messages in a blocking fashion
	private class HandleMessageThread implements Runnable {
		public void run() {
			try {
				handleIncomingMessages(client.incoming, client.outgoing);
			} catch (Exception e) {
				System.err.println("Unexpected Incoming Message Handler Thread Exception: " + e + "\n" + Arrays.toString(e.getStackTrace()) );
			} finally {
				//Shutdown threads in case this thread encountered an error
				shutdown();
			}
		}
	}
	
	public void shutdown() {
		go.set(false);
		messageHandler.interrupt();
		client.shutdown();
	}

	
	/**
	 * This function is called by CahClient when the server
	 * says that a card should be added to our hand in the UI.
	 * 
	 * @param card Card to be added to hand
	 */
	public void addCardToHand(final Card card) {
		cahActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				cahActivity.addCardToHand(card);
			}
		});
		
	}
	
	/**
	 * This function is called by CahClient when a player
	 * joins our table. This adds the player to the table UI.
	 * 
	 * @param player The player that joined
	 */
	public void playerJoined(Player player) {
		//TODO: Implement this function.
	}
	
	/** 
	 * This function is called by CahClient
	 * when a player leaves our table.
	 * This removes the player from the table UI.
	 * 
	 * @param player The player that left
	 */
	public void playerLeft(Player player) {
		//TODO: Implement this function.
	}
	
	/**
	 * This function is called by CahClient when the
	 * current czar changes. This changes the table UI
	 * so that a crown appears next to whoever is the czar.
	 * 
	 * @param player The player that is now the czar
	 */
	public void playerIsCzar(Player player) {
		//TODO: Implement this function.
	}
	
	public void showError(final String error, final String errorMessage) {
		cahActivity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				AlertDialog.Builder alertBuilder = new AlertDialog.Builder(cahActivity);
				alertBuilder.setTitle(error);
				alertBuilder.setMessage(errorMessage);
				alertBuilder.setPositiveButton("Close", null);
				alertBuilder.show();
			}
			
		});
	}
	
	public void showDebugText(final String debugText) {
		cahActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				TextView debugTextView = (TextView) cahActivity.findViewById(R.id.debugTextView);
				debugTextView.setText("Table ID: " + CahPlayer.this.tableID + "\n" + debugText);
			}
		});
	}
}
