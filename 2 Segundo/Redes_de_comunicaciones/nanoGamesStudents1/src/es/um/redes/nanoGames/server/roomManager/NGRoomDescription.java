package es.um.redes.nanoGames.server.roomManager;

public class NGRoomDescription {

	private final String name;
	private final String description;
	
	// Constructor para juego sin límite de jugadores
	public NGRoomDescription(String name, String description){
		this.name = name;
		this.description = description;
	}
	
	public String getDescription() {
		String copia = new String(this.description);
		return copia;
	}
	
	public String getName() {
		String copia = new String(this.name);
		return copia;
	}
	
}
