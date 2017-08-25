package authoringaid;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.SwingWorker;

/**
 * Worker class to create a certain component of a given creation in a separate thread. 
 * The component to create is specified by the integer codes VIDEO, AUDIO, COMBINED.
 * @author Darcy Cox
 *
 */
public class CreationManager extends SwingWorker<Integer, Integer> {
	// integer codes for sp
	public static int VIDEO = 5;
	public static int AUDIO = 7;
	public static int COMBINED = 9;

	private int _compToCreate;
	private Creation _creation;
	private JComponent _dialogParent;
	private DefaultListModel<String> _creationsList;

	/**
	 * Creates a new CreationManager object to create the specified
	 * creation component. (VIDEO, AUDIO, or COMBINED). Adds the 
	 * 
	 * @param process The component this instance is to create (VIDEO, AUDIO, or COMBINED)
	 * @param c The creation to create the component for
	 */
	public CreationManager(int compToCreate, Creation c, JComponent dialogParent) {
		if (compToCreate != VIDEO || compToCreate != AUDIO || compToCreate != COMBINED) {
			throw new RuntimeException("first argument must be either VIDEO, AUDIO, or COMBINED");
		} else if (c == null) {
			throw new RuntimeException("second argument cannot be null");
		}
		_compToCreate = compToCreate;
		_creation = c;
		_dialogParent = dialogParent;
	}

	protected Integer doInBackground() {
		File path;
		if (_compToCreate == VIDEO) {

			try {
				path = _creation.getFileName(Creation.VIDEO);

				ProcessBuilder vid = new ProcessBuilder("bash","-c",
						"ffmpeg -y -f lavfi -i color=c=blue -vf \"drawtext=fontfile=:fontsize=30:fontcolor=white:" +
								"x=(w-text_w)/2:y=(h-text_h)/2:text='" + _creation + "'\" -t 3 " + path.getPath());

				Process vidP = vid.start();

				int exitVal =  vidP.waitFor();
				return exitVal;
			} catch (IOException | InterruptedException e) {

			}



		} else if (_compToCreate == AUDIO) {

		} else {
			// default to COMBINED

		}



		return null;
	}

	protected void done() {
		try {
			int exitVal = get();
		} catch (InterruptedException | ExecutionException e) {
			
		}

	}

}
