package authoringaid;

import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;

import authoringaid.Creation.Components;
import authoringaid.CreationWorker.WorkerUse;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;

import java.io.File;
import java.io.IOException;

@SuppressWarnings("serial")
public class MathsAid extends JFrame {
	//TODO: Create constants to refer to component sizes/display text
	//TODO: display initial creations in alphabetical order?

	private static final String CREATE_MODE_BUTTON = "Enter Create Mode";
	private static final String QUIT_BUTTON = "Exit Create Mode";
	private static final String PLAY_BUTTON = "Play";
	private static final String DELETE_BUTTON = "Delete";
	private static final String RECORD_BUTTON = "Record";
	private static final String CANCEL_BUTTON = "Cancel";
	private static final String CREATION_TEXT_FIELD = "Enter name of creation";
	private static final String LABEL_EXISTING = "Existing Creations";
	private static final String LABEL_CREATE_WELCOME = "Welcome to create mode!";
	private static final String LABEL_CREATE_INFO = "Please enter the name of your creation below";
	private static final String CREATE_BUTTON = "Create";

	// string identifiers for each view used by CardLayout.
	private static final String VIDEO_VIEW_ID = "Video View";
	private static final String CREATE_MODE_ID = "Create Mode";

	// menu components
	private JPanel _pnlMenu;
	private JButton _btnPlay;
	private JButton _btnDelete;
	private JButton _btnCreateMode;

	// components for display of existing creations
	private JPanel _pnlDisplayExisting;
	private JLabel _lblExisting;
	private JScrollPane _scrExisting;
	private JList<Creation> _listExisting;
	private DefaultListModel<Creation> _existingCrtns;

	// panel for the main area (uses card layout to toggle between views)
	private JPanel _pnlMain;

	// components for create mode view
	private JPanel _pnlCreateMode;
	private JTextField _txtCrtnName;
	private JButton _btnCreate;
	private JLabel _lblCreateWelcome;
	private JLabel _lblCreateInfo;
	private JPanel _pnlCreateModeInput;
	private JPanel _pnlCreateBtnWrapper;
	private JLabel _lblCreateModeStatus;

	// components for play/delete view
	private JPanel _pnlVideoView;
	private EmbeddedMediaPlayerComponent _video;
	private final EmbeddedMediaPlayer _player;

	public MathsAid() {
		super("Maths Authoring Aid");
		setSize(700,600);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				_video.release();
				System.exit(0);
			}
		});

		/***************** GUI Layout Setup **************************/

		// set up menu panel
		_pnlMenu = new JPanel();
		_btnCreateMode = new JButton(CREATE_MODE_BUTTON);
		_btnDelete = new JButton(DELETE_BUTTON);
		_btnPlay = new JButton(PLAY_BUTTON);
		_pnlMenu.add(_btnPlay);
		_pnlMenu.add(_btnDelete);
		_pnlMenu.add(_btnCreateMode);
		add(_pnlMenu,BorderLayout.NORTH);

		// set up existing creations panel
		_lblExisting = new JLabel(LABEL_EXISTING, SwingConstants.CENTER);
		_lblExisting.setAlignmentX(CENTER_ALIGNMENT);
		_lblExisting.setFont(_lblExisting.getFont().deriveFont((float)14));
		_lblExisting.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));

		initializeCreationsList();
		_listExisting = new JList<Creation>(_existingCrtns);
		_listExisting.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		_listExisting.setFont(_listExisting.getFont().deriveFont((float)14));

		_scrExisting = new JScrollPane(_listExisting);

		_pnlDisplayExisting = new JPanel();
		_pnlDisplayExisting.setPreferredSize(new Dimension(180,500));
		_pnlDisplayExisting.setLayout(new BoxLayout(_pnlDisplayExisting, BoxLayout.Y_AXIS));
		_pnlDisplayExisting.add(_lblExisting);
		_pnlDisplayExisting.add(_scrExisting);
		add(_pnlDisplayExisting, BorderLayout.WEST);

		// set up video view
		_pnlVideoView = new JPanel(new BorderLayout());
		_video = new EmbeddedMediaPlayerComponent();
		_player = _video.getMediaPlayer();
		_pnlVideoView.add(_video, BorderLayout.CENTER);

		// set up create mode view
		_lblCreateWelcome = new JLabel(LABEL_CREATE_WELCOME, SwingConstants.CENTER);
		_lblCreateWelcome.setBorder(BorderFactory.createEmptyBorder(20, 0, 50, 0));
		_lblCreateWelcome.setFont(_lblCreateWelcome.getFont().deriveFont((float)20));

		_lblCreateInfo = new JLabel(LABEL_CREATE_INFO, SwingConstants.CENTER);
		_lblCreateInfo.setAlignmentX(CENTER_ALIGNMENT);
		_lblCreateInfo.setFont(_lblCreateInfo.getFont().deriveFont((float)15));
		_lblCreateInfo.setBorder(BorderFactory.createEmptyBorder(0,0,15,0));

		_txtCrtnName = new JTextField();
		_txtCrtnName.setHorizontalAlignment(SwingConstants.CENTER);
		_txtCrtnName.setFont(_txtCrtnName.getFont().deriveFont((float)15));
		_txtCrtnName.setAlignmentX(CENTER_ALIGNMENT);
		_txtCrtnName.setMaximumSize(new Dimension(400,25));

		_btnCreate = new JButton(CREATE_BUTTON);
		_btnCreate.setAlignmentX(CENTER_ALIGNMENT);
		_btnCreate.setFont(_btnCreate.getFont().deriveFont((float)16));
		_pnlCreateBtnWrapper = new JPanel();
		_pnlCreateBtnWrapper.setAlignmentX(CENTER_ALIGNMENT);
		_pnlCreateBtnWrapper.setBorder(BorderFactory.createEmptyBorder(25,0,0,0));
		_pnlCreateBtnWrapper.add(_btnCreate);

		_pnlCreateModeInput = new JPanel();
		_pnlCreateModeInput.setLayout(new BoxLayout(_pnlCreateModeInput,BoxLayout.Y_AXIS));
		_pnlCreateModeInput.add(_lblCreateInfo);
		_pnlCreateModeInput.add(_txtCrtnName);
		_pnlCreateModeInput.add(_pnlCreateBtnWrapper);
		_pnlCreateModeInput.setBorder(BorderFactory.createEmptyBorder(50,70,80,70));

		_lblCreateModeStatus = new JLabel("Initial Text");
		_lblCreateModeStatus.setFont(_lblCreateModeStatus.getFont().deriveFont((float)16));
		_lblCreateModeStatus.setHorizontalAlignment(SwingConstants.CENTER);
		_lblCreateModeStatus.setPreferredSize(new Dimension(1000,150));
		_lblCreateModeStatus.setHorizontalTextPosition(SwingConstants.CENTER);

		_pnlCreateMode = new JPanel();
		_pnlCreateMode.setLayout(new BorderLayout());
		_pnlCreateMode.add(_lblCreateWelcome, BorderLayout.NORTH);
		_pnlCreateMode.add(_pnlCreateModeInput, BorderLayout.CENTER);
		_pnlCreateMode.add(_lblCreateModeStatus, BorderLayout.SOUTH);

		// set up main panel (which will switch between video view and create mode)
		_pnlMain = new JPanel(new CardLayout());
		_pnlMain.add(_pnlVideoView, VIDEO_VIEW_ID);
		_pnlMain.add(_pnlCreateMode, CREATE_MODE_ID);
		((CardLayout)_pnlMain.getLayout()).show(_pnlMain, VIDEO_VIEW_ID); // initially show video view
		add(_pnlMain, BorderLayout.CENTER);


		/***************** Event Handlers ******************/

		// functionality for create mode button, should toggle main panel to display create mode
		_btnCreateMode.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (_btnCreateMode.getText().equals(CREATE_MODE_BUTTON)) {
					((CardLayout)_pnlMain.getLayout()).show(_pnlMain, CREATE_MODE_ID);
					// play/delete buttons should not be usable in this mode
					_btnPlay.setEnabled(false);
					_btnDelete.setEnabled(false);
					_btnCreateMode.setText(QUIT_BUTTON);
				} else {
					((CardLayout)_pnlMain.getLayout()).show(_pnlMain, VIDEO_VIEW_ID);
					// play/delete buttons should be usable again
					_btnPlay.setEnabled(true);
					_btnDelete.setEnabled(true);
					_btnCreateMode.setText(CREATE_MODE_BUTTON);
				}
			}
		});

		// functionality for create button
		_btnCreate.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				// functionality when this button says create
				String crtnName = _txtCrtnName.getText();
				Creation c = new Creation(crtnName);
				if (_btnCreate.getText().equals(CREATE_BUTTON)) {
					
					if (CreationManager.validName(crtnName)) {
						if (_existingCrtns.contains(c)) {
							int overWriteSel = JOptionPane.showConfirmDialog(_pnlCreateMode, "The creation \"" + c + "\" already exists.\n"
									+ "Do you wish to overwrite it?","Creation already exists", JOptionPane.YES_NO_OPTION);
							if (overWriteSel != JOptionPane.YES_OPTION) {
								return; // exit function if user does not wish to overwrite.
							}
						}
						CreationManager.setUpCreation(c);
						CreationWorker vidCreator = new CreationWorker(c, _pnlCreateMode, _existingCrtns, _lblCreateModeStatus, 
								WorkerUse.VIDEO, _btnCreate);
						vidCreator.execute();
					} else {
						JOptionPane.showMessageDialog(_pnlCreateMode,"Names may only include alphanumeric characters, hyphens, or underscores.",
								"Invalid Name",JOptionPane.ERROR_MESSAGE);
						return;
					}
					
					// functionality when this button says record
				} else {
					CreationWorker creator = new CreationWorker(c,_pnlCreateMode,_existingCrtns,_lblCreateModeStatus,
							WorkerUse.REMAINING, _btnCreate);
					creator.execute();
				}
			}
		});

		//		// functionality for delete button
		//		_btnDelete.addActionListener(new ActionListener (){
		//			public void actionPerformed(ActionEvent e) {
		//
		//				Creation toDelete = _listCrtns.getSelectedValue();
		//				if (toDelete != null) {
		//					// ask user if they wish to delete
		//					int selection = JOptionPane.showConfirmDialog(_video,
		//							"Are you sure you want to delete the creation \"" + toDelete +"\"?",
		//							"Select an option",JOptionPane.YES_NO_OPTION);
		//
		//					if (selection == JOptionPane.YES_OPTION) {
		//						CreationManager.deleteCreation(toDelete);
		//					}
		//				}
		//			}
		//		});
		//
		//		// functionality for play button
		//		_btnPlay.addActionListener(new ActionListener () {
		//			public void actionPerformed(ActionEvent e) {
		//
		//				Creation toPlay = _listCrtns.getSelectedValue();
		//				if (toPlay != null) {
		//					_player.mute(false);
		//					_player.playMedia(toPlay.getFileName(Creation.Components.COMBINED).getPath(),"");
		//				}
		//			}
		//		});
		//
		//
		//		// functionality for create button
		//		_btnCreate.addActionListener(new ActionListener() {
		//			public void actionPerformed(ActionEvent e) {
		//
		//				String name = _txtCrtnName.getText();
		//				boolean validName = CreationManager.validName(name);
		//				if (!validName) {
		//					// let user know the creation name is not valid
		//					JOptionPane.showMessageDialog(_panelCreate, "The name you entered is not valid.\n" +
		//							"Names may only include alphanumeric character, hyphens, or underscores.", "Invalid Name",
		//							JOptionPane.ERROR_MESSAGE);
		//					return;
		//				}
		//
		//				Creation c = new Creation(name);
		//				boolean overwrite = true;
		//				if (_existingCrtns.contains(c)) {
		//					// ask for overwrite confirmation
		//					int overWriteSel = JOptionPane.showConfirmDialog(_panelCreate, "The creation \"" + name + "\" already exists.\n"
		//							+ "Do you wish to overwrite it?","Please select an option", JOptionPane.YES_NO_OPTION);
		//					if (overWriteSel != JOptionPane.YES_OPTION) {
		//						return;
		//					}
		//					// user has chosen to overwrite
		//					CreationManager.deleteCreation(c);
		//					_existingCrtns.removeElement(c);
		//				}
		//
		//				if (overwrite) {
		//					CreationManager.setUpCreation(c); // set up root directory
		//
		//					// get paths to each component of the creation
		//					File pathToVideo = c.getFileName(Creation.Components.VIDEO);
		//					File pathToAudio = c.getFileName(Creation.Components.AUDIO);
		//					File pathToCombined = c.getFileName(Creation.Components.COMBINED);
		//
		//					try {
		//						// bash command for creating the video
		//						ProcessBuilder vid = new ProcessBuilder("bash","-c",
		//								"ffmpeg -y -f lavfi -i color=c=blue -vf \"drawtext=fontfile=:fontsize=30:fontcolor=white:" +
		//										"x=(w-text_w)/2:y=(h-text_h)/2:text='" + c + "'\" -t 3 " + pathToVideo.getPath());
		//						Process vidP = vid.start();
		//						if (vidP.waitFor() == 1){
		//							CreationManager.deleteCreation(c); // remove partially finished creation
		//							return;
		//						}
		//
		//						JOptionPane.showMessageDialog(_panelCreate, "Video component created!\n"
		//								+ "Press OK to record the audio for this creation");
		//
		//						while (true) {
		//							// bash command for recording audio component
		//							ProcessBuilder audio = new ProcessBuilder("bash","-c",
		//									"ffmpeg -f alsa -ac 2 -i default -t 3 " + pathToAudio.getPath());
		//							Process rec = audio.start();
		//							if (rec.waitFor() == 1) {
		//								CreationManager.deleteCreation(c); // remove partially finished creation
		//								return; // do not proceed if something went wrong
		//							}
		//
		//							int playBackSel = JOptionPane.showConfirmDialog(_panelCreate, "Would you like to listen to the recording?",
		//									"Please select an option", JOptionPane.YES_NO_OPTION);
		//
		//							if (playBackSel == JOptionPane.YES_OPTION) {
		//								_player.mute(false);
		//								_player.playMedia(pathToAudio.getPath(), "");
		//							} else {
		//								break;
		//							}
		//
		//							// show dialog asking user to keep or redo recording
		//							Object[] options = {"Keep","Redo"};
		//							int selection = JOptionPane.showOptionDialog(_panelCreate, "Do you want to keep or redo this recording?",
		//									"Please choose an action to take", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
		//									null, options, null);
		//
		//							if (selection != 1) {
		//								break;
		//							} else {
		//								pathToAudio.delete();
		//								JOptionPane.showMessageDialog(_panelCreate, "Press OK to record");
		//							}
		//						}
		//
		//						// bash command to combine audio and video components
		//						ProcessBuilder combine = new ProcessBuilder("bash","-c",
		//								"ffmpeg -i " + pathToAudio.getPath() + " -i " + pathToVideo.getPath() +
		//								" -codec copy " + pathToCombined.getPath());
		//						Process p = combine.start();
		//						if (p.waitFor() == 0) {
		//							_existingCrtns.addElement(c); // add creation to gui if merge was successful
		//						} else {
		//							CreationManager.deleteCreation(c); // delete all files if an error occurred
		//						}
		//
		//					} catch (IOException | InterruptedException e1) {
		//						// clean up by deleting all files if an exception was thrown
		//						CreationManager.deleteCreation(c);
		//					}
		//				}
		//			}
		//		});

	}


	public static void main(String[] args) {

		NativeDiscovery nd = new NativeDiscovery();
		nd.discover();


		System.out.println("New Version! 2.0");

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				MathsAid frame = new MathsAid();
				frame.setVisible(true);
				frame.setResizable(false);
			}
		});
	}

	/**
	 *  Initialize the list model on program startup, by scanning through
	 *  existing creations and adding them to the list.
	 */
	private void initializeCreationsList() {
		_existingCrtns = new DefaultListModel<Creation>();

		File crtnsFolder = new File("creations");
		if (crtnsFolder.exists()) {
			String[] crtns = crtnsFolder.list();
			if (crtns != null) {
				for (String c : crtns) {
					_existingCrtns.addElement(new Creation(c));
				}
			}
		}
	}


}
