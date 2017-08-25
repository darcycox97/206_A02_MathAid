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
public class CreationWorker extends SwingWorker<Integer, Integer> {
	public enum CreationPart {
		VIDEO, AUDIO, COMBINED
	}

	private CreationPart _compToCreate;
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
	public CreationWorker(CreationPart compToCreate, Creation c, JComponent dialogParent) {
		_compToCreate = compToCreate;
		_creation = c;
		_dialogParent = dialogParent;
	}

	protected Integer doInBackground() {
		File path;
		if (_compToCreate == CreationPart.VIDEO) {

			try {
				path = _creation.getFileName(Creation.Components.VIDEO);

				ProcessBuilder vid = new ProcessBuilder("bash","-c",
						"ffmpeg -y -f lavfi -i color=c=blue -vf \"drawtext=fontfile=:fontsize=30:fontcolor=white:" +
								"x=(w-text_w)/2:y=(h-text_h)/2:text='" + _creation + "'\" -t 3 " + path.getPath());

				Process vidP = vid.start();

				int exitVal =  vidP.waitFor();
				return exitVal;
			} catch (IOException | InterruptedException e) {

			}



		} else if (_compToCreate == CreationPart.AUDIO) {

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
