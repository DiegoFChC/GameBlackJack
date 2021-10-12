package common;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.Serializable;

import javax.swing.ImageIcon;

public class Card implements Serializable{
    private String value;
    private String suit;
 	
    public Card(String value, String suit) {
		this.value = value;
		this.suit = suit;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getSuit() {
		return suit;
	}

	public void serSuit(String suit) {
		this.suit = suit;
	}
	
	public String toString() {
		return value + suit;
	}
	
}
