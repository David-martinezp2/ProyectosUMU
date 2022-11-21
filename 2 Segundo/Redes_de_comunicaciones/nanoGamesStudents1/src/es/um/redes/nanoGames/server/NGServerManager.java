package es.um.redes.nanoGames.server;

import java.util.HashMap;
import java.util.LinkedList;

import es.um.redes.nanoGames.server.roomManager.NGBlackJackManager;
import es.um.redes.nanoGames.server.roomManager.NGMathManager;
import es.um.redes.nanoGames.server.roomManager.NGRoomManager;

/**
 * This class contains the general status of the whole server (without the logic related to particular games)
 */
class NGServerManager {
	
	//Players registered in this server
	private HashMap<String, NGPlayerInfo> players;
	//Current rooms and their related RoomManagers
	// Data structure to relate rooms and RoomManagers
	private HashMap<String, LinkedList<NGRoomManager>> rooms;
	
	public NGServerManager() {
		players = new HashMap<String, NGPlayerInfo>();
		rooms = new HashMap<String, LinkedList<NGRoomManager>>();
		rooms.put("MathChallenge", null);
		rooms.put("BlackJack", null);
	}
	
	/*
	public void registerRoomManager(NGRoomManager rm) {
		//When a new room manager is registered we assigned it to a room
	}
	*/
	
	//Returns the set of existing rooms
	public synchronized HashMap<String, LinkedList<NGRoomManager>> getRoomList() {
		HashMap<String, LinkedList<NGRoomManager>> copia = new HashMap<String, LinkedList<NGRoomManager>>(rooms);
		return copia;
	}
	
	//Given a room it returns the description
	public synchronized String getRoomDescription(String room) {
		//We make use of the RoomManager to obtain an updated description of the room
		if (rooms.get(room) == null){
			return rooms.get(room).getFirst().getDescription();
		} else {
			return null;
		}
	}
	
	//False is returned if the nickname is already registered, True otherwise and the player is registered
	public synchronized boolean addPlayer(NGPlayerInfo player) {
		if (players.containsKey(player.getNick())){
			return false;
		} else {
			players.put(player.getNick(), player);
			return true;
		}
	}
	
	//The player is removed from the list
	public synchronized void removePlayer(NGPlayerInfo player) {
		players.remove(player.getNick());
	}
	
	//A player request to enter in a room. If the access is granted the RoomManager is returned
	public synchronized NGRoomManager enterRoom(NGPlayerInfo p, String room) {
		if (rooms.get(room) != null){
			for (NGRoomManager sala : rooms.get(room)) {
				if (sala.available()){
					if (sala.registerPlayer(p)) {
						return sala;
					} else {
						NGRoomManager nueva = null;
						if (room.equals("MathChallenge")){
							nueva = new NGMathManager();
						} else {
							nueva = new NGBlackJackManager();
						}
						rooms.get(room).addLast(nueva);
						if (rooms.get(room).getLast().registerPlayer(p)) {
							return rooms.get(room).getLast();
						} else {
							return null;
						}
					}
				}
			}
		}
		NGRoomManager nueva = null;
		if (room.equals("MathChallenge")){
			nueva = new NGMathManager();
		} else {
			nueva = new NGBlackJackManager();
		}
		LinkedList<NGRoomManager> lista = new LinkedList<>();
		rooms.replace(room, lista);
		rooms.get(room).add(nueva);
		if (rooms.get(room).getLast().registerPlayer(p)) {
			return rooms.get(room).getLast();
		} else {
			return null;
		}
	}
	
	//A player leaves the room 
	public synchronized void leaveRoom(NGPlayerInfo p, String room) {
		if (rooms.get(room) != null){
			for (NGRoomManager sala : rooms.get(room)){
				if (sala.contains(p)){
					sala.removePlayer(p);
					rooms.get(room).remove(sala);
					if (rooms.get(room).isEmpty()){
						rooms.replace(room, null);
					}
					return;
				}
			}
		}
	}
}
