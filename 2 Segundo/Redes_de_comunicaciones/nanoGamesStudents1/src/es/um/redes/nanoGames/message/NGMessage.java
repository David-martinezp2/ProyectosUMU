package es.um.redes.nanoGames.message;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;

public abstract class NGMessage {
	protected static String type;

	private final static char DELIMITER = ':';    //Define el delimitador
	
	public static final String OP_INVALID_CODE = "invalid_code";
	public static final String OP_SEND_TOKEN = "send_token";
	public static final String OP_TOKEN_VALID = "token_valid";
	public static final String OP_TOKEN_INVALID = "token_invalid";
	public static final String OP_NICK = "nick";
	public static final String OP_NICK_OK = "nick_ok";
	public static final String OP_NICK_DUPLICATED = "nick_duplicated";
	public static final String OP_REMOVE_NICK = "remove_nick";
	public static final String OP_ROOMLIST = "roomlist";
	public static final String OP_ENTER_ROOM = "enter_room";
	public static final String OP_ROOMS = "rooms";
	public static final String OP_INROOM = "inroom";
	public static final String OP_OUTROOM = "outroom";
	public static final String OP_EXIT = "exit";
	public static final String OP_RESPUESTA = "respuesta";
	public static final String OP_TIMEOUT = "timeout";
	public static final String OP_PREGUNTA = "pregunta";
	public static final String OP_RULES = "rules";
	public static final String OP_GET_STATUS = "get_status";
	public static final String OP_STATUS = "status";
	public static final String OP_REGLAS = "reglas";
	public static final String OP_CONTESTA = "contesta";
	public static final String OP_COMIENZA = "comienza";
	public static final String OP_FIN = "fin";
	
	//Returns the opcode of the message
	public String getType() {
		return type;
	}

	//Method to be implemented specifically by each subclass of NGMessage
	public abstract String toString();
	
	// public static NGMessage readMessageFromSocket(BufferedReader br) throws IOException { 
	//Reads the opcode of the incoming message and uses the subclass to parse the rest of the message
	public static NGMessage readMessageFromSocket(DataInputStream dis) throws IOException { 
		//We use the operation to differentiate among all the subclasses
		
		String message = dis.readUTF();
		String[] lineas = message.split("\n");
		
		String line = lineas[0];
		String field = "";
		if (!line.equals("")) {  //Si no hemos llegado al fin del mensaje seguimos
			int idx = line.indexOf(DELIMITER); //Posición del delimitador
			field = line.substring(0, idx).toLowerCase(); //a minúsculas
			type = line.substring(idx+1).trim(); //trim() borra espacios
		}
		assert(field.equals("type"));	// Asi o con equals?
		
		switch (type) {
		//TODO additional messages
		case (OP_SEND_TOKEN):		// Formato Número
		{
			return NGNumberMessage.readFromReader(type, message);
		}
		case (OP_NICK): 		// Formato Nick
		case (OP_RESPUESTA):
		case (OP_PREGUNTA):
		case (OP_REGLAS):
		case (OP_STATUS):
		case (OP_TIMEOUT):
		case (OP_ENTER_ROOM):
		case (OP_CONTESTA):
		{
			return NGTextMessage.readFromReader(type, message);
		}
		case (OP_ROOMLIST):		// Formato Control
		case (OP_EXIT):
		case (OP_TOKEN_VALID):
		case (OP_NICK_OK):
		case (OP_NICK_DUPLICATED):
		case (OP_REMOVE_NICK):
		case (OP_INROOM):
		case (OP_OUTROOM):
		case (OP_RULES):
		case (OP_GET_STATUS):
		case (OP_COMIENZA):
		case (OP_FIN):
		{
			return NGControlMessage.readFromReader(type, message);
		}
		case (OP_ROOMS):
		{
			return NGGameMessage.readFromReader(type, message);
		}
		default:
			System.err.println("Unknown message type received: "+OP_INVALID_CODE);
		}
		return null;
	}

	//The following method is just an example
	public static NGMessage makeNumberMessage(String type, long number) {
		return (new NGNumberMessage(type, number));
	}
	public static NGMessage makeTextMessage(String type, String texto) {
		return (new NGTextMessage(type, texto));
	}
	public static NGMessage makegGameMessage(String type, HashMap<String, String> rooms) {
		return (new NGGameMessage(type, rooms));
	}
	public static NGMessage makeControlMessage(String type) {
		return (new NGControlMessage(type));
	}
}
