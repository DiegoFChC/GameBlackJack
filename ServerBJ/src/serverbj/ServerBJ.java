package serverbj;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import common.Deck;
import common.Card;
import common.BlackJackData;

/* Clase encargada de realizar la gesti?n del juego, esto es, el manejo de turnos y estado del juego.
 * Tambi?n gestiona al jugador Dealer. 
 * El Dealer tiene una regla de funcionamiento definida:
 * Pide carta con 16 o menos y Planta con 17 o mas.
 */
public class ServerBJ implements Runnable{
	//constantes para manejo de la conexion.
	public static final int PORT = 7377;
	public static final String IP = "127.0.0.1";
	public static final int QUEUE_LENGTH = 2;

	// variables para funcionar como servidor
	private ServerSocket server;
	private Socket connectionPlayer;
	
	//variables para manejo de hilos
	private ExecutorService threadManager;
	private Lock gameLock;
	private Condition waitStart, waitTurn, finish;
	private Player[] players;
	
	//variables de control del juego
	private String[] idPlayers;
	private int playerInTurn;
	//private boolean iniciarJuego;
	private Deck deck;
	private ArrayList<ArrayList<Card>> playersCardsHand;
	private ArrayList<Card> player1CardsHand;
	private ArrayList<Card> player2CardsHand;
	private ArrayList<Card> dealerCardsHand;
	private int[] cardsHandValue;
	private BlackJackData dataSend;
	
	public ServerBJ() {
	    //inicializar variables de control del juego
		initializeRoundControlVariables();
	    //inicializar las variables de manejo de hilos
		initializeThreadsManagerVariables();
		//crear el servidor
    	try {
    		showMessage("Starting the server...");
			server = new ServerSocket(PORT, QUEUE_LENGTH);			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
    private void initializeThreadsManagerVariables() {
		// TODO Auto-generated method stub
    	threadManager = Executors.newFixedThreadPool(QUEUE_LENGTH);
    	gameLock = new ReentrantLock();
    	waitStart = gameLock.newCondition();
    	waitTurn = gameLock.newCondition();
    	finish = gameLock.newCondition();
    	players = new Player[QUEUE_LENGTH];	
	}

	private void initializeRoundControlVariables() {
		// TODO Auto-generated method stub
    	 //Variables de control del juego.
		
		idPlayers = new String[2];
		cardsHandValue = new int[3];
		
		deck = new Deck();
		Card card;
		
		player1CardsHand = new ArrayList<Card>();
		player2CardsHand = new ArrayList<Card>();
		dealerCardsHand = new ArrayList<Card>();
		
		//reparto inicial jugadores 1 y 2
		for(int i=1;i<=2;i++) {
		  card = deck.getCard();
		  player1CardsHand.add(card);
		  calculateCardsHandValue(card, 0);
		  card = deck.getCard();
		  player2CardsHand.add(card);
		  calculateCardsHandValue(card, 1);
		}
		//Carta inicial Dealer
		card = deck.getCard();
		dealerCardsHand.add(card);
		calculateCardsHandValue(card,2);
		
		//gestiona las tres manos en un solo objeto para facilitar el manejo del hilo
		playersCardsHand = new ArrayList<ArrayList<Card>>(3);
		playersCardsHand.add(player1CardsHand);
		playersCardsHand.add(player2CardsHand);
		playersCardsHand.add(dealerCardsHand);
	}

	private void calculateCardsHandValue(Card card, int i) {
		// TODO Auto-generated method stub
    	
			if(card.getValue().equals("As")) {
				cardsHandValue[i]+=11;
			}else {
				if(card.getValue().equals("J") || card.getValue().equals("Q")
						   || card.getValue().equals("K")) {
					cardsHandValue[i]+=10;
				}else {
					cardsHandValue[i]+=Integer.parseInt(card.getValue()); 
				}
		}
	}
	
	public void start() {
       	//esperar a los clientes
		showMessage("Waiting for the players...");
    	
    	for(int i = 0; i < QUEUE_LENGTH;i++) {
    		try {
    			connectionPlayer = server.accept();
    			players[i] = new Player(connectionPlayer, i);
				threadManager.execute(players[i]);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
    	} 	
    }
    
	private void showMessage(String message) {
		System.out.println(message);
	}
	
	private void startGameRound() {
		
		this.showMessage("Blocking the server to wake up player 1");
		gameLock.lock();
    	
    	//despertar al jugador 1 porque es su turno
    	try {
    		this.showMessage("Waking up player 1 to start the game");
    		players[0].setSuspended(false);
        	waitStart.signal();
    	}catch(Exception e) {
    		
    	}finally {
    		this.showMessage("Unlocking the server after waking up Player 1 to start the game");
    		gameLock.unlock();
    	}			
	}
	
    private boolean roundIsOver() {
       return false;	
    }
    
    private void analyzeMessage(String input, int playerIndex) {
		// TODO Auto-generated method stub
        //garantizar que solo se analice la petici?n del jugador en turno.
    	while(playerIndex != playerInTurn) {
    		gameLock.lock();
    		try {
    			waitTurn.await();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		gameLock.unlock();
    	}
    	
    	//valida turnos para jugador 0 o 1
        	
    	if(input.equals("hit")) { //Hit: Pedir
    		//dar carta 
    		showMessage("Card was sent to the player " + idPlayers[playerIndex]);
    		Card card = deck.getCard();
    		//adicionar la carta a la mano del jugador en turno
    		playersCardsHand.get(playerIndex).add(card);
    		calculateCardsHandValue(card, playerIndex);
    		
    		dataSend = new BlackJackData();
    		dataSend.setIdPlayers(idPlayers);
    		dataSend.setCardsHandValue(cardsHandValue);
			dataSend.setCard(card);
			dataSend.setPlayer(idPlayers[playerIndex]);
    		//determinar qu? sucede con la carta dada en la mano del jugador y 
			//mandar mensaje a todos los jugadores
    		if(cardsHandValue[playerIndex]>21) {
    			//jugador Vol?
    			dataSend.setMessage(idPlayers[playerIndex]+" you have " + cardsHandValue[playerIndex]+" you flew :(");	
    			dataSend.setPlayerStatus("flew");
	    		
	    		players[0].sendMessageClient(dataSend);
	    		players[1].sendMessageClient(dataSend);
	    		
	    		//notificar a todos que jugador sigue
	    		if(playerInTurn==0) {
	        		
	    			dataSend = new BlackJackData();
	    			dataSend.setIdPlayers(idPlayers);
	    			dataSend.setCardsHandValue(cardsHandValue);
	    			dataSend.setPlayer(idPlayers[1]);
	    			dataSend.setPlayerStatus("start");
	    			dataSend.setMessage(idPlayers[1]+" you have to play and you have " + cardsHandValue[1]);
					
					players[0].sendMessageClient(dataSend);
					players[1].sendMessageClient(dataSend);
					
					//levantar al jugador en espera de turno
					
					gameLock.lock();
		    		try {
						//esperarInicio.await();
		    			players[0].setSuspended(true);
						waitTurn.signalAll();
						playerInTurn++;
					}finally {
						gameLock.unlock();
					}
	        	} else {//era el jugador 2 entonces se debe iniciar el dealer
	        		//notificar a todos que le toca jugar al dealer
	        		dataSend = new BlackJackData();
	        		dataSend.setIdPlayers(idPlayers);
	        		dataSend.setCardsHandValue(cardsHandValue);
	        		dataSend.setPlayer("dealer");
	        		dataSend.setPlayerStatus("start");
	        		dataSend.setMessage("Dealer, card will be dealt");
					
					players[0].sendMessageClient(dataSend);
					players[1].sendMessageClient(dataSend);
					
					startDealer();
	        	}		
    		}else {//jugador no se pasa de 21 puede seguir jugando
    			dataSend.setCard(card);
    			dataSend.setPlayer(idPlayers[playerIndex]);
    			dataSend.setMessage(idPlayers[playerIndex]+" now you have " + cardsHandValue[playerIndex]);
    			dataSend.setPlayerStatus("keep");//keep: sigue
	    		
	    		players[0].sendMessageClient(dataSend);
	    		players[1].sendMessageClient(dataSend);
	    		
    		}
    	}else {
    		//jugador en turno plant?
    		dataSend = new BlackJackData();
    		dataSend.setIdPlayers(idPlayers);
    		dataSend.setCardsHandValue(cardsHandValue);
    		dataSend.setPlayer(idPlayers[playerIndex]);
    		dataSend.setMessage(idPlayers[playerIndex]+" stood");
    		dataSend.setPlayerStatus("stand");//Stand: Plant?
    		
    		players[0].sendMessageClient(dataSend);		    		
    		players[1].sendMessageClient(dataSend);
    		
    		
    		//notificar a todos el jugador que sigue en turno
    		if(playerInTurn==0) {
        		
    			dataSend = new BlackJackData();
    			dataSend.setIdPlayers(idPlayers);
    			dataSend.setCardsHandValue(cardsHandValue);
    			dataSend.setPlayer(idPlayers[1]);
    			dataSend.setPlayerStatus("start");
    			dataSend.setMessage(idPlayers[1]+" you have to play and you have " + cardsHandValue[1]);
				
				players[0].sendMessageClient(dataSend);
				players[1].sendMessageClient(dataSend);
				
				//levantar al jugador en espera de turno
				
				gameLock.lock();
	    		try {
					//esperarInicio.await();
	    			players[playerIndex].setSuspended(true);
					waitTurn.signalAll();
					playerInTurn++;
				}finally {
					gameLock.unlock();
				}
        	} else {
        		//notificar a todos que le toca jugar al dealer
        		dataSend = new BlackJackData();
        		dataSend.setIdPlayers(idPlayers);
        		dataSend.setCardsHandValue(cardsHandValue);
        		dataSend.setPlayer("dealer");
        		dataSend.setPlayerStatus("start");
        		dataSend.setMessage("Dealer, card will be dealt");
				
				players[0].sendMessageClient(dataSend);
				players[1].sendMessageClient(dataSend);
			
				startDealer();
        	}	
    	}
   } 
    
    public void startDealer() {
       //le toca turno al dealer.
    	Thread dealer = new Thread(this);
    	dealer.start();
    }
    
    /*The Class Jugador. Clase interna que maneja el servidor para gestionar la comunicaci?n
     * con cada cliente Jugador que se conecte
     */
    private class Player implements Runnable{
       
    	//varibles para gestionar la comunicaci?n con el cliente (Jugador) conectado
        private Socket clientConnection;
    	private ObjectOutputStream out;
    	private ObjectInputStream in;
    	private String input;
    	
    	//variables de control
    	private int playerIndex;
    	private boolean suspended;
  
		public Player(Socket clientConnection, int playerIndex) {
			this.clientConnection = clientConnection;
			this.playerIndex = playerIndex;
			suspended = true;
			//crear los flujos de E/S
			try {
				out = new ObjectOutputStream(clientConnection.getOutputStream());
				out.flush();
				in = new ObjectInputStream(clientConnection.getInputStream());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
		}	
				
		private void setSuspended(boolean suspended) {
			this.suspended = suspended;
		}
	   
		@Override
		public void run() {
			// TODO Auto-generated method stub	
			//procesar los mensajes eviados por el cliente
			
			//ver cual jugador es 
			if(playerIndex==0) {
				//es jugador 1, debe ponerse en espera a la llegada del otro jugador
				
				try {
					//guarda el nombre del primer jugador
					idPlayers[0] = (String) in.readObject();
					showMessage("Thread established with player (1) " + idPlayers[0]);
				} catch (ClassNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				showMessage("locks server to put in wait for start to player 1");
				gameLock.lock(); //bloquea el servidor
				
				while(suspended) {
					showMessage("Stopping Player 1 waiting for the other player...");
					try {
						waitStart.await();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}finally {
						showMessage("Unlock Server after blocking player 1");
						gameLock.unlock();
					}
				}
				
				//ya se conect? el otro jugador, 
				//le manda al jugador 1 todos los datos para montar la sala de Juego
				//le toca el turno a jugador 1
				
				showMessage("send player 1 all the data to set up GameRoom");
				dataSend = new BlackJackData();
				dataSend.setDealerCardsHand(playersCardsHand.get(2));
				dataSend.setPlayer1CardsHand(playersCardsHand.get(0));
				dataSend.setPlayer2CardsHand(playersCardsHand.get(1));		
				dataSend.setIdPlayers(idPlayers);
				dataSend.setCardsHandValue(cardsHandValue);
				dataSend.setMessage("You start " + idPlayers[0] + " you have " + cardsHandValue[0]);
				sendMessageClient(dataSend);
				playerInTurn=0;
			}else {
				   //Es jugador 2
				   //le manda al jugador 2 todos los datos para montar la sala de Juego
				   //jugador 2 debe esperar su turno
				try {
					idPlayers[1]=(String) in.readObject();
					showMessage("Thread player (2)" + idPlayers[1]);
				} catch (ClassNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				showMessage("send player 2 the name of player 1");
				
				dataSend = new BlackJackData();
				dataSend.setDealerCardsHand(playersCardsHand.get(2));
				dataSend.setPlayer1CardsHand(playersCardsHand.get(0));
				dataSend.setPlayer2CardsHand(playersCardsHand.get(1));			
				dataSend.setIdPlayers(idPlayers);
				dataSend.setCardsHandValue(cardsHandValue);
				dataSend.setMessage("You start "+idPlayers[0]+" you have " + cardsHandValue[0]);
				sendMessageClient(dataSend);
				
				startGameRound(); //despertar al jugador 1 para iniciar el juego
				showMessage("Block the server to put in turn wait to player 2");
				gameLock.lock();
				try {
					showMessage("Puts on turn wait to player 2");
					waitTurn.await();
					showMessage("Wake up from the wait for start of the game to player 1");
                    //
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}finally {
					gameLock.unlock();
				}	
			}
			
			while(!roundIsOver()) {
				try {
					input = (String) in.readObject();
					analyzeMessage(input, playerIndex);
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					//controlar cuando se cierra un cliente
				}
			}
			//cerrar conexi?n
		}
		
		public void sendMessageClient(Object message) {
			try {  
				out.writeObject(message);
				out.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}			
    }//fin inner class Jugador      

    //Jugador dealer emulado por el servidor
	@Override
	public void run() {
		// TODO Auto-generated method stub
		showMessage("Start the dealer ...");
        boolean hit = true;
        
        while(hit) {
		  	Card card = deck.getCard();
			//adicionar la carta a la mano del dealer
		  	playersCardsHand.get(2).add(card);
			calculateCardsHandValue(card, 2);
			
			showMessage("The dealer receives " + card.toString()+" adds "+ cardsHandValue[2]);
			

			dataSend = new BlackJackData();
			dataSend.setCard(card);
			dataSend.setPlayer("dealer");
				
			if(cardsHandValue[2]<=16) {
				dataSend.setPlayerStatus("keep");
				dataSend.setMessage("Dealer now has "+cardsHandValue[2]);
				showMessage("The dealer keeps playing");
			}else {
				if(cardsHandValue[2] > 21) {
					dataSend.setPlayerStatus("flew");
					dataSend.setMessage("Dealer now has "+cardsHandValue[2]+" flew :(");
					hit=false;
					showMessage("The dealer flew");
				}else {
					dataSend.setPlayerStatus("stand");
					dataSend.setMessage("Dealer now has "+cardsHandValue[2]+" stand");
					hit=false;
					showMessage("The dealer stand");
				}
			}
			//envia la jugada a los otros jugadores
			dataSend.setCard(card);
			players[0].sendMessageClient(dataSend);
			players[1].sendMessageClient(dataSend);
				
        }//fin while
        
	}
    
}//Fin class ServidorBJ
