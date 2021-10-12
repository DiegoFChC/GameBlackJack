package common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

public class BlackJackData implements Serializable{
	private String[] idPlayers;
	private ArrayList<Card> player1CardsHand, player2CardsHand, dealerCardsHand;
	private int[] cardsHandValue;
	private Card card;
	private String message;
	private String player, playerStatus;
	
	public String getPlayer() {
		return player;
	}
	public void setPlayer(String player) {
		this.player = player;
	}
	
	public String getPlayerStatus() {
		return playerStatus;
	}
	public void setPlayerStatus(String playerStatus) {
		this.playerStatus = playerStatus;
	}
		
	public String[] getIdPlayers() {
		return idPlayers;
	}
	public void setIdPlayers(String[] idPlayers) {
		this.idPlayers = idPlayers;
	}
	
	public ArrayList<Card> getPlayer1CardsHand() {
		return player1CardsHand;
	}
	public void setPlayer1CardsHand(ArrayList<Card> player1CardsHand) {
		this.player1CardsHand = player1CardsHand;
	}
	
	public ArrayList<Card> getPlayer2CardsHand() {
		return player2CardsHand;
	}
	public void setPlayer2CardsHand(ArrayList<Card> player2CardsHand) {
		this.player2CardsHand = player2CardsHand;
	}
	
	public ArrayList<Card> getDealerCardsHand() {
		return dealerCardsHand;
	}
	public void setDealerCardsHand(ArrayList<Card> dealerCardsHand) {
		this.dealerCardsHand = dealerCardsHand;
	}
	
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public void setCardsHandValue(int[] cardsHandValue) {
		this.cardsHandValue=cardsHandValue;
	}
	public int[] getCardsHandValue() {
		return cardsHandValue;	
	}
	public void setCard(Card card) {
		this.card=card;
	}
	public Card getCard() {
		return card;
	}
}
