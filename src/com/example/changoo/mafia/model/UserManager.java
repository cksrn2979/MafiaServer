package com.example.changoo.mafia.model;

import java.util.HashMap;
import java.util.Vector;

import com.example.changoo.mafia.network.MyNetwork;

public class UserManager {
	Vector<UserInfo> users = new Vector<>();
	HashMap<String, MyNetwork> userNetworks = new HashMap<>();

	public Vector<UserInfo> getUsers() {
		return users;
	}

	public void setUsers(Vector<UserInfo> users) {
		this.users = users;
	}

	public void addUser(String userName) {
		UserInfo userInfo=new UserInfo(userName);
		userInfo.setState("wait");
		users.add(userInfo);
	}

	public UserInfo getUser(int i) {
		return users.get(i);
	}

	public UserInfo getUser(String username) {
		for (int i = 0; i < users.size(); i++) {
			if (username.equals(users.get(i).getName()))
				return users.get(i);
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
		users.remove(getUser(userName));
		userNetworks.remove(userName);
	}

	public int size() {
		return users.size();
	}

	public boolean checkingName(String userName) {
		for (int i = 0; i < users.size(); i++) {
			UserInfo u = users.get(i);
			if (userName.equals(u.getName()) == true)
				return false;
		}
		return true;
	}

	public boolean isAllUserReady() {
		Boolean result = true;
		for (int i = 0; i < users.size(); i++) {
			UserInfo user = users.get(i);
			if (user.getState().equals("ready") == false)
				result = false;
		}
		return result;
	}
	
	public boolean isAllUserPlay() {
		Boolean result = true;
		for (int i = 0; i < users.size(); i++) {
			UserInfo user = users.get(i);
			if (user.getState().equals("play") == false)
				result = false;
		}
		return result;
	}
	
	public boolean isAllUserWantNext() {
		Boolean result = true;
		for (int i = 0; i < users.size(); i++) {
			UserInfo user = users.get(i);
			if (user.isWantnext()==false)
				result = false;
		}
		return result;
	}
	
	public boolean isAllUserInSunny() {
		Boolean result = true;
		for (int i = 0; i < users.size(); i++) {
			UserInfo user = users.get(i);
			if (user.getWhen().equals("sunny") == false)
				result = false;
		}
		return result;
	}
	
	public boolean isAllUserInNight() {
		Boolean result = true;
		for (int i = 0; i < users.size(); i++) {
			UserInfo user = users.get(i);
			if (user.getWhen().equals("nigth") == false)
				result = false;
		}
		return result;
	}
	
	

}
