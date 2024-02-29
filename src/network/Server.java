
package network;
/**
 *
 * @author field
 */
import centralpoint.Card;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import static java.lang.Character.isDigit;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server implements Runnable
{
	private InetAddress hostAddress;
	private int port;

	private Selector selector;
	private ServerSocketChannel channel;
	private NetEventListener listener;

	private final List changeRequests = new LinkedList();
	private final Map pendingData = new HashMap();

	public Server(InetAddress hostAddress, int port, NetEventListener listener) throws IOException
	{
		this.hostAddress = hostAddress;
		this.port = port;
		this.selector = this.initSelector();
		this.listener = listener;
	}

	public InetAddress getAddress()
	{
		return hostAddress;
	}

	private Selector initSelector() throws IOException
	{
		Selector s = SelectorProvider.provider().openSelector();

		channel = ServerSocketChannel.open();
		channel.configureBlocking(false);
		channel.socket().bind(new InetSocketAddress(this.hostAddress, this.port));
		channel.register(s, SelectionKey.OP_ACCEPT);

		if (hostAddress == null)
			hostAddress = channel.socket().getInetAddress();

		return s;
	}

	public boolean hasChannel(SocketChannel ch)
	{
		return ch != null && ch.keyFor(selector) != null;
	}

	public void run()
	{
		while (true) {
			try {
				synchronized(changeRequests) {
					Iterator changes = changeRequests.iterator();
					while (changes.hasNext()) {
						ChangeRequest change = (ChangeRequest) changes.next();
						switch (change.type) {
						case ChangeRequest.CHANGEOPS:
							SelectionKey key = change.socket.keyFor(selector);
							key.interestOps(change.ops);
						}
					}
					changeRequests.clear();
				}
				selector.select();

				Iterator selectedKeys = selector.selectedKeys().iterator();
				while (selectedKeys.hasNext()) {
					SelectionKey  key = (SelectionKey)selectedKeys.next();
					selectedKeys.remove();

					if (!key.isValid())
						continue;

					if (key.isAcceptable())
						accept(key);
					else if (key.isReadable())
						read(key);
					else if (key.isWritable())
						write(key);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void accept(SelectionKey key) throws IOException
	{
		ServerSocketChannel ch = (ServerSocketChannel) key.channel();
		SocketChannel s_ch = ch.accept();

		s_ch.configureBlocking(false);
		s_ch.register(selector, SelectionKey.OP_READ);
		if (!listener.handleConnection(s_ch)) {
			s_ch.close();
			key.cancel();
		}
	}

	private void read(SelectionKey key) throws IOException
	{
		SocketChannel ch = (SocketChannel) key.channel();

		ByteBuffer buffer = ByteBuffer.allocate(1024);
		int count;
		try {
			count = ch.read(buffer);
		} catch (IOException e) {
			close(ch);
			return;
		}

		buffer.flip();
		if (count == -1
			|| (listener != null && !listener.handleRead(ch, buffer, count)))
			close(ch);
	}

	private void write(SelectionKey key) throws IOException
	{
		SocketChannel ch = (SocketChannel) key.channel();
		int count = 0;

		synchronized(pendingData) {
			List queue = (List) pendingData.get(ch);
			/**
			 * Null exception alert:
			 *		This can happen once a connection was established.
			 * Check send() for more information.
			 */
			if (queue == null)
				return;

			while (!queue.isEmpty()) {
				ByteBuffer buf = (ByteBuffer) queue.get(0);
				ch.write(buf);

				count += buf.capacity() - buf.remaining();
				if (buf.remaining() > 0)
					break;
				queue.remove(0);
			}

			if (queue.isEmpty())
				key.interestOps(SelectionKey.OP_READ);
		}

		if (!listener.handleWrite(ch, count))
			close(ch);
	}

	public void send(SocketChannel ch, byte[] data)
	{
		synchronized(changeRequests) {
			changeRequests.add(new ChangeRequest(ch, ChangeRequest.CHANGEOPS, SelectionKey.OP_WRITE));
			synchronized(pendingData) {
				List queue = (List) pendingData.get(ch);
				if (queue == null) {
					queue = new ArrayList();
					pendingData.put(ch, queue);
				}
				queue.add(ByteBuffer.wrap(data));
                                processString(data.toString());
			}
		}

		selector.wakeup();
	}

	public void close(SocketChannel ch)
	{
		if (!listener.handleConnectionClose(ch))
			return;

		try {
			ch.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		ch.keyFor(selector).cancel();
		synchronized(changeRequests) {
			Iterator changes = changeRequests.iterator();
			while (changes.hasNext()) {
				ChangeRequest req = (ChangeRequest) changes.next();
				if (req.socket == ch) {
					changeRequests.remove(req);
					break;
				}
			}
		}
	}
        
        public List<Card> processCard(String card_data)
        {
            System.out.println("Processing card information...");
            List<Card> card_list = new ArrayList();
            boolean[] card_arr = {false, false, false, false, false};
            String[] banks = {"standard", "capitec", "fnb", "absa", "nedbank", "investec", "mercantile"};
            String[] holders = {"mr", "miss", "mrs"};
            Card new_card = new Card();
            int startPos = 0;
            String message = card_data.toLowerCase();
            for(int i = 0; i<message.length(); i++)
            {
                if(new_card.bankName == "")
                {
                    for(int j = 0; j<banks.length; j++)
                    {
                        startPos = message.indexOf(banks[j]);
                        if(startPos!=-1)
                        {
                            new_card.bankName = banks[j];
                            card_arr[0] = true;
                            message = message.replaceFirst(banks[j],"");
                        }
                    }
                }
                else if(new_card.cardNum == "")
                {
                    int count = 0;
                    String cardNum = "";
                    for(int j = 0; j<message.length(); j++)
                    {
                        if(isDigit(message.charAt(j)) && count<=16)
                        {                    
                            if(count==16)
                            {
                                count++;
                                new_card.cardNum = cardNum;
                                card_arr[1] = true;
                                message = message.replaceFirst(cardNum,"");
                            }
                            else
                            {
                                cardNum = cardNum+message.charAt(j);
                                count++;
                            }
                        }
                        
                    }
                }
                else if(new_card.cardHolder == "")
                {
                    for(int j = 0; j<holders.length; j++)
                    {
                        startPos = message.indexOf(holders[j]);
                        if(startPos!=-1)
                        {
                            String name = "";
                            int count = 0;
                            while (count<=3)
                            {
                                if(count==3)
                                {
                                    count++;
                                    new_card.cardHolder = name;
                                    card_arr[2] = true;
                                    
                                    message = message.replaceFirst(name,"");
                                }
                                else
                                {
                                    name=name+message.charAt(startPos);
                                    startPos++;
                                    if(message.charAt(startPos)==' ')
                                    {
                                        count++;
                                    }
                                    
                                }
                                
                            }
                            
                        }
                        
                    }
                }
                else if(new_card.branchCode == "")
                {
                    int count = 0;
                    String cardNum = "";
                    for(int j = 0; j<message.length(); j++)
                    {
                        if(isDigit(message.charAt(j)) && count<=6)
                        {   if(count==6)
                            {
                                count++;
                                new_card.branchCode = cardNum;
                                card_arr[3] = true;
                                message = message.replaceFirst(cardNum,"");
                            }
                            else
                            {
                                cardNum = cardNum+message.charAt(j);
                                count++;
                                
                            }
                            
                        }
                    }
                }
                else if(new_card.expiryDate == "")
                {
                    startPos = message.indexOf("/");
                    String expiry = "";
                    if(startPos!=-1)
                    {
                    for(int j = startPos-2; j<startPos+5; j++)
                    {
                        if(isDigit(message.charAt(j)) || message.charAt(j)=='/')
                        {
                            expiry = expiry+message.charAt(j);
                            
                        }
                        else
                        {   
                            expiry = "";
                            break;
                        }
                    }
                    card_arr[4] = true;
                    message = message.replaceFirst(expiry,"");
                    new_card.expiryDate = expiry;
                }}
                else if (areAllTrue(card_arr))
                {
                    card_list.add(new_card);
                    try {
                        writeToFile(new_card.toString());
                    } catch (IOException ex) {
                        System.out.println("Unable to write to file!");
                    }
                    new_card = new Card();
                }
                else
                {
                    System.out.println("No matches!");
                    break;
                }
                
            }
            return card_list;
        }
        
        public static boolean areAllTrue(boolean[] array)
        {
            for(boolean b : array) if(!b) return false;
            return true;
        }
        
        public void processString(String msg)
	{
		int len = msg.length();
		if (len != 0)
                {
                    List<Card> card_list = processCard(msg);
                    String card_data = "";
                    for(int i = 0; i<card_list.size(); i++)
                    {
                        card_data= card_data+card_list.get(i).toString();
                    }
                    System.out.println(card_data);
                }
	}
        
        public void handleFile()
        {
             try {
                File myObj = new File("serverOutput.txt");
                if (myObj.createNewFile()) {
                  System.out.println("File created: " + myObj.getName());
                } else {
                  System.out.println("File already exists.");
                }
              } catch (IOException e) {
                System.out.println("An error occurred.");
                e.printStackTrace();
              }
        }
        
        public void writeToFile(String cardInfo) throws IOException
        {
            File file = new File("serverOutput.txt");
            FileWriter fr = new FileWriter(file, true);
            BufferedWriter br = new BufferedWriter(fr);
            PrintWriter pr = new PrintWriter(br);
            pr.println(cardInfo);
            pr.close();
            br.close();
            fr.close();
            System.out.println("Successfully appended card details to serverOutput.txt");
        }
        
}