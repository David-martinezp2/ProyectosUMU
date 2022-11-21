package es.um.redes.nanoGames.client.application;

import java.io.IOException;
import java.net.UnknownHostException;

//import com.sun.corba.se.spi.ior.MakeImmutable;
//import com.sun.javafx.sg.prism.NGText;

import es.um.redes.nanoGames.broker.BrokerClient;
import es.um.redes.nanoGames.client.comm.NGGameClient;
import es.um.redes.nanoGames.client.shell.NGCommands;
import es.um.redes.nanoGames.client.shell.NGShell;
import es.um.redes.nanoGames.message.NGMessage;
import es.um.redes.nanoGames.message.NGTextMessage;

public class NGController {
	// Number of attempts to get a token
	private static final int MAX_NUMBER_OF_ATTEMPTS = 5;
	// The client for the broker
	private BrokerClient brokerClient;
	// The client for the game server
	private NGGameClient ngClient;
	// The shell for user commands from the standard input
	private NGShell shell;
	// Last command provided by the user
	private byte currentCommand;
	// Nickname of the user
	private String nickname;
	// Current room of the user (if any)
	private String room;
	// Current answer of the user (if any)
	private String answer;
	// Token obtained from the broker
	private long token = 0;
	// Server hosting the games
	private String serverHostname;
	private boolean yaRegistrado = false;
	private boolean jugando = false;
	private boolean fin = false;

	public NGController(String brokerHostname, String serverHostname) throws UnknownHostException {
		brokerClient = new BrokerClient(brokerHostname);
		shell = new NGShell();
		this.serverHostname = serverHostname;
		
	}

	public byte getCurrentCommand() {
		return this.currentCommand;
	}

	public void setCurrentCommand(byte command) {
		currentCommand = command;
	}

	public void setCurrentCommandArguments(String[] args) {
		// According to the command we register the related parameters
		// We also check if the command is valid in the current state
		switch (currentCommand) {
		case NGCommands.COM_NICK:
			nickname = args[0];
			break;
		case NGCommands.COM_ENTER:
			room = args[0];
			break;
		case NGCommands.COM_ANSWER:
			answer = args[0];
			break;
		default:
		}
	}

	// Process commands provided by the users when they are not in a room
	public void processCommand() throws IOException {
		switch (currentCommand) {
		case NGCommands.COM_TOKEN:
			getTokenAndDeliver();
			break;
		case NGCommands.COM_NICK:
			if (yaRegistrado){
				System.out.println("¡! Ya estás registrado con un nick");
			} else {
				registerNickName();
			}
			break;
		case NGCommands.COM_ROOMLIST:
			getAndShowRooms();
			break;
		case NGCommands.COM_ENTER:
			fin = false;
			enterTheGame();
			break;
		case NGCommands.COM_QUIT:
			ngClient.disconnect();
			brokerClient.close();
			break;
		default:
		}
	}

	private void getAndShowRooms() throws IOException {
		// We obtain the rooms from the server and we display them
		ngClient.getRooms();
	}

	private void registerNickName() {
		// We try to register the nick in the server (it will check for
		// duplicates)
		try {
			if (ngClient.registerNickname(nickname)){
				System.out.println("* Bienvenido al servidor de juegos, " + nickname);
				yaRegistrado = true;
			} else {
				System.out.println("¡! Ese nick ya está usado");
			}
		} catch (IOException e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void enterTheGame() throws IOException {
		// The users request to enter in the room
		if (ngClient.enterRoom(room)){
			System.out.println("* Se encuentra en la sala " + room);
			// If success, we change the state in order to accept new commands
			do {
				// We will only accept commands related to a room
				readGameCommandFromShell();
				processGameCommand();
			} while ((currentCommand != NGCommands.COM_EXIT) && (!fin));
		} else System.out.println("¡! No ha podido entrar en la sala");
	}

	private void processGameCommand() throws IOException {
		switch (currentCommand) {
		case NGCommands.COM_RULES:
			ngClient.getRules();
			break;
		case NGCommands.COM_STATUS:
			ngClient.getStatus();
			break;
		case NGCommands.COM_ANSWER:
			if (jugando) sendAnswer();
			break;
		case NGCommands.COM_SOCKET_IN:
			// In this case the user did not provide a command but an incoming
			// message was received from the server
			processGameMessage();
			break;
		case NGCommands.COM_EXIT:
			exitTheGame();
		}
	}

	private void exitTheGame() throws IOException {
		// We notify the server that the user is leaving the room
		ngClient.exitGame();
	}

	private void sendAnswer() throws IOException {
		// In case we have to send an answer we will wait for the response to
		// display it
		ngClient.sendAnswer(answer);
	}

	private void processGameMessage() {
		// This method processes the incoming message received when the shell
		// was waiting for a user command
		NGMessage mensaje = ngClient.serverMessages();
		
		switch(mensaje.getType()){
		case(NGMessage.OP_COMIENZA):
		{
			jugando = true;
			break;
		}
		case(NGMessage.OP_FIN):
		{
			fin = true;
			break;
		}
		default:
		{
			NGTextMessage message = (NGTextMessage) mensaje;
			System.out.println(message.getText());
			break;
		}
		}
	}

	// Method to obtain the token from the Broker
	private void getTokenAndDeliver() {
		// There will be a max number of attempts
		int attempts = MAX_NUMBER_OF_ATTEMPTS;
		while (token == 0 && attempts!=0) {
			// We try to obtain a token from the broker
			attempts--;
			token = brokerClient.getToken();
			// If we have a token then we will send it to the game serve
			if (token != 0) {
				try {
					// We initialize the game client to be used to connect with
					// the name server
					ngClient = new NGGameClient(serverHostname);
					// We send the token in order to verify it
					if (!ngClient.verifyToken(token)) {
						System.out.println("¡! The token is not valid.");
						token = 0;
					}
				} catch (IOException e) {
					System.out
							.println("* Check your connection, the game server is not available.");
					token = 0;
				}
			}
		}
	}

	public void readGameCommandFromShell() {
		// We ask for a new game command to the Shell (and parameters if any)
		shell.readGameCommand(ngClient);
		setCurrentCommand(shell.getCommand());
		setCurrentCommandArguments(shell.getCommandArguments());
	}

	public void readGeneralCommandFromShell() {
		// We ask for a general command to the Shell (and parameters if any)
		shell.readGeneralCommand();
		setCurrentCommand(shell.getCommand());
		setCurrentCommandArguments(shell.getCommandArguments());
	}

	public boolean sendToken() throws IOException {
		// We simulate that the Token is a command provided by the user in order
		// to reuse the existing code
		System.out.println("* Obtaining the token...");
		setCurrentCommand(NGCommands.COM_TOKEN);
		processCommand();
		if (token != 0) {
			System.out.println("* Token is " + token
					+ " and it was validated by the server.");
		}
		return (token != 0);
	}

	public boolean shouldQuit() {
		return currentCommand == NGCommands.COM_QUIT;
	}

}
