package gsxconfig;

public interface ConfigureI {
	String getIP();
	boolean isConnected();
	void connect(String ip, Progress p);
	void disconnect();
	void retrieve();
	String getMac();
	Parameters getParameters();
	String configure(Progress progress, boolean credentialsOnly);
}
