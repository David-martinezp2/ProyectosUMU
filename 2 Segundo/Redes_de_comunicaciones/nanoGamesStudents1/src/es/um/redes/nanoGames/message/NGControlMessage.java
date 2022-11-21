package es.um.redes.nanoGames.message;

import java.io.IOException;

public class NGControlMessage extends NGMessage{

	private String type;
	private final static char DELIMITER = ':';    //Define el delimitador
	private final char END_LINE = '\n';    //Define el carácter de fin de línea
	
	@Override
	public String toString() {
		//TODO Transform the internal representation into a byte array ready to be trasmitted
		StringBuffer sb = new StringBuffer();
		sb.append("type"+DELIMITER+type+END_LINE); //Construimos el campo type
		//sb.append(END_LINE);  //Marcamos el final del mensaje
		String message = sb.toString(); //Se obtiene el mensaje 
		return message;
	}
	
	
	//Constructor
	public NGControlMessage (String type){
		this.type = type;
	}
	
	
	public static NGControlMessage readFromReader(String type, String message) throws IOException {
		// Decode the message received from the Input Stream
		NGControlMessage resultado = new NGControlMessage(type);
		return resultado;
	}

		// Replace this method according to your specific message
	
	public String getType() {
		return type;
	}
	
}
