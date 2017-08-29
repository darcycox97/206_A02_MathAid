package authoringaid;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import authoringaid.Creation.Components;

/**
 * Worker class to execute calls to ffmpeg on a thread other than EDT. Use of this class
 * should prevent the GUI freezing when the user is recording. Observer pattern is used to 
 * notify any listeners of when to update their GUI components.
 * @author Darcy Cox
 *
 */
public class CreationWorker extends SwingWorker<Integer, Void> {

	private Creation _creation;
	private ArrayList<CreationWorkerListener> _listeners;

	public CreationWorker(Creation creation) {
		_creation = creation;
		_listeners = new ArrayList<CreationWorkerListener>();
	}
	
	public void addCreationWorkerListener(CreationWorkerListener l) {
		_listeners.add(l);
	}

	protected Integer doInBackground() {
		
		publish(); // process method will disable recording button on EDT
		
		try {
			File audioPath = _creation.getFileName(Components.AUDIO);
			ProcessBuilder audio = new ProcessBuilder("bash","-c",
					"ffmpeg -f alsa -ac 2 -i default -t 3 " + audioPath.getPath());
			Process audioP = audio.start();
			int exitVal = audioP.waitFor();
			if (exitVal != 0) {
				for (CreationWorkerListener l : _listeners) {
					l.cleanUp();
				}
				return 1;
			}
			return 0;

		} catch (IOException | InterruptedException e) {
			for (CreationWorkerListener l : _listeners) {
				l.cleanUp();
			}
			return 1; 
		}
	}

	protected void done() {
		try {
			int exit = get();
			if (exit == 0) {
				for (CreationWorkerListener l : _listeners) {
					l.audioComponentCreated();
				}
				
			} else {
				for (CreationWorkerListener l : _listeners) {
					l.cleanUp();
				}
			}
		} catch (InterruptedException | ExecutionException e) {
			for (CreationWorkerListener l : _listeners) {
				l.cleanUp();
			}
		}
	}

	@Override
	protected void process(List<Void> chunks) {
		for (CreationWorkerListener l : _listeners) {
			l.recording();
		}
	}

}
