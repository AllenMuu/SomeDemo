package com.example.demo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ����̨������� �����Ӧ�ó���
 * 
 * @author Jun
 *
 */
public class ChatServer {
	/**
	 * ServerSocket �������ڷ���˵�Socket ���������˿ڣ��ȴ��ͻ��˵����ӣ� һ�����ӳɹ��ͻ᷵����ÿͻ���ͨ�ŵ�Socket
	 */
	private ServerSocket mServerSocket;

	/**
	 * �����̳߳�������ͻ��˵������߳� ����ϵͳ��Դ�����˷�
	 */
	private ExecutorService mThreadPool;

	/**
	 * ������������ſͻ���֮��˽�ĵ���Ϣ
	 */
	private Map<String, PrintWriter> allOut;

	/**
	 * ���췽��������˳�ʼ��
	 * @param port �˿ں�
	 */
	public ChatServer(int port) {
		try {
			/*
			 * ����ServerSocket�����������˿� �����ͻ��˾���ͨ���ö˿����ӷ���˳����
			 */
			mServerSocket = new ServerSocket(port);

			/*
			 * ��ʼ��Map���ϣ���ſͻ�����Ϣ
			 */
			allOut = new HashMap<String, PrintWriter>();			
			

			/*
			 * ��ʼ���̳߳أ������̵߳�����
			 */
			mThreadPool = Executors.newFixedThreadPool(10);

			/*
			 * ��ʼ��������ſͻ���������ļ��ϣ� ÿ��һ���ͻ������ӣ��ͻὫ�ÿͻ��˵����������ü��ϣ�
			 * ÿ��һ���ͻ��˶Ͽ����ӣ��ͻὫ�����иÿͻ��˵������ɾ���� ÿ��ת��һ����Ϣ����Ҫ���������е����������(Ԫ��)
			 * ���ת����Ƶ�ʸ��ڿͻ��˵���ǳ���Ƶ�ʣ� ����Ӧ��ʹ��ArrayList���洢Ԫ�أ�����Ⱥ�ģ�˽�Ĳ��� allOut = new
			 * ArrayList<PrintWriter>();
			 */
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * ���ͻ��˵���Ϣ��Map��ʽ���뼯����
	 */
	private void addOut(String key, PrintWriter value) {
		synchronized (this) {
			allOut.put(key, value);
		}
	}

	/*
	 * ��������������ӹ�������ɾ�� ����Ϊ�ͻ���userName,��ΪMap��key��
	 */
	private synchronized void removeOut(String key) {
		allOut.remove(key);
		System.out.println("��ǰ��������Ϊ��" + allOut.size());
	}

	/*
	 * ����������Ϣת�������пͻ���
	 */
	private synchronized void sendMsgToAll(String message) {
		for (PrintWriter out : allOut.values()) {
			out.println(message);
			System.out.println("��ǰ��������Ϊ��" + allOut.size());
		}
	}

	/*
	 * ����������Ϣת����˽�ĵĿͻ���
	 */
	private synchronized void sendMsgToPrivate(String nickname, String message) {
		PrintWriter pw = allOut.get(nickname); // ����Ӧ�ͻ��˵�������Ϣȡ����Ϊ˽�����ݷ��ͳ�ȥ
		if (pw != null) {
			pw.println(message);
			System.out.println("��ǰ����˽������Ϊ��" + allOut.size());
		}
	}

	/**
	 * ����������ķ���
	 */
	public void start() {
		try {
			while (true) {
				/*
				 * ��������Ķ˿�
				 */
				System.out.println("�ȴ��ͻ�������... ... ");
				/*
				 * Socket accept() ����һ��������������һֱ�ڴ���Ķ˿ڽ��м���
				 * ֱ��һ���ͻ��������ϣ���ʱ�÷����Ὣ������ͻ��˽���ͨ�ŵ�Socket����
				 */
				Socket socket = mServerSocket.accept();
				System.out.println("�ͻ������ӳɹ��� ");

				/*
				 * ����һ���̣߳����߳�������ͻ��˵��������������ٴμ��� ��һ���ͻ��˵�������
				 */
				Runnable run = new GetClientMsgHandler(socket);
				mThreadPool.execute(run); // ͨ���̳߳��������߳�
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * ���߳����������������ĳһ���ͻ��˵���Ϣ��ѭ�����տͻ��˷��� ��ÿһ���ַ����������������̨
	 * 
	 * @author Jun
	 *
	 */
	class GetClientMsgHandler implements Runnable {
		/*
		 * �������ǵ�ǰ�̴߳���ľ���Ŀͻ��˵�Socket
		 * 
		 * @see java.lang.Runnable#run()
		 */
		private Socket mSocket;

		/*
		 * ��ȡ�ͻ��˵ĵ�ַ��Ϣ private String hostIP;
		 */

		/*
		 * ��ȡ�ͻ��˵��ǳ�
		 */
		private String userName;

		/*
		 * �������췽��
		 */
		public GetClientMsgHandler(Socket socket) {
			this.mSocket = socket;

			/*
			 * ��ȡԶ�˿ͻ���Ip��ַ��Ϣ ����ͻ��˵�IP��ַ�ַ��� InetAddress address =
			 * socket.getInetAddress(); hostIP = address.getHostAddress();
			 */
		}

		/*
		 * �����ڲ�������ȡ�ǳ�
		 */
		private String getUserName() throws Exception {
			try {
				// ����˵���������ȡ�ͻ��˷��������ǳ������
				InputStream iin = mSocket.getInputStream();
				InputStreamReader isr = new InputStreamReader(iin, "UTF-8");
				BufferedReader bReader = new BufferedReader(isr);
				// ����˽��ǳ���֤���ͨ���������������͸��ͻ���
				OutputStream out = mSocket.getOutputStream();
				OutputStreamWriter iosw = new OutputStreamWriter(out, "UTF-8");
				PrintWriter ipw = new PrintWriter(iosw, true);
				// ��ȡ�ͻ��˷������ǳ�
				String nameString = bReader.readLine();
				while (true) {
					if (nameString.trim().length() == 0) {
						ipw.println("FAIL");
					}
					if (allOut.containsKey(nameString)) {
						ipw.println("FAIL");
					} else {
						ipw.println("OK");
						return nameString;
					}
					nameString = bReader.readLine();
				}
			} catch (Exception e) {
				throw e;
			}
		}

		@Override
		public void run() {
			PrintWriter pw = null;
			try {
				/*
				 * ͨ���ͻ��˵�Socket��ȡ�ͻ��˵������ ��������Ϣ���͸��ͻ���
				 */
				OutputStream os = mSocket.getOutputStream();
				OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
				pw = new PrintWriter(osw, true);

				/*
				 * ���ͻ��ǳƺ�����˵�Ļ���ΪԪ�ش��빲����HashMap��
				 */
				userName = getUserName();
				addOut(userName, pw);
				Thread.sleep(100);

				/*
				 * �����֪ͨ���пͻ��ˣ�ĳ�û���¼
				 */
				sendMsgToAll("[ϵͳ֪ͨ]����ӭ**" + userName + "**��½������!");

				/*
				 * ͨ���ͻ��˵�Socket��ȡ������ ��ȡ�ͻ��˷���������Ϣ
				 */
				InputStream is = mSocket.getInputStream();
				InputStreamReader isr = new InputStreamReader(is, "UTF-8");
				BufferedReader br = new BufferedReader(isr);
				String msgString = null;

				while ((msgString = br.readLine()) != null) {
					// ��֤�Ƿ���˽��
					if (msgString.startsWith("@")) {
						/*
						 * ˽�ĸ�ʽ��@�ǳƣ�����
						 */
						int index = msgString.indexOf(":");
						if (index >= 0) {
							// ��ȡ�ǳ�
							String name = msgString.substring(1, index);
							String info = msgString.substring(index + 1, msgString.length());
							info = userName + "����˵��" + info;
							// ��˽����Ϣ���ͳ�ȥ
							sendMsgToPrivate(name, info);
							// ����˲��ڹ㲥˽�ĵ���Ϣ
							continue;
						}
					}
					/*
					 * ������������������ÿͻ��˷��͵���Ϣת�������пͻ���
					 */
					System.out.println(userName + "˵��" + msgString);
					sendMsgToAll(userName + "˵��" + msgString);
				}
			} catch (Exception e) {
				/*
				 * ��ΪWinϵͳ�û��Ŀͻ��˶Ͽ����Ӻ�br.readLine()������ȡ
				 * ������Ϣ�ͻ��׳��쳣����Linuxϵͳ���������null�� �������Ͳ��ڽ�������쳣�׳��ˡ�
				 */
			} finally {
				/*
				 * ��ִ�е��˴�ʱ��˵���ͻ����Ѿ������˶Ͽ����� �򽫸ÿͻ��˴��ڹ������е������ɾ��
				 */
				removeOut(userName);
				/*
				 * ֪ͨ���пͻ��ˣ�ĳĳ�ͻ��Ѿ�����
				 */
				sendMsgToAll("[ϵͳ֪ͨ]��" + userName + "�Ѿ������ˡ�");

				/*
				 * �ر�socket����ͨ��Socket��ȡ�����������Ҳһͬ�ر���
				 */
				if (mSocket != null) {
					try {
						mSocket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	public static void main(String[] args) {
		ChatServer server = new ChatServer(110);
		server.start();
	}
}