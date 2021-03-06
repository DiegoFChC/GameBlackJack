package common;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import javax.imageio.ImageIO;

public class Deck {	
   public static final String CARDS_FILE="/resources/cards.png";
   public static final String COVERED_CARD_FILE="/resources/cardBack.png";
   public static final int CARD_WIDTH=45;
   public static final int CARD_HEIGHT=60;
   private static final int SUITS=4;
   private static final int VALUES=13;
   private static final int CARD_BACK_INDEX=SUITS*VALUES;
   private static final int TOTAL_IMAGES=SUITS*VALUES+1;
  
   private ArrayList<Card> deck;
   private Random random;
   
   public Deck() {
	   random = new Random();
	   deck = new ArrayList<Card>();
	   String value;
	   for(int i=1;i<=4;i++) {
		   for(int j=2;j<=14;j++) {
			   switch(j) {
			   case 11: value="J";break;
			   case 12: value="Q";break;
			   case 13: value="K";break;
			   case 14: value="As";break;
			   default: value= String.valueOf(j);break;
			   } 
			   switch(i) {
			   case 1: deck.add(new Card(value,"C"));break;
			   case 2: deck.add(new Card(value,"D"));break;
			   case 3: deck.add(new Card(value,"P"));break;
			   case 4: deck.add(new Card(value,"T"));break;
			   }
		   }
	   }
   }
     
   public Card getCard() {
	   int index = random.nextInt(deckSize());
	   Card card = deck.get(index);
	   deck.remove(index); //elimina del mazo la carta usada
	   return card;
   }
   
   public int deckSize() {
	   return deck.size();
   }
}
