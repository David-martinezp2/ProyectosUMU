package es.um.redes.nanoGames.server.roomManager;

import java.util.HashMap;

import es.um.redes.nanoGames.server.NGPlayerInfo;

public class NGMathManager extends NGRoomManager{
	
	
	
	private static final int NPLAYERS = 2;
	private static final int NUMEROPREGUNTAS = 3;
	private int nplayers;				// Numero de jugadores actual
	private NGPlayerInfo[] players;		// Jugadores del juego
	private String name;
	private NGRoomStatus status;
	private NGChallenge challenge;
	private NGRoomDescription description;
	private HashMap<NGPlayerInfo, Integer> puntuaciones;
	private int listo;
	private int resultado;
	private boolean primero;
	private int fin;
	private boolean eliminar;
	private boolean end;
	
	public synchronized boolean contains(NGPlayerInfo player){
		for (int i = 0; i < nplayers; i++){
			if (players[i].getNick().equals(player.getNick())){
				return true;
			}
		}
		return false;
	}
	
	public NGMathManager(){
		this.nplayers = 0;
		players = new NGPlayerInfo[NPLAYERS];
		rules = "[REGLAS] Se el primer jugador en resolver la operación matemática. Número de jugadores: " + NPLAYERS + ". Respuesta correcta: +1 pto\tPrimera respuesta correcta: +2 pto\n";
		gameTimeout = 40000;
		name = "MathChallenge";
		status = new NGRoomStatus((short) 0, "[ACTUALIZACIÓN DE SALA]\nSin jugadores");
		description = new NGRoomDescription("MathChallenge", "Juego matemático");
		puntuaciones = new HashMap<NGPlayerInfo, Integer>();
		listo = 0;
		challenge = new NGChallenge();
		fin = 0;
		end = false;
	}
	
	//the only requirement to add a player is that only MAX_PLAYERS are accepted
	public synchronized boolean registerPlayer(NGPlayerInfo p){
		if(this.nplayers < NPLAYERS){
			players[nplayers] = p;
			nplayers++;
			puntuaciones.put(p, 0);
			p.setJugando(true);
			String estado = "[ACTUALIZACIÓN DE SALA]\nNick\tScore\n";
			if (nplayers == NPLAYERS){
				//INICIAR JUEGO
				// Resetear puntuaciones
				for (NGPlayerInfo iter : puntuaciones.keySet()) {
					puntuaciones.replace(iter, 0);
				}
				estado = estado + "\n* COMIENZA UN NUEVO JUEGO *\n[PUNTUACIONES INICIALES]\nNick\tScore\n";
				for (NGPlayerInfo iter : puntuaciones.keySet()) {
					estado = estado + iter.getNick() + "\t" + puntuaciones.get(iter) + "\n";
				}
				status = new NGRoomStatus((short) (status.getStatusNumber() + 1), estado);
				listo = nplayers;
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
		if ((fin == NUMEROPREGUNTAS) && (listo == nplayers)){
			int mejorPuntuacion = -1;
			String ganador = "";
			listo = 0;
			String estado = "* FIN DEL JUEGO *\n[PUNTUACIONES FINALES]\nNick\tScore\n";
			for (NGPlayerInfo iter : puntuaciones.keySet()) {
				String nick = iter.getNick();
				int puntuacion = puntuaciones.get(iter);
				estado = estado + nick + "\t" + puntuacion + "\n";
				if (puntuacion > mejorPuntuacion){
					mejorPuntuacion = puntuacion;
					ganador = nick;
				}
			}
			estado = estado + "\nEl GANADOR es " + ganador;
			end = true;
			status = new NGRoomStatus((short) (status.getStatusNumber() + 1), estado);
		}
		return status;
	}
	//Check for a new challenge. We can make use of that checking in order to build a new one if the conditions are satisfied 
	public synchronized NGChallenge checkChallenge(NGPlayerInfo p){
		if ((nplayers == NPLAYERS) && (listo == nplayers) && (fin != NUMEROPREGUNTAS)){	// Se iniciará el juego cuando haya dos jugadores y si todos han contestado
			fin++;
			listo = 0;
			// Hacemos un nuevo challenge
			String aux = nuevaOperacion();
			challenge = new NGChallenge((short) (challenge.getChallengeNumber() + 1), aux, "Todos");
		} else {
			if ((end) && (fin == NUMEROPREGUNTAS)) eliminar = true;
		}
		return challenge;
	}
	//The player provided no answer and we process that situation
	public synchronized NGRoomStatus noAnswer(NGPlayerInfo p){
		
		String estado = "[ACTUALIZACIÓN DE SALA]\nNick\tScore\n";
		for (NGPlayerInfo iter : puntuaciones.keySet()) {
			estado = estado + iter.getNick() + "\t" + puntuaciones.get(iter) + "\n";
		}
		status = new NGRoomStatus((short) (status.getStatusNumber() + 1), estado);
		
		listo++;
		return status;
	}
	//The answer provided by the player has to be processed
	public synchronized NGRoomStatus answer(NGPlayerInfo p, String answer){
		NGRoomStatus estadoJugador;
		try {
			
			int a = Integer.parseInt(answer);
			
			if (a == resultado){
				if (primero){
					estadoJugador = new NGRoomStatus((short) -1, "[Respuesta del servidor]\nTu respuesta es correcta y eres el primero -> +2 puntos");
					primero = false;
					puntuaciones.replace(p, puntuaciones.get(p) + 2);
				} else {
					estadoJugador = new NGRoomStatus((short) -1, "[Respuesta del servidor]\nTu respuesta es correcta, pero no eres el primero -> +1 puntos");
					puntuaciones.replace(p, puntuaciones.get(p) + 1);
				}
				
				String estado = "[ACTUALIZACIÓN DE SALA]\nNick\tScore\n";
				for (NGPlayerInfo iter : puntuaciones.keySet()) {
					estado = estado + iter.getNick() + "\t" + puntuaciones.get(iter) + "\n";
				}
				status = new NGRoomStatus((short) (status.getStatusNumber() + 1), estado);
				
				listo++;
				return estadoJugador;
			} else {
				estadoJugador = new NGRoomStatus((short) -1, "[Respuesta del servidor]\nTu respuesta es incorrecta -> +0 puntos");
				
				String estado = "[ACTUALIZACIÓN DE SALA]\nNick\tScore\n";
				for (NGPlayerInfo iter : puntuaciones.keySet()) {
					estado = estado + iter.getNick() + "\t" + puntuaciones.get(iter) + "\n";
				}
				status = new NGRoomStatus((short) (status.getStatusNumber() + 1), estado);
				
				listo++;
				return estadoJugador;
			}
		} catch (NumberFormatException exception){
			estadoJugador = new NGRoomStatus((short) -1, "[Respuesta del servidor]\nTu respuesta es incorrecta -> +0 puntos");
			
			String estado = "[ACTUALIZACIÓN DE SALA]\nNick\tScore\n";
			for (NGPlayerInfo iter : puntuaciones.keySet()) {
				estado = estado + iter.getNick() + "\t" + puntuaciones.get(iter) + "\n";
			}
			status = new NGRoomStatus((short) (status.getStatusNumber() + 1), estado);
			
			listo++;
			return estadoJugador;
		}
	}
	//The player is removed (maybe the status has to be updated)
	public synchronized void removePlayer(NGPlayerInfo p){
		status = new NGRoomStatus((short) (status.getStatusNumber() + 1), "El juego ha acabado");
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
	
	public synchronized String nuevaOperacion(){
		Integer n1 = (int) (Math.random()*100);
		Integer n2 = (int) (Math.random()*100);
		int op = (int) (Math.random()*3);
		primero = true;
		switch (op) {
		case 0:
			resultado = n1 + n2;
			return n1.toString() + "+" + n2.toString();
		case 1:
			n1 = n1 / 10;
			n2 = n2 / 10;
			resultado = n1 * n2;
			return n1.toString() + "*" + n2.toString();
		case 2:
			if (n1 >= n2){
				resultado = n1 - n2;
				return n1.toString() + "-" + n2.toString();
			} else {
				resultado = n2 - n1;
				return n2.toString() + "-" + n1.toString();
			}
		}
		return null;
	}

	public synchronized boolean available() {
		if (nplayers == NPLAYERS){
			return false;
		} else {
			return true;
		}
	}
	
	public synchronized boolean checkEliminar(){
		return eliminar;
	}
	
}
