package com.cah.datastructures;

public class Card {
	public enum Color {BLACK, WHITE};
	public final Color color;
	public final String text;
	public final int numberOfBlanks;
	
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
}
