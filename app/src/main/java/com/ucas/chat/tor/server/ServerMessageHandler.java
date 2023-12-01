package com.ucas.chat.tor.server;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.gson.Gson;
import com.ucas.chat.bean.UserBean;
import com.ucas.chat.bean.contact.ConstantValue;
import com.ucas.chat.eventbus.Event;
import com.ucas.chat.jni.ServiceLoaderImpl;
import com.ucas.chat.jni.common.IDecry;
import com.ucas.chat.jni.common.IEntry;
import com.ucas.chat.tor.message.ACKMessage;
import com.ucas.chat.tor.message.DataMessage;
import com.ucas.chat.tor.message.FailedTextMessage;
import com.ucas.chat.tor.message.FileMessage;
import com.ucas.chat.tor.message.HandShakeMessage;
import com.ucas.chat.tor.message.Message;
import com.ucas.chat.tor.message.MessageItem;
import com.ucas.chat.tor.util.AESCrypto;
import com.ucas.chat.tor.util.ConnectionItems;
import com.ucas.chat.tor.util.ConnectionStatusItem;
import com.ucas.chat.tor.util.Constant;
import com.ucas.chat.tor.util.FileTask;
import com.ucas.chat.tor.util.MailItem;
import com.ucas.chat.tor.util.RSACrypto;
import com.ucas.chat.tor.util.RecordXOR;
import com.ucas.chat.tor.util.XORutil;
import com.ucas.chat.ui.home.InterfaceOffline.sendOfflineFile;
import com.ucas.chat.ui.home.InterfaceOffline.sendOfflineText;
import com.ucas.chat.utils.LogUtils;
import com.ucas.chat.utils.RandomUtil;
import com.ucas.chat.utils.SharedPreferencesUtil;

import org.apaches.commons.codec.digest.DigestUtils;
import org.greenrobot.eventbus.EventBus;
import org.torproject.android.service.OrbotServiceAction;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import static com.ucas.chat.MyApplication.getContext;
import static com.ucas.chat.TorManager.startTor;
import static com.ucas.chat.TorManager.stopTor;
import static org.torproject.android.service.OrbotServiceAction.STATUSCHANGE_ACTION;
import static org.torproject.android.service.OrbotServiceAction.STATUSCHANGE_MESSAGE;


public class ServerMessageHandler {
	private static String TAG = ConstantValue.TAG_CHAT + "ServerMessageHandler";
	/**
	 * key: onionname value: onionhash
	 */
	private ConcurrentHashMap<String, MailItem> mailList = new ConcurrentHashMap<String, MailItem>();
	/**
	 * key:onionname value: socket before data
	 */
	private ConcurrentHashMap<String, ConnectionItems> allConByOnionName = new ConcurrentHashMap<String, ConnectionItems>();

	private ConcurrentHashMap<String, HandShakeMessage> handShakeMessageHandlerByOnionName = new ConcurrentHashMap<String, HandShakeMessage>();

	private LinkedBlockingQueue<MessageItem> sendQueue = new LinkedBlockingQueue<MessageItem>();
	private LinkedBlockingQueue<MessageItem> recieveQueue = new LinkedBlockingQueue<MessageItem>();
	private ConcurrentHashMap<String, FileTask> senderFileMap = new ConcurrentHashMap<String, FileTask>();

	private CopyOnWriteArrayList<RecordXOR> recordXORs = new CopyOnWriteArrayList<>();// TODO: 2021/10/3 记录各条消息使用的xor情况
//	private int startXORFileName = 0;//保存与对方建立连接后，开始使用的xor文件名。后面每收发一次消息就更新一次
//	private int startXORIndex = 0;//从该xor文件的哪个位置开始用。该位置定义为距离xor文件尾的byte数。后面每收发一次消息就对应上面的更新一次
	private RecordXOR commonRecordXOR = new RecordXOR();//保存开始要用的xor文件信息。后面每收发一次消息就更新一次。共同使用这个全局变量


	private static ServerMessageHandler instance = new ServerMessageHandler();

	private LinkedBlockingQueue<FailedTextMessage> failedQueue = new LinkedBlockingQueue<FailedTextMessage>();

	private Context context;

	private int localPort;
	private String privateKey;

	private UserBean mySelfBean;

	private String channelServer;




	public void setChannelServer(String channelServer) {
		this.channelServer = channelServer;
	}

	public UserBean getMySelfBean() {
		return mySelfBean;
	}

	public void setMySelfBean(UserBean mySelfBean) {
		this.mySelfBean = mySelfBean;
	}

	private ServerMessageHandler() {


//		ClientTransport textClient = new ClientTransport(this.mailList.get(Constant.REMOTE_ONION_NAME),
//				Constant.CLIENT_PRIVATE_KEY, 5, Constant.SOCKET_PURPOSE_TEXT);
//		textClient.start();
//		ClientTransport fileClient = new ClientTransport(this.mailList.get(Constant.REMOTE_ONION_NAME),
//				Constant.CLIENT_PRIVATE_KEY, 5, Constant.SOCKET_PURPOSE_FILE);
//		fileClient.start();
//		createConnection(Constant.REMOTE_ONION_NAME,Constant.SOCKET_PURPOSE_TEXT);
//		createConnection(Constant.REMOTE_ONION_NAME,Constant.SOCKET_PURPOSE_FILE);
	}

	public void init(Context context) {
		this.context = context;
//		this.initMailList();
		ServerTransport server = new ServerTransport(Constant.REMOTE_ONION_PORT, 10, Constant.CLIENT_PRIVATE_KEY);
		server.start();

		Writer writer = new Writer();
		writer.start();
		Reader reader = new Reader();
		reader.start();
//		reader = new Reader();
//		reader.start();
//		reader = new Reader();
//		reader.start();
		StatusCheckerTransport selfChecker = new StatusCheckerTransport(mySelfBean.getOnionName(),Constant.REMOTE_ONION_PORT,Constant.CONNECT_RETRY_COUNT);
		selfChecker.start();
//		reader = new Reader();
//		reader.start();
//		reader = new Reader();
//		reader.start();
//		reader = new Reader();
//		reader.start();
//		reader = new Reader();
//		reader.start();
//		reader = new Reader();
//		reader.start();


	}

	public void setLocalPort(int localPort) {
		this.localPort = localPort;
	}

	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}

	public  void addContact(MailItem item){
		this.mailList.put(item.getOnionName(), item);
	}

	public static ServerMessageHandler getInstance() {
		return instance;
	}

	/**
	 *
	 * @param onionHash
	 * @return
	 */
	public MailItem getMailItemByOnionHash(byte[] onionHash) {
//		System.out.println("ServerMessageHandler targetOnionHash " + AESCrypto.bytesToHex(onionHash));
		for (Map.Entry<String, MailItem> entry : this.mailList.entrySet()) {
//			System.out.println("ServerMessageHandler key " + entry.getKey());

//			System.out.println("ServerMessageHandler value " + entry.getValue());
			MailItem item = entry.getValue();
			if (Arrays.equals(item.getOnionHash(), onionHash))
				return item;
		}
		return null;
	}

	public void createConnectionAsyc(String onionName, int purpose){

		//onionName = liqf2ad7xgi4ewixwvk6qxf5bsevaq7qojvfzu74ruwusvc4ullfonyd.onion(b) , purpose = 0
		System.out.println(TAG + " createConnectionAsyc:: onionName = " + onionName + " , purpose = " + purpose);
		ConnectionStatusItem item = this.getConnectionStatusItemByOnionName(onionName, purpose);
		if(item==null){
			////onionName = liqf2ad7xgi4ewixwvk6qxf5bsevaq7qojvfzu74ruwusvc4ullfonyd.onion(b) , purpose = 0
			System.out.println(TAG + " createConnectionAsyc:: 222222 onionName = " + onionName + " , purpose = " + purpose);
			MailItem mailItem = this.mailList.get(onionName);
			CreateConnectionThread t=new CreateConnectionThread(mailItem,purpose,Constant.CONNECT_RETRY_COUNT);
			t.start();
		}

	}

	/**
	 *
	 * @param onionName
	 * @param purpose
	 * @return
	 */
	public boolean startHandShakeProcess(String onionName, int purpose) {
		System.out.println(TAG + " startHandShakeProcess:: " + onionName + " , " + purpose);
		MailItem mailItem = this.mailList.get(onionName);
		System.out.println(TAG + " startHandShakeProcess:: mailItem: " + mailItem.toString());
//		int result = this.connect(mailItem, purpose);
		ConnectionStatusItem item = this.getConnectionStatusItemByOnionName(onionName, purpose);
		if(item!=null){
			if(item.getShareKey()==null){
				System.out.println(
						TAG + " startHandShakeProcess::  create socket success " + onionName + " , " + purpose);
				LogUtils.d(TAG ," startHandShakeProcess::  CLIENT_PRIVATE_KEY: " + Constant.CLIENT_PRIVATE_KEY);
				ClientTransport client = new ClientTransport(mailItem, Constant.CLIENT_PRIVATE_KEY, item, purpose);
				client.start();
			}
		}else{
			System.out.println(TAG + " startHandShakeProcess:: item is null " + purpose);
		}
//		if (result == 1) {


		return true;

	}


	public boolean createConnection(String onionName, int purpose) {
		System.out.println(TAG + " createConnection:: onionName = " + onionName + " .purpose = " + purpose);
		MailItem mailItem = this.mailList.get(onionName);
		System.out.println(TAG + " createConnection:: mailItem = " + mailItem.toString());
		int result = this.connect(mailItem, purpose);
		ConnectionStatusItem item = this.getConnectionStatusItemByOnionName(onionName, purpose);
		if (result == 1) {//create connection success
			if(item!=null){
				if(item.getShareKey()==null){//not handshake
					System.out.println(TAG + " createConnection:: create socket success onionName = " + onionName + " ,purpose = " + purpose);
					LogUtils.d(TAG, " createConnection:: CLIENT_PRIVATE_KEY: " + Constant.CLIENT_PRIVATE_KEY);
					ClientTransport client = new ClientTransport(mailItem, Constant.CLIENT_PRIVATE_KEY, item, purpose);
					client.start();
				}
			}

			return true;
		}else{
			return  false;
		}

	}

	public void addNewHandShakeMessageHandler(HandShakeMessage item) {
		String onionName = item.getRemoteOnion();
//		HandShakeMessage items = this.handShakeMessageHandlerByOnionHash.get(onionName);
		System.out.println(TAG + " addNewHandShakeMessageHandler:: before add "
				+ this.handShakeMessageHandlerByOnionName.size());
		this.handShakeMessageHandlerByOnionName.put(onionName, item);
		System.out.println(TAG + " addNewHandShakeMessageHandler after add "
				+ this.handShakeMessageHandlerByOnionName.size());
	}

	public void deletehandShakeMessageHandler(HandShakeMessage item) {
		String onionName = item.getRemoteOnion();
		HandShakeMessage items = this.handShakeMessageHandlerByOnionName.get(onionName);
		if (items != null) {
			System.out.println(TAG + " deletehandShakeMessageHandler before delte "
					+ this.handShakeMessageHandlerByOnionName.size());
			this.handShakeMessageHandlerByOnionName.remove(onionName);
			System.out.println(TAG + " deletehandShakeMessageHandler after delte "
					+ this.handShakeMessageHandlerByOnionName.size());
		}
	}

	public void addNewConnection(ConnectionStatusItem item) {
		String onionName = item.getOnionName();
		ConnectionItems items = this.allConByOnionName.get(onionName);
		if (items == null) {

			items = new ConnectionItems();
		}
		System.out.println(TAG + " addNewConnection:: before add items = " + items.toString());
		items.addNewConn(item);
		this.allConByOnionName.put(onionName, items);
		System.out.println(TAG + " addNewConnection:: after add items = " + items.toString());
	}

	public void deleteConnection(ConnectionStatusItem item) {
		if (item.getPurpose() == Constant.SOCKET_PURPOSE_FILE) {
			for (Map.Entry<String, FileTask> entry : this.senderFileMap.entrySet()) {
				FileTask fileTask = entry.getValue();
				if (fileTask.getOnionName().equals(item.getOnionName())) {
					fileTask.setStatus(0);
				}
			}
		}
		this.deleteConnection(item.getOnionName(), item.getSocket());

	}

	public void deleteConnection(String onionName, Socket socket) {
		ConnectionItems items = this.allConByOnionName.get(onionName);
		int flag=0;
		if (items != null) {
			System.out.println(
					"ServerMessageHandler deleteConnection before delte ########## " + items.toString() + " socket " + socket);
			int purpose = items.removeConnBySocket(socket);
			// delete socket success and recheck there exists socket of purpose
			if (purpose > -1) {
				System.out.println("ServerMessageHandler deleteConnection after delte " + items.toString());
				ConnectionStatusItem item = items.getConnByPurpose(purpose);
				if (item == null) {
					System.out.println("ServerMessageHandler deleteConnection after delte there does not exist purpose "
							+ purpose + ", need create connection");
					createConnectionAsyc(onionName, purpose);
					flag=1;
//					if (!this.createConnection(onionName, purpose)) {
//						// create connection failed for max times, offline process
//						this.processOfficeMessage(onionName);
//					}
				}
			} else {
				System.out.println("ServerMessageHandler deleteConnection  socket " + socket + " already delte ");
			}
			System.out.println("deleteConnection current connction number "+items.getConnectionNumber());
			if(flag==0 && items.getConnectionNumber()==0){
				if(purpose>-1)
					this.createConnectionAsyc(onionName, purpose);
				else
					this.createConnectionAsyc(onionName, 0);
			}
		}


	}

	public ConnectionStatusItem getConnectionStatusItem(MessageItem item) {
		String onionName = item.getOnionName();
		ConnectionItems items = this.allConByOnionName.get(onionName);
		if (items == null) {
			return null;
		}
		return items.getConnBySocket(item.getSocket());
	}

	public ConnectionStatusItem getConnectionStatusItemByOnionName(String onionName, int purpose) {
		ConnectionItems items = this.allConByOnionName.get(onionName);
		if (items == null) {
			System.out.println(
					"ServerMessageHandler getConnectionStatusItemByOnionName " + onionName + " not exist conneciton");
			return null;
		}
		return items.getConnByPurpose(purpose);
	}

	public FileTask getFileTaskByFileNameHash(byte[] fileNameHash) {
		for (Map.Entry<String, FileTask> entry : this.senderFileMap.entrySet()) {
//			System.out.println("ServerMessageHandler key " + AESCrypto.bytesToHex(entry.getKey()));
//			System.out.println("ServerMessageHandler key " + AESCrypto.bytesToHex(fileNameHash));
			if(Arrays.equals(entry.getValue().getFileNameHash(),fileNameHash))
				return entry.getValue();
//			if (Arrays.equals(entry.getKey(), fileNameHash))

		}
		return null;
	}



	public int connect(MailItem mailItem, int purpose) {

		int count = 0;
		int status = 0;
		while (count < Constant.CONNECT_RETRY_COUNT) {//3次
			SocketAddress addr = new InetSocketAddress(Constant.TOR_SOCKS_PROXY_SERVER, Constant.TOR_SOCKS_PROXY_PORT);
			Proxy proxy = new Proxy(Proxy.Type.SOCKS, addr);
			Socket socket = new Socket(proxy);
			System.out.println(TAG + " ServerMessageHandler the " + count + " connecting");

			InetSocketAddress dest = new InetSocketAddress(mailItem.getOnionName(), mailItem.getPort());//好友的
			System.out.println(dest);
			try {
				socket.connect(dest, Constant.CONNECT_TIME_OUT);//每次1分钟   ，共耗时3分钟！
				System.out.println(TAG + " connect connect success ");
				System.out.println(TAG + " connect connect remoteOnion " + mailItem.getOnionName());
				System.out.println(TAG + " connect connect port " + mailItem.getPort());
				socket.sendUrgentData(Thread.MAX_PRIORITY);
				System.out.println(Thread.currentThread().getName() + " socket: " + socket);
				System.out.println(TAG + " ServerMessageHandler.connect.ssocket:" + socket.getInetAddress());
				System.out.println(TAG + " ServerMessageHandler.connect.ssocket:" + socket.getInetAddress()
						+ socket.getLocalSocketAddress());
//				if(socket.getInetAddress()==null) {
//					count = count + 1;
//					socket.close();
//					continue;
//				}
				ConnectionStatusItem statusItem = new ConnectionStatusItem(mailItem.getOnionName(),
						mailItem.getOnionHash(), socket, 0, null);
				statusItem.setPurpose(purpose);
				addNewConnection(statusItem);
				HandShakeMessage handShakeMessage = this.handShakeMessageHandlerByOnionName
						.get(mailItem.getOnionName());
				if (handShakeMessage == null) {
					handShakeMessage = new HandShakeMessage(mailItem.getOnionName(), Constant.CLIENT_PRIVATE_KEY,
							mailItem.getPublicKey());
					addNewHandShakeMessageHandler(handShakeMessage);
				}
				status = 1;
				break;
			} catch (IOException e) {
//				System.out.println("connect connect failed");
				System.out.println(TAG + " connect connect failed remoteOnion " + mailItem.getOnionName());
				System.out.println(TAG + " connect connect failed port " + mailItem.getPort());
				System.out.println(TAG + " connect connect failed "+e.getMessage());
				count = count + 1;
				try {
					socket.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
		return status;
	}



	/**
	 * send message to remote peer
	 *
	 * @author
	 *
	 */
	private class Writer extends Thread {

		private Set<Socket> sockets = new HashSet<Socket>();

		private void reput(MessageItem item) {

			if (item.getType() == 0) {
				// handshake message is discard
				return;
			}

			if (item.getPurpose() == Constant.SOCKET_PURPOSE_TEXT) {
				// text message
				try {
					FailedTextMessage  tmp = new FailedTextMessage(item.getOnionName(),item.getRawMessage());
					System.out.println(TAG + " Writer put message into failedqueue "+tmp.toString());
					failedQueue.put(tmp);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			} else {
				// file or pic discard
				System.out.println(TAG + " reput  ");
			}

			if (item.getSocket() != null) {
				if(this.sockets.contains(item.getSocket())){
					if (this.sockets.size()%10==0){
						this.sockets.clear();
					}
				}else {
					this.sockets.add(item.getSocket());
					deleteConnection(item.getOnionName(), item.getSocket());
				}
//				item.setSocket(null);
			}
		}

		@Override
		public void run() {
			int count = 0;
			while (true) {
				Socket socket = null;
				MessageItem item = null;
				try {
					item = sendQueue.poll();
					if (item != null) {
						socket = item.getSocket();
						socket.getOutputStream().write(item.getData());//只发data即finalPayload
						if (count % 100 == 0)
							Thread.sleep(2000);
						count = count + 1;
						System.out.println(TAG + " Writer "+item.getRawMessage()+"  "+item.getSocket()+"  "+item.getPieceID()+"  "+item.getPurpose()+"  "+item.getType()+" "+AESCrypto.bytesToHex(item.getData()));
					} else {
						Thread.sleep(1000);
//						count = 1;
//						System.out.println("Writer kong        ...........");
					}
				} catch (SocketException e) {
					this.reput(item);
					System.out.println(TAG + " Writer SocketException " + e.getMessage());
				} catch (Exception e) {
					System.out.println(TAG + " Writer Exception " + e.getMessage());
					this.reput(item);
				}
//				System.out.println("Writer finall        ...........");
			}
		}

	}

	/**
	 * process recieved data
	 *
	 * @author happywindy
	 *
	 */
	private class Reader extends Thread {
		@Override
		public void run() {
			while (true) {
				try {
					MessageItem item = recieveQueue.poll();
					if (item != null) {
						try {
							if (item.getType() == 0) {//handshake message 0 or data message 1 处理握手包
//								System.out.println("握手"+item.getData());
								handShakeMessageHandle(item);
							} else if (item.getType() == 1) {
								dataMessageHandle(item);//处理实际消息
							} else {
								System.out.println(TAG + " Reader socket status is not valid {0,1}" + item.getType());
							}

						} catch (Exception e) {
							// TODO Auto-generated catch block
//							e.printStackTrace();
							System.out.println(TAG + " Reader error " + e.getMessage());
						}
					} else {
//						Thread.sleep(5000);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.out.println(TAG + " Reader error "+e.getMessage());
				}

			}
		}
	}

	private void updateSocketStatus(MessageItem item, int status, byte[] sharedKey) {
		System.out.println(
				TAG + " updateSocketStatus:: before update status " + status);
		String onionName = item.getOnionName();
		Socket socket = item.getSocket();
		ConnectionItems items = this.allConByOnionName.get(onionName);
		if (items != null) {
			System.out.println(
					TAG + " updateSocketStatus:: before update " + items.toString() );
			items.updateStatus(socket, status, sharedKey);//更新状态和sharedKey
			System.out.println(
					TAG + " updateSocketStatus:: after update " + items.toString() );
		}
		// if this socket has sharedkey
		if (status == 1) {
			// resend text or file messages corresponding to this onionName
			this.tryResend(onionName);
		}

	}

	private  void tryResendAll(){

		for (Map.Entry<String, MailItem> entry : this.mailList.entrySet()) {
			MailItem item = entry.getValue();
			this.tryResend(item.getOnionName());
		}

	}


	private void tryResend(String onionName) {

		System.out.println(TAG + " tryResend:: messages to "+onionName);
		ConnectionStatusItem conn = null;
		FailedTextMessage tmp = null;
		ArrayList<FailedTextMessage> tmpList = new ArrayList<FailedTextMessage>();
		// process text message
		conn = this.getConnectionStatusItemByOnionName(onionName, Constant.SOCKET_PURPOSE_TEXT);
		if (conn != null) {
			while (!this.failedQueue.isEmpty()) {
				tmp = this.failedQueue.poll();
				System.out.println(TAG + " tryResend:: failed messages  "+tmp.toString());
				if (tmp.getOnionName().equals(onionName)) {
					String messageContent= tmp.getRawMessage();
					System.out.println(TAG + " tryResend:: messages  "+messageContent);
					byte[] hell = null;
					try {
						hell = messageContent.getBytes("utf-8");
					} catch (UnsupportedEncodingException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					byte type = 5;

					String messageID = RandomUtil.randomChar(); // TODO: 2021/8/24 更新消息的id，用这个来唯一标记当前次的发送情况
					RecordXOR recordXOR = new RecordXOR();// TODO: 2021/10/5 后期处理 ？？
					byte[] externaltmpPayload = Message.createDataMessageExternalPaylod(type, hell, messageID,recordXOR);


					byte[] finalPayload = Message.packDataPayload(externaltmpPayload, conn.getShareKey());
					MessageItem tmps = new MessageItem(onionName, conn.getSocket(), finalPayload, 1, 0, null);
					tmps.setRawMessage(messageContent);
					System.out.println(TAG + " tryResend:: tryResend send text " + messageContent + " to " + onionName);
					try {
						this.sendQueue.put(tmps);//只发tmps.data
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					tmpList.add(tmp);
					System.out.println(TAG + " tryResend:: tryResend repack text " + tmp.getRawMessage() );
				}
			}

			for (int i = 0; i < tmpList.size(); i++) {
				try {
					this.failedQueue.put(tmpList.get(i));
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			for (int i = 0; i < tmpList.size(); i++) {
				tmpList.remove(i);
			}
			tmpList = null;
		}
		// process fileTask
		conn = this.getConnectionStatusItemByOnionName(onionName, Constant.SOCKET_PURPOSE_FILE);
		if (conn != null && conn.getShareKey()!=null) {
			for (Map.Entry<String, FileTask> entry : this.senderFileMap.entrySet()) {
				FileTask fileTask = entry.getValue();
				if (onionName.equals(fileTask.getOnionName())) {
					int direction = fileTask.getDirection();
					if (direction == 0) {
						if(fileTask.getStatus()==4){

//							String messageID = RandomUtil.randomChar(); // TODO: 2021/8/24 更新消息的id，用这个来唯一标记当前次的发送情况
							String messageID = fileTask.getMessageID();// TODO: 2021/10/27
//							RecordXOR recordXOR = new RecordXOR();// TODO: 2021/10/25 给发送文件增加文件xor指针
							RecordXOR recordXOR = fileTask.getFileTaskRecordXOR();// TODO: 2021/10/27

							System.out.println(TAG + " tryResend:: 重连的fileTask："+fileTask);

							byte[] finalPayload = FileMessage.buildFileMetaMessage(conn.getShareKey(), fileTask.getFileName(),
									fileTask.getTotalSize(), fileTask.getContentHash(),messageID,recordXOR);// TODO: 2021/10/25 给发送文件增加头尾xor指针

							MessageItem tmps = new MessageItem(onionName, conn.getSocket(), finalPayload, 1, 1, null);
							try {
								sendQueue.put(tmps);
								System.out.println(TAG + " tryResend:: handleFileMessageSend send file filename " + fileTask.getFileName());
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}else{
							fileTask.setStatus(1);
							// send file
							System.out.println(TAG + " tryResend:: " + fileTask.getFileName() + " to " + onionName);

							String messageID = fileTask.getMessageID(); // TODO: 2021/8/24 更新消息的id，用这个来唯一标记当前次的发送情况
							this.resend(fileTask, conn,messageID);
						}

					} else {
						if (fileTask.getStatus() == 0) {
							// recieve file
							System.out.println(TAG + " ryResend retransfer siganl of file " + fileTask.getFileName() + " to "
									+ onionName);
							byte[] sharedKey = conn.getShareKey();
							byte[] messageHash = fileTask.getFileNameHash();
							int ackMessageType = 2;
							String ack = Constant.DATA_CK_CONTENT;
							int pieceNumber = fileTask.getFileTransferStatusMap().size();
							System.out.println(
									TAG + " FileName: " + fileTask.getFileName() + " tryResend already recieve piece number is " + pieceNumber);

							//							String messageID = RandomUtil.randomChar(); // TODO: 2021/8/24 更新消息的id，用这个来唯一标记当前次的发送情况
							String messageID = fileTask.getMessageID();// TODO: 2021/10/27
//							RecordXOR recordXOR = new RecordXOR();// TODO: 2021/10/25 给发送文件增加文件xor指针
							RecordXOR recordXOR = fileTask.getFileTaskRecordXOR();// TODO: 2021/10/27

							System.out.println(TAG + " 重连的fileTask："+fileTask);

							byte[] finalPayload = DataMessage.buildACKDataMessage(sharedKey, ackMessageType,
									pieceNumber, ack, messageHash,messageID,recordXOR);

							MessageItem tmps = new MessageItem(onionName, conn.getSocket(), finalPayload, 1, 1, null);
							try {
								sendQueue.put(tmps);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							fileTask.setStatus(3);
						} else {
							System.out.println(TAG + " tryResend:: already send retransfer siganl of file " + fileTask.getFileName()
									+ " to " + onionName);
						}
					}
				}
			}
		}else{
			System.out.println(TAG + " tryResend:: conn or sharkey is null "+conn);
		}

	}

	private void updateConnectionPurpose(MessageItem item, int purpose) {
		String onionName = item.getOnionName();
		Socket socket = item.getSocket();
		ConnectionItems items = this.allConByOnionName.get(onionName);
		if (items != null) {
			System.out.println(TAG + " updateConnectionPurpose:: before update " + items.toString()
					+ " status" + purpose);
			items.updateConnectionPurpose(socket, purpose);
			System.out.println(TAG + " updateConnectionPurpose:: after update " + items.toString()
					+ " status" + purpose);
		}
	}

	private boolean sendMessage(Socket socket, byte[] data) {//发送数据到网络流，给对方的
		try {
			socket.getOutputStream().write(data);
			return true;
		} catch (SocketException e) {
			System.out.println(TAG + " sendMessage:: SocketException " + e.getMessage());
			try {
				socket.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				return false;
			}
			// offline process
			return false;
//			return this.handleTextMessageOfflineManner(messageContent,remoteOnion,remotePort);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	private void handShakeMessageHandle(MessageItem item) {//处理握手包
		byte[] externalPayload = item.getData();
		System.out.println(TAG +" handShakeMessageHandle:: 判断握手解密"+Arrays.toString(externalPayload));
//		System.out.println("握手"+AESCrypto.bytesToHex(externalPayload));
		HandShakeMessage handShakeMessage = item.getHandShakeMessage();
		byte[] byteMessageType = Message.subBytes(externalPayload, Constant.BYTE_MESSAGE_TYPE_BEGIN,
				Constant.BYTE_MESSAGE_TYPE);
		int messageType = Integer.parseInt(AESCrypto.bytesToHex(byteMessageType), 16);
		byte[] internalPayload = Message.parseExternalPaylod(externalPayload);


		if (Constant.SESSION_REQUEST == messageType) {//类型1

			byte[] startXORFileName = Message.parseFirstHandShakeStartXORFileName(externalPayload);// TODO: 2021/10/4  对方发来要我确认的第一次握手的开始xor文件名
			byte[] startXORIndex = Message.parseFirstHandShakeStartXORIndex(externalPayload);//解析开始位置

			int friendStartXORFileName = Integer.parseInt(AESCrypto.bytesToHex(startXORFileName),16);//恢复原样,好友当前能用的开始文件名
			int friendStartXORIndex = Integer.parseInt(AESCrypto.bytesToHex(startXORIndex),16);//好友当前能用的开始文件的（距离文件尾的）位置
			Log.i(TAG + " handShakeMessageHandle:: 握手xor文件名他",""+friendStartXORFileName);//好友的文件
			Log.i(TAG + "handShakeMessageHandle:: 握手xor文件位置他",""+friendStartXORIndex);//好友的位置

			RecordXOR myRecordStartXOR = XORutil.getStartXOR( context);//获取自己当前能用的开始文件和位置
			int myStartXORFileName = myRecordStartXOR.getStartFileName();//自己的文件
			int myStartXORIndex = myRecordStartXOR.getStartFileIndex();//自己的位置
			Log.i(TAG + " handShakeMessageHandle:: 握手xor文件名我",""+myStartXORFileName);
			Log.i(TAG + " handShakeMessageHandle:: 握手xor文件位置我",""+myStartXORIndex);

			int commonStartXORFileName = XORutil.commonStartXORFileName(friendStartXORFileName,myStartXORFileName);// TODO: 2021/10/4 共同的开始文件

			int commonStartXORIndex = XORutil.compareKeyIndex(friendStartXORFileName , friendStartXORIndex, myStartXORFileName, myStartXORIndex);//获取共同开始的位置
			Log.i(TAG + " handShakeMessageHandle:: 握手xor文件名共同",""+commonStartXORFileName);
			Log.i(TAG + " handShakeMessageHandle:: 握手xor文件位置共同",""+commonStartXORIndex);

			commonRecordXOR.setStartFileName(commonStartXORFileName);
			commonRecordXOR.setStartFileIndex(commonStartXORIndex);
			commonRecordXOR.setEndFileName(commonStartXORFileName);
			commonRecordXOR.setEndFileIndex(commonStartXORIndex);

			SharedPreferencesUtil.saveCommonRecordXOR(context,commonRecordXOR);// TODO: 2021/10/26 保存全局文件xor异或指针



			System.out.println(TAG + " handShakeMessageHandle:: Recieve SESSION_REQUEST");
			updateSocketStatus(item, 0, null);//更新连接状态
			
			byte[] startFileNameAndIndex = XORutil.xorFile2Byte(commonStartXORFileName,commonStartXORIndex);//按设计的大小合并文件名和位置

			byte[] finalPayload = handShakeMessage.createSessionAuthMessage("c",startFileNameAndIndex);// TODO: 2021/10/4 增加xor字段 //创建会话身份验证消息 ，2
			if (!this.sendMessage(item.getSocket(), finalPayload)) {//发送2的
				System.out.println(TAG + " handShakeMessageHandle:: send SESSION_AUTH failed to "
						+ item.getOnionName());
				if (item.getSocket() != null)
					this.deleteConnection(item.getOnionName(), item.getSocket());
			} else {
				System.out.println(TAG + " handShakeMessageHandle:: send SESSION_AUTH success to "
						+ item.getOnionName());
			}

		} else if (Constant.SESSION_AUTH == messageType) {//类型2

			byte[] startXORFileName = Message.parseStartXORFileName(externalPayload);// TODO: 2021/10/4  对方发来已确认的第一次握手的开始xor文件名
			byte[] startXORIndex = Message.parseStartXORIndex(externalPayload);//解析开始位置
			int startXORFileNameInt = Integer.parseInt(AESCrypto.bytesToHex(startXORFileName),16);//恢复原样,当前能用的开始文件名
			int startXORIndexInt = Integer.parseInt(AESCrypto.bytesToHex(startXORIndex),16);//当前能用的开始文件的（距离文件尾的）位置

			Log.i(TAG + " handShakeMessageHandle:: 握手xor文件名解析",""+startXORFileNameInt);
			Log.i(TAG + " handShakeMessageHandle:: 握手xor文件位置解析",""+startXORIndexInt);

			commonRecordXOR.setStartFileName(startXORFileNameInt);
			commonRecordXOR.setStartFileIndex(startXORIndexInt);
			commonRecordXOR.setEndFileName(startXORFileNameInt);
			commonRecordXOR.setEndFileIndex(startXORIndexInt);

			SharedPreferencesUtil.saveCommonRecordXOR(context,commonRecordXOR);// TODO: 2021/10/26 保存全局文件xor异或指针


			System.out.println(TAG + " handShakeMessageHandle:: Recieve SESSION_AUTH");
			boolean result = HandShakeMessage.parseSessionAuthPaylod(internalPayload);//解析会话身份验证Paylod
			if (result) {
				System.out.println(result);
				String myPassword = "c";
				byte[] sharedKey = AESCrypto.paddingToLength(new byte[0], Constant.SHAREDKEY_LENGTH);

				byte[] finalPayload = handShakeMessage.createSessionExchangeMessage(myPassword, sharedKey);//创建会话交换消息，3

				if (!this.sendMessage(item.getSocket(), finalPayload)) {//发送3的
					System.out.println(TAG + " handShakeMessageHandle::  send SESSION_EXCHANGE failed to "
							+ item.getOnionName());
					if (item.getSocket() != null)
						this.deleteConnection(item.getOnionName(), item.getSocket());
				} else {
					System.out.println(TAG + " handShakeMessageHandle::  send SESSION_EXCHANGE success to "
							+ item.getOnionName());
					updateSocketStatus(item, 0, sharedKey);//更新连接状态
				}
			} else {
				System.out.println(TAG + " handShakeMessageHandle:: Session Auth Failed");
				// delete corresponding socket mapping items
			}

		} else if (Constant.SESSION_EXCHANGE == messageType) {//类型3
			System.out.println(TAG + " handShakeMessageHandle:: Recieve SESSION_EXCHANGE");
			byte[] sharedKey = HandShakeMessage.parseSessionExchangePaylod(internalPayload);//解析会话交换Paylod

			byte[] finalPayload = handShakeMessage.createSessionDoneMessage();//创建会话完成消息，4
			if (!this.sendMessage(item.getSocket(), finalPayload)) {//发送4的
				System.out.println(TAG + " handShakeMessageHandle:: send SESSION_DONE failed to "
						+ item.getOnionName());
				if (item.getSocket() != null)
					this.deleteConnection(item.getOnionName(), item.getSocket());
			} else {
				System.out.println(TAG + " handShakeMessageHandle::  send SESSION_DONE success to "
						+ item.getOnionName());
				updateSocketStatus(item, 1, sharedKey);//更新连接状态

				Intent intent = new Intent();
				intent.setAction(Constant.TOR_BROAD_CAST_ACTION);
				intent.putExtra(Constant.TOR_BROAD_CAST_INTENT_KEY, Constant.START_COMMUNICATION_SUCCESS);
				intent.setComponent(new ComponentName(Constant.PACKAGE,Constant.TOR_BROAD_CAST_PATH));
				context.sendBroadcast(intent);
				Log.d(TAG ," handShakeMessageHandle:: connect ready");

			}

		} else if (Constant.SESSION_DONE == messageType) {//类型4
			System.out.println(TAG + " handShakeMessageHandle:: Recieve SESSION_DONE");
			if (internalPayload.length == 0) {
				updateSocketStatus(item, 1, null);//更新连接状态
				Intent intent = new Intent();
				intent.setAction(Constant.TOR_BROAD_CAST_ACTION);
				intent.putExtra(Constant.TOR_BROAD_CAST_INTENT_KEY, Constant.START_COMMUNICATION_SUCCESS);
				intent.setComponent(new ComponentName(Constant.PACKAGE,Constant.TOR_BROAD_CAST_PATH));
				context.sendBroadcast(intent);//更新界面连接状态
				Log.d(TAG + " handShakeMessageHandle:: ServerMessageHandler", " connect ready");

				MailItem mitem = null;
				for (Map.Entry<String, MailItem> entry : this.mailList.entrySet()) {
//					System.out.println("ServerMessageHandler key " + entry.getKey());
//					System.out.println("ServerMessageHandler value " + entry.getValue());
					mitem = entry.getValue();
					break;

				}
//				handleTextMessageSend("���000000000", mitem.getOnionName(), mitem.getPort());
				String filePath = "C:\\Users\\happywindy\\Documents\\Tencent Files\\838913592\\FileRecv\\DarkGo\\DarkGo\\FirefoxPortable.exe";
////					filePath="C:\\Users\\happywindy\\Documents\\WeChat Files\\wxid_tl0jut38w16m21\\FileStorage\\File\\2021-07\\SanAuth����.zip";
////					filePath="C:\\Users\\happywindy\\Desktop\\raspery\\meek\\win10-64bit-meek-tbb909-003.pcap.pcapng";
//
//				filePath = "C:\\Users\\happywindy\\Desktop\\raspery\\meek\\win10-64bit-meek-tbb909-002.pcap.pcapng";
//				filePath = "C:\\Users\\happywindy\\Videos\\WeChat_20210706152126.mp4";
//				filePath = "C:\\Users\\happywindy\\Downloads\\mz.war";
//				handleFileMessageSend(filePath, mitem.getOnionName(), mitem.getPort());

			} else {
				System.out.println(TAG + " handShakeMessageHandle:: session done message internalpayload length is not 0");
			}
		}
	}

	public int handleTextMessageSend(String messageContent, String remoteOnion, int remotePort,String messageID) {
		LogUtils.d(TAG, " handleTextMessageSend:: remoteOnion: " + remoteOnion);
		Log.d(TAG, " handleTextMessageSend:: messageContent = " + messageContent);

		ConnectionStatusItem statusItem = getConnectionStatusItemByOnionName(remoteOnion, Constant.SOCKET_PURPOSE_TEXT);
		if (statusItem == null) {
			System.out.println(TAG + " handleTextMessageSend:: socket not find in mapping list");
			this.createConnectionAsyc(remoteOnion, Constant.SOCKET_PURPOSE_TEXT);
		}
		if (statusItem.getShareKey() == null) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (statusItem.getShareKey() == null) {
				// offline process
				return this.handleTextMessageOfflineManner(messageContent, remoteOnion, remotePort, messageID);//转为离线发送
			}
		}
		byte[] hell = null;
		byte[] jniHellByteArray = null;

		System.out.println(TAG + " handleTextMessageSend:: 发送文本异或的指针前commonRecordXOR："+commonRecordXOR);

		RecordXOR recordXOR = new RecordXOR();
		recordXOR.setStartFileName(commonRecordXOR.getEndFileName());//硬拷贝，commonRecordXOR记录了最新使用xor文件的情况。应该从结束文件开始xor新的一次消息
		recordXOR.setStartFileIndex(commonRecordXOR.getEndFileIndex());
		recordXOR.setMessageID(messageID);

		try {
			hell = messageContent.getBytes("utf-8");//文本转为byte
			System.out.println(TAG + " handleTextMessageSend:: 未加密的文本字节16进制："+AESCrypto.bytesToHex(hell));

			//jni算法加密文本
			jniHellByteArray = ServiceLoaderImpl.load(IEntry.class).entry("++++",null,hell);
			//hell = FileTask.TextXOR(hell,recordXOR);// TODO: 2021/10/4 增加 // TODO: 2021/9/23 文本也采用异或
			System.out.println(TAG + " handleTextMessageSend:: jni算法加密的文本字节16进制："+AESCrypto.bytesToHex(jniHellByteArray));

			commonRecordXOR.setStartFileName(recordXOR.getStartFileName());//硬拷贝,在这里就修改全局变量指针了，也没考虑对方有没有收到
			commonRecordXOR.setStartFileIndex(recordXOR.getStartFileIndex());
			commonRecordXOR.setEndFileName(recordXOR.getEndFileName());
			commonRecordXOR.setEndFileIndex(recordXOR.getEndFileIndex());
			System.out.println(TAG + " handleTextMessageSend:: 发送文本异或的指针："+recordXOR);
			System.out.println(TAG + " handleTextMessageSend:: 发送文本异或的指针后commonRecordXOR："+commonRecordXOR);
			recordXORs.add(recordXOR);//保存全部信息，方便下次查找后删除xor文件

			SharedPreferencesUtil.saveCommonRecordXOR(context,commonRecordXOR);// TODO: 2021/10/26 保存全局文件xor异或指针


		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return 0;
		} catch (IOException e) {
			e.printStackTrace();
		}

		byte type = 5;//文本是指定为5的类型
		// TODO: 2021/10/5 增加jni加密文件的使用信息 //组装
		byte[] externaltmpPayload = Message.createDataMessageExternalPaylod(type, jniHellByteArray, messageID,recordXOR);
		//byte[] externaltmpPayload = Message.createDataMessageExternalPaylod(type, hell, messageID,recordXOR);// TODO: 2021/10/5 增加xor文件的使用信息 //组装
		System.out.println(TAG + " handleTextMessageSend:: 已发送的消息"+Arrays.toString(externaltmpPayload));
		byte[] finalPayload = Message.packDataPayload(externaltmpPayload, statusItem.getShareKey());//加密
		MessageItem tmps = new MessageItem(remoteOnion, statusItem.getSocket(), finalPayload, 1, 0, null);
		tmps.setRawMessage(messageContent);//Raw：未经加工的
		try {
			sendQueue.put(tmps);
			System.out.println(TAG + " handleTextMessageSend::  发送消息" + messageContent+" time :"+new Date().toString());
		} catch (InterruptedException e) {
			e.printStackTrace();
			return 0;
		}
		return 1;
	}

	public int handleTextMessageOfflineManner(String messageContent, String remoteOnion, int remotePort,String messageID) {
		/*********/

		System.out.println(TAG + " handleTextMessageOfflineManner:: " + messageContent + " to " + remoteOnion);
		String from = DigestUtils.sha256Hex(this.mySelfBean.getOnionName().replace(".onion", "")); //M
		String to = DigestUtils.sha256Hex(remoteOnion.replace(".onion", ""));
		System.out.println(TAG + " handleTextMessageOfflineManner:: send offline text from = " + this.mySelfBean.getOnionName() + " to =" + remoteOnion + " text = " + messageContent);
		sendOfflineText sendOfflineText = new sendOfflineText(from, to, messageContent,this.channelServer, messageID);
		sendOfflineText.start();

		return 0;
	}

	public int handleFileMessageSend(String fileFullPath, String remoteOnion, int remotePort,String messageID) {//发文件
		ConnectionStatusItem statusItem = getConnectionStatusItemByOnionName(remoteOnion, Constant.SOCKET_PURPOSE_FILE);
		if (statusItem == null) {
			System.out.println(TAG + " handleFileMessageSend:: socket not find in mapping list");
//			this.createConnectionAsyc(remoteOnion, Constant.SOCKET_PURPOSE_FILE);
//			if (!) {
//				// offline process
//				return 0;
//			} else {
			// online process
			statusItem = getConnectionStatusItemByOnionName(remoteOnion, Constant.SOCKET_PURPOSE_TEXT);
//			}
		}

		RecordXOR recordXOR = new RecordXOR();// TODO: 2021/10/25 给发送文件增加文件xor指针
		recordXOR.setStartFileName(commonRecordXOR.getEndFileName());//硬拷贝，commonRecordXOR记录了最新使用xor文件的情况。应该从结束文件开始xor新的一次消息
		recordXOR.setStartFileIndex(commonRecordXOR.getEndFileIndex());
		recordXOR.setMessageID(messageID);

		FileTask fileTask = new FileTask(remoteOnion, fileFullPath, 0, recordXOR );// TODO: 2021/10/25 给发送文件增加文件xor指针
		fileTask.setStartTime(new Date());
		fileTask.setMessageID(messageID);

		commonRecordXOR.setStartFileName(recordXOR.getStartFileName());//硬拷贝,在这里就修改全局变量指针了，也没考虑对方有没有收到
		commonRecordXOR.setStartFileIndex(recordXOR.getStartFileIndex());
		commonRecordXOR.setEndFileName(recordXOR.getEndFileName());
		commonRecordXOR.setEndFileIndex(recordXOR.getEndFileIndex());
		System.out.println(TAG + " handleFileMessageSend:: 发送文本异或的指针："+recordXOR);
		recordXORs.add(recordXOR);//保存全部信息，方便下次查找后删除xor文件

		SharedPreferencesUtil.saveCommonRecordXOR(context,commonRecordXOR);// TODO: 2021/10/26 保存全局文件xor异或指针


		if(statusItem!=null){
			byte[] finalPayload = FileMessage.buildFileMetaMessage(statusItem.getShareKey(), fileTask.getFileName(),
					fileTask.getTotalSize(), fileTask.getContentHash(),messageID,recordXOR);// TODO: 2021/10/25 给发送文件增加头尾xor指针 // TODO: 2021/8/25 增加动态消息id //组装报文

			System.out.println(TAG + " handleFileMessageSend:: 哈哈哈哈哈哈哈哈哈哈"+new String(finalPayload));

			MessageItem tmps = new MessageItem(remoteOnion, statusItem.getSocket(), finalPayload, 1, 1, null);
			try {
				sendQueue.put(tmps);
				System.out.println(TAG + " handleFileMessageSend:: send file filename " + fileFullPath);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}else{
			// not send file meta message
			fileTask.setStatus(4);
		}
		this.senderFileMap.put(fileTask.getFileName(), fileTask);
		System.out.println(TAG + " handleFileMessageSend:: FileMessageHandler filename hash " + AESCrypto.bytesToHex(fileTask.getFileNameHash()));
		return 1;
	}



	public int handleByteMessageSend(String fileFullPath,byte[] bitmapBytes, String remoteOnion, int remoteOnionPort,String messageID ){// TODO: 2021/8/26 增加消息id。在线发送byte图片 // TODO: 2021/8/12 专门在线发送byte

		ConnectionStatusItem statusItem = getConnectionStatusItemByOnionName(remoteOnion, Constant.SOCKET_PURPOSE_FILE);
		if (statusItem == null) {
			System.out.println(TAG + " handleByteMessageSend:: socket not find in mapping list");
//			this.createConnectionAsyc(remoteOnion, Constant.SOCKET_PURPOSE_FILE);
//			if (!) {
//				// offline process
//				return 0;
//			} else {
			// online process
			statusItem = getConnectionStatusItemByOnionName(remoteOnion, Constant.SOCKET_PURPOSE_TEXT);
//			}
		}

		RecordXOR recordXOR = new RecordXOR();// TODO: 2021/10/25 给发送文件增加文件xor指针
		recordXOR.setStartFileName(commonRecordXOR.getEndFileName());//硬拷贝，commonRecordXOR记录了最新使用xor文件的情况。应该从结束文件开始xor新的一次消息
		recordXOR.setStartFileIndex(commonRecordXOR.getEndFileIndex());
		recordXOR.setMessageID(messageID);


		FileTask fileTask = new FileTask(remoteOnion, fileFullPath, 0,bitmapBytes,recordXOR);// TODO: 2021/10/25 记录使用的文件xor指针
		fileTask.setStartTime(new Date());
		fileTask.setMessageID(messageID);

		commonRecordXOR.setStartFileName(recordXOR.getStartFileName());//硬拷贝,在这里就修改全局变量指针了，也没考虑对方有没有收到
		commonRecordXOR.setStartFileIndex(recordXOR.getStartFileIndex());
		commonRecordXOR.setEndFileName(recordXOR.getEndFileName());
		commonRecordXOR.setEndFileIndex(recordXOR.getEndFileIndex());
		System.out.println(TAG + " handleByteMessageSend::发送文本异或的指针："+recordXOR);
		recordXORs.add(recordXOR);//保存全部信息，方便下次查找后删除xor文件

		SharedPreferencesUtil.saveCommonRecordXOR(context,commonRecordXOR);// TODO: 2021/10/26 保存全局文件xor异或指针


		if(statusItem!=null){

//			String messageID = RandomUtil.randomChar(); // TODO: 2021/8/24 更新消息的id，用这个来唯一标记当前次的发送情况
			byte[] finalPayload = FileMessage.buildFileMetaMessage(statusItem.getShareKey(), fileTask.getFileName(),
					fileTask.getTotalSize(), fileTask.getContentHash(),messageID,recordXOR);// TODO: 2021/10/25 给发送文件增加头尾xor指针

			MessageItem tmps = new MessageItem(remoteOnion, statusItem.getSocket(), finalPayload, 1, 1, null);
			try {
				sendQueue.put(tmps);
				System.out.println(TAG + " handleByteMessageSend::send file filename " + fileFullPath);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}else{
			// not send file meta message
			fileTask.setStatus(4);
		}
		this.senderFileMap.put(fileTask.getFileName(), fileTask);
		System.out.println(TAG + " handleByteMessageSend::filename hash " + AESCrypto.bytesToHex(fileTask.getFileNameHash()));
		return 1;



	}


	private RecordXOR getRecordXOR(byte[] externalPayload,String messageID ){// TODO: 2021/10/24 解析本次消息的异或指针，改变commonRecordXOR的全局指针


		byte[] byteStartXORFileName = Message.parseStartXORFileName(externalPayload);//xor开始文件
		byte[] byteStartXORIndex = Message.parseStartXORIndex(externalPayload);//开始位置
		byte[] byteEndXORFileName = Message.parseEndXORFileName(externalPayload);//xor结束文件
		byte[] byteEndXORIndex = Message.parseEndXORIndex(externalPayload);//结束位置

		int startXORFileName = Integer.parseInt(AESCrypto.bytesToHex(byteStartXORFileName),16);//恢复原样
		int startXORIndex = Integer.parseInt(AESCrypto.bytesToHex(byteStartXORIndex),16);//恢复原样
		int endXORFileName = Integer.parseInt(AESCrypto.bytesToHex(byteEndXORFileName),16);//恢复原样
		int endXORIndex = Integer.parseInt(AESCrypto.bytesToHex(byteEndXORIndex),16);//恢复原样
		RecordXOR recordXOR = new RecordXOR(startXORFileName,startXORIndex,endXORFileName,endXORIndex,messageID);
		System.out.println(TAG + " getRecordXOR:: 收到消息异或的指针："+recordXOR);

		XORutil.addRecordXOR(recordXORs,recordXOR);// TODO: 2021/10/6 将recordXOR加入到recordXORs中，若存在就不加入，一切以recordXORs中的为准
		recordXOR = XORutil.getRecordXOR(recordXORs, messageID);// TODO: 2021/10/6 从recordXORs获取 messageID的recordXOR

//		commonRecordXOR = recordXOR;//方便下次发消息的起点，这里直接更新有不同步的bug
		commonRecordXOR = XORutil.changecommonRecordXOR(commonRecordXOR,recordXOR);// TODO: 2021/10/24 新的方法
		SharedPreferencesUtil.saveCommonRecordXOR(context,commonRecordXOR);// TODO: 2021/10/26 保存全局文件xor异或指针


		return recordXOR;
	}



	private void dataMessageHandle(MessageItem item) throws UnsupportedEncodingException {//解析收到的不同类型的消息
		byte[] externalPayload = item.getData();//已解密的文本！！
		System.out.println(TAG + " dataMessageHandle:: 已解密的消息"+Arrays.toString(externalPayload));
//		HandShakeMessage handShakeMessage = item.getHandShakeMessage();
		ConnectionStatusItem statusItem = getConnectionStatusItem(item);
		if (statusItem == null) {
			System.out.println(TAG + " dataMessageHandle:: socket not find in mapping list");
			return;
		}else {
			System.out.println(TAG + " dataMessageHandle:: sharekey "+AESCrypto.bytesToHex(statusItem.getShareKey()));
		}

		byte[] byteUtcTimestamp = Message.subBytes(externalPayload, Constant.BYTE_APPLICATION_ID,
				Constant.BYTE_UTC_TIMESTAMP);
		long timel = Long.parseLong(AESCrypto.bytesToHex(byteUtcTimestamp), 16);


		byte[] byteMessageID = Message.subBytes(externalPayload, Constant.BYTE_MESSAGE_TIMESTAMP_BEGIN,
				Constant.BYTE_MESSAGE_NO);// TODO: 2021/8/24  提取得到消息id

		String oldmessageID = new String(byteMessageID);// TODO: 2021/10/25 旧的解析 /真实的消息id
		System.out.println(TAG + " dataMessageHandle:: 接收的messageID： "+oldmessageID);

		String messageID = String.valueOf(Integer.parseInt(AESCrypto.bytesToHex(byteMessageID), 16));// TODO: 2021/10/25 新的解析方式 //将以16进制数的“String s”转为十进制数
		System.out.println(TAG + " dataMessageHandle:: 接收的messageID新解析int： "+messageID);


		byte[] byteMessageType = Message.subBytes(externalPayload, Constant.BYTE_MESSAGE_TYPE_BEGIN,
				Constant.BYTE_MESSAGE_TYPE);//得到类型  从第10字节开始，取到Constant.BYTE_MESSAGE_TYPE即1字节
		int messageType = Integer.parseInt(AESCrypto.bytesToHex(byteMessageType), 16);//将以16进制数的“String s”转为十进制数
		System.out.println(TAG + " dataMessageHandle:: 接收的类型： "+messageType);

		byte[] internalPayload = Message.parseExternalPaylod(externalPayload);//解析文本内容。！！



		if (Constant.DATA_MESSAGE == messageType) {//收到文本消息 对应5
			System.out.println(TAG + " dataMessageHandle:: Recieve DATA_MESSAGE");
			RecordXOR recordXOR =  getRecordXOR(externalPayload,messageID);// TODO: 2021/10/24 解析本次消息的异或指针，改变commonRecordXOR的全局指针

			try {

                //jni算法解密文本
				internalPayload = ServiceLoaderImpl.load(IDecry.class).decry("++++",null,internalPayload);
				Log.d(TAG, " dataMessageHandle:: jni解密 internalPayload[] = " + AESCrypto.bytesToHex(internalPayload));

				//internalPayload = FileTask.TextXOR(internalPayload, recordXOR);// TODO: 2021/10/4 增加 // TODO: 2021/9/23  再次异或解密
				String reply = new String(internalPayload, "utf-8");// TODO: 2021/8/23 这里就直接是原文了
				Intent intent = new Intent();
				intent.setAction(Constant.TOR_BROAD_CAST_ACTION);
				intent.putExtra(Constant.TOR_BROAD_CAST_INTENT_KEY, Constant.HAS_RECEIVED_MESSAGE);//收到文本消息
				intent.putExtra("Message", reply);
				intent.putExtra("PeerHostname",statusItem.getOnionName());// TODO: 2021/8/24 改为statusItem.getOnionName() 
				intent.putExtra("MessageID", messageID);// TODO: 2021/8/24 消息id
				intent.setComponent(new ComponentName(Constant.PACKAGE,Constant.TOR_BROAD_CAST_PATH));
				context.sendBroadcast(intent);//发广播
				System.out.println(TAG + " dataMessageHandle:: Recive text replyMessage =  " + reply + " replyOnion = " +statusItem.getOnionName() );

			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			byte[] sharedKey = statusItem.getShareKey();
			byte[] messageHash = AESCrypto.digest_fast(internalPayload);
			int ackMessageType = 0;
			String ack = Constant.DATA_ACK_CONTENT;//ok
			int pieceID = 1;
			byte[] payload = DataMessage.buildACKDataMessage(sharedKey, ackMessageType, pieceID, ack, messageHash,messageID,recordXOR);// TODO: 2021/10/25 在ack增加xor异或头尾指针 // TODO: 2021/8/24 加入文本消息的id //组装ACK报文

			if (!this.sendMessage(statusItem.getSocket(), payload)) {//给发送方发ack//发送messageType = 12的ack
				System.out.println(
						TAG + " dataMessageHandle:: send DATA_ACK failed to " + item.getOnionName());
				if (statusItem.getSocket() != null)
					this.deleteConnection(item.getOnionName(), statusItem.getSocket());
			} else {
				System.out.println(
						TAG + " dataMessageHandle:: send DATA_ACK success to " + item.getOnionName());

				XORutil.deleteRecordXORandXORFile(recordXORs,messageID);// TODO: 2021/10/6  可以删除recordXORs中的recordXOR和已用过的XORFile了

			}
//			handleTextMessageSend("你好000000000", statusItem.getOnionName(), 6677);

		} else if (Constant.DATA_FILE_META == messageType) {// 6 ，接收方收到文件消息的第一步，只是通知，让接收方准备好接下来的文件接收环境

			RecordXOR recordXOR =  getRecordXOR(externalPayload,messageID);// TODO: 2021/10/24 解析本次消息的异或指针，改变commonRecordXOR的全局指针

			updateConnectionPurpose(item, Constant.SOCKET_PURPOSE_FILE);//标记为文件接收
			System.out.println(TAG + " dataMessageHandle:: Recieve DATA_FILE_META");
			FileTask fileTask = FileMessage.parseFileMetaMessage(item.getOnionName(), internalPayload);
//			String key = new String(fileTask.getFileNameHash());

			byte[] finalPayload = FileMessage.buildFileReadyMessage(statusItem.getShareKey(),
					fileTask.getFileNameHash(),messageID,recordXOR);// TODO: 2021/8/25 增加消息id //构建ACK报文，告诉发送方，我收到了要接收文件的通知

			if (!this.sendMessage(statusItem.getSocket(), finalPayload)) {//发送messageType = 7的ack
				System.out.println(
						TAG + " dataMessageHandle::  send DATA_FILE_READY failed to " + item.getOnionName());
				if (item.getSocket() != null)
					this.deleteConnection(item.getOnionName(), statusItem.getSocket());
			} else {
				System.out.println(TAG + " dataMessageHandle:: send DATA_FILE_READY success to "
						+ item.getOnionName());
				fileTask.setStatus(0);
				this.senderFileMap.put(fileTask.getFileName(), fileTask);//备后面接收查询用

				Event.FileMetaMessage mess = new Event.FileMetaMessage(fileTask.getFileName(), fileTask.getTotalSize());
				Gson gson = new Gson();
				String messJson = gson.toJson(mess);
//							Log.d("FILE_MESSAGE111" , " messJson = " + messJson +"\ttmpNumber"+tmpNumber+"\t getTotalPieceNumber "+fileTask.getTotalPieceNumber());
				Log.d(TAG , " dataMessageHandle:: messJson = "  + messJson);
				EventBus.getDefault().post(new Event(Event.RECIEVE_ONLINE_FILE, messJson, messageID));// TODO: 2021/8/25 改为messageID 方便更新

//				Event.FileMetaMessage mess = new Event.FileMetaMessage(fileTask.getFileName(),fileTask.getTotalSize());
//				Gson gson = new Gson();
//				String messJson = gson.toJson(mess);
//				Log.d("FILE_MESSAGE777777" , " messJson = " + messJson );
//				EventBus.getDefault().post(new Event(Event.RECIEVE_ONLINE_FILE, messJson, fileTask.getOnionName()));
			}

		} else if (Constant.DATA_FILE_READY == messageType) { // 7,这里发送方知道 接收方收到了要开始接收文件的通知。然后发送方在这里将一个个分片发送出去

			RecordXOR recordXOR =  getRecordXOR(externalPayload,messageID);// TODO: 2021/10/24 解析本次消息的异或指针，改变commonRecordXOR的全局指针

			updateConnectionPurpose(item, Constant.SOCKET_PURPOSE_FILE);
			System.out.println(TAG + " dataMessageHandle:: Recieve FLIE_READY");
			byte[] fileNameHash = FileMessage.parseFileReadyMessage(internalPayload);
			FileTask fileTask = getFileTaskByFileNameHash(fileNameHash);
			if (fileTask == null) {
				System.out.println(TAG + " dataMessageHandle:: FileMessageHandler recieved filename hash not in our list "
						+ AESCrypto.bytesToHex(fileNameHash));
				return;
			}
			fileTask.setStatus(1);//设置状态为转移中
			byte[] sharedKey = statusItem.getShareKey();
			fileTask.setStartTime(new Date());
			ConcurrentHashMap<Integer, byte[]> filePieceContentMap = fileTask.getFilePieceContentMap();//获取发送文件的一个个分片

			ConcurrentHashMap<Integer, RecordXOR> filePieceRecordXORMap = fileTask.getFilePieceRecordXORMap();// TODO: 2021/10/25 记录每一个文件分片的xor指针

			int totalPieceNumber = fileTask.getTotalPieceNumber();
			System.out.println(TAG + " dataMessageHandle:: fileName" + fileTask.getFileName() + " total pieceNumber " + totalPieceNumber);
			byte[] fileContent = null;
			byte[] payload = null;
			MessageItem tmp = null;

			RecordXOR pieceRecordXOR = new RecordXOR();//每一个分片的指针

			for (int i = 0; i < totalPieceNumber; i++) {//发送方在这里将一个个分片发送出去
				fileContent = filePieceContentMap.get(i);

//				pieceRecordXOR = filePieceRecordXORMap.get(i);// TODO: 2021/10/25 获取该序号的分片的指针

				System.out.println(TAG + " dataMessageHandle:: 发出片段的:"+i+"：messageID："+messageID+"recordXOR："+recordXOR);

				payload = FileMessage.buildFileDataMessage(sharedKey, fileNameHash, i, fileContent,messageID,recordXOR);// TODO: 2021/8/25 增加消息id 组装报文

				tmp = new MessageItem(item.getOnionName(), item.getSocket(), payload, 1, 1, item.getHandShakeMessage());
//				System.out.println("dataMessageHandle  sendfiledata "+AESCrypto.bytesToHex(payload));
				tmp.setPieceID(i);
				try {
					sendQueue.put(tmp);//发送messageType = 8的报文
					System.out.println(fileTask.getFileName() + " pieceNumber " + i + " put into sending queue");
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					System.out.println(TAG + " dataMessageHandle:: FileMessageHandler fileDataMessageHandle" + e.getMessage());
				}
			}
			System.out.println(TAG + " dataMessageHandle:: FileMessageHandler put file data into send queue complete " + totalPieceNumber);
		} else if (Constant.DATA_FILE_DATA == messageType) {//接收方处理文件内容 的每一个分片   8

			RecordXOR recordXOR =  getRecordXOR(externalPayload,messageID);// TODO: 2021/10/24 解析本次消息的异或指针，改变commonRecordXOR的全局指针

			System.out.println(TAG + " dataMessageHandle:: Recieve DATA_FILE_DATA");

			byte[] byteFileNameHash = Message.subBytes(internalPayload, 0, 20);
			System.out
					.println(TAG + " dataMessageHandle:: FileMessage parseFileDataMessage fileNameHash " + AESCrypto.bytesToHex(byteFileNameHash));

			byte[] bytePieceID = Message.subBytes(internalPayload, 20, 4);
			int pieceID = Integer.parseInt(AESCrypto.bytesToHex(bytePieceID), 16);
			System.out.println(TAG + " dataMessageHandle:: FileMessage parseFileMetaMessage.pieceID:" + pieceID);

			byte[] fileContent = Message.subBytes(internalPayload, 24, internalPayload.length - 24);

			FileTask fileTask = getFileTaskByFileNameHash(byteFileNameHash);
			if (fileTask == null) {
				System.out.println(TAG + " dataMessageHandle:: FileMessageHandler recieved filename hash not in our list "
						+ AESCrypto.bytesToHex(byteFileNameHash));
				return;
			}
			fileTask.recievePieceData(pieceID, fileContent);//接收方的还是空的，所以在这里分片存储//计算接收的速度，更新界面
			fileTask.setStatus(1);//设置状态为转移中
			LogUtils.d(TAG, " dataMessageHandle:: file piece " + pieceID + " is recieved of file " + fileTask.getFileName());
			LogUtils.d(TAG, " dataMessageHandle:: file " + fileTask.getFileName() + " transfer " + fileTask.getPercent() + " "
					+ fileTask.getSpeed() + "KB/s");

			int tmpNumber = fileTask.getFileTransferStatusMap().size();
			int num = tmpNumber%fileTask.getStepNumber();//？？
			if(num==0) {
				Event.FileMessage mess = new Event.FileMessage(fileTask.getFileName(), fileTask.getPercent(), fileTask.getSpeed());
				Gson gson = new Gson();
				String messJson = gson.toJson(mess);
//							Log.d("FILE_MESSAGE111" , " messJson = " + messJson +"\ttmpNumber"+tmpNumber+"\t getTotalPieceNumber "+fileTask.getTotalPieceNumber());
				Log.d(TAG ,"  dataMessageHandle:: FILE_MESSAGE555555" + " messJson = " + messJson);
				EventBus.getDefault().post(new Event(Event.FILE_MESSAGE, messJson, messageID));// TODO: 2021/8/25 fileTask.getOnionName()改为 messageID



			}


			byte[] sharedKey = statusItem.getShareKey();

			byte[] messageHash = fileTask.getFileNameHash();
			int ackMessageType = 2;
			String ack = Constant.DATA_ACK_CONTENT;

//			String messageID = RandomUtil.randomChar(); // TODO: 2021/8/24 更新消息id标记，用这个来唯一标记当前次的发送情况
			byte[] finalPayload = DataMessage.buildACKDataMessage(sharedKey, ackMessageType, pieceID, ack, messageHash,messageID,recordXOR);// TODO: 2021/10/25 在ack增加xor异或头尾指针// TODO: 2021/10/25 增加 //发ack给发送方
//			MessageItem tmps = new MessageItem(item.getOnionName(), statusItem.getSocket(), finalPayload, 1, 1,
//					item.getHandShakeMessage());
//			try {
//				sendQueue.put(tmps);
//				System.out.println("handleFileMessageSend recieve file filename " + fileTask.getFileName()
//						+ " filesize " + fileTask.getTotalSize());
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}

			if (!this.sendMessage(statusItem.getSocket(), finalPayload)) {//发送messageType = 12的ack给发送方
				fileTask.setStatus(0);
				System.out.println(
						TAG + "  dataMessageHandle::  send DATA_FILE_ACK failed to " + item.getOnionName());
				if (statusItem.getSocket() != null)
					this.deleteConnection(item.getOnionName(), statusItem.getSocket());
			} else {
				System.out.println(
						TAG + " dataMessageHandle::  send DATA_FILE_ACK success to " + item.getOnionName());
			}

			/**
			 * is the last piece
			 */
			if (fileTask.getFileTransferStatusMap().size() == fileTask.getTotalPieceNumber()) {

				String filePath = fileTask.mergeFileUsedXOR(fileTask.getFileName(),recordXOR);// TODO: 2021/10/26 根据文件xor指针开始解异或 //合并各个分片文件，XOR解密文件

				if (filePath != null) {

					finalPayload = FileMessage.buildFileDoneMessage(sharedKey, fileTask.getFileNameHash(), messageID,recordXOR );// TODO: 2021/8/25 增加消息id //通知发送者已收完

					if (!this.sendMessage(statusItem.getSocket(), finalPayload)) {//发送messageType = 9的ack
						fileTask.setStatus(0);
						System.out.println(TAG + " dataMessageHandle:: send DATA_FILE_DONE failed to "
								+ item.getOnionName());
						if (statusItem.getSocket() != null)
							this.deleteConnection(item.getOnionName(), statusItem.getSocket());
					} else {
						System.out.println(TAG + " dataMessageHandle:: send DATA_FILE_DONE success to "
								+ item.getOnionName());

						Event.FileMessage mess = new Event.FileMessage(fileTask.getFileName(),"100.00",fileTask.getSpeed());
						Gson gson = new Gson();
						String messJson = gson.toJson(mess);
						Log.d(TAG , " dataMessageHandle:: messJson = " + messJson );
						EventBus.getDefault().post(new Event(Event.FILE_MESSAGE, messJson, messageID));// TODO: 2021/8/25 fileTask.getOnionName()改为 messageID



						fileTask.setStatus(2);//设置为接收完
						System.out.println(TAG + " dataMessageHandle:: remove fileTask before " + this.senderFileMap.size());
						this.senderFileMap.remove(fileTask.getFileName());//移除本次文件接收任务
						System.out.println(TAG + " dataMessageHandle:: remove fileTask before " + this.senderFileMap.size());
					}

				} else {
					System.out.println(TAG + " dataMessageHandle:: recieve error ");
				}
			}
		} else if (Constant.DATA_FILE_DONE == messageType) {// 9 ，发送方收到接收方收完了
			System.out.println(TAG + " dataMessageHandle:: Recieve FLIE_DONE");
			byte[] fileNameHash = FileMessage.parseFileReadyMessage(internalPayload);
			FileTask fileTask = getFileTaskByFileNameHash(fileNameHash);
			if (fileTask == null) {
				System.out.println(TAG + " dataMessageHandle:: FileMessageHandler recieved file done message filehash not in our list "
						+ AESCrypto.bytesToHex(fileNameHash));
				return;
			} else {
				fileTask.setStatus(2);
				System.out.println(TAG + " dataMessageHandle:: FileMessageHandler send file complete and success" + fileTask.getFileName());
			}

			Event.FileMessage mess = new Event.FileMessage(fileTask.getFileName(),"100.00",fileTask.getSpeed());
			Gson gson = new Gson();
			String messJson = gson.toJson(mess);
			Log.d(TAG , " dataMessageHandle::  messJson = " + messJson );
			EventBus.getDefault().post(new Event(Event.FILE_MESSAGE, messJson, messageID));// TODO: 2021/8/25 fileTask.getOnionName()改为 messageID
			// remove file transfer task
			fileTask.setEndTime(new Date());
			System.out.println(TAG + " dataMessageHandle:: remove fileTask before " + this.senderFileMap.size());
			this.senderFileMap.remove(fileTask.getFileName());
			System.out.println(TAG + " dataMessageHandle:: remove fileTask after " + this.senderFileMap.size());

		} else if (Constant.DATA_ACK == messageType) {//12，	发送方收到	文件或文本的ack报文
//			System.out.println("Recieve DATA_ACK");
			ACKMessage message = DataMessage.parseACKDataMessage(internalPayload);//拆解ack内容 结构为：ack_type		receive-piece-number		ok,这是小写	file_id_hash

			if (message.getMessageType() > 0) {//发文件，对方回复的ack。更新发文件的进度
				FileTask fileTask = getFileTaskByFileNameHash(message.getMessageHash());
				if (fileTask != null) {
					int pieceID = message.getRecievePieceNumber();
					if (message.getAckok().equals("ok")) {

//						System.out.println("file piece " + pieceID + " is acked of file " + fileTask.getFileName());
						fileTask.updateTransferStatus(pieceID);//计算接收的速度，更新界面
//						System.out.println("file " + fileTask.getFileName() + " transfer " + fileTask.getPercent() + " "
//								+ fileTask.getSpeed() + "KB/s");
						fileTask.setStatus(1);

//						Log.d("FILE_MESSAGE111",  " name= " + fileTask.getFileName() + " percent = "
//								+ fileTask.getPercent() + " speed = " + fileTask.getSpeed() );
						int tmpNumber = fileTask.getFileTransferStatusMap().size();
						int num = tmpNumber%fileTask.getStepNumber();//更新发送方自己的发送进度，但是是每到一段段的更新

						if (num == 0||(tmpNumber==fileTask.getTotalPieceNumber())){
							Event.FileMessage mess = new Event.FileMessage(fileTask.getFileName(),fileTask.getPercent(),fileTask.getSpeed());
							Gson gson = new Gson();
							String messJson = gson.toJson(mess);
//							Log.d("FILE_MESSAGE111" , " messJson = " + messJson +"\ttmpNumber"+tmpNumber+"\t getTotalPieceNumber "+fileTask.getTotalPieceNumber());
							Log.d(TAG , " dataMessageHandle:: messJson = " + messJson );
							EventBus.getDefault().post(new Event(Event.FILE_MESSAGE, messJson, messageID));// TODO: 2021/8/25 fileTask.getOnionName()改为 messageID

							byte[] tmp= new byte[2048];
//							System.out.println("2048000000 "+AESCrypto.bytesToHex(tmp));
							MessageItem tmps = new MessageItem(item.getOnionName(), item.getSocket(), tmp, 1, 1, item.getHandShakeMessage());//发握手
							try {
								sendQueue.put(tmps);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								System.out.println(TAG + " dataMessageHandle:: send 2048000000" + e.getMessage());
							}
						}
					} else if (message.getAckok().equals("ck")) {
						this.resend(fileTask, statusItem,messageID);// TODO: 2021/8/25 增加消息id
					}
				} else {
					System.out.println(TAG + " dataMessageHandle:: recieve file piece ack ,but filetask not exists, just neglet");
				}
			} else {//发文本后，对方发来的ack
				if (message != null && messageID!=null) {
					System.out.println(TAG + " dataMessageHandle:: message " + message.getMessageHash() + " is acked");
					Intent intent = new Intent();
					intent.setAction(Constant.TOR_BROAD_CAST_ACTION);
					intent.putExtra(Constant.TOR_BROAD_CAST_INTENT_KEY,Constant.PEER_HAS_RECEIVED_MESSAGE);//Status
//					intent.putExtra("result",new String(message.getMessageHash()));// TODO: 2021/8/24 这里应该改为以消息id验证。因为 message不唯一
					intent.putExtra("result",messageID);// TODO: 2021/8/24 这里应该改为以消息id验证。因为 message不唯一
					intent.setComponent(new ComponentName(Constant.PACKAGE,Constant.TOR_BROAD_CAST_PATH));
					context.sendBroadcast(intent);

					XORutil.deleteRecordXORandXORFile(recordXORs,messageID);// TODO: 2021/10/6 可以删除recordXORs中的recordXOR和已用过的XORFile了
					
				}
			}
		} else {
			System.out.println(TAG + " dataMessageHandle::  messageType : " + messageType);
		}
	}

	private void resend(FileTask fileTask, ConnectionStatusItem statusItem,String messageID ) {// TODO: 2021/8/25 增加消息id

		ConcurrentHashMap<Integer, byte[]> filePieceContentMap = fileTask.getFilePieceContentMap();
		int totalPieceNumber = fileTask.getTotalPieceNumber();

		Set<Integer> transferedPieceIDSET = fileTask.getFileTransferStatusMap().keySet();

		byte[] fileContent = null;
		byte[] payload = null;
		MessageItem tmp = null;
		byte[] fileNameHash = fileTask.getFileNameHash();

		byte[] sharedKey = statusItem.getShareKey();
		if(sharedKey!=null) {
			System.out.println(TAG + " resend:: sharedKey " + AESCrypto.bytesToHex(sharedKey));

			for (int i = 0; i < totalPieceNumber; i++) {
				if (transferedPieceIDSET.contains(i)) {
					continue;
				}
				fileContent = filePieceContentMap.get(i);

//				String messageID = RandomUtil.randomChar(); // TODO: 2021/8/24 更新消息id标记，用这个来唯一标记当前次的发送情况

				RecordXOR recordXOR = fileTask.getFileTaskRecordXOR();// TODO: 2021/10/25 还是空的！！！

				payload = FileMessage.buildFileDataMessage(sharedKey, fileNameHash, i, fileContent,messageID,recordXOR);

				tmp = new MessageItem(statusItem.getOnionName(), statusItem.getSocket(), payload, 1, 1, null);
				tmp.setPieceID(i);
				try {
					sendQueue.put(tmp);
					System.out.println(TAG + " resend:: fileName: " +fileTask.getFileName() + " pieceNumber: " + i + " put into sending queue");
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}else{
			System.out.println(TAG + " resend::  sharedKey is null" +statusItem.toString());
		}

	}

	public void processOfflineMessage(String onionName,String connectServer,String messageID) {
		System.out
				.println(TAG + " processOfflineMessage:: process text and file message of  " + onionName + " in offline manner");
		FailedTextMessage tmp = null;
		ArrayList<FailedTextMessage> tmpList = new ArrayList<FailedTextMessage>();
		String from = DigestUtils.sha256Hex(this.mySelfBean.getOnionName().replace(".onion", "")); //M
		while (!this.failedQueue.isEmpty()) {
			tmp = this.failedQueue.poll();
			if (tmp.getOnionName().equals(onionName)) {
				String messageContent = tmp.getRawMessage();

				String remoteOnion = onionName;
				String to = DigestUtils.sha256Hex(remoteOnion.replace(".onion", ""));
				System.out.println(TAG + "processOfflineMessage:: send offline text from = " + this.mySelfBean.getOnionName() + " to =" + onionName + " text = " + messageContent);
				sendOfflineText sendOfflineText = new sendOfflineText(from, to, messageContent,this.channelServer, messageID);
				sendOfflineText.start();
			} else {
				tmpList.add(tmp);
			}
		}
		for (int i = 0; i < tmpList.size(); i++) {
			try {
				this.failedQueue.put(tmpList.get(i));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		for (int i = 0; i < tmpList.size(); i++) {
			tmpList.remove(i);
		}
		tmpList = null;


		// process fileTask
//			for (Map.Entry<String, FileTask> entry : this.senderFileMap.entrySet()) {
//				FileTask fileTask = entry.getValue();
//				if (onionName.equals(fileTask.getOnionName())) {
//					String fromid = DigestUtils.sha256Hex(this.mySelfBean.getOnionName()); //M
//					String toid = DigestUtils.sha256Hex(onionName);
//					int direction = fileTask.getDirection();
//					if (direction == 0) {
//						sendOfflineFile sendOfflineFile = new sendOfflineFile(fromid,toid,"file",fileTask.getFileFullPath(),connectServer);
//						sendOfflineFile.start();
//						this.senderFileMap.remove(entry.getKey());
//					}
//				}
//			}
	}







	private class ServerTransport extends Thread {//模拟服务端的，接收对方的连接请求和被动接收的消息
		private ServerSocket serverSocket;
		private ExecutorService pool;
		private int localServerPort;
		private int poolSize;
		private String localPrivateKey;

		public ServerTransport(int serverPort, int poolSize, String localPrivateKey) {
			this.localServerPort = serverPort;
			this.localPrivateKey = localPrivateKey;
			this.poolSize = poolSize;
			this.pool = Executors.newFixedThreadPool(this.poolSize);

		}

		@Override
		public void run() {
			Socket client = null;
			try {
				this.serverSocket = new ServerSocket(this.localServerPort);
				System.out.println(TAG + " ServerTransport run:: listen local port "+this.localServerPort);
				while (true) {
					client = this.serverSocket.accept();
					this.pool.execute(new RequestHandler(client, this.localPrivateKey));//真实文本的解密再加入队列
				}
			} catch (IOException e) {
//				e.printStackTrace();
				System.out.println(TAG + " ServerTransport run:: error " + e.getMessage());
			}
		}

		class RequestHandler implements Runnable {//真实文本的解密再加入队列
			private Socket socket;
			private String localPrivateKey;
			private MailItem mailItem = null;
			private ConnectionStatusItem statusItem = null;
			private HandShakeMessage handShakeMessage = null;

			public RequestHandler(Socket socket, String localPrivateKey) {
				this.socket = socket;
				this.localPrivateKey = localPrivateKey;
				System.out.println(TAG + " RequestHandler accept a socket " + socket.toString());
			}

			private byte[] readExactly(InputStream in) {
				byte[] b = new byte[2048];
				int n = 0;
				int offset = 0;
				int count = 0;
				while (offset < 2048) {
					try {
						n = in.read(b, offset, 2048 - offset);
						if (n > 0) {
							offset = offset + n;
//							System.out.println(this.socket + " readExactly " + offset);
						}
						count = count + 1;
//						System.out.println(this.socket + " read count " + count);
						if (count > 10)
							return null;
						Thread.sleep(200);
					} catch (SocketException e) {
						System.out.println(TAG + " RequestHandler readExactly:: " + e.getMessage());
						return null;
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return null;
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return null;
					}
				}
//				Log.i("握手原始",AESCrypto.bytesToHex(b));//好友的文件

				System.out.println(TAG + " RequestHandler readExactly::  ServerTransport握手原始"+Arrays.toString(b));

				System.out.println(TAG  + " RequestHandler readExactly:: ServerTransport握手原始"+AESCrypto.bytesToHex(b));//好友的文件

				return b;
			}

			@Override
			public void run() {
				try {
					byte[] b = null;
					b = new byte[2048];
					InputStream in = this.socket.getInputStream();
					byte[] flag = new byte[2048];
					while (true) {
						b = readExactly(in);
						if (b == null) {
							if (this.statusItem != null)
								deleteConnection(this.statusItem);
							in.close();
							this.socket.close();
							this.mailItem = null;
							this.statusItem = null;
							break;
						}

						if (Arrays.equals(b, flag)) {
							System.out.println(TAG + " RequestHandler run:: recieved :\n" + RSACrypto.bytesToHex(b));
							continue;
						}
						System.out.println(TAG + " RequestHandler run:: read data "+b.length);
						byte[] externalPayload = null;
//						System.out.println("RequestHandler recieve payload :\n" + RSACrypto.bytesToHex(b));
						if (this.mailItem == null) {
							try {
								externalPayload = Message.parse(b, this.localPrivateKey);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								if (this.statusItem != null)
									deleteConnection(this.statusItem);
								in.close();
								this.socket.close();
								this.mailItem = null;
								this.statusItem = null;
								break;
							}
							byte[] remoteOnionHash = Message.subBytes(externalPayload, 33, 32);
							MailItem item = getMailItemByOnionHash(remoteOnionHash);
							if (item != null) {
								this.mailItem = item;
								this.statusItem = new ConnectionStatusItem(item.getOnionName(), item.getOnionHash(),
										this.socket, 0, null);
								this.handShakeMessage = new HandShakeMessage(item.getOnionName(),
										Constant.CLIENT_PRIVATE_KEY, item.getPublicKey());
								addNewConnection(this.statusItem);
								addNewHandShakeMessageHandler(handShakeMessage);
							} else {
								System.out.println(TAG + " RequestHandler run:: does not in my contact list break "
										+ RSACrypto.bytesToHex(remoteOnionHash));
								if (this.statusItem != null)
									deleteConnection(this.statusItem);
								in.close();
								this.socket.close();
								this.mailItem = null;
								this.statusItem = null;
								break;
							}
						}
						try {
							MessageItem item = null;

							int type = 0;
							if (this.statusItem.getStatus() == 0) {
								externalPayload = Message.parse(b, this.localPrivateKey, this.mailItem.getPublicKey());
							} else if (this.statusItem.getStatus() == 1) {
								externalPayload = Message.parseDataPayload(b, this.statusItem.getShareKey());//在线接收的解密
								type = 1;
							} else {
								System.out.println(TAG + " RequestHandler run:: socket status is not valid {0,1}" + this.statusItem.getStatus());
								continue;
							}
							if (externalPayload == null) {
								System.out.println(TAG + " RequestHandler run::  externalPayload is null");
//								Thread.sleep(5000);
								continue;
							}

							System.out.println(TAG + " RequestHandler run:: ServerTransport握手解密"+Arrays.toString(externalPayload));
							System.out.println(TAG + " RequestHandler run:: ServerTransport握手解密"+AESCrypto.bytesToHex(externalPayload));


//							System.out.println("ClientTransport externalPayload :\n" + RSACrypto.bytesToHex(externalPayload));
							item = new MessageItem(this.mailItem.getOnionName(), this.socket, externalPayload, type, 0,
									this.handShakeMessage);
//							item.setHandShakeMessage(this.handShakeMessage);
							try {
								recieveQueue.put(item);
							} catch (InterruptedException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
//							Thread.sleep(100);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							break;
						}
					}
					if (this.statusItem != null)
						deleteConnection(this.statusItem);
					in.close();
					this.socket.close();
					this.mailItem = null;
					this.statusItem = null;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					if (this.statusItem != null)
						deleteConnection(this.statusItem);
					this.mailItem = null;
					this.statusItem = null;
				}

			}

		}

	}

	private class CreateConnectionThread extends  Thread{//连接好友ServerTransport线程的tor网络服务器

		private MailItem mailItem;
		private int purpose;
		private  int retryCount;

		public CreateConnectionThread(MailItem mailItem, int purpose,int retryCount){
			this.mailItem=mailItem;
			this.purpose=purpose;
			this.retryCount=retryCount;
		}

		@Override
		public void run() {
			connect();
		}
		public int connect() {
			int count = 0;
			int status = 0;
			while (count < this.retryCount && mailItem!=null) {
				SocketAddress addr = new InetSocketAddress(Constant.TOR_SOCKS_PROXY_SERVER, Constant.TOR_SOCKS_PROXY_PORT);
				Proxy proxy = new Proxy(Proxy.Type.SOCKS, addr);
				Socket socket = new Socket(proxy);
				//MailItem [onionName=liqf2ad7xgi4ewixwvk6qxf5bsevaq7qojvfzu74ruwusvc4ullfonyd.onion(b), port=6677]
				Log.e(TAG + " CreateConnectionThread run:: cc线程ServerMessagndler88",mailItem.toString());
				System.out.println("cc线程CreateConnectionThread try connect to  " +this.mailItem.getOnionName()+ " with time "+ (count +1));

				InetSocketAddress dest = new InetSocketAddress(this.mailItem.getOnionName(), mailItem.getPort());//好友的
				System.out.println(dest);
				try {
					socket.connect(dest, Constant.CONNECT_TIME_OUT);//花1分钟
					System.out.println(TAG + " CreateConnectionThread run:: cc线程connect success ");
					System.out.println(TAG + " CreateConnectionThread run:: cc线程remote onoin " + mailItem.getOnionName());
					System.out.println(TAG + " CreateConnectionThread run:: cc线程remote port" + mailItem.getPort());
//					socket.sendUrgentData(Thread.MAX_PRIORITY);
					System.out.println(Thread.currentThread().getName() + " socket: " + socket);
					System.out.println(TAG + " CreateConnectionThread run:: cc线程ServerMessageHandler.connect.ssocket:" + socket.getInetAddress());
					System.out.println(TAG + " CreateConnectionThread run:: cc线程ServerMessageHandler.connect.ssocket:" + socket.getInetAddress()
							+ socket.getLocalSocketAddress());
//				if(socket.getInetAddress()==null) {
//					count = count + 1;
//					socket.close();
//					continue;
//				}
					ConnectionStatusItem statusItem = new ConnectionStatusItem(mailItem.getOnionName(),
							mailItem.getOnionHash(), socket, 0, null);
					statusItem.setPurpose(purpose);
					addNewConnection(statusItem);
					HandShakeMessage handShakeMessage = handShakeMessageHandlerByOnionName
							.get(mailItem.getOnionName());
					if (handShakeMessage == null) {
						handShakeMessage = new HandShakeMessage(mailItem.getOnionName(), Constant.CLIENT_PRIVATE_KEY,
								mailItem.getPublicKey());
						addNewHandShakeMessageHandler(handShakeMessage);
					}
					status = 1;
					//连接成功
					EventBus.getDefault().post(new Event(Event.CREATE_CONNECTION_SUCCESS, "success", this.mailItem.getOnionName()));
					break;
				} catch (SocketException e) {
					System.out.println(TAG + " CreateConnectionThread run:: cc线程CreateConnectionThread connect connect failed "+e.getMessage());
					if(e.getMessage().equals("SOCKS: Host unreachable")){
						try {
							Thread.sleep(Constant.CONNECT_TIME_OUT);
						} catch (InterruptedException interruptedException) {
							interruptedException.printStackTrace();
						}
					}
					count = count +1;
				} catch (IOException e) {
//					System.out.println("connect error");
					System.out.println(TAG + " CreateConnectionThread run:: cc线程CreateConnectionThread remote onion " + mailItem.getOnionName());
					System.out.println(TAG + " CreateConnectionThread run:: cc线程CreateConnectionThread remote port" + mailItem.getPort());
					System.out.println(TAG + " CreateConnectionThread run:: cc线程CreateConnectionThread connect connect failed "+e.getMessage());

					count = count + 1;
					try {
						socket.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}


			if(mailItem!=null)
				//临时修改
				//EventBus.getDefault().post(new Event(Event.CREATE_CONNECTION_SUCCESS, "success", this.mailItem.getOnionName()));
			    EventBus.getDefault().post(new Event(Event.CREATE_CONNECTION_SUCCESS, "fail", this.mailItem.getOnionName()));
			return status;
		}

	}

	private class ClientTransport extends Thread {//模拟客户端传输，接收对方服务端回应的握手的连接请求和主动发给服务端消息后回应的ack
		private String localPrivateKey;
		private MailItem mailItem = null;
		private ConnectionStatusItem statusItem = null;
		private HandShakeMessage handShakeMessage = null;

		private Socket socket;
		private int purpose;

		public ClientTransport(MailItem mailItem, String localPrivateKey, ConnectionStatusItem statusItem,
							   int purpose) {
			super();
			this.mailItem = mailItem;
			this.localPrivateKey = localPrivateKey;
			this.purpose = purpose;
			this.statusItem = statusItem;
			this.socket = statusItem.getSocket();
			this.handShakeMessage = handShakeMessageHandlerByOnionName.get(mailItem.getOnionName());
		}

		private byte[] readExactly(InputStream in) {
			byte[] b = new byte[2048];
			int n = 0;
			int offset = 0;
			while (offset < 2048) {
				try {
					n = in.read(b, offset, 2048 - offset);
					if (n > 0) {
						offset = offset + n;
						System.out.println(TAG + " ClientTransport readExactly:: " + offset);
					}
				} catch (SocketException e) {
					System.out.println(TAG + " ClientTransport readExactly::  "+e.getMessage());
					return null;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return null;
				}
			}

			System.out.println(TAG + " ClientTransport readExactly:: 握手原始"+Arrays.toString(b));
			System.out.println(TAG + " ClientTransport readExactly:: 握手原始"+AESCrypto.bytesToHex(b));//好友的文件

			return b;
		}

		public void handle() {
			int result = this.sendSessionRequestMessage();//第一次握手
			if (result == 0) {
				if (this.statusItem != null)
					deleteConnection(this.statusItem);
				try {
					this.socket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				this.statusItem = null;
				return;
			}
			try {

				byte[] b = null;
				b = new byte[2048];
				byte[] flag = new byte[2048];
				InputStream in = socket.getInputStream();

				while (true) {
					b = readExactly(in);
					if (b == null) {
						if (this.statusItem != null)
							deleteConnection(this.statusItem);
						in.close();
						this.socket.close();
						this.mailItem = null;
						this.statusItem = null;
						break;
					}
					if (Arrays.equals(b, flag)) {
						System.out.println(TAG + " ClientTransport handle:: recieved :\n" + RSACrypto.bytesToHex(b));
						continue;
					}
					try {
						System.out.println(TAG + " ClientTransport handle:: read data "+b.length);
						MessageItem item = null;
						byte[] externalPayload = null;
						int type = 0;
						if (this.statusItem.getStatus() == 0) {//未连接时，处理连接握手包，type = 0
							LogUtils.d(TAG, " handle:: localPrivateKey: " + this.localPrivateKey);
							LogUtils.d(TAG, " handle:: publicKey: " + this.mailItem.getPublicKey());
							externalPayload = Message.parse(b, this.localPrivateKey, this.mailItem.getPublicKey());
						} else if (this.statusItem.getStatus() == 1) {//已连接，处理实际消息
							externalPayload = Message.parseDataPayload(b, this.statusItem.getShareKey());//在线接收的解密
							type = 1;
						} else {
							System.out.println(TAG + " ClientTransport handle:: socket status is not valid {0,1}" + this.statusItem.getStatus());
							continue;
						}
						if (externalPayload == null) {
							System.out.println(TAG + " ClientTransport handle:: externalPayload is null");
//							Thread.sleep(5000);
							continue;
						}

						System.out.println(TAG + " ClientTransport handle:: 握手解密"+Arrays.toString(externalPayload));
						System.out.println(TAG + " ClientTransport handle:: 握手解密"+AESCrypto.bytesToHex(externalPayload));

//						System.out
//								.println("ClientTransport externalPayload :\n" + RSACrypto.bytesToHex(externalPayload));
//
						item = new MessageItem(this.mailItem.getOnionName(), this.socket, externalPayload, type, 0,
								this.handShakeMessage);
//						item.setHandShakeMessage(this.handShakeMessage);

						try {
							recieveQueue.put(item);
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
//						Thread.sleep(100);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						break;
					}
				}
				if (this.statusItem != null)
					deleteConnection(this.statusItem);
				in.close();
				this.socket.close();
//				this.mailItem = null;
				this.statusItem = null;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				if (this.statusItem != null)
					deleteConnection(this.statusItem);
//				this.mailItem = null;
				this.statusItem = null;
			}
		}

		@Override
		public void run() {
//	        send_protrol(sendByte,peerHostname);
			handle();
		}

		private int sendSessionRequestMessage() {
			OutputStream out;
			try {
				out = socket.getOutputStream();
				byte[] body = handShakeMessage.createSessionRequestMessage( context);//握手包
				out.write(body);
				return 1;
			} catch (IOException e) {
				// TODO Auto-generated catch block
//				e.printStackTrace();
				System.out.println(TAG + " ClientTransport handle:: sendSessionRequestMessage failed" + e.getMessage());
				return 0;
			}

		}

	}



	double timeFactor = 1;// TODO: 2022/4/12  时间因子，控制时间的长短


	private class StatusCheckerTransport extends Thread {//网络状态检测
		private String TargetOnionName;
		private int targetPort;
		private int retryCount;
		private Socket socket;
		private int connectionStatus=1;
		private int rebootCount;

		public StatusCheckerTransport(String remotePeerOnion, int remotePeerPort, int retryCount) {
			super();
			this.TargetOnionName = remotePeerOnion.trim();
			this.targetPort = remotePeerPort;
			this.retryCount = retryCount;
			this.rebootCount=0;
		}

		public void handle() {
			int result = this.connect();
			if (result == 0) {

				return;
			}
			try {
				System.out.println(TAG + " StatusCheckerTransport handle:: 网络定时检测成功，开始休眠 this.socket.setSoTimeout(60000)");
				this.socket.setSoTimeout((int)(6000*timeFactor));//（以毫秒为单位）设置了超时时间后,如果socket.getInputStream().read()方法读不到数据,处于等待读取数据的状态时,就会开始计算超时时间;当到达超时时间还没有新的数据可以读取的时候,read()方法就会抛出io异常
			} catch (SocketException e1) {
				// TODO Auto-generated catch block
				System.out.println(TAG + " StatusCheckerTransport handle:: status checker handle error "+e1.getMessage());
				alarmStatusChange(0);
				return;
			}

			while (true) {
				try {
					System.out.println(TAG + " StatusCheckerTransport handle:: 网络定时检测 连接成功后的循环 this.socket.sendUrgentData(MAX_PRIORITY)");

					sendnewBroadcast("regularly monitor network status");//网络连接成功后定时监听网络状态

					this.socket.sendUrgentData(MAX_PRIORITY);//判断远端服务器是否已经断开连接
					alarmStatusChange(1);
					System.out.println(TAG + " StatusCheckerTransport handle::  网络status checker sendUrgentData to "+ this.TargetOnionName.trim()+":"+this.targetPort+" success");
					System.out.println(TAG + " StatusCheckerTransport handle::  网络status checker sendUrgentData "+"  Thread.sleep(60000)");
					Thread.sleep((int)(60000*timeFactor));//和上面的socket.setSoTimeout一样
				} catch (SocketException e) {
//					e.printStackTrace();
					System.out.println(TAG + " StatusCheckerTransport handle::  网络status checker handle error "+e.getMessage());
					alarmStatusChange(0);
					break;
				} catch (IOException e) {
					// TODO Auto-generated catch block
//					e.printStackTrace();
					System.out.println(TAG + " StatusCheckerTransport handle::  网络status checker handle error "+e.getMessage());
					alarmStatusChange(0);
					break;
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
//					e.printStackTrace();
					System.out.println(TAG + " StatusCheckerTransport handle::  网络status checker handle error "+e.getMessage());
					alarmStatusChange(0);
					break;
				}
			}

		}

		@Override
		public void run() {
			while (true) {
				System.out.println(TAG + " StatusCheckerTransport run:: 网络定时检测开始0 handle");
				handle();

			}

		}

		private void alarmStatusChange(int status) {


			System.out.println(TAG + " StatusCheckerTransport alarmStatusChange:: 网络状态"+  " 旧的this.connectionStatus  is " + this.connectionStatus+" 新status is " + status);

//			if (this.connectionStatus == status)
//				return;// TODO: 2022/4/11 问题所在，与其他类中的状态修改不同步
			
			if(this.connectionStatus ==1&&status==1){
				sendnewBroadcast("network connection succeeded");//网络连接成功
				return;// TODO: 2022/4/12 新旧状态都是在线1，不需要执行下面耗时的连接好友的操作了
			}



			this.connectionStatus = status;
			if (this.connectionStatus == 0) {
				this.rebootCount=this.rebootCount+1;

				sendnewBroadcast("network reconnection failed");//网络重接失败
				sendnewBroadcast("status changes to offline");//状态改变为离线

				mySelfBean.setOnlineStatus("0");
				System.out.println(TAG + " StatusCheckerTransport alarmStatusChange:: 网络写入0");
				SharedPreferencesUtil.setUserBeanSharedPreferences(getContext(), mySelfBean);

				sendnewBroadcast("save status to local storage");//将状态存入本地

				System.out.println(TAG + " StatusCheckerTransport alarmStatusChange:: 网络--写入BeanSharedPreferences： "+"ServerMessageHandler-alarmStatusChange(0)");
				System.out.println(TAG + " StatusCheckerTransport alarmStatusChange:: 网络本地存储的"+SharedPreferencesUtil.getUserBeanSharedPreferences(getContext()).getOnlineStatus());
				this.closeSocket();

				sendnewBroadcast("close socket");//关闭Socket

//				if(this.rebootCount>3) {
//					this.rebootCount=0;
					System.out.println(TAG + " StatusCheckerTransport alarmStatusChange:: begin stop and start tor " + new Date().toString());
				    System.out.println(TAG + " StatusCheckerTransport alarmStatusChange:: tor重启网络begin stop and start tor " + new Date().toString());
					stopTor(getContext());
					startTor(getContext());

				    sendnewBroadcast("restart tor service");//重启tor

//				}
			}else{
				sendnewBroadcast("network reconnection succeeded");//网络重接成功
				sendnewBroadcast("status changes to online");//状态改变为在线

				this.connectionStatus=1;
//				this.rebootCount=0;
				mySelfBean.setOnlineStatus("1");
				System.out.println(TAG + " StatusCheckerTransport alarmStatusChange:: 网络写入1");
				SharedPreferencesUtil.setUserBeanSharedPreferences(getContext(), mySelfBean);

				sendnewBroadcast("save status to local storage");//将状态存入本地

				System.out.println(TAG + " StatusCheckerTransport alarmStatusChange::网络--写入BeanSharedPreferences： "+"ServerMessageHandler-alarmStatusChange(1)");
				System.out.println(TAG + " StatusCheckerTransport alarmStatusChange:: 网络本地存储的"+SharedPreferencesUtil.getUserBeanSharedPreferences(getContext()).getOnlineStatus());

				new Thread(new Runnable() {// TODO: 2022/4/11 用线程来解决耗时问题
					@Override
					public void run() {

						System.out.println(TAG + " StatusCheckerTransport run:: 网络begin tryResendAll failed messages in online manner "+ new Date().toString());

						for (Map.Entry<String, MailItem> entry : mailList.entrySet()) {// TODO: 2022/4/11 这里会耗时n分钟（由好友n数决定 3n）！！会阻塞线程的网络状态监控
							MailItem item = entry.getValue();
							createConnection(item.getOnionName(),Constant.SOCKET_PURPOSE_TEXT);//重新与所有好友连接，耗时太久！（而且刚登录并不需要与对方连接）
							sendnewBroadcast("connect friends");//连接好友
						}
						tryResendAll();
						sendnewBroadcast("tryResendAll");
						System.out.println(TAG + " StatusCheckerTransport run::  网络tryResendAll完成");


					}
				}).start();




			}


		}

		private void closeSocket() {
			try {
				socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		private int connect() {
			int count = 0;
			int status = 0;
			while (count < this.retryCount) {
				System.out.println(TAG + " StatusCheckerTransport connect:: 网络定时检测-循环不断连接次数："+count);

				sendnewBroadcast(TAG + " StatusCheckerTransport connect:: Try "+ (count + 1) + " times socket connection");//尝试第"+(count+1)+"次socket连接

				SocketAddress addr = new InetSocketAddress(Constant.TOR_SOCKS_PROXY_SERVER,
						Constant.TOR_SOCKS_PROXY_PORT);
				Proxy proxy = new Proxy(Proxy.Type.SOCKS, addr);
				this.socket = new Socket(proxy);
				InetSocketAddress dest = new InetSocketAddress(this.TargetOnionName, this.targetPort);//我的
				try {

					sendnewBroadcast(TAG + " StatusCheckerTransport connect::socket is connecting");//socket连接中

					System.out.println(TAG + " StatusCheckerTransport connect:: 网络定时检测连接中： this.socket.connect(dest, 60000");
					this.socket.connect(dest, (int)(60000*timeFactor));//进行连接，耗时操作，且重启tor需要时间


					alarmStatusChange(1);
					status = 1;
					System.out.println(TAG + " StatusCheckerTransport connect:: 网络定时检测连接成功status checker connect to "+ this.TargetOnionName.trim()+":"+this.targetPort);
					break;
				} catch (IOException e) {
					System.out.println(TAG + " StatusCheckerTransport connect:: 网络定时检测连接失败status checker connect to "+ this.TargetOnionName.trim()+":"+this.targetPort+" error "+e.getMessage());
					count = count + 1;
//					this.closeSocket();
					alarmStatusChange(0);
					try {
						System.out.println(TAG + " StatusCheckerTransport connect:: 网络定时检测连接失败"+"Thread.sleep(10000)");
						Thread.sleep((int)(10000*timeFactor));//这段时间留给tor重启
					} catch (InterruptedException interruptedException) {
						interruptedException.printStackTrace();
					}
				}
			}
			return status;
		}
	}





	public void sendnewBroadcast(String message){
		LogUtils.d(TAG, " sendnewBroadcast:: message = " + message);

		Intent intent = new Intent(); // TODO: 2021/7/20 安卓8以后的静态广播都需要 intent.setComponent(new ComponentName())才能让接收器收到广播

		intent.setAction(STATUSCHANGE_ACTION);
		intent.setComponent(new ComponentName(OrbotServiceAction.PACKAGE,
				"com.ucas.chat.ui.login.LoginActivity$ProgressReceiver"));
		intent.putExtra(STATUSCHANGE_MESSAGE, message);
		context.sendBroadcast(intent);

		intent.setComponent(new ComponentName(OrbotServiceAction.PACKAGE,
				"com.ucas.chat.ui.home.NewsFragment$ProgressReceiver"));
		context.sendBroadcast(intent);//要2次才行


	}













}
