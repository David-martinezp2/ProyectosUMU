package es.um.redes.nanoGames.message;

import java.io.IOException;

public class NGTextMessage extends NGMessage{
	
	private String type;
	private final String texto;
	private final static char DELIMITER = ':';    //Define el delimitador
	private final char END_LINE = '\n';    //Define el carácter de fin de línea
	
	@Override
	public String toString() {
		// Transform the internal representation into a byte array ready to be trasmitted
		StringBuffer sb = new StringBuffer();
		sb.append("type"+DELIMITER+type+END_LINE); //Construimos el campo type
		String field1 = "nick";    //Nombre del campo
		sb.append(field1+DELIMITER+texto+END_LINE); //Construimos el campo
		//sb.append(END_LINE);  //Marcamos el final del mensaje
		String message = sb.toString(); //Se obtiene el mensaje 
		return message;
	}
	
	
	//Constructor
	public NGTextMessage (String type, String texto){
		this.type = type;
		this.texto = texto;
	}
	
	
	public static NGTextMessage readFromReader(String type, String message) throws IOException {
		// Decode the message received from the Input Stream
		String value = "";
		String[] lineas = message.split("\n");
		//assert(lineas[lineas.length - 1].equals(""));
		for (int i = 1; i < lineas.length; i++){
			String line = lineas[i];
			int idx = line.indexOf(DELIMITER); //Posición del delimitador
			if (i == 1){
				value = line.substring(idx+1).trim(); //trim() borra espacios
			} else {
				value = value + "\n" + line.substring(idx+1).trim(); //trim() borra espacios
			}
		}
		NGTextMessage resultado = new NGTextMessage(type, value);
		return resultado;
	}

	public String getText() {
		return this.texto;
	}
	
	public String getType() {
		return type;
	}

}
