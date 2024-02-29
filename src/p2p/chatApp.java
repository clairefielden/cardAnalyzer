package p2p;
/**
 *
 * @author field
 */
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;


import javax.swing.SwingUtilities;
import javax.swing.text.DefaultCaret;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;


public class chatApp extends JFrame
{
	private User User;

	private JList chatParticipants;
	private JTextArea chatTextArea;
	private JTextField chatTextField;
	private JList UserList;

	private JPopupMenu chatParticipantsPopup;
	private JPopupMenu UserListPopup;

	private final DefaultListModel UserListModel;
	private final DefaultListModel chatParticipantsModel;

	private String centralHost;
	private int centralPort;

	private boolean hasPublishedSelf;

	private static chatApp instance;

	@SuppressWarnings("LeakingThisInConstructor")
	public chatApp(String nick, String host, int port)
	{
		try {
			User = new User(null, nick, host, port);
		} catch (IOException e) {
			e.printStackTrace();
		}

		UserListModel = new DefaultListModel();
		chatParticipantsModel = new DefaultListModel();

		instance = this;
                initComponents();
                
		DefaultCaret caret = (DefaultCaret) chatTextArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

		chatTextArea.setLineWrap(true);
                chatTextArea.setPreferredSize(new Dimension(400, 400));
                chatTextArea.setMinimumSize(new Dimension(400, 400));
                chatTextArea.setBounds(0, 0, 400, 400);
                
		chatTextField.setText(
			"Enter text for credit card reader \n"
                                + "Alternatively, submit a PNG/PDF file for processing \n"
		);
                chatTextField.setEditable(false);
                
	}

	public static chatApp get()
	{
		return instance;
	}

	public void setCentralInfo(String host, int port)
	{
		centralHost = host;
		centralPort = port;

		hasPublishedSelf = User.publishSelf(host, port);
	}

	@SuppressWarnings("unchecked")
	private void initComponents() {
		JScrollPane jScrollPane1 = new JScrollPane();
		JScrollPane jScrollPane4 = new JScrollPane();
		JScrollPane jScrollPane3 = new JScrollPane();

		JButton uploadFile = new JButton("Upload");
		JButton sendButton = new JButton("Send");

		chatParticipants = new JList();
		UserList = new JList();
                
                chatTextField = new JTextField();
                
		chatTextArea = new JTextArea();
		chatTextArea.setEditable(true);
		chatTextArea.setColumns(10);
		chatTextArea.setRows(3);
		jScrollPane1.setViewportView(chatTextArea);

		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
                
		

		uploadFile.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
                            try {
                                chooseFileActionPerformed(evt);
                            } catch (IOException ex) {
                                Logger.getLogger(chatApp.class.getName()).log(Level.SEVERE, null, ex);
                            }
			}
		});

		chatParticipants.setModel(chatParticipantsModel);
		chatParticipants.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				chatParticipantsMouseClicked(evt);
			}
			public void mouseReleased(java.awt.event.MouseEvent evt) {
				if (evt.isPopupTrigger())
					chatParticipantsPopup.show(evt.getComponent(), evt.getX(), evt.getY());
			}
		});
		jScrollPane3.setViewportView(chatParticipants);

		UserList.setModel(UserListModel);
		UserList.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				UserListMouseClicked(evt);
			}
			public void mouseReleased(java.awt.event.MouseEvent evt) {
				if (evt.isPopupTrigger())
					UserListPopup.show(evt.getComponent(), evt.getX(), evt.getY());
			}
		});
		jScrollPane4.setViewportView(UserList);

		sendButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
                            try {
                                sendTextMessage();
                            } catch (IOException ex) {
                                Logger.getLogger(chatApp.class.getName()).log(Level.SEVERE, null, ex);
                            }
			}
		});

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(
			layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
			.addGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, true)
					.addComponent(chatTextField)
					.addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 452, Short.MAX_VALUE))
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
					//.addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
					.addComponent(sendButton, javax.swing.GroupLayout.DEFAULT_SIZE, 93, Short.MAX_VALUE))
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
					.addComponent(uploadFile, javax.swing.GroupLayout.DEFAULT_SIZE, 91, Short.MAX_VALUE)
					//.addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                                )
                                )
		);
		layout.setVerticalGroup(
			layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
			.addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
					//.addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 371, Short.MAX_VALUE)
					.addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING)
					//.addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.LEADING)
                                )
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
					.addComponent(uploadFile, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(chatTextField)
					.addComponent(sendButton)))
		);

		UserListPopup = new JPopupMenu("Action...");
		JMenuItem mItemDisconnect = new JMenuItem("Disconnect");
		mItemDisconnect.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String UserInfo = (String) UserList.getSelectedValue();
				if (UserInfo == null)
					return;

				String UserHost = UserInfo.substring(0, UserInfo.indexOf(":"));
				if (User.disconnectFrom(UserHost))
					chatTextArea.append("<Network> Successfully disconnected from: " + UserHost + "\n");
			}
		});

		JMenuItem mItemConnect = new JMenuItem("Connect");
		mItemConnect.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String hostName = JOptionPane.showInputDialog("Hostname/IP:");
				String portName = JOptionPane.showInputDialog("Port:");

				int port;
				try {
					port = Integer.parseInt(portName);
				} catch (NumberFormatException ex) {
					JOptionPane.showMessageDialog(null, "Invalid port name " + portName + "!");
					return;
				}

				try {
					User.connect(hostName, port);
				} catch (IOException ex) {
					JOptionPane.showMessageDialog(null, "Unable to establish a connection to " + hostName + ":" + portName + "!");
				}

				UserListModel.addElement(hostName + ":" + port);
			}
		});

		UserListPopup.add(mItemConnect);
		UserListPopup.add(mItemDisconnect);

		chatParticipantsPopup = new JPopupMenu("Action...");
		JMenuItem mItemKick = new JMenuItem("Kick");
		mItemKick.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				String nickName = (String) chatParticipants.getSelectedValue();
				if (nickName != null)
					User.kick(nickName);
			}
		});
		chatParticipantsPopup.add(mItemKick);
		pack();

	}

	private void sendTextMessage() throws IOException
	{
            System.out.println("Sent to server");
		String message = chatTextArea.getText();
		if ("".equals(message))
                {
                    return;
                }
			
		if (message.charAt(0) == '/') {
			String[] splitted = message.split(" ");
			if (splitted.length > 1) {
				if (splitted[0].equals("/name")) {
					// A Nickname can contain spaces
					String newNick = new String();
					for (int i = 1; i < splitted.length; ++i)
						newNick += splitted[i] + " ";

					User.setName(newNick);
					chatTextField.setText("");
					chatTextArea.append("You changed your name to " + newNick + ".");
				} else if (splitted[0].equals("/kick")) {
					// A Nickname can contain spaces
					String nick = new String();
					for (int i = 1; i < splitted.length; ++i)
						nick += splitted[i] + " ";

					if (UserListModel.contains(nick))
						User.kick(nick);
                                } else if (splitted[0].equals("/connect")) {
                                    String host = splitted[1];
                                    int port = 9119;
                                    if (splitted.length > 2)
                                        port = Integer.parseInt(splitted[2]);

                                    try {
                                        chatTextArea.append("Attempting connection to " + host + ":" + port + "\n");
                                        User.connect(host, port);
                                    } catch (IOException e) {
                                        chatTextArea.append("Unable to connect to: " + host + ":" + port + "\n");
                                        e.printStackTrace();
                                    }
				} else
					chatTextArea.append("Invalid command.");
			} else if (splitted[0].equals("/help")) {
				chatTextArea.append("Commands available:\n" +
					"/name <new nickname> (Can contain spaces)\n" +
					"/kick <nickname> (Can contain spaces)\n" +
                                        "/connect <host> <port>"
				);
			}
			return;
		}

		String selected = (String) chatParticipants.getSelectedValue();
                //String selected = "server";
		if (selected != null)
			User.sendMessage(message, selected);
		else
			User.sendMessage(message, (User) null);

		chatTextField.setText("");
		chatTextArea.append("\n<" + User.UserName + "> " + message + "\n");
        }

	private void chooseFileActionPerformed(java.awt.event.ActionEvent evt) throws IOException
	{
		JFileChooser chooser = new JFileChooser();
                if(chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = chooser.getSelectedFile();
                    String exportPath = chooser.getSelectedFile().getAbsolutePath();
                    String data = readFile(exportPath, StandardCharsets.UTF_8);
                    chatTextArea.append(data);
                    sendTextMessage();
                }
                
	}
        
        static String readFile(String path, Charset encoding)
        throws IOException
      {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
      }


	private void UserListMouseClicked(java.awt.event.MouseEvent evt)
	{
		if (SwingUtilities.isLeftMouseButton(evt)) {
			String UserInfo = (String) UserList.getSelectedValue();
			if (UserInfo == null)
				return;

			int sep = UserInfo.indexOf(":");
			String UserHost = UserInfo.substring(0, sep);
			int UserPort = Integer.parseInt(UserInfo.substring(sep + 1, UserInfo.length()));

			try {
				User.connect(UserHost, UserPort);
			} catch (IOException e) {
				chatTextArea.append("Unable to connect to: " + UserInfo + "\n");
				e.printStackTrace();
			}
		} else if (evt.isPopupTrigger())
			UserListPopup.show(evt.getComponent(), evt.getX(), evt.getY());
	}

	private void chatParticipantsMouseClicked(java.awt.event.MouseEvent evt)
	{
		String selected = (String) chatParticipants.getSelectedValue();
		if (selected != null)
			chatTextArea.append("You're now private messaging " + selected + " (CTRL+LCLICK to unselect)\n");
		else
			chatTextArea.append("\n Information submitted to server.\n");

		if (evt.isPopupTrigger())
			chatParticipantsPopup.show(evt.getComponent(), evt.getX(), evt.getY());
	}

	public void appendText(String sender, String text)
	{
		if (sender == null)
			sender = "Network";

		chatTextArea.append("<" + sender + "> " + text + "\n");
	}

	public void UserConnected(User newUser)
	{
		if (newUser.UserName == null)
			newUser.UserName = "unnamed";

		chatTextArea.append(newUser.UserName + " has connected.\n");
		chatParticipantsModel.addElement(newUser.UserName);
	}

	public void UserDisconnected(User node, boolean timeout)
	{
		int idx = chatParticipantsModel.indexOf(node.UserName);
		if (idx != -1)
			chatParticipantsModel.remove(idx);

		if (!timeout)
			chatTextArea.append(node.UserName + " has disconnected.\n");
		else
			chatTextArea.append(node.UserName + " has timed out.\n");
	}

	public void UserNameChanged(User node, String oldName, String newName)
	{
		int index = chatParticipantsModel.indexOf(oldName);
		if (index != -1) {
			if (User.isChild(node)) {
				while (chatParticipantsModel.contains(newName) || newName.equals(User.UserName)) {
					newName += "_";
					node.UserName = newName;
					User.sendNameChangeRequest(node);
				}
			}

			chatParticipantsModel.setElementAt(newName, index);
			chatTextArea.append(oldName + " has changed name to " + newName + "\n");
		} else {
			System.out.println("Unable to find User name " + oldName + " (" + newName + ")");
			chatParticipantsModel.addElement(newName);
		}
	}

	public void UserAcked(String from, String hostName, int port)
	{
		if (!UserListModel.contains(hostName + ":" + port)) {
			chatTextArea.append("New User Acked from " + from + ": " + hostName + ":" + port + "\n");
			UserListModel.addElement(hostName + ":" + port);
		}
	}

	public void centralConnectionFailed()
	{
		chatTextArea.append("Unable to establish a connection to the central server.\n");
	}

}