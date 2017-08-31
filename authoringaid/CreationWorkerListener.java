package authoringaid;

/**
 * Defines the methods that CreationWorker can invoke on its listeners.
 */
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
	 * @param creation the creation being generated
	 * @param msg The message to display on the dialog.
	 */
	public void cleanUp(Creation creation, String msg);
}
