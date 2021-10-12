package clientebj;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.JInternalFrame;
import javax.swing.JLabel;

public class VentanaEspera extends JInternalFrame {
	private JLabel enEspera, jugador;
	
	public VentanaEspera(String jugador) {
        initInternalFrame(jugador);
		
		this.setTitle("Bienvenido a la sala de espera");
		this.pack();
		this.setResizable(true);
		this.setLocation((ClienteBlackJack.WIDTH-this.getWidth())/2, 
				         (ClienteBlackJack.HEIGHT-this.getHeight())/2);
		this.show();
	}

	private void initInternalFrame(String idJugador) {
		// TODO Auto-generated method stub
		this.getContentPane().setLayout(new FlowLayout());
		
		jugador = new JLabel(idJugador);
		Font font = new Font(Font.DIALOG,Font.BOLD,15);
		jugador.setFont(font);
		jugador.setForeground(Color.BLUE);
		add(jugador);
		enEspera = new JLabel();
		enEspera.setText("debes esperar al otro jugador...");
		enEspera.setFont(font);
		add(enEspera);
	}
	
	public void cerrarSalaEspera() {
		this.dispose();
	}

}
