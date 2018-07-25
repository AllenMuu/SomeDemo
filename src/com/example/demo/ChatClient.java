package com.example.demo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 * 控制台聊天程序 客户端应用程序
 * 
 * @author Jun
 *
 */
public class ChatClient {
	/**
	 * 客户端用于与服务端连接的Socket
	 */
	private Socket mScoket;

	/**
	 * 构造方法，客户端初始化
	 * 
	 * @param host地址
	 * @param port
	 *            端口号
	 */
	public ChatClient(String ipAddress, int port) {
		try {
			/*
			 * socket(String host, int port) 地址： IP地址，用来定位网络上的计算机 端口：
			 * 用来找到远端计算机上用来连接的服务端应用程序
			 */
			mScoket = new Socket(ipAddress, port);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 客户端录入用户名发送到服务端验证用户名是否存在.
	 * 
	 * @param 为Scanner
	 */
	private void inputUserName(Scanner scan) throws Exception {
		String userName = null;
		// 创建输出流
		PrintWriter pw = new PrintWriter(new OutputStreamWriter(mScoket.getOutputStream(), "UTF-8"), true);
		// 创建输入流
		BufferedReader br = new BufferedReader(new InputStreamReader(mScoket.getInputStream(), "UTF-8"));

		while (true) {
			System.out.println("请创建您的昵称：");
			userName = scan.nextLine();
			if (userName.trim().equals("")) {//--判断昵称是否为空.
				System.out.println("昵称不得为空");
			} else {
				pw.println(userName);
				String pass = br.readLine();
				if (pass != null && !pass.equals("OK")) {
					System.out.println("昵称已经被占用，请更换！");
				} else {
					System.out.println("你好！" + userName + "可以开始聊天了");
					break;
				}
			}
		}
	}

	/**
	 * 客户端启动的方法
	 */
	public void start() {
		try {
			/*
			 * 创建Scanner，读取用户输入内容 目的是设置客户端的昵称
			 */
			Scanner scanner = new Scanner(System.in);
			inputUserName(scanner);

			/*
			 * 将用于接收服务器端发送过来的信息的线程启动
			 */
			Runnable run = new GetServerMsgHandler();
			Thread t = new Thread(run);
			t.start();

			/*
			 * 建立输出流，给服务端发信息
			 */
			OutputStream os = mScoket.getOutputStream();
			OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
			//--true  autoFlush
			PrintWriter pw = new PrintWriter(osw, true);

			//--循环.向服务端发送消息.
			while (true) {
				pw.println(scanner.nextLine());
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (mScoket != null) {
				try {
					mScoket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 该线程体用来循环读取服务端发送过来的信息 并输出到客户端的控制台
	 * 
	 * @param args
	 */
	class GetServerMsgHandler implements Runnable {
		@Override
		public void run() {
			try {
				InputStream is = mScoket.getInputStream();
				InputStreamReader isr = new InputStreamReader(is, "UTF-8");
				BufferedReader br = new BufferedReader(isr);
				String msgString = null;
				while ((msgString = br.readLine()) != null) {
					System.out.println("服务端提示：" + msgString);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		ChatClient client = new ChatClient("127.0.0.1", 110);
		client.start();
	}
}