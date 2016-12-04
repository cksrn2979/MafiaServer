package com.example.changoo.mafia.network;

import java.io.IOException;

import com.example.changoo.mafia.command.ChatCommand;
import com.example.changoo.mafia.command.Command;
import com.example.changoo.mafia.command.LoginCommand;
import com.example.changoo.mafia.command.PlayCommand;
import com.example.changoo.mafia.command.WaitCommand;
import com.example.changoo.mafia.log.Logger;
import com.example.changoo.mafia.logic.GameLogic;
import com.example.changoo.mafia.model.SocketData;
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

	public void send_Message_ToCivil(String send_command, String send_name, Object send_object) {
		for (int i = 0; i < userManager.size(); i++) {
			UserInfo imsi = userManager.getUser(i);
			if (imsi.getCharacter().getName().equals("CIVIL")) {
				MyNetwork network = userManager.getUserNetwork(imsi);
				network.send_Message(send_command, send_name, send_object);
			}
		}
	}

	public void send_Message(String send_command, String send_name, Object send_object) {
		try {
			Logger.append("SEND " + send_command + "  >>>>>  To. " + myName + "\n");
			Logger.append("\n");
			mySocket.writeObject(send_command);
			mySocket.writeObject(send_name);
			mySocket.writeObject(send_object);
			mySocket.outFlush();

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
		UserInfo userinfo;
		switch (recv_command) {

		case LoginCommand.REQUESTCONFIRMNAME:
			if (userManager.checkingName(recv_username) == true) {
				send_Message(LoginCommand.CONFIRMNAME, recv_username, true);
				myName = recv_username;
				Logger.append("동일 이름 확인되지 않음, 접속 가능!\n");
				userManager.addUser(recv_username);
				userManager.addUserNetwork(recv_username, MyNetwork.this);
				Logger.append("유저 추가, 접속 가능!\n");
				send_Message(LoginCommand.GOWAITROOM, recv_username, true);

			} else {
				send_Message(LoginCommand.CONFIRMNAME, recv_username, false);
				Logger.append("동일 이름 확인, 접속 불가!\n");
			}
			break;

		case WaitCommand.IMWAITACTIVITY:
			broad_cast(WaitCommand.NOTICE, "", recv_username + " 님이 입장하셨습니다!");
			broad_cast(Command.USERUPDATE, "", userManager.getUsers());
			break;

		case WaitCommand.IMREADY:
			userinfo = userManager.getUser(recv_username);
			userinfo.setState((String) recv_object);
			broad_cast(Command.USERUPDATE, "", userManager.getUsers());

			if (((String) recv_object).equals("ready"))
				broad_cast(WaitCommand.NOTICE, "", recv_username + " 님 READY!!");
			else
				broad_cast(WaitCommand.NOTICE, recv_username, recv_username + " 님 WAIT!!");

			if (userManager.isAllUserReady())
				gameLogic.setState("ready");

			if (gameLogic.getState().equals("ready") && gameLogic.isInsizeUserNumber()) {
				Logger.append("---- GAME START-------\n");
				new Thread() {
					public void run() {
						int count = 5;
						while (count > 0 && gameLogic.getState().equals("ready")
								&& gameLogic.isInsizeUserNumber() == true) {
							count--;
							broad_cast(WaitCommand.NOTICE, "", "Count..." + count);
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								return;
							}
						}

						if (count == 0) {
							broad_cast(WaitCommand.NOTICE, "", "게임 스타트!!!!");

							if (gameLogic.updateCharacter())
								Logger.append("---- Character update-------\n");
							broad_cast(Command.USERUPDATE, "", userManager.getUsers());
							broad_cast(WaitCommand.STARTGAME, "", "");

						}

					}
				}.start();

			}

			break;

		case PlayCommand.IMSTARTGAME:
			userinfo = userManager.getUser(recv_username);
			userinfo.setState((String) recv_object);
			if (userManager.isAllUserPlay())
				gameLogic.setState("play");

			broad_cast(Command.USERUPDATE, "", userManager.getUsers());
			if (userManager.isAllUserPlay()) {
				broad_cast(ChatCommand.CHATNOTICE, "", "게임이 시작 되었습니다");
				send_Message_ToMafia(ChatCommand.CHATNOTICE, "", "당신은 마피아 입니다.\n사람을 죽이십시오.");
				send_Message_ToCop(ChatCommand.CHATNOTICE, "", "당신은 경찰 입니다.\n마피아를 찾아주세요.");
				send_Message_ToDoctor(ChatCommand.CHATNOTICE, "", "당신은 의사 입니다.\n마피아로 부터 구해주세요.");
				send_Message_ToCivil(ChatCommand.CHATNOTICE, "", "당신은 시민 입니다.\n");
			}
			break;

		case PlayCommand.IMINSUNNY:
			userinfo = userManager.getUser(recv_username);
			userinfo.setWhen((String) recv_object);
			broad_cast(Command.USERUPDATE, "", userManager.getUsers());

			if (userManager.isAllUserInSunny())
				gameLogic.setWhen("sunny");

			if (gameLogic.getWhen().equals("sunny") && gameLogic.getState().equals("play")) {
				new Thread() {
					public void run() {
						Integer timer = 100;
						while (gameLogic.getWhen().equals("sunny") && gameLogic.getState().equals("play")) {
							timer--;
							broad_cast(PlayCommand.TIMER, "", timer);
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
							}
						}
					}
				}.start();
			}
			break;

		case PlayCommand.IWANTNEXT:
			userinfo = userManager.getUser(recv_username);
			userinfo.setWantnext((boolean) recv_object);
			if((boolean) recv_object==true)
				broad_cast(ChatCommand.CHATNOTICE, "server", recv_username + "님이 밤으로 가길 원합니다.");
			else			
				broad_cast(ChatCommand.CHATNOTICE, "server", recv_username + "님이 밤으로 가길 원하지 않습니다.");
			
			if (userManager.isAllUserWantNext() == true){
				broad_cast(ChatCommand.CHATNOTICE, "server", "모든 플레이어가 밤으로 가길 원합니다.");
				gameLogic.setWantnext(true);
			}
			
			if (gameLogic.isWantnext() == true && gameLogic.getState().equals("play")
					&& gameLogic.getWhen().equals("sunny")){
				gameLogic.setState("night");
				broad_cast(ChatCommand.CHATNOTICE, "server", "밤이 찾아 왔습니다..투표를 시작합니다");
				broad_cast(PlayCommand.GONIGHT, "", "");
				gameLogic.newChoice();
			}

			break;
		case PlayCommand.CHOICUSER:
			gameLogic.updateChoice(recv_username, (String)recv_object);
			if(gameLogic.isAllUserChoice()==true){
				String name=gameLogic.getMaxChocieUsername();
				userManager.getUser(name).setState("die");
				send_Message(Command.USERUPDATE, "server", userManager.getUsers());
			}
			
	

		case ChatCommand.SENDMESSAGE:
			broad_cast(ChatCommand.SENDMESSAGE, recv_username, recv_object);
			break;

		}
	}

}