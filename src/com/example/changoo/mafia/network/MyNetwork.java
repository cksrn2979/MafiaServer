package com.example.changoo.mafia.network;

import java.io.IOException;

import javax.swing.plaf.basic.DefaultMenuLayout;

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

	public MyNetwork(MySocket mySocket, UserManager users, GameLogic gameLogic) {
		this.mySocket = mySocket;
		this.userManager = users;
		this.gameLogic = gameLogic;
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

	public void broad_cast(String send_command, String send_name, Object send_object) {
		for (int i = 0; i < userManager.size(); i++) {
			UserInfo imsi = userManager.getUser(i);
			MyNetwork network = userManager.getUserNetwork(imsi);
			network.send_Message(send_command, send_name, send_object);
		}
	}

	public void send_Message_ToMafia(String send_command, String send_name, Object send_object) {
		for (int i = 0; i < userManager.size(); i++) {
			UserInfo imsi = userManager.getUser(i);
			if (imsi.getCharacter().equals("MAFIA")) {
				MyNetwork network = userManager.getUserNetwork(imsi);
				network.send_Message(send_command, send_name, send_object);
			}
		}
	}

	public void send_Message_ToCop(String send_command, String send_name, Object send_object) {
		for (int i = 0; i < userManager.size(); i++) {
			UserInfo imsi = userManager.getUser(i);
			if (imsi.getCharacter().equals("COP")) {
				MyNetwork network = userManager.getUserNetwork(imsi);
				network.send_Message(send_command, send_name, send_object);
			}
		}
	}

	public void send_Message_ToDoctor(String send_command, String send_name, Object send_object) {
		for (int i = 0; i < userManager.size(); i++) {
			UserInfo imsi = userManager.getUser(i);
			if (imsi.getCharacter().equals("DOCTOR")) {
				MyNetwork network = userManager.getUserNetwork(imsi);
				network.send_Message(send_command, send_name, send_object);
			}
		}
	}

	public void send_Message_ToCivil(String send_command, String send_name, Object send_object) {
		for (int i = 0; i < userManager.size(); i++) {
			UserInfo imsi = userManager.getUser(i);
			if (imsi.getCharacter().equals("CIVIL")) {
				MyNetwork network = userManager.getUserNetwork(imsi);
				network.send_Message(send_command, send_name, send_object);
			}
		}
	}

	public void send_Message(String send_command, String send_name, Object send_object) {
		try {
			Logger.append("SEND " + send_command + "  >>>>>  To. " + myName + "\n");
			Logger.append("\n");
			mySocket.writeObject(new SocketData(send_command, send_name, send_object));

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

					SocketData socketData = (SocketData) mySocket.readObject();

					String recv_command = socketData.getCommand();
					String recv_name = socketData.getName();
					Object recv_object = socketData.getObject();
					Logger.append("RECV " + recv_command + "  <<<<  By. " + recv_name + "\n");
					Logger.append("\n");

					recvMsgLogic(recv_command, recv_name, recv_object);

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

	///// *받은 데이터의 , 맞는 로직 구현*/
	private void recvMsgLogic(String recv_command, String recv_name, Object recv_object) throws IOException {
		UserInfo userinfo;

		switch (recv_command) {

		/* 이름 확인 요청 */
		case LoginCommand.REQUESTCONFIRMNAME:

			/* 동일 이름 확인, 접속가능  */
			if (userManager.checkingName(recv_name) == true) {
				Logger.append("동일 이름 확인되지 않음, 접속 가능!\n");
				send_Message(LoginCommand.CONFIRMNAME, recv_name, true);
				myName = recv_name;				
				userManager.addUser(recv_name);
				userManager.addUserNetwork(recv_name, MyNetwork.this);
				Logger.append("유저 추가, 접속 가능!\n");
				send_Message(LoginCommand.GOWAITROOM, recv_name, true);

			} 
			
			/*동일 이름 확인, 접속 불가*/
			else {
				send_Message(LoginCommand.CONFIRMNAME, recv_name, false);
				Logger.append("동일 이름 확인, 접속 불가!\n");
			}
			break;

		/* 유저 대기실로 입장 */
		case WaitCommand.IMWAITACTIVITY:
			broad_cast(WaitCommand.NOTICE, "", recv_name + " 님이 입장하셨습니다!");
			broad_cast(Command.USERUPDATE, "", userManager.getUsers());
			break;

		/* 유저 레디 */
		case WaitCommand.IMREADY:
			userinfo = userManager.getUser(recv_name);
			userinfo.setState((String) recv_object);
			broad_cast(Command.USERUPDATE, "", userManager.getUsers());

			if (((String) recv_object).equals("ready"))
				broad_cast(WaitCommand.NOTICE, "", recv_name + " 님 READY!!");
			else if (((String) recv_object).equals("wait"))
				broad_cast(WaitCommand.NOTICE, "", recv_name + " 님 WAIT!!");

			/*모든 유저가 레디 상태인지 확인, 게임 상태 변경 */
			if (userManager.isAllUserReady())
				gameLogic.setState("ready");
			else
				gameLogic.setState("wait");

			/*게임 상태가 레디이고, 유저 인원 수가 가능한 인원인지 확인 */
			if (gameLogic.getState().equals("ready") && gameLogic.isInsizeUserNumber()) {
				
				/*카운트 다운을 시작함 */
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
						
						/*카운트 다운이 정상적으로 끝날경우, 게임을 시작함*/
						if (count == 0) {
							Logger.append("---- GAME START-------\n");
							broad_cast(WaitCommand.NOTICE, "", "게임 스타트!!!!");
							
							/*유저들에게 직업을 부여함 */
							if (gameLogic.updateCharacter())
								Logger.append("---- Character update-------\n");
							
							/*요저 정보 갱신을 요청함 */
							broad_cast(Command.USERUPDATE, "", userManager.getUsers());
							
							/*게임을 시작 하기를 요청함 */
							broad_cast(WaitCommand.STARTGAME, "", "");

						}

					}
				}.start();

			}

			break;

		/* 유저 게임 시작 */
		case PlayCommand.IMSTARTGAME:
			userinfo = userManager.getUser(recv_name);
			userinfo.setState((String) recv_object);

			if (userManager.isAllUserPlay())
				gameLogic.setState("play");
			else
				gameLogic.setState("ready");

			broad_cast(Command.USERUPDATE, "", userManager.getUsers());
			if (userManager.isAllUserPlay()) {
				broad_cast(ChatCommand.CHATNOTICE, "", "게임이 시작 되었습니다");
				send_Message_ToMafia(ChatCommand.CHATNOTICE, "", "당신은 마피아 입니다.\n사람을 죽이십시오.");
				send_Message_ToCop(ChatCommand.CHATNOTICE, "", "당신은 경찰 입니다.\n마피아를 찾아주세요.");
				send_Message_ToDoctor(ChatCommand.CHATNOTICE, "", "당신은 의사 입니다.\n마피아로 부터 구해주세요.");
				send_Message_ToCivil(ChatCommand.CHATNOTICE, "", "당신은 시민 입니다.\n");
			}
			break;

		/* 유저 낮에 있음 */
		case PlayCommand.IMINSUNNY:
			userinfo = userManager.getUser(recv_name);
			userinfo.setWhen((String) recv_object);
			broad_cast(Command.USERUPDATE, "", userManager.getUsers());

			if (userManager.isAllUserInSunny())
				gameLogic.setWhen("sunny");
			else
				gameLogic.setWhen("night");

			if (gameLogic.getWhen().equals("sunny") && gameLogic.getState().equals("play")) {
				new Thread() {
					public void run() {
						Integer timer = 100;
						while (timer >= 0 && gameLogic.getWhen().equals("sunny")
								&& gameLogic.getState().equals("play")) {
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

		//// * 유저 다음 턴으로 가기를 원함 */
		case PlayCommand.IWANTNEXT:
			userinfo = userManager.getUser(recv_name);
			userinfo.setWantnext((boolean) recv_object);
			if ((boolean) recv_object == true)
				broad_cast(ChatCommand.CHATNOTICE, "server", recv_name + "님이 밤으로 가길 원합니다.");
			else
				broad_cast(ChatCommand.CHATNOTICE, "server", recv_name + "님이 밤으로 가길 원하지 않습니다.");

			/* 모든 유저가 밤으로 가기를 원할경우 */
			if (userManager.isAllUserWantNext() == true) {

				gameLogic.setWantnext(true);

				broad_cast(ChatCommand.CHATNOTICE, "server", "모든 플레이어가 밤으로 가길 원합니다.");

				/* 터치 불가한 상태로 전환 */
				broad_cast(Command.NOTOUCH, "", "");

				/* 카운트 다운 시작 */
				new Thread() {
					public void run() {
						broad_cast(ChatCommand.CHATNOTICE, "server", "5초후 투표를 시작합니다");
						int count = 5;
						while (count > 0) {
							count--;
							broad_cast(ChatCommand.CHATNOTICE, "", "투표까지 " + "");
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								return;
							}
						}

						if (count == 0) {
							broad_cast(ChatCommand.CHATNOTICE, "server", "투표를 시작합니다..");

							/* 모든 사용자에게 투표 시작을 요청함 */
							broad_cast(PlayCommand.STARTCHOICE, "", "");

							/* 새로운 투표 시작 */
							gameLogic.newVote();
						}
					}
				}.start();

			}

			/* 모든 유저가 밤으로 가기를 원하지 않은 경우 */
			else
				gameLogic.setWantnext(false);
			break;

		//// * 유저 투표함. 선택한 사람 전송 */
		case PlayCommand.CHOICUSER:

			/* 투표 업데이트 */
			gameLogic.updateVote(recv_name, (String) recv_object);

			/* 모든 유저가 투표를 완료함 */
			if (gameLogic.isAllUserVote() == true) {

				/* 가장 많은 표를 받은 유저 확인 */
				String name = gameLogic.getMaxVotedUser();

				/* 가장 많읂 표를 받은 유저 상태를 die로 전환 */
				userManager.getUser(name).setState("die");

				/* 유저 정보 갱신, 사망자 공지 */
				broad_cast(Command.USERUPDATE, "server", userManager.getUsers());
				broad_cast(ChatCommand.CHATNOTICE, "server", name + "님이 투표로 처형 되었습니다.");

				/* 유저 터치 불가한 상태로 전환 */
				broad_cast(Command.NOTOUCH, "", "");

				/* 밤이 오기 카운트 시작 */
				broad_cast(ChatCommand.CHATNOTICE, "server", "5초후 밤이 찾아옵니다.");
				new Thread() {
					public void run() {

						int count = 5;
						while (count > 0) {
							count--;
							broad_cast(ChatCommand.CHATNOTICE, "", "밤까지 " + "");
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								return;
							}
						}

						if (count == 0) {
							broad_cast(ChatCommand.CHATNOTICE, "server", "밤이 찾아옵니다..활동이 시작됩니다");

							/* 모든 사용자에게 밤으로 가기를 요청함 */
							broad_cast(PlayCommand.GONIGHT, "", "");
						}
					}
				}.start();

			}
			break;

		/* 유저 채팅을 보냄 */
		case ChatCommand.SENDMESSAGE:
			broad_cast(ChatCommand.SENDMESSAGE, recv_name, recv_object);
			break;

		case ChatCommand.SENDEMOTICON:
			broad_cast(ChatCommand.SENDEMOTICON, recv_name, recv_object);
			break;

		}
	}

}