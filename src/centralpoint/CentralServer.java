package centralpoint;
/**
 *
 * @author field
 */
import network.NetEventListener;
import network.Server;

import java.io.IOException;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
/*
 * Few notes on the bytes used:
 *		Once a User has connected to this server, it must send:
 *		1. 0x1A to retrieve the User list.
 *			The User list is sent as follows:
 *				Integer - Number of available Users
 *				byte[4] - for each User address
 *				Integer - User port
 *		2. 0x1B to acknowledge self.
 *			Integer - port
*/
public class CentralServer implements NetEventListener
{
	private Server m_server;
	private List m_Users = new LinkedList();

	public CentralServer() throws IOException
	{
		m_server = new Server(null, 9118, this);
		new Thread(m_server).start();
	}

	@Override
	public boolean handleWrite(SocketChannel ch, int nr_wrote)
	{
		return true;
	}

	@Override
	public boolean handleRead(SocketChannel ch, ByteBuffer buf, int nread)
	{
		try {
			while (buf.hasRemaining()) {
				byte request = buf.get();
                                ByteBuffer out = ByteBuffer.allocate(1024);
                                out.putInt(m_Users.size());
                                Iterator it = m_Users.iterator();
                                while (it.hasNext()) {
						System.out.println(it.next());
                                }
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	@Override
	public boolean handleConnection(SocketChannel ch)
	{
		return true;
	}

	@Override
	public boolean handleConnectionClose(SocketChannel ch)
	{
		return true;
	}

	public static void main(String args[])
	{
		try {
			new CentralServer();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}