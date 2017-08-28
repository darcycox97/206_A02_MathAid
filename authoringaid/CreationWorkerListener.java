package authoringaid;

public interface CreationWorkerListener {

	/**
	 * Method that all creation worker listeners must implement.
	 * Defines actions to be taken in the GUI when a recording has completed.
	 * @param c the creation that is being generated
	 */
	public void audioComponentCreated(Creation c);
}
