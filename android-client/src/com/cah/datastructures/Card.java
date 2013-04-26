package com.cah.datastructures;

import com.cah.Delta;

/* Used by CahClient.java to encode and decode cards! */
public class Card extends Delta {
	public enum Color {BLACK, WHITE};
	public Color color;
	public String text;
	public int numberOfBlanks;


	/**
	 * Main constructor for Card class.
	 *
	 * @param color The color of the card.
	 * @param text Text that appears on the card.
	 * @param numberOfBlanks For black cards, the number of white cards that must be picked. Set to 0 if the card is white.
	 * @throws IllegalArgumentException Thrown if a white card has more than zero blanks.
	 */
	public Card(Color color, String text, int numberOfBlanks) throws IllegalArgumentException {
		if(color == Color.WHITE && numberOfBlanks > 0) {
			throw new IllegalArgumentException("Invalid numberOfBlanks");
		}

		this.color = color;
		this.text = text;
		this.numberOfBlanks = numberOfBlanks;
	}

	/**
	 * Constructor for Card class
	 *
	 * @param color The color of the card. Sets numberOfBlanks to 1 if the card color isn't WHITE.
	 * @param text Text that appears on the card.
	 */
	public Card(Color color, String text) {
		if(color == Color.WHITE){
			this.numberOfBlanks = 0;
		} else {
			this.numberOfBlanks = 1;
		}

		this.color = color;
		this.text = text;
	}

	/**
	 * Constructor that guesses card type, used for decoding
	 *
	 * @param text Text that appears on the card.
	 */
	public Card(String text) {
		this.text = text;

		//all chards under 6 characters are white
		if( this.text.length() > 6 ) {
			if( this.text.contains("_____________") ) {
				this.color = Color.BLACK;
				//calc number of __'s
				int count = 0;
				for( int i = 0; i < text.length(); i++ ) {
					if( text.charAt(i) == '_' ) {
						count++;
					}
				}

				this.numberOfBlanks = Math.min(count/13,1);

				return;
			} else if( this.text.lastIndexOf('?') == this.text.length()-1 ) {
				this.color = Color.BLACK;
				this.numberOfBlanks = 1;

				return;
			}
		}

		this.color = Color.WHITE;
		this.numberOfBlanks = 0;
	}
}
