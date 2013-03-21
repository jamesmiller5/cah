package com.cah;

import java.io.IOException;

import android.app.AlertDialog;

import com.cah.datastructures.Card;
import com.cah.datastructures.Player;

/**
 * Class that handles all gameplay interactions with the UI
 *
 */
public class CahPlayer {
	
	final Cah cahActivity;
	final CahClient client;
	final String tableID;

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
	
	/**
	 * This function is called by CahClient when the server
	 * says that a card should be added to our hand in the UI.
	 * 
	 * @param card Card to be added to hand
	 */
	public void addCardToHand(Card card) {
		//TODO: Implement this function.
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
}
