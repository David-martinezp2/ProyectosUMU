package es.um.redes.nanoGames.server.roomManager;

import java.util.LinkedList;


import es.um.redes.nanoGames.server.NGPlayerInfo;

public class NGBlackJackManager extends NGRoomManager{

	private static final int NPLAYERS = 2;
	private int nplayers;				// Numero de jugadores actual
	private NGPlayerInfo[] players;		// Jugadores del juego
	private String name;
	private NGRoomStatus status;
	private NGChallenge challenge;
	private NGRoomDescription description;
	private int jugadorActual;
	private Estado_Jugador_BJ[] estados_partida;
	private Integer[] puntos;
	private boolean nuevoChallenge;
	private String dirigidoA;
	private boolean eliminar;
	private boolean fin;
	
	public NGBlackJackManager(){
		this.nplayers = 0;
		players = new NGPlayerInfo[NPLAYERS];
		puntos = new Integer[NPLAYERS];
		estados_partida = new Estado_Jugador_BJ[NPLAYERS];
		rules = "[REGLAS] El jugador que consiga el número más próximo a 21, sin excederlo, gana. Número de jugadores: " + NPLAYERS + "\n";
		gameTimeout = 40000;
		name = "BlackJack";
		status = new NGRoomStatus((short) 0, "[ACTUALIZACIÓN DE SALA]\nSin jugadores");
		description = new NGRoomDescription("BlackJack", "Juego de cartas");
		challenge = new NGChallenge();
		jugadorActual = -1;
		fin = false;
		eliminar = false;
		nuevoChallenge = false;
	}
	
	//the only requirement to add a player is that only MAX_PLAYERS are accepted
	public synchronized boolean registerPlayer(NGPlayerInfo p){
		if(this.nplayers < NPLAYERS){
			players[nplayers] = p;
			puntos[nplayers] = -1;
			estados_partida[nplayers] = Estado_Jugador_BJ.JUGANDO;
			p.setJugando(true);
			nplayers++;
			if (nplayers == NPLAYERS){
				String estado = "";
				status = new NGRoomStatus((short) (status.getStatusNumber() + 1), estado + repartirCartas());
				nuevoChallenge = true;
			}
			return true;
		} else return false;
	}
	
	//Rules are returned
	public synchronized String getRules(){
		return rules;
	}
	
	//The current status is returned
	public synchronized NGRoomStatus checkStatus(NGPlayerInfo p){
		if (fin){
			
			LinkedList<Integer> mejoresJugadores = new LinkedList<>();
			int mejorNumero = -1;
			int i = 0;
			int ganador = -1;
			
			for (i = 0; i < nplayers; i++){
				if (puntos[i] == 21){
					ganador = i;
					mejoresJugadores = null;
					i = nplayers;
				} else if ((puntos[i] > mejorNumero) && (puntos[i] < 21)){
					mejorNumero = puntos[i];
					mejoresJugadores = new LinkedList<>();
					mejoresJugadores.add(i);
				} else if (puntos[i] == mejorNumero){
					mejoresJugadores.add(i);
				}
			}
			
			String estado = "";
			estado = estado + "* FIN DEL JUEGO *\n[PUNTOS FINALES]\nNick\tScore\n";
			
			if (ganador != -1){
				for (i = 0; i < nplayers; i++) {
					if (i == ganador){
						estado = estado + players[i].getNick() + "\t" + puntos[i] + "\tGANADOR\n";
					} else {
						estado = estado + players[i].getNick() + "\t" + puntos[i] + "\n";
					}
				}
			} else {
				for (i = 0; i < nplayers; i++){
					if (mejoresJugadores.contains(i)){
						estado = estado + players[i].getNick() + "\t" + puntos[i] + "\tGANADOR\n";
					} else {
						estado = estado + players[i].getNick() + "\t" + puntos[i] + "\n";
					}
				}
			}
			
			status = new NGRoomStatus((short) (status.getStatusNumber() + 1), estado);
			
			eliminar = true;
			
		}
		
		return status;
	}
	
	//Check for a new challenge. We can make use of that checking in order to build a new one if the conditions are satisfied
	public synchronized NGChallenge checkChallenge(NGPlayerInfo p){
		if (nuevoChallenge) {
			nuevoChallenge = false;
			String aux = nuevaCarta();
			challenge = new NGChallenge((short) (challenge.getChallengeNumber() + 1), aux, dirigidoA);
		}
		return challenge;
	}
	
	//The player provided no answer and we process that situation
	public synchronized NGRoomStatus noAnswer(NGPlayerInfo p){
		
		estados_partida[jugadorActual] = Estado_Jugador_BJ.PLANTADO;
		String estado = "Te has plantado\n";
		NGRoomStatus estadoJugador = new NGRoomStatus((short) (status.getStatusNumber() + 1), estado);
		fin = comprobarFin();
		if (!fin){
			estado = players[jugadorActual].getNick() + " se planta\n" + status.getStatus();	//BORRAR?
			status = new NGRoomStatus((short) (status.getStatusNumber() + 1), estado);	//BORRAR?
			nuevoChallenge = true;
		}
		
		return estadoJugador;
	}
	
	//The answer provided by the player has to be processed
	public synchronized NGRoomStatus answer(NGPlayerInfo p, String answer){
		
		NGRoomStatus estadoJugador = null;

		if (answer.equals("y")) {

			puntos[jugadorActual] = puntos[jugadorActual] + (int) (Math.random() * 9 + 1);
			String estado = "Tienes nueva carta\n";
			estadoJugador = new NGRoomStatus((short) (status.getStatusNumber() + 1), estado);

			if (puntos[jugadorActual] == 21) {
				estados_partida[jugadorActual] = Estado_Jugador_BJ.GANADOR;
				fin = true;
			} else if (puntos[jugadorActual] > 21) {
				estados_partida[jugadorActual] = Estado_Jugador_BJ.SOBREPASA;
				fin = comprobarFin();
			}

		} else {
			estados_partida[jugadorActual] = Estado_Jugador_BJ.PLANTADO;
			String estado = "Te has plantado\n";
			estadoJugador = new NGRoomStatus((short) (status.getStatusNumber() + 1), estado);
			fin = comprobarFin();
		}
		
		if (!fin) {
			String estado = players[jugadorActual].getNick() + " tiene nueva carta\n" + "[ACTUALIZACION DE PUNTOS]\n";
			for (int i = 0; i < nplayers; i++) {
				estado = estado + players[i].getNick() + "[" + puntos[i] + "]  ";
			}
			estado = estado + "\n";

			status = new NGRoomStatus((short) (status.getStatusNumber() + 1), estado);

			nuevoChallenge = true;
		}

		return estadoJugador;
		
	}
	
	//The player is removed (maybe the status has to be updated)
	public synchronized void removePlayer(NGPlayerInfo p){
		status = new NGRoomStatus((short) (status.getStatusNumber() + 1), "El jugador " + p.getNick() + " ha dejado la sala");
		for (int i = 0; i < nplayers; i++){
			players[i].setJugando(false);
		}
	}
	//Creates a copy of the room manager
	public synchronized NGRoomManager duplicate(){
		return null;
	}
	//Returns the name of the game
	public synchronized String getRegistrationName(){
		String copia = new String(name);
		return copia;
	}
	//Returns the description of the room
	public synchronized String getDescription(){
		return description.getDescription();
	}
	//Returns the current number of players in the room
	public synchronized int playersInRoom(){
		return nplayers;
	}
	
	public synchronized String nuevaCarta(){
		jugadorActual = (jugadorActual + 1) % nplayers;
		while(!estados_partida[jugadorActual].equals(Estado_Jugador_BJ.JUGANDO)){
			jugadorActual = (jugadorActual + 1) % nplayers;
		}
		dirigidoA = players[jugadorActual].getNick();
		String challenge = players[jugadorActual].getNick() + ", ¿desea nueva carta?\n";
		return challenge;
	}
	
	public synchronized String repartirCartas(){
		String estado = "";
		for (int i = 0; i < nplayers; i++){
			puntos[i] = (int) (Math.random()*9 + 1);
			estado = estado + players[i].getNick() + " [" + puntos[i] + "]  ";
		}
		return estado;
	}
	
	public synchronized boolean comprobarFin(){
		
		int njugando = 0; 
		int nplantados = 0;
		
		for (int i = 0; i < nplayers; i++){
			if (estados_partida[i].equals(Estado_Jugador_BJ.JUGANDO)){
				njugando++;
			} else if (estados_partida[i].equals(Estado_Jugador_BJ.PLANTADO)){
				nplantados++;
			}
		}
		
		if (njugando > 1){
			return false;
		} else if (njugando == 1){
			if (nplantados == 0) return true;
			else return false;
		} else {
			return true;
		}
		
	}

	public synchronized boolean available() {
		if (nplayers == NPLAYERS){
			return false;
		} else {
			return true;
		}
	}

	public synchronized boolean checkEliminar() {
		return eliminar;
	}
	
	public synchronized boolean contains(NGPlayerInfo player){
		for (int i = 0; i < nplayers; i++){
			if (players[i].getNick().equals(player.getNick())){
				return true;
			}
		}
		return false;
	}
	
}