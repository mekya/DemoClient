package io.antmedia.demo_client;

public class Settings {
	public static Settings instance = new Settings();
	
	private String serverAddress = "10.2.41.185";
	

	public String getServerAddress() {
		return serverAddress;
	}

	public void setServerAddress(String serverAddress) {
		this.serverAddress = serverAddress;
	}

}
