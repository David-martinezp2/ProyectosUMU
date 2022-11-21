package es.um.redes.nanoGames.server.roomManager;

public class NGRoomStatus {
	public short statusNumber;
	//TODO Change the status to represent accurately your game status
	public String status;
	
	//Status initialization
	NGRoomStatus() {
		statusNumber = 0;
		status = null;
	}

	public NGRoomStatus(short currentStatus, String message) {
		statusNumber = currentStatus;
		this.status = message;
	}
	
	public String getStatus(){
		return status;
	}
	
	public short getStatusNumber() {
		return statusNumber;
	}
	
}
