package com.example.changoo.mafia.network;

import java.io.IOException;

import com.example.changoo.mafia.command.ChatCommand;
import com.example.changoo.mafia.command.Command;
import com.example.changoo.mafia.command.LoginCommand;
import com.example.changoo.mafia.command.PlayCommand;
import com.example.changoo.mafia.command.WaitCommand;
import com.example.changoo.mafia.log.Logger;
import com.example.changoo.mafia.logic.GameLogic;
import com.example.changoo.mafia.model.UserInfo;
import com.example.changoo.mafia.model.UserManager;

public class MyNetwork extends Thread {
	private Thread receiveMsg;
	private MySocket mySocket;
	private UserManager userManager;
	private String myName;
	private GameLogic gameLogic;

	public MyNetwork(MySocket mySocket, UserManager users) {
		this.mySocket = mySocket;
		this.userManager = users;
		this.gameLogic = new GameLogic(userManager);
	}

	public void stopRecvmsg() {
		receiveMsg = null;
	}

	public void run() {
		try {
			receiveMsg = new Thread(new ReceiveMsgThread());
			receiveMsg.setDaemon(true);
			receiveMsg.start();
		} catch (Exception e) {
			e.printStackTrace();

		}
	}

	public void broad_cast(String recv_command, String recv_username, Object recv_object) {
		for (int i = 0; i < userManager.size(); i++) {
			UserInfo imsi = userManager.getUser(i);
			MyNetwork network = userManager.getUserNetwork(imsi);
			network.send_Message(recv_command, recv_username, recv_object);
		}
	}

	public void send_Message_ToMafia(String recv_command, String recv_username, Object recv_object) {
		for (int i = 0; i < userManager.size(); i++) {
			UserInfo imsi = userManager.getUser(i);
			if (imsi.getCharacter().getName().equals("MAFIA")) {
				MyNetwork network = userManager.getUserNetwork(imsi);
				network.send_Message(recv_command, recv_username, recv_object);
			}
		}
	}

	public void send_Message_ToCop(String recv_command, String recv_username, Object recv_object) {
		for (int i = 0; i < userManager.size(); i++) {
			UserInfo imsi = userManager.getUser(i);
			if (imsi.getCharacter().getName().equals("COP")) {
				MyNetwork network = userManager.getUserNetwork(imsi);
				network.send_Message(recv_command, recv_username, recv_object);
			}
		}
	}

	public void send_Message_ToDoctor(String recv_command, String recv_username, Object recv_object) {
		for (int i = 0; i < userManager.size(); i++) {
			UserInfo imsi = userManager.getUser(i);
			if (imsi.getCharacter().getName().equals("DOCTOR")) {
				MyNetwork network = userManager.getUserNetwork(imsi);
				network.send_Message(recv_command, recv_username, recv_object);
			}
		}
	}

	public void send_Message_ToCivil(String recv_command, String recv_username, Object recv_object) {
		for (int i = 0; i < userManager.size(); i++) {
			UserInfo imsi = userManager.getUser(i);
			if (imsi.getCharacter().getName().equals("CIVIL")) {
				MyNetwork network = userManager.getUserNetwork(imsi);
				network.send_Message(recv_command, recv_username, recv_object);
			}
		}
	}

	public void send_Message(String recv_command, String recv_username, Object recv_object) {
		try {
			Logger.append("SEND " + recv_command + "  >>>>>  To. " + myName + "\n");
			Logger.append("\n");
			mySocket.writeObject(recv_command);
			mySocket.writeObject(recv_username);
			mySocket.writeObject(recv_object);

		} catch (IOException | ClassNotFoundException e) {
			Logger.append(myName.toString() + "\n" + "메시지 송신 에러 발생\n");
		}
	}

	class ReceiveMsgThread implements Runnable {
		@SuppressWarnings("null")
		@Override
		public void run() {
			while (Thread.currentThread() == receiveMsg) {
				try {
					String recv_command = (String) mySocket.readObject();
					String recv_userinfo = (String) mySocket.readObject();
					Object recv_object = mySocket.readObject();
					Logger.append("RECV " + recv_command + "  <<<<  By. " + recv_userinfo + "\n");
					Logger.append("\n");

					recvMsgLogic(recv_command, recv_userinfo, recv_object);

				} catch (IOException | ClassNotFoundException e) {
					try {
						e.printStackTrace();
						mySocket.close();
						userManager.removeUser(myName);
						broad_cast(Command.USERUPDATE, myName, userManager.getUsers());
						Logger.append(userManager.size() + " : 현재 벡터에 담겨진 사용자 수\n");
						Logger.append("사용자 접속 끊어짐 자원 반납\n");
						return;

					} catch (Exception ee) {
					}
				}
			}
		}

	}// run메소드 끝

	public void recvMsgLogic(String recv_command, String recv_username, Object recv_object) throws IOException {
		switch (recv_command) {
		case Command.REQUESTNULL:
			broad_cast(Command.REQUESTNULL, null, null);
			break;
		case LoginCommand.REQUESTCONFIRMNAME:
			if (userManager.checkingName(recv_username) == true) {
				send_Message(LoginCommand.CONFIRMNAME, recv_username, true);
				myName = recv_username;
				Logger.append("동일 이름 확인되지 않음, 접속 가능!\n");
				send_Message(LoginCommand.GOWAITROOM, recv_username, true);
				userManager.addUser(recv_username);
				userManager.addUserNetwork(recv_username, MyNetwork.this);
			} else {
				send_Message(LoginCommand.CONFIRMNAME, recv_username, false);
				Logger.append("동일 이름 확인, 접속 불가!\n");
			}
			break;

		case WaitCommand.IMWAITACTIVITY:
			broad_cast(WaitCommand.NOTICE, null, recv_username + " 님이 입장하셨습니다!");
			broad_cast(Command.USERUPDATE, null, userManager.getUsers());
			break;

		case WaitCommand.IMREADY:
			UserInfo userinfo = userManager.getUser(recv_username);
			userinfo.setState((String) recv_object);
			broad_cast(Command.USERUPDATE, null, userManager.getUsers());
			if (((String) recv_object).equals("ready")) {
				broad_cast(WaitCommand.NOTICE, null, recv_username + " 님 READY!!");
				if (gameLogic.isPlayAvailable() == true) {
					Logger.append("---- GAME START-------\n");
					new Thread() {
						public void run() {
							int count = 5;
							while (count > 0 && gameLogic.isPlayAvailable() == true) {
								count--;
								broad_cast(WaitCommand.NOTICE, null, "Count..." + count);
								try {
									Thread.sleep(1000);
								} catch (InterruptedException e) {
									return;
								}
							}

							if (count == 0) {
								broad_cast(WaitCommand.NOTICE, null, "게임 스타트!!!!");

								if (gameLogic.updateCharacter())
									Logger.append("---- Character update-------\n");
								broad_cast(Command.USERUPDATE, null, userManager.getUsers());
								broad_cast(WaitCommand.STARTGAME, null, null);

							}

						}
					}.start();

				}
			} else
				broad_cast(WaitCommand.NOTICE, recv_username, recv_username + " 님 WAIT!!");
			break;

		case PlayCommand.IMSTARTGAME:
			UserInfo userinfo2 = userManager.getUser(recv_username);
			userinfo2.setState((String) recv_object);
			broad_cast(Command.USERUPDATE, null, userManager.getUsers());
			if (userManager.isAllUserPlay()) {
				broad_cast(ChatCommand.CHATNOTICE, null, "게임이 시작 되었습니다");
				send_Message_ToMafia(ChatCommand.CHATNOTICE, null, "당신은 마피아 입니다.\n사람을 죽이십시오.");
				send_Message_ToCop(ChatCommand.CHATNOTICE, null, "당신은 경찰 입니다.\n마피아를 찾아주세요.");
				send_Message_ToDoctor(ChatCommand.CHATNOTICE, null, "당신은 의사 입니다.\n마피아로 부터 구해주세요.");
				send_Message_ToCivil(ChatCommand.CHATNOTICE, null, "당신은 시민 입니다.\n");
			}
			break;

		case PlayCommand.IMINSUNNY:
			UserInfo userinfo3 = userManager.getUser(recv_username);
			userinfo3.setWhen((String) recv_object);
			broad_cast(Command.USERUPDATE, null, userManager.getUsers());
			if (gameLogic.isSunnyTimerAvailable()) {
				new Thread() {
					public void run() {
						Integer timer = 100;
						while (gameLogic.isSunnyTimerAvailable()) {
							timer--;
							broad_cast(PlayCommand.TIMER, null, timer);
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
							}
						}
					}
				}.start();
			}
			break;

		case ChatCommand.SENDMESSAGE:
			broad_cast(ChatCommand.SENDMESSAGE, recv_username, recv_object);
			break;

		}
	}

}