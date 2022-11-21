package es.um.redes.nanoGames.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.Timer;
import java.util.TimerTask;

import es.um.redes.nanoGames.broker.BrokerClient;
import es.um.redes.nanoGames.message.NGControlMessage;
import es.um.redes.nanoGames.message.NGGameMessage;
import es.um.redes.nanoGames.message.NGMessage;
import es.um.redes.nanoGames.message.NGNumberMessage;
import es.um.redes.nanoGames.message.NGTextMessage;
import es.um.redes.nanoGames.server.roomManager.NGChallenge;
import es.um.redes.nanoGames.server.roomManager.NGRoomManager;
import es.um.redes.nanoGames.server.roomManager.NGRoomStatus;

/**
 * A new thread runs for each connected client
 */
public class NGServerThread extends Thread {
	//Time difference between the token provided by the client and the one obtained from the broker directly
	private static final long TOKEN_THRESHOLD = 1500; //15 seconds
	//Socket to exchange messages with the client
	private Socket socket = null;
	//Global and shared manager between the threads
	private NGServerManager serverManager = null;
	//Input and Output Streams
	private DataInputStream dis;
	private DataOutputStream dos;
	//Buffer Reader
	//Utility class to communicate with the Broker
	BrokerClient brokerClient;
	//Current player
	NGPlayerInfo player;
	//Current RoomManager (it depends on the room the user enters)
	NGRoomManager roomManager;
	private long token;
	private short statusNumber;
	private short challengeNumber;
	private AtomicBoolean timeout_triggered = new AtomicBoolean();
	private boolean primerChallenge;
	
	public NGServerThread(NGServerManager manager, Socket socket, String brokerHostname) throws UnknownHostException {
		//Initialization of the thread
		this.serverManager = manager;
		brokerClient = new BrokerClient(brokerHostname);
		this.socket = socket;
	}

	//Main loop
	public void run() {
		try {
			//We obtain the streams from the socket
			dis = new DataInputStream(socket.getInputStream());
			dos = new DataOutputStream(socket.getOutputStream());
			
			//The first step is to receive and to verify the token
			receiveAndVerifyToken();
			//The second step is to receive and to verify the nick name
			receiveAndVerifyNickname();
			//While the connection is alive...
			while (true) {
				// Rest of messages according to the automata
				roomManager = null;
				NGMessage command = NGMessage.readMessageFromSocket(dis);
				
				switch(command.getType()){
					case(NGMessage.OP_ROOMLIST):
					{
						sendRoomList();
						break;
					}
					case(NGMessage.OP_REMOVE_NICK):
					{
						serverManager.removePlayer(this.player);
						break;
					}
					case(NGMessage.OP_ENTER_ROOM):
					{
						primerChallenge = true;
						NGTextMessage roomRequest = (NGTextMessage) command;
						String sala = "";
						switch (roomRequest.getText().toUpperCase()) {
							case "A":
							{
								sala = "MathChallenge";
								break;
							}
							case "B":
							{
								sala = "BlackJack";
								break;
							}
						}
						if (enterRoom(sala)){
							NGControlMessage inroom = (NGControlMessage) NGMessage.makeControlMessage(NGMessage.OP_INROOM);
							dos.writeUTF(inroom.toString());
							
							processRoomMessages();
						} else {
							NGControlMessage outroom = (NGControlMessage) NGMessage.makeControlMessage(NGMessage.OP_OUTROOM);
							dos.writeUTF(outroom.toString());
						}
						break;
					}
				}
			}
		} catch (Exception e) {
			//If an error occurs with the communications the user is removed from all the managers and the connection is closed
			if (roomManager != null){
				serverManager.leaveRoom(player, roomManager.getRegistrationName());
				serverManager.removePlayer(player);
				roomManager = null;
			}
		}
		// Close the socket
		try {
			socket.close();
		} catch (IOException e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
	}

	//Receive and verify Token
	private void receiveAndVerifyToken() throws IOException {
		long tokenClient;
		boolean tokenVerified = false;
		while (!tokenVerified) {
				this.token = brokerClient.getToken();
				NGNumberMessage recepcion = (NGNumberMessage) NGMessage.readMessageFromSocket(dis);
				tokenClient = recepcion.getToken();
				if ((token - tokenClient) < TOKEN_THRESHOLD){
					tokenVerified = true;		
				} else {
					tokenVerified = false;
				}
				//We extract the token from the message
				//now we obtain a new token from the broker
				//We check the token and send an answer to the client
				NGControlMessage verificacion;
				if (tokenVerified){
					verificacion = (NGControlMessage) NGMessage.makeControlMessage(NGMessage.OP_TOKEN_VALID);
				} else {
					verificacion = (NGControlMessage) NGMessage.makeControlMessage(NGMessage.OP_TOKEN_INVALID);
				}
				dos.writeUTF(verificacion.toString());
		}
	}

	//We obtain the nick and we request t//First we send the rules and the initial statushe server manager to verify if it is duplicated
	private void receiveAndVerifyNickname() throws IOException {
		boolean nickVerified = false;
		//this loop runs until the nick provided is not duplicated
		while (!nickVerified) {
			//We obtain the nick from the message
			NGTextMessage nickMessage = (NGTextMessage) NGMessage.readMessageFromSocket(dis);
			this.player = new NGPlayerInfo(nickMessage.getText());
			//we try to add the player in the server manager
			if (serverManager.addPlayer(this.player)){
				nickVerified = true;
			}
			//if success we send to the client the NICK_OK message
			//otherwise we send DUPLICATED_NICK
			NGControlMessage nickControl;
			if (nickVerified){
				nickControl = (NGControlMessage) NGMessage.makeControlMessage(NGMessage.OP_NICK_OK);
			} else {
				nickControl = (NGControlMessage) NGMessage.makeControlMessage(NGMessage.OP_NICK_DUPLICATED);
			}
			dos.writeUTF(nickControl.toString());
		}
	}

	//We send to the client the room list
	private void sendRoomList() throws IOException {
		//The room list is obtained from the server manager
		//Then we build all the required data to send the message to the client
		HashMap<String, String> salas = new HashMap<>();
		NGRoomManager sala = null;
		for (String clave : serverManager.getRoomList().keySet()) {
			if (serverManager.getRoomList().get(clave) != null){
				sala = serverManager.getRoomList().get(clave).getLast();
				if (!sala.available()){
					salas.put(clave, String.valueOf(0));
				} else {
					salas.put(clave, String.valueOf(sala.playersInRoom()));
				}
			} else {
				salas.put(clave, String.valueOf(0));
			}
		}
		NGGameMessage rooms = (NGGameMessage) NGMessage.makegGameMessage(NGMessage.OP_ROOMS, salas);
		dos.writeUTF(rooms.toString());
	}

	//Method to process messages received when the player is in the room
	//First we send the rules and the initial status
	private void processRoomMessages() throws IOException {
		statusNumber = 0;
		challengeNumber = 0;
		//First we send the rules and the initial status
		// Enviamos las reglas cada vez que entramos a una sala
				
				NGTextMessage reglas = (NGTextMessage) NGMessage.makeTextMessage(NGMessage.OP_REGLAS, roomManager.getRules());
				dos.writeUTF(reglas.toString());
				NGTextMessage status;
				
		//Now we check for incoming messages, status updates and new challenges
		boolean exit = false;
		while (!exit) {
			if (dis.available() != 0){
				NGMessage command = NGMessage.readMessageFromSocket(dis);
				switch(command.getType()){
				case (NGMessage.OP_EXIT):
				{
					serverManager.leaveRoom(player, roomManager.getRegistrationName());
					exit = true;
					break;
				}
				case (NGMessage.OP_RULES):
				{
					reglas = (NGTextMessage) NGMessage.makeTextMessage(NGMessage.OP_REGLAS, roomManager.getRules());
					dos.writeUTF(reglas.toString());
					break;
				}
				case (NGMessage.OP_GET_STATUS):
				{
					status = (NGTextMessage) NGMessage.makeTextMessage(NGMessage.OP_STATUS, roomManager.checkStatus(player).getStatus());
					dos.writeUTF(status.toString());
					break;
				}
				default: break;
				}
			}
			
			NGRoomStatus actual;
			short nuevoNumber;
			
			while (roomManager.esperar){
				actual = roomManager.checkStatus(player);
				nuevoNumber = actual.getStatusNumber();
				if (nuevoNumber > statusNumber){
					statusNumber = (short) nuevoNumber;
					status = (NGTextMessage) NGMessage.makeTextMessage(NGMessage.OP_STATUS, actual.getStatus());
					dos.writeUTF(status.toString());
				}
			}
			
			actual = roomManager.checkStatus(player);
			nuevoNumber = actual.getStatusNumber();
			if (nuevoNumber > statusNumber){
				statusNumber = (short) nuevoNumber;
				status = (NGTextMessage) NGMessage.makeTextMessage(NGMessage.OP_STATUS, actual.getStatus());
				dos.writeUTF(status.toString());
			}
			
			if (!player.getJugando()){
				exit = true;
				NGControlMessage salir = (NGControlMessage) NGMessage.makeControlMessage(NGMessage.OP_FIN);
				dos.writeUTF(salir.toString());
			}
			
			if (roomManager.checkEliminar()){
				serverManager.leaveRoom(player, roomManager.getRegistrationName());
				exit = true;
				NGControlMessage salir = (NGControlMessage) NGMessage.makeControlMessage(NGMessage.OP_FIN);
				dos.writeUTF(salir.toString());
			}
			
			NGChallenge challenge;
			challenge = roomManager.checkChallenge(player);
			if (challenge != null){
				short nuevoChallenge = challenge.getChallengeNumber();
				if (nuevoChallenge > challengeNumber){
					challengeNumber = (short) nuevoChallenge;
					String dirigidoA = challenge.getNick();
					String nickPlayer = player.getNick();
					if (dirigidoA.equals("Todos")){
						exit = processNewChallenge(challenge);
					} else if (dirigidoA.equals(nickPlayer)){
						roomManager.esperar = true;
						exit = processNewChallenge(challenge);
					}
				}
			}
			
		}
	}
	
	private boolean enterRoom(String room){
		player.reset();
		roomManager = serverManager.enterRoom(player, room);
		if (roomManager != null){
			return true;
		}
		return false;
	}

	// Private class to implement a very simple timer
	private class Timeout extends TimerTask {
		@Override
		public void run() {
			timeout_triggered.set(true);
		}
	}
	
	private boolean processNewChallenge(NGChallenge challenge) throws IOException {
		// We send the challenge to the client
		if (primerChallenge){
			primerChallenge = false;
			NGControlMessage inicio = (NGControlMessage) NGMessage.makeControlMessage(NGMessage.OP_COMIENZA);
			dos.writeUTF(inicio.toString());
		}
		NGTextMessage pregunta = (NGTextMessage) NGMessage.makeTextMessage(NGMessage.OP_PREGUNTA, challenge.getChallenge());
		dos.writeUTF(pregunta.toString());
		// Now we set the timeout
		Timer timer = null;
		timeout_triggered.set(false);
		timer = new Timer();
		timer.schedule(new Timeout(), roomManager.getTimeout(), roomManager.getTimeout());
		boolean answerProvided = false;
		// Loop until an answer is provided or the timeout expires
		while (!timeout_triggered.get() && !answerProvided) {
			if (dis.available() > 0) {
				// The client sent a message
				// Process the message
				NGMessage command = NGMessage.readMessageFromSocket(dis);
				
				switch(command.getType()){
					case(NGMessage.OP_EXIT):
					{
						answerProvided = true;
						serverManager.leaveRoom(player, roomManager.getRegistrationName());
						return true;
					}
					case(NGMessage.OP_RESPUESTA):
					{
						NGTextMessage respuesta = (NGTextMessage) command;
						answerProvided = true;
						timer.cancel();
						// IF ANSWER Then call roomManager.answer() and proceed
						roomManager.esperar = false;
						String estado = roomManager.answer(player, respuesta.getText()).getStatus();
						NGTextMessage enviar = (NGTextMessage) NGMessage.makeTextMessage(NGMessage.OP_CONTESTA, estado);
						dos.writeUTF(enviar.toString());
						break;
					}
				}
				
			} else
				try {
					// To avoid a CPU-consuming busy wait
					Thread.sleep(50);
				} catch (InterruptedException e) {
					// Ignore
				}
		}
		if (!answerProvided) {
			// The timeout expired
			timer.cancel();
			// call roomManager.noAnswer() and proceed
			roomManager.noAnswer(player);
			NGTextMessage timeout = (NGTextMessage) NGMessage.makeTextMessage(NGMessage.OP_TIMEOUT, "¡! Tiempo excedido");
			dos.writeUTF(timeout.toString());
		}
		return false;
	}
	
}
