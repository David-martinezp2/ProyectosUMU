package es.um.redes.nanoGames.message;

import java.io.IOException;
import java.util.HashMap;


public class NGGameMessage extends NGMessage{

	private String type;
	private HashMap<String, String> rooms;
	private final static char DELIMITER = ':';    //Define el delimitador
	private final char END_LINE = '\n';    //Define el carácter de fin de línea
	
	@Override
	public String toString() {
		// Transform the internal representation into a byte array ready to be trasmitted
		StringBuffer sb = new StringBuffer();
		sb.append("type"+DELIMITER+type+END_LINE); //Construimos el campo type
		String field;
		String value;
		for (String clave : rooms.keySet()) {
			field = clave;    //Nombre del campo
			value = rooms.get(clave);	// Valor del campo
			sb.append(field+DELIMITER+value+END_LINE); //Construimos el campo
		}
		//sb.append(END_LINE);  //Marcamos el final del mensaje
		String message = sb.toString(); //Se obtiene el mensaje 
		return message;
	}
	
	
	//Constructor
	public NGGameMessage (String type, HashMap<String, String> rooms){
		this.type = type;
		this.rooms = new HashMap<>(rooms);
		
	}
	
	
	public static NGGameMessage readFromReader(String type, String message) throws IOException {
		// Decode the message received from the Input Stream
		HashMap<String, String> rooms = new HashMap<>();
		//Integer nPlayers = 0;
		String[] lineas = message.split("\n");
		//assert(lineas[lineas.length - 1].equals(""));
		for (int i = 1; i < lineas.length; i++){
			String line = lineas[i];
			int idx = line.indexOf(DELIMITER); //Posición del delimitador
			String field = line.substring(0, idx).toLowerCase(); //a minúsculas
			String value = line.substring(idx+1).trim(); //trim() borra espacios
			//nPlayers = Integer.parseInt(value);
			rooms.put(field, value);
		}
		
		NGGameMessage resultado = new NGGameMessage(type, rooms);
		return resultado;
	}

	public HashMap<String, String> getRooms() {
		HashMap<String, String> copia = new HashMap<>(rooms);
		return copia;
	}
	
	public String getType() {
		return type;
	}
	
}
