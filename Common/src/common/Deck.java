package common;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import javax.imageio.ImageIO;

public class Deck {	
   public static final String CARTAS_FILE="/resources/cards.png";
   public static final String CARTA_TAPADA_FILE="/resources/cardBack.png";
   public static final int CARTA_WIDTH=45;
   public static final int CARTA_HEIGHT=60;
   private static final int PALOS=4;
   private static final int VALORES=13;
   private static final int CARTA_BACK_INDEX=PALOS*VALORES;
   private static final int TOTAL_IMAGES=PALOS*VALORES+1;
  
   private ArrayList<Card> mazo;
   private Random aleatorio;
   
   public Deck() {
	   aleatorio = new Random();
	   mazo = new ArrayList<Card>();
	   String valor;
	   for(int i=1;i<=4;i++) {
		   for(int j=2;j<=14;j++) {
			   switch(j) {
			   case 11: valor="J";break;
			   case 12: valor="Q";break;
			   case 13: valor="K";break;
			   case 14: valor="As";break;
			   default: valor= String.valueOf(j);break;
			   } 
			   switch(i) {
			   case 1: mazo.add(new Card(valor,"C"));break;
			   case 2: mazo.add(new Card(valor,"D"));break;
			   case 3: mazo.add(new Card(valor,"P"));break;
			   case 4: mazo.add(new Card(valor,"T"));break;
			   }
		   }
	   }
   }
     
   public Card getCarta() {
	   int index = aleatorio.nextInt(mazoSize());
	   Card carta = mazo.get(index);
	   mazo.remove(index); //elimina del mazo la carta usada
	   return carta;
   }
   
   public int mazoSize() {
	   return mazo.size();
   }
}
