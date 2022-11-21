package es.um.redes.nanoGames.server.roomManager;

public class NGChallenge {
	public short challengeNumber;
	//TODO Change the challenge to represent accurately your game challenge
	public String challenge;
	public String dirigidoA;
	
	//Status initialization
	NGChallenge() {
		challengeNumber = 0;
		challenge = null;
		dirigidoA = null;
	}

	public NGChallenge(short currentChallengeNumber, String currentChallenge, String dirigidoA) {
		this.challengeNumber = currentChallengeNumber;
		challenge = currentChallenge;
		this.dirigidoA = dirigidoA;
	}
	
	public short getChallengeNumber() {
		return challengeNumber;
	}
	
	public String getChallenge() {
		return challenge;
	}
	
	public String getNick(){
		return dirigidoA;
	}

}
