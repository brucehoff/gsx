package gsxconfig;

public interface Progress {
	/**
	 * Called by the process to inform the UI of the level of progress
	 * @param d a value from 0 to 1
	 */
	public void set(double d);
	
	/**
	 * Called by the process to see if the UI is requesting it cancel
	 * @return true if the UI is requesting
	 */
	public boolean isCancelled();
}
