package authoringaid;

import java.io.File;

import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import authoringaid.Creation.Components;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

/**
 * Class that defines what the gui does when the user selects an item in the list.
 */
public class SelectionPreview implements ListSelectionListener {

	EmbeddedMediaPlayer _player;

	public SelectionPreview(EmbeddedMediaPlayer p) {
		_player = p;
	}

	/**
	 * Gets the media player to show a preview of the creation
	 */
	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (e.getSource() instanceof JList<?>) {
			JList<?> list = (JList<?>)e.getSource();
			Object selected = list.getSelectedValue();
			if (selected instanceof Creation) {
				if (selected != null) {
					selected = (Creation)selected;
					File prev = ((Creation) selected).getFileName(Components.PREVIEW);
					_player.playMedia(prev.getPath(),"");
				}
			}
		}
	}

}
