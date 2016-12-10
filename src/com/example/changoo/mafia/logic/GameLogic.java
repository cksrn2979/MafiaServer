package com.example.changoo.mafia.logic;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import com.example.changoo.mafia.log.Logger;
import com.example.changoo.mafia.model.UserInfo;
import com.example.changoo.mafia.model.UserManager;

public class GameLogic {
	private final static int MINUSER = 1;
	private final static int MAXUSER = 8;

	private String state;
	private String when;
	private boolean wantnext = false;

	private HashMap<Integer, Integer[]> chractorOfUserSize; // MAFIA, DOCTOR,COP,CIVIL;
	private HashMap<String, Integer> numberOfChractor;
	private HashMap<String, String> userVote;

	private UserManager userManager;

	public GameLogic(UserManager userManager) {
		this.userManager = userManager;
		
		chractorOfUserSize = new HashMap<>();
		userVote = new HashMap<>();
		numberOfChractor = new HashMap<>();

		chractorOfUserSize.put(1, new Integer[] { 0, 1, 0, 0 });
		chractorOfUserSize.put(2, new Integer[] { 1, 1, 0, 0 });
		// numberOfCharacter.put(3, new Integer[]{1,0,0,0});
		// numberOfCharacter.put(4, new Integer[]{1,0,0,0});
		chractorOfUserSize.put(5, new Integer[] { 1, 1, 1, 2 });
		chractorOfUserSize.put(6, new Integer[] { 2, 1, 1, 2 });
		chractorOfUserSize.put(7, new Integer[] { 2, 1, 1, 3 });
		chractorOfUserSize.put(8, new Integer[] { 2, 1, 1, 4 });
		chractorOfUserSize.put(9, new Integer[] { 3, 1, 1, 4 });
		chractorOfUserSize.put(10, new Integer[] { 3, 1, 1, 5 });
	}
	
	public boolean isInsizeUserNumber() {
		if (userManager.size() >= MINUSER && userManager.size() <= MAXUSER)
			return true;
		return false;
	}

	public boolean updateCharacter() {
		Integer numberOfUsers = userManager.size();
		Integer[] caracterDivision = chractorOfUserSize.get(numberOfUsers);

		int numberOfMafias = 0;
		int numberOfCops = 0;
		int numberOfDoctors = 0;
		int numberOfCivils = 0;

		int maxOfMafias = caracterDivision[0];
		int maxOfCops = caracterDivision[1];
		int maxOfDoctors = caracterDivision[2];
		int maxOfCivils = caracterDivision[3];

		for (int i = 0; i < numberOfUsers; i++) {
			UserInfo user = userManager.getUser(i);
			while (true) {
				int random = (int) (Math.random() * 4);
				if (random == 0 && numberOfMafias < maxOfMafias) {
					user.setCharacter("MAFIA");
					numberOfMafias++;
					break;
				}

				else if (random == 1 && numberOfCops < maxOfCops) {
					user.setCharacter("COP");
					numberOfCops++;
					break;
				}

				else if (random == 2 && numberOfDoctors < maxOfDoctors) {
					user.setCharacter("DOCTOR");
					numberOfDoctors++;
					break;
				}

				else if (random == 3 && numberOfCivils < maxOfCivils) {
					user.setCharacter("CIVIL");
					numberOfCivils++;
					break;
				}
			}
		}
		numberOfChractor.put("MAFIA", numberOfMafias);
		numberOfChractor.put("COP", numberOfCops);
		numberOfChractor.put("DOCTOR", numberOfDoctors);
		numberOfChractor.put("CIVIL", numberOfCivils);

		return true;

	}



	public void newVote() {
		userVote.clear();
		String[] aliveUsername = userManager.getAliveUserNames();
		for (int i = 0; i < aliveUsername.length; i++)
			userVote.put(aliveUsername[i], "");
	}

	public void updateVote(String name, String choice) {
		userVote.put(name, choice);
	}

	public boolean isAllUserVote() {
		Set<String> set = userVote.keySet();
		Iterator<String> iter = set.iterator();
		while (iter.hasNext()) {
			String name = iter.next();
			if (userVote.get(name).equals(""))
				return false;
		}
		return true;
	}

	public String getMaxVotedUser() {
		HashMap<String, Integer> votedUser = new HashMap<>();

		Set<String> set = userVote.keySet();
		Iterator<String> iter = set.iterator();

		/* 유저, 뽑혀진 숫자 */
		while (iter.hasNext()) {
			String name = iter.next();
			String value = userVote.get(name);
			if (votedUser.get(value) != null) {
				int choice = votedUser.get(value);
				votedUser.put(value, choice + 1);
			} else
				votedUser.put(value, 1);
		}

		/*가장 많이 뽑혀진 저를 찾음*/
		Set<String> set2 = votedUser.keySet();
		Iterator<String> iter2 = set2.iterator();
		int maxint = 0;
		String maxuser = null;
		while (iter2.hasNext()) {
			String name = iter2.next();
			Integer choice = votedUser.get(name);
			Logger.append(name + choice + "\n");
			if (choice > maxint) {
				maxint = choice;
				maxuser = name;
			}
		}

		return maxuser;
	}


	
	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getWhen() {
		return when;
	}

	public void setWhen(String when) {
		this.when = when;
	}

	public boolean isWantnext() {
		return wantnext;
	}

	public void setWantnext(boolean wantnext) {
		this.wantnext = wantnext;
	}

}
