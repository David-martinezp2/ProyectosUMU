package es.um.redes.nanoGames.client.comm;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import es.um.redes.nanoGames.message.NGControlMessage;
import es.um.redes.nanoGames.message.NGGameMessage;
import es.um.redes.nanoGames.message.NGMessage;
import es.um.redes.nanoGames.message.NGNumberMessage;
import es.um.redes.nanoGames.message.NGTextMessage;

//This class provides the functionality required to exchange messages between the client and the game server 
public class NGGameClient {
	private Socket socket;
	protected DataOutputStream dos;
	protected DataInputStream dis;
	protected BufferedReader br;
	protected PrintWriter pw;
	
	private static final int SERVER_PORT = 6969;

	public NGGameClient(String serverName) throws UnknownHostException, IOException {
		//Creation of the socket and streams
		this.socket = new Socket(InetAddress.getByName(serverName), SERVER_PORT);
		this.dos = new DataOutputStream(socket.getOutputStream());
		this.dis = new DataInputStream(socket.getInputStream());
		br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		pw = new PrintWriter(socket.getOutputStream(), true);
	}

	public boolean verifyToken(long token) throws IOException {
		//SND(token) and RCV(TOKEN_VALID) or RCV(TOKEN_INVALID)
		NGNumberMessage message = (NGNumberMessage) NGMessage.makeNumberMessage(NGMessage.OP_SEND_TOKEN, token);
		dos.writeUTF(message.toString());
		NGControlMessage response = (NGControlMessage) NGMessage.readMessageFromSocket(dis);
		return (response.getType().equals(NGMessage.OP_TOKEN_VALID));
	}
	
	public boolean registerNickname(String nick) throws IOException {
		//SND(nick) and RCV(NICK_OK) or RCV(NICK_DUPLICATED)
		//NGTextMessage nick = (NGTextMessage) NGMessage.readMessageFromSocket(br);
		// Mensaje con el token
		NGTextMessage nickMessage = (NGTextMessage) NGMessage.makeTextMessage(NGMessage.OP_NICK, nick);
		dos.writeUTF(nickMessage.toString());
		NGControlMessage nickAnswer = (NGControlMessage) NGMessage.readMessageFromSocket(dis);
		if (nickAnswer.getType().equals(NGMessage.OP_NICK_OK)){
			return true;
		} else return false;
	}

	//add additional methods for all the messages to be exchanged between client and game server
	public void getRooms() throws IOException{
		while (isDataAvailable()){
			NGMessage.readMessageFromSocket(dis);
		}
		NGControlMessage roomlist = (NGControlMessage) NGMessage.makeControlMessage(NGMessage.OP_ROOMLIST);
		dos.writeUTF(roomlist.toString());
		try {
			NGGameMessage rooms = (NGGameMessage) NGMessage.readMessageFromSocket(dis);
			int contador = 65;
			System.out.println("* Estas son las salas:");
			for (String clave : rooms.getRooms().keySet()) {
				System.out.println((char)contador + "\t" + clave + "\t\tJugadores: " + rooms.getRooms().get(clave));
				contador++;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	//Used by the shell in order to check if there is data available to read 
	public boolean isDataAvailable() throws IOException {
		return (dis.available() != 0);
	}
	
	//To close the communication with the server
	public void disconnect() throws IOException {
		//TODO
		NGControlMessage quit = (NGControlMessage) NGMessage.makeControlMessage(NGMessage.OP_REMOVE_NICK);
		dos.writeUTF(quit.toString());
	}
	
	public boolean enterRoom(String room) throws IOException {
		while (isDataAvailable()){
			NGMessage.readMessageFromSocket(dis);
		}
		NGTextMessage roomRequest = (NGTextMessage) NGMessage.makeTextMessage(NGMessage.OP_ENTER_ROOM, room);
		dos.writeUTF(roomRequest.toString());
		NGControlMessage state = null;
		try {
			state = (NGControlMessage) NGMessage.readMessageFromSocket(dis);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (state.getType().equals(NGMessage.OP_INROOM)){
			return true;
		} else return false;
	}
	
	public void exitGame() throws IOException{
		NGControlMessage exit = (NGControlMessage) NGMessage.makeControlMessage(NGMessage.OP_EXIT);
		dos.writeUTF(exit.toString());
	}
	
	public NGMessage serverMessages(){
		try {
			NGMessage mensaje = NGMessage.readMessageFromSocket(dis);
			return mensaje;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public void getRules() throws IOException{
		NGControlMessage control = (NGControlMessage) NGMessage.makeControlMessage(NGMessage.OP_RULES);
		dos.writeUTF(control.toString());
	}
	
	public void getStatus() throws IOException{
		NGControlMessage control = (NGControlMessage) NGMessage.makeControlMessage(NGMessage.OP_GET_STATUS);
		dos.writeUTF(control.toString());
	}
	
	public void sendAnswer(String respuesta) throws IOException{
		NGTextMessage answer = (NGTextMessage) NGMessage.makeTextMessage(NGMessage.OP_RESPUESTA, respuesta);
		dos.writeUTF(answer.toString());
	}
}
