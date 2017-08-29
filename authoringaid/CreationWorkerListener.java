package authoringaid;

public interface CreationWorkerListener {

	/**
	 * Method that defines actions to be taken in the GUI when a recording has completed.
	 */
	public void audioComponentCreated();
	
	/**
	 * Method for the listener to specify what needs to happen during recording
	 */
	public void recording();
	
	/**
	 * Method for the listener to specify how to clean up partially generated creations.
	 * Should reset the gui to a state where it can safely continue after any error occurs.
	 */
	public void cleanUp();
}
