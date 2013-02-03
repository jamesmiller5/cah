package com.cah;

import android.util.Log;
import java.util.Queue;
import java.util.LinkedList;

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

	Queue<Delta> incoming;
	Queue<Delta> outgoing;
	public CahClient(Queue<Delta> x, Queue<Delta> y) {
		incoming = x;
		outgoing = y;
}

	public void run() {
		Log.d("Cah", "schlemschlemschlem");
	}

}
