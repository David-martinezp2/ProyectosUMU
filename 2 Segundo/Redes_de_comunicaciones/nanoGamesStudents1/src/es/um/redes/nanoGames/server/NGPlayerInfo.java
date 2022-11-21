package es.um.redes.nanoGames.server;

public class NGPlayerInfo {
	
	//TODO Include additional fields if required
	public String nick; //Nickname of the user
	public int id;
	public byte status; //Current status of the user (according to the automata)
	public int score;  //Current score of the user
	public boolean jugando;
	
	//Constructor to make copies
	public NGPlayerInfo(NGPlayerInfo p) {
		this.nick = new String(p.nick);
		this.status = p.status;
		this.score = p.score;
		this.jugando = false;
		this.id = -1;
	}
	
	//Default constructor
	public NGPlayerInfo(String nick) {
		this.nick = nick;
		this.score = 0;
		// FALTA HACER STATUS
	}
	
	public String getNick() {
		String copia = new String(this.nick);
		return copia;
	}
	
	public int getScore() {
		return score;
	}
	
	public void reset(){
		score = 0;
		jugando = false;
	}
	
	public boolean getJugando(){
		return jugando;
	}
	
	public void setJugando(boolean valor){
		jugando = valor;
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
}
