package clientbj;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import common.BlackJackData;

public class VentanaSalaJuego extends JInternalFrame {
	    
		private PanelJugador dealer, yo, jugador2;
		private JTextArea areaMensajes;
		private JButton hit, stand;
		private JPanel panelYo, panelBotones, yoFull, panelDealer,panelJugador2;
		
		private String yoId, jugador2Id;
		//private DatosBlackJack datosRecibidos;
		private Escucha escucha;
		
		public VentanaSalaJuego(String yoId, String jugador2Id) {
			this.yoId = yoId;
			this.jugador2Id = jugador2Id;
			//this.datosRecibidos=datosRecibidos;
						
			initGUI();
			
			//default window settings
			this.setTitle("Sala de juego BlackJack - Jugador: "+yoId);
			this.pack();
			this.setLocation((ClienteBlackJack.WIDTH-this.getWidth())/2, 
			         (ClienteBlackJack.HEIGHT-this.getHeight())/2);
			this.setResizable(false);
			this.show();
		}

		private void initGUI() {
			// TODO Auto-generated method stub
			//set up JFrame Container y Layout
	        
			//Create Listeners objects
			escucha = new Escucha();
			//Create Control objects
						
			//Set up JComponents
			panelDealer = new JPanel();
			dealer = new PanelJugador("Dealer");
			panelDealer.add(dealer);
			add(panelDealer,BorderLayout.NORTH);		
			
			panelJugador2 = new JPanel();
			jugador2= new PanelJugador(jugador2Id);	
			panelJugador2.add(jugador2);
			add(panelJugador2,BorderLayout.EAST);	
			
			areaMensajes = new JTextArea(8,18);
			JScrollPane scroll = new JScrollPane(areaMensajes);	
			Border blackline;
			blackline = BorderFactory.createLineBorder(Color.black);
			TitledBorder bordes;
			bordes = BorderFactory.createTitledBorder(blackline, "Area de Mensajes");
	        bordes.setTitleJustification(TitledBorder.CENTER);
			scroll.setBorder(bordes);
			areaMensajes.setOpaque(false);
			areaMensajes.setBackground(new Color(0, 0, 0, 0));
			areaMensajes.setEditable(false);

			scroll.getViewport().setOpaque(false);
			scroll.setOpaque(false);
			add(scroll,BorderLayout.CENTER);
			
			panelYo = new JPanel();
			panelYo.setLayout(new BorderLayout());
			yo = new PanelJugador(yoId);
			panelYo.add(yo);
				
			hit = new JButton("Carta");
			hit.setEnabled(false);
			hit.addActionListener(escucha);
			stand = new JButton("Plantar");
			stand.setEnabled(false);
			stand.addActionListener(escucha);
			panelBotones = new JPanel();
			panelBotones.add(hit);
			panelBotones.add(stand);
			
			yoFull = new JPanel();
			yoFull.setPreferredSize(new Dimension(206,100));
			yoFull.add(panelYo);
			yoFull.add(panelBotones);
			add(yoFull,BorderLayout.WEST);	
		}
		
		public void activarBotones(boolean turno) {
			hit.setEnabled(turno);
			stand.setEnabled(turno);
		}
		
		public void pintarCartasInicio(BlackJackData datosRecibidos) {
			if(datosRecibidos.getIdPlayers()[0].equals(yoId)) {
				yo.pintarCartasInicio(datosRecibidos.getPlayer1CardsHand());
				jugador2.pintarCartasInicio(datosRecibidos.getPlayer2CardsHand());
			}else {
				yo.pintarCartasInicio(datosRecibidos.getPlayer2CardsHand());
				jugador2.pintarCartasInicio(datosRecibidos.getPlayer1CardsHand());
			}
			dealer.pintarCartasInicio(datosRecibidos.getDealerCardsHand());
			
			areaMensajes.append(datosRecibidos.getMessage()+"\n");
		}
		
		public void pintarTurno(BlackJackData datosRecibidos) {
			areaMensajes.append(datosRecibidos.getMessage()+"\n");	
			ClienteBlackJack cliente = (ClienteBlackJack)this.getTopLevelAncestor();
			
			if(datosRecibidos.getPlayer().contentEquals(yoId)){
				if(datosRecibidos.getPlayerStatus().equals("start")) {
					activarBotones(true);
				}else {
					if(datosRecibidos.getPlayerStatus().equals("stand") ){
						cliente.setTurno(false);
					}else {
						yo.pintarLaCarta(datosRecibidos.getCard());
						if(datosRecibidos.getPlayerStatus().equals("flew")) {
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									// TODO Auto-generated method stub
									activarBotones(false);
									cliente.setTurno(false);
								}});			
						      }
						}
					} 
			 }else {//movidas de los otros jugadores
					if(datosRecibidos.getPlayer().equals(jugador2Id)) {
						//mensaje para PanelJuego jugador2
						if(datosRecibidos.getPlayerStatus().equals("keep")||
						   datosRecibidos.getPlayerStatus().equals("flew")) {
							jugador2.pintarLaCarta(datosRecibidos.getCard());
						}
					}else {
						//mensaje para PanelJuego dealer
						if(datosRecibidos.getPlayerStatus().equals("keep") ||
						   datosRecibidos.getPlayerStatus().equals("flew")	||
						   datosRecibidos.getPlayerStatus().equals("stand")) {
							dealer.pintarLaCarta(datosRecibidos.getCard());
						}
					}
				}			 	
		}		
	   
	   private void enviarDatos(String mensaje) {
			// TODO Auto-generated method stub
		  ClienteBlackJack cliente = (ClienteBlackJack)this.getTopLevelAncestor();
		  cliente.enviarMensajeServidor(mensaje);
		}
		   
	  
	   private class Escucha implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent actionEvent) {
			// TODO Auto-generated method stub
			if(actionEvent.getSource()==hit) {
				//enviar pedir carta al servidor
				enviarDatos("hit");				
			}else {
				//enviar plantar al servidor
				enviarDatos("stand");
				activarBotones(false);
			}
		}
	   }
}
