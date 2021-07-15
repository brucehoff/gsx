package gsxconfig;

public interface Callback {
	/**
	 * 
	 * @param message the message returned by Executable.execute()
	 */
	public void call(String message);
}
