package com.example.changoo.mafia.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import com.example.changoo.mafia.log.Logger;
import com.example.changoo.mafia.network.MyNetwork;

public class UserManager {
	Vector<UserInfo> userinfos = new Vector<>();
	HashMap<String, MyNetwork> userNetworks = new HashMap<>();

	public Vector<UserInfo> getUsers() {
		return userinfos;
	}

	public void setUsers(Vector<UserInfo> users) {
		this.userinfos = users;
	}

	public void addUser(String userName) {
		UserInfo userInfo = new UserInfo(userName);
		userInfo.setState("wait");
		userinfos.add(userInfo);
	}

	public UserInfo getUser(int i) {
		return userinfos.get(i);
	}

	public UserInfo getUser(String username) {
		for (int i = 0; i < userinfos.size(); i++) {
			if (username.equals(userinfos.get(i).getName()))
				return userinfos.get(i);
		}
		return null;
	}

	public MyNetwork getUserNetwork(UserInfo user) {
		return userNetworks.get(user.getName());
	}

	public void addUserNetwork(String username, MyNetwork network) {
		userNetworks.put(username, network);
	}

	public void removeUser(String userName) {
		userinfos.remove(getUser(userName));
		userNetworks.remove(userName);
	}

	public int size() {
		return userinfos.size();
	}

	public boolean checkingName(String userName) {
		for (int i = 0; i < userinfos.size(); i++) {
			UserInfo u = userinfos.get(i);
			if (userName.equals(u.getName()) == true)
				return false;
		}
		return true;
	}

	public boolean isAllUserReady() {
		Boolean result = true;
		for (int i = 0; i < userinfos.size(); i++) {
			UserInfo user = userinfos.get(i);
			if (user.getState().equals("ready") == false)
				result = false;
		}
		return result;
	}

	public boolean isAllUserPlay() {
		Boolean result = true;
		for (int i = 0; i < userinfos.size(); i++) {
			UserInfo user = userinfos.get(i);
			if (user.getState().equals("play") == false)
				result = false;
		}
		return result;
	}

	public boolean isAllUserWantNext() {
		Boolean result = true;
		for (int i = 0; i < userinfos.size(); i++) {
			UserInfo user = userinfos.get(i);
			if (user.isWantnext() == false && user.getState().equals("play"))
				result = false;
		}
		return result;
	}

	public boolean isAllUserInSunny() {
		Boolean result = true;
		for (int i = 0; i < userinfos.size(); i++) {
			UserInfo user = userinfos.get(i);
			if (user.getWhen().equals("sunny") == false && user.getState().equals("play"))
				result = false;
		}
		return result;
	}

	public boolean isAllUserInNight() {
		Boolean result = true;
		for (int i = 0; i < userinfos.size(); i++) {
			UserInfo user = userinfos.get(i);

			if (user.getWhen().equals("night") == false && user.getState().equals("play")) {
				Logger.append(user.getName() + "님은 WHEN :  " + user.getWhen() + "상태는 : " + user.getState() + "\n");
				result = false;
			}
		}
		return result;
	}

	public String[] getAliveUserNames() {
		ArrayList<String> usernames = new ArrayList<>();
		int size = userinfos.size();
		for (int i = 0; i < size; i++) {
			if (userinfos.get(i).getState().equals("play"))
				usernames.add(userinfos.get(i).getName());
		}
		String[] names = usernames.toArray((new String[usernames.size()]));
		return names;
	}

}
