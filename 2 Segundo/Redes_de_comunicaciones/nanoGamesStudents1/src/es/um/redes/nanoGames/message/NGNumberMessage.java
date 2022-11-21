package es.um.redes.nanoGames.message;

import java.io.IOException;

public class NGNumberMessage extends NGMessage {
	
	private long number;
	private String type;
	private final static char DELIMITER = ':';    //Define el delimitador
	private final char END_LINE = '\n';    //Define el carácter de fin de línea
	
	@Override
	public String toString() {
		// Transform the internal representation into a byte array ready to be trasmitted
		StringBuffer sb = new StringBuffer();
		sb.append("type"+DELIMITER+type+END_LINE); //Construimos el campo type
		String field1 = "token";    //Nombre del campo
		sb.append(field1+DELIMITER+number+END_LINE); //Construimos el campo
		//sb.append(END_LINE);  //Marcamos el final del mensaje
		String message = sb.toString(); //Se obtiene el mensaje 
		return message;
	}
	
	
	//Constructor
	public NGNumberMessage (String type, long number){
		this.type = type;
		this.number = number;
	}
	
	
	public static NGNumberMessage readFromReader(String type, String message) throws IOException {
		// Decode the message received from the Input Stream
		long number = 0;
		String[] lineas = message.split("\n");
		//assert(lineas[lineas.length - 1].equals(""));
		for (int i = 1; i < lineas.length; i++){
			String line = lineas[i];
			int idx = line.indexOf(DELIMITER); //Posición del delimitador
			String value = line.substring(idx+1).trim(); //trim() borra espacios
			number = Long.parseLong(value);
		}
		NGNumberMessage resultado = new NGNumberMessage(type, number);
		return resultado;
	}

	// Replace this method according to your specific message
	public long getToken() {
		return this.number;
	}

}
