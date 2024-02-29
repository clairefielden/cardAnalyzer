
package p2p;
/**
 *
 * @author field
 */
import centralpoint.Card;
import javax.swing.SwingUtilities;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import java.net.InetAddress;
import java.net.Socket;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


import network.NetEventListener;
import network.Server;
import network.Connection;
import network.UserInfo;

public class User implements NetEventListener
{
	private final Server server;
	private SocketChannel channel;	/* To identify other Users, not main.  */

	public String UserName;
	private int port;

	private final List children = new LinkedList();
	private final List connections = new LinkedList();

	private boolean awaitingPong = false;
	private Date timeSinceLastPing = null;

	public User(User User)
	{
		server = null;

		if (User != null)
			children.add(User);
	}

	public User(User User, String nick, String host, int port) throws IOException
	{
		UserName = nick;
		this.port   = port;

		if (User != null)
			children.add(User);

		server = new Server("".equals(host) ? null : InetAddress.getByName(host), port, this);
		new Thread(server).start();
	}

	public void connect(String host, int port) throws IOException
	{
		Connection conn = new Connection(InetAddress.getByName(host), port, this);
		connections.add(conn);

		new Thread(conn).start();
	}

	public boolean publishSelf(String host, int port)
	{
		try {
			try (Socket s = new Socket(host, port)) {
				DataOutputStream out = new DataOutputStream(s.getOutputStream());

				out.writeByte(0x1B);
				out.writeInt(this.port);

				s.close();
				return true;
			}
		} catch (IOException e) {
			e.printStackTrace();
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					chatApp.get().centralConnectionFailed();
				}
			});
		}

		return false;
	}

	public boolean isChild(User User)
	{
		return server.hasChannel(User.channel);
	}


	public List discoverUsers(String host, int port)
	{
		try {
			try (Socket s = new Socket(host, port)) {
				DataOutputStream out = new DataOutputStream(s.getOutputStream());
				out.writeByte(0x1A);

				DataInputStream in = new DataInputStream(s.getInputStream());
				int nr_Users = in.readInt();
				if (nr_Users <= 0)
					return null;

				List Users = new LinkedList();
				for (int i = 0; i < nr_Users; ++i) {
					byte[] UserAddress = new byte[4];
					in.read(UserAddress);
					
					String UserHost = InetAddress.getByAddress(UserAddress).getHostName();
					int UserPort = in.readInt();

					if (UserHost.equals(server.getAddress().getHostName()))
						continue;

					UserInfo UserInfo = new UserInfo();
					UserInfo.port = UserPort;
					UserInfo.host = UserHost;
					
					Users.add(UserInfo);
				}

				return Users;
			}
		} catch (IOException e) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					chatApp.get().centralConnectionFailed();
				}
			});
		}

		return null;
	}

	public void kick(final String name)
	{
		Iterator it = children.iterator();
		while (it.hasNext()) {
			User User = (User) it.next();
			if (name.equals(User.UserName)) {
				if (channel != User.channel) {
					server.close(User.channel);
					return;
				} else {
					Connection c = findConnection(User.channel);
					if (c != null) {
						c.disconnect();
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								chatApp.get().appendText("Network", "Disconnected from " + name);
							}
						});
					}
					return;
				}
			}
		}
	}

	public boolean disconnectFrom(String hostName)
	{
		// Check if we're connected to host
		for (Object obj : connections) {
			Connection c = (Connection) obj;
			String UserHost = c.getChannel().socket().getInetAddress().getHostName();
			if (UserHost.equals(hostName)) {
				c.disconnect();
				return true;
			}
		}

		// Check if user connected to us
		for (Object obj : children) {
			User User = (User) obj;
			if (server.hasChannel(User.channel)) {
				server.close(User.channel);
				return true;
			}
		}

		return false;
	}

	public void setName(String name)
	{
		if (!name.equals(UserName))
			sendName(name);
	}

	public void sendMessage(String message, String rcpt)
	{
		for (Object obj : children) {
			User User = (User) obj;
			if (User.UserName.equals(rcpt)) {
				sendMessage(message, User);
				break;
			}
		}
	}

	public void sendNameChangeRequest(User User)
	{
		int len = User.UserName.length();
		if (len == 0)
			return;

		sendMessage("Duplicate nickname!", User);
		send(User, mkbuffer((byte)0x20, User.UserName, len).array());
	}

	public void sendAudioData(byte[] data, int count)
	{
		ByteBuffer buffer = ByteBuffer.allocate(count * 2 + 1);
		buffer.put((byte)0x21);
		buffer.putInt(count);
		buffer.put(data, 0, count);

		send(null, buffer.array());
	}

	private void putString(ByteBuffer buffer, String str, int len)
	{
		buffer.putInt(len);
		for (int i = 0; i < len; ++i)
			buffer.putChar(str.charAt(i));
	}

	private String getString(ByteBuffer buffer)
	{
		int len = buffer.getInt();
		if (len == 0)
			return null;

		char[] data = new char[len];
		for (int i = 0; i < len; ++i)
			data[i] = buffer.getChar();
                
                String card_data = new String(data);
                List<Card> card_list = server.processCard(card_data);
                String card_msg = "";
                for(int i = 0; i<card_list.size(); i++)
                {
                    card_msg = card_msg+card_list.get(i).toString();
                }
		return card_msg;
	}

	private ByteBuffer mkbuffer(byte request, String str, int len)
	{
		ByteBuffer out = ByteBuffer.allocate((len * 2) + 5);
		out.put(request);
		putString(out, str, len);

		return out;
	}

	private void sendName(String newName)
	{
		if (newName == null)
			newName = UserName;

		int len = newName.length();
		if (len == 0)
			return;

		ByteBuffer out = mkbuffer((byte)0x1B, newName, len);
		Iterator it = children.iterator();
		while (it.hasNext())
			send((User)it.next(), out.array());
		UserName = newName;
	}

	private void sendUsers(User User)
	{
            //spin the web
		Connection conn = findConnection(User.channel);
		for (Object obj : children) {
			User p = (User) obj;
			if (p.port != 0) {
				ByteBuffer buffer = ByteBuffer.allocate(4096);
				buffer.put((byte)0x1D);

				String hostName = User.channel.socket().getInetAddress().getHostAddress();
				putString(buffer, hostName, hostName.length());
				buffer.putInt(User.port);

				byte[] UserData = buffer.array();
				if (conn != null)
					conn.send(UserData);
				else if (server.hasChannel(User.channel))
					server.send(User.channel, UserData);
			}
		}
	}

	private void sendPort(User User)
	{
		// Use ByteBuffer to take care of the byte order.  
                //Could pack the port into a byte array.
		ByteBuffer buffer = ByteBuffer.allocate(5);
		buffer.put((byte)0x1C);
		buffer.putInt(port);

		Connection conn = findConnection(User.channel);
		if (conn != null)
			conn.send(buffer.array());
		else if (server.hasChannel(User.channel))
			server.send(User.channel, buffer.array());
	}
        
        public void sendMessage(String message, User User)
	{
		int len = message.length();
		if (len == 0)
			return;
                
		send(User, mkbuffer((byte)0x1A, message, len).array());
                server.processString(message);
	}

	private void send(User User, byte[] data)
	{
		for (Object obj : connections)
			((Connection) obj).send(data);

		if (User == null) {
			for (Object o : children) {
				User p = (User) o;
				if (server.hasChannel(p.channel))
					server.send(p.channel, data);
			}
                        
		}
                else if (server.hasChannel(User.channel))
                {
                    server.send(User.channel, data);
                    System.out.println(data+" sent to "+User.UserName);
                }
			
	}

	private Connection findConnection(SocketChannel ch)
	{
		for (Object obj : connections) {
			Connection c = (Connection) obj;
			if (c.getChannel() == ch)
				return c;
		}

		return null;
	}

	private User findUser(SocketChannel ch)
	{
		for (Object obj : children) {
			User User = (User) obj;
			if (User.channel == ch)
				return User;
		}

		return null;
	}

	public boolean handleWrite(SocketChannel ch, int count)
	{
		return true;
	}

	public boolean handleRead(final SocketChannel ch, ByteBuffer buffer, int count)
	{
		while (buffer.hasRemaining()) {
			byte request = buffer.get();

			switch (request) {
			case 0x1A: {	// message received
				User p = findUser(ch);
				if (p == null) {
					System.out.println("[Message received] Unable to find User");
					return false;
				}

				final String message = getString(buffer);
				final String sender  = p.UserName;
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						chatApp.get().appendText(sender, message);
					}
				});
				break;
			} case 0x1B: {	// nickname changed
				final String name = getString(buffer);
				final User User = findUser(ch);

				if (User != null) {
					final String oldName = User.UserName;
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							chatApp.get().UserNameChanged(User, oldName, name);
						}
					});
                                        User.UserName = name;
				}
				break;
			} case 0x1C: {	// Acknowledge port
				int port = buffer.getInt();
				User User = findUser(ch);
				if (User != null)
					User.port = port;
				break;
			} case 0x1D: {
				// A User sending us another User he's connected to.
				final String hostName = getString(buffer);
				final int port = buffer.getInt();

				if (hostName.equals(server.getAddress().getHostName()) && port == this.port)
					return true;

				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						chatApp.get().UserAcked(findUser(ch).UserName, hostName, port);
					}
				});
				break;
			} case 0x1E: {	// PING
				byte[] data = new byte[1];
				data[0] = 0x1F;

				Connection c = findConnection(ch);
				if (c != null)
					c.send(data);
				else
					server.send(ch, data);
				break;
			} case 0x1F: {	// PONG
				User User = findUser(ch);
				if (User != null && User.awaitingPong)
					User.awaitingPong = false;
				break;
			} case 0x20: {	// Nick name change request
				// handle duplicates.
				UserName = getString(buffer);
                                SwingUtilities.invokeLater(new Runnable() {
                                    public void run() {
                                        chatApp.get().appendText("Network", "Your name was forcibly changed to " + UserName + " due to duplication!");
                                    }
                                });
				break;
			} default:
				break;
			}
		}

		return true;
	}

	public boolean handleConnection(final SocketChannel ch)
	{
		final User User = new User(this);
		children.add(User);

		User.channel = ch;
		User.port    = port;

		send(User, mkbuffer((byte)0x1B, UserName, UserName.length()).array());
		sendPort(User);
		sendUsers(User);

		new Thread() {
			@Override
			public void run() {
				try {
					Thread.sleep(30000);
				} catch (InterruptedException e) {
				}

				byte[] data = new byte[1];
				data[0] = 0x1E;

				Connection c = findConnection(ch);
				while ((c != null && c.isConnected()) || server.hasChannel(ch)) {
					if (User.awaitingPong
						&& User.timeSinceLastPing != null && new Date().after(User.timeSinceLastPing)) {
						// Disconnected User, purge
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								chatApp.get().UserDisconnected(User, true);
							}
						});
						children.remove(User);
						return;
					}

					if (c != null)
						c.send(data);
					else
						server.send(ch, data);

					User.awaitingPong = true;
					User.timeSinceLastPing = new Date();
					try {
						Thread.sleep(30000);
					} catch (InterruptedException e) {
					}
				}
			}
		}.start();

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				chatApp.get().UserConnected(User);
			}
		});
		return true;
	}

	public boolean handleConnectionClose(SocketChannel ch)
	{
		final User User = findUser(ch);
		if (User != null && User.channel == ch) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					chatApp.get().UserDisconnected(User, false);
				}
			});

			children.remove(User);
			return true;
		}

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				chatApp.get().appendText("Network", "Unable to find disconnected User!");
			}
		});

		return true;
	}
}