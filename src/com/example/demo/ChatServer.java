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
 * 控制台聊天程序 服务端应用程序
 * 
 * @author Jun
 *
 */
public class ChatServer {
	/**
	 * ServerSocket 是运行在服务端的Socket 用来监听端口，等待客户端的连接， 一旦连接成功就会返回与该客户端通信的Socket
	 */
	private ServerSocket mServerSocket;

	/**
	 * 创建线程池来管理客户端的连接线程 避免系统资源过度浪费
	 */
	private ExecutorService mThreadPool;

	/**
	 * 该属性用来存放客户端之间私聊的信息
	 */
	private Map<String, PrintWriter> allOut;

	/**
	 * 构造方法，服务端初始化
	 * @param port 端口号
	 */
	public ChatServer(int port) {
		try {
			/*
			 * 创建ServerSocket，并申请服务端口 将来客户端就是通过该端口连接服务端程序的
			 */
			mServerSocket = new ServerSocket(port);

			/*
			 * 初始化Map集合，存放客户端信息
			 */
			allOut = new HashMap<String, PrintWriter>();			
			

			/*
			 * 初始化线程池，设置线程的数量
			 */
			mThreadPool = Executors.newFixedThreadPool(10);

			/*
			 * 初始化用来存放客户端输出流的集合， 每当一个客户端连接，就会将该客户端的输出流存入该集合；
			 * 每当一个客户端断开连接，就会将集合中该客户端的输出流删除； 每当转发一条信息，就要遍历集合中的所有输出流(元素)
			 * 因此转发的频率高于客户端登入登出的频率， 还是应该使用ArrayList来存储元素，仅限群聊，私聊不行 allOut = new
			 * ArrayList<PrintWriter>();
			 */
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * 将客户端的信息以Map形式存入集合中
	 */
	private void addOut(String key, PrintWriter value) {
		synchronized (this) {
			allOut.put(key, value);
		}
	}

	/*
	 * 将给定的输出流从共享集合中删除 参数为客户端userName,作为Map的key键
	 */
	private synchronized void removeOut(String key) {
		allOut.remove(key);
		System.out.println("当前在线人数为：" + allOut.size());
	}

	/*
	 * 将给定的消息转发给所有客户端
	 */
	private synchronized void sendMsgToAll(String message) {
		for (PrintWriter out : allOut.values()) {
			out.println(message);
			System.out.println("当前在线人数为：" + allOut.size());
		}
	}

	/*
	 * 将给定的消息转发给私聊的客户端
	 */
	private synchronized void sendMsgToPrivate(String nickname, String message) {
		PrintWriter pw = allOut.get(nickname); // 将对应客户端的聊天信息取出作为私聊内容发送出去
		if (pw != null) {
			pw.println(message);
			System.out.println("当前在线私聊人数为：" + allOut.size());
		}
	}

	/**
	 * 服务端启动的方法
	 */
	public void start() {
		try {
			while (true) {
				/*
				 * 监听传入的端口
				 */
				System.out.println("等待客户端连接... ... ");
				/*
				 * Socket accept() 这是一个阻塞方法，会一直在传入的端口进行监听
				 * 直到一个客户端连接上，此时该方法会将与这个客户端进行通信的Socket返回
				 */
				Socket socket = mServerSocket.accept();
				System.out.println("客户端连接成功！ ");

				/*
				 * 启动一个线程，由线程来处理客户端的请求，这样可以再次监听 下一个客户端的连接了
				 */
				Runnable run = new GetClientMsgHandler(socket);
				mThreadPool.execute(run); // 通过线程池来分配线程
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 该线程体用来处理给定的某一个客户端的消息，循环接收客户端发送 的每一个字符串，并输出到控制台
	 * 
	 * @author Jun
	 *
	 */
	class GetClientMsgHandler implements Runnable {
		/*
		 * 该属性是当前线程处理的具体的客户端的Socket
		 * 
		 * @see java.lang.Runnable#run()
		 */
		private Socket mSocket;

		/*
		 * 获取客户端的地址信息 private String hostIP;
		 */

		/*
		 * 获取客户端的昵称
		 */
		private String userName;

		/*
		 * 创建构造方法
		 */
		public GetClientMsgHandler(Socket socket) {
			this.mSocket = socket;

			/*
			 * 获取远端客户的Ip地址信息 保存客户端的IP地址字符串 InetAddress address =
			 * socket.getInetAddress(); hostIP = address.getHostAddress();
			 */
		}

		/*
		 * 创建内部类来获取昵称
		 */
		private String getUserName() throws Exception {
			try {
				// 服务端的输入流读取客户端发送来的昵称输出流
				InputStream iin = mSocket.getInputStream();
				InputStreamReader isr = new InputStreamReader(iin, "UTF-8");
				BufferedReader bReader = new BufferedReader(isr);
				// 服务端将昵称验证结果通过自身的输出流发送给客户端
				OutputStream out = mSocket.getOutputStream();
				OutputStreamWriter iosw = new OutputStreamWriter(out, "UTF-8");
				PrintWriter ipw = new PrintWriter(iosw, true);
				// 读取客户端发来的昵称
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
				 * 通过客户端的Socket获取客户端的输出流 用来将消息发送给客户端
				 */
				OutputStream os = mSocket.getOutputStream();
				OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
				pw = new PrintWriter(osw, true);

				/*
				 * 将客户昵称和其所说的话作为元素存入共享集合HashMap中
				 */
				userName = getUserName();
				addOut(userName, pw);
				Thread.sleep(100);

				/*
				 * 服务端通知所有客户端，某用户登录
				 */
				sendMsgToAll("[系统通知]：欢迎**" + userName + "**登陆聊天室!");

				/*
				 * 通过客户端的Socket获取输入流 读取客户端发送来的信息
				 */
				InputStream is = mSocket.getInputStream();
				InputStreamReader isr = new InputStreamReader(is, "UTF-8");
				BufferedReader br = new BufferedReader(isr);
				String msgString = null;

				while ((msgString = br.readLine()) != null) {
					// 验证是否是私聊
					if (msgString.startsWith("@")) {
						/*
						 * 私聊格式：@昵称：内容
						 */
						int index = msgString.indexOf(":");
						if (index >= 0) {
							// 获取昵称
							String name = msgString.substring(1, index);
							String info = msgString.substring(index + 1, msgString.length());
							info = userName + "对你说：" + info;
							// 将私聊信息发送出去
							sendMsgToPrivate(name, info);
							// 服务端不在广播私聊的信息
							continue;
						}
					}
					/*
					 * 遍历所有输出流，将该客户端发送的信息转发给所有客户端
					 */
					System.out.println(userName + "说：" + msgString);
					sendMsgToAll(userName + "说：" + msgString);
				}
			} catch (Exception e) {
				/*
				 * 因为Win系统用户的客户端断开连接后，br.readLine()方法读取
				 * 不到信息就会抛出异常，而Linux系统会持续发送null； 因此这里就不在将捕获的异常抛出了。
				 */
			} finally {
				/*
				 * 当执行到此处时，说明客户端已经与服务端断开连接 则将该客户端存在共享集合中的输出流删除
				 */
				removeOut(userName);
				/*
				 * 通知所有客户端，某某客户已经下线
				 */
				sendMsgToAll("[系统通知]：" + userName + "已经下线了。");

				/*
				 * 关闭socket，则通过Socket获取的输入输出流也一同关闭了
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