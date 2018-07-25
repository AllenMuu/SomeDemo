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
 * ����̨������� �ͻ���Ӧ�ó���
 * 
 * @author Jun
 *
 */
public class ChatClient {
	/**
	 * �ͻ����������������ӵ�Socket
	 */
	private Socket mScoket;

	/**
	 * ���췽�����ͻ��˳�ʼ��
	 * 
	 * @param host��ַ
	 * @param port
	 *            �˿ں�
	 */
	public ChatClient(String ipAddress, int port) {
		try {
			/*
			 * socket(String host, int port) ��ַ�� IP��ַ��������λ�����ϵļ���� �˿ڣ�
			 * �����ҵ�Զ�˼�������������ӵķ����Ӧ�ó���
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
	 * �ͻ���¼���û������͵��������֤�û����Ƿ����.
	 * 
	 * @param ΪScanner
	 */
	private void inputUserName(Scanner scan) throws Exception {
		String userName = null;
		// ���������
		PrintWriter pw = new PrintWriter(new OutputStreamWriter(mScoket.getOutputStream(), "UTF-8"), true);
		// ����������
		BufferedReader br = new BufferedReader(new InputStreamReader(mScoket.getInputStream(), "UTF-8"));

		while (true) {
			System.out.println("�봴�������ǳƣ�");
			userName = scan.nextLine();
			if (userName.trim().equals("")) {//--�ж��ǳ��Ƿ�Ϊ��.
				System.out.println("�ǳƲ���Ϊ��");
			} else {
				pw.println(userName);
				String pass = br.readLine();
				if (pass != null && !pass.equals("OK")) {
					System.out.println("�ǳ��Ѿ���ռ�ã��������");
				} else {
					System.out.println("��ã�" + userName + "���Կ�ʼ������");
					break;
				}
			}
		}
	}

	/**
	 * �ͻ��������ķ���
	 */
	public void start() {
		try {
			/*
			 * ����Scanner����ȡ�û��������� Ŀ�������ÿͻ��˵��ǳ�
			 */
			Scanner scanner = new Scanner(System.in);
			inputUserName(scanner);

			/*
			 * �����ڽ��շ������˷��͹�������Ϣ���߳�����
			 */
			Runnable run = new GetServerMsgHandler();
			Thread t = new Thread(run);
			t.start();

			/*
			 * �����������������˷���Ϣ
			 */
			OutputStream os = mScoket.getOutputStream();
			OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
			//--true  autoFlush
			PrintWriter pw = new PrintWriter(osw, true);

			//--ѭ��.�����˷�����Ϣ.
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
	 * ���߳�������ѭ����ȡ����˷��͹�������Ϣ ��������ͻ��˵Ŀ���̨
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
					System.out.println("�������ʾ��" + msgString);
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