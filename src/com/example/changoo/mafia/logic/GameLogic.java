package com.example.changoo.mafia.logic;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import com.example.changoo.mafia.log.Logger;
import com.example.changoo.mafia.model.CharacterCivil;
import com.example.changoo.mafia.model.CharacterCop;
import com.example.changoo.mafia.model.CharacterDoctor;
import com.example.changoo.mafia.model.CharacterMafia;
import com.example.changoo.mafia.model.UserInfo;
import com.example.changoo.mafia.model.UserManager;

public class GameLogic {
	private final static int MINUSER = 1;
	private final static int MAXUSER = 10;

	private String state;
	private String when;
	private boolean wantnext = false;
	
	private HashMap<Integer, Integer[]> numberOfCharacter; // MAFIA, DOCTOR, COP,CIVIL;
	private HashMap<String,String > userChoice;
	
	private UserManager userManager;

	public GameLogic(UserManager userManager) {
		this.userManager = userManager;	
		numberOfCharacter = new HashMap<>();
		userChoice=new HashMap<>();
		numberOfCharacter.put(1, new Integer[] { 0, 1, 0, 0 });
		numberOfCharacter.put(2, new Integer[] { 1, 1, 0, 0 });
		// numberOfCharacter.put(3, new Integer[]{1,0,0,0});
		// numberOfCharacter.put(4, new Integer[]{1,0,0,0});
		numberOfCharacter.put(5, new Integer[] { 1, 1, 1, 2 });
		numberOfCharacter.put(6, new Integer[] { 2, 1, 1, 2 });
		numberOfCharacter.put(7, new Integer[] { 2, 1, 1, 3 });
		numberOfCharacter.put(8, new Integer[] { 2, 1, 1, 4 });
		numberOfCharacter.put(9, new Integer[] { 3, 1, 1, 4 });
		numberOfCharacter.put(10, new Integer[] { 3, 1, 1, 5 });
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
	
	public void newChoice(){
		userChoice.clear();
		for(int i=0; i<userManager.size();i++){
			userChoice.put(userManager.getUser(i).getName(),"");
		}
	}
	
	public void updateChoice(String name, String choice){		
		userChoice.put(name,choice);
	}
	
	
	public boolean isAllUserChoice(){
		Set<String> set=userChoice.keySet();
		Iterator<String> iter=set.iterator();
		while(iter.hasNext()){
			String name=iter.next();
			if(userChoice.get(name).equals(""))
				return false;
		}
		return true;
	}
	
	public String getMaxChocieUsername(){
		HashMap<String, Integer>choicedUser =new HashMap<>();
		
		Set<String> set=userChoice.keySet();
		Iterator<String> iter=set.iterator();
		
		while(iter.hasNext()){
			String name=iter.next();
			String value=userChoice.get(name);
			if(choicedUser.get(value)!=null){
				int choice=choicedUser.get(value);
				choicedUser.put(value, choice+1);
			}
			else
				choicedUser.put(value,1);
		}
		
		
		Set<String> set2=choicedUser.keySet();
		Iterator<String> iter2=set2.iterator();
		int maxint=0;
		String maxuser = null;
		while(iter2.hasNext()){
			String name=iter2.next();
			Integer choice=choicedUser.get(name);
			Logger.append(name + choice +"\n");
			if(choice>maxint){
				maxint=choice;
				maxuser=name;
			}
		}
		
		return maxuser;
	}
	


	public boolean isInsizeUserNumber() {
		if (userManager.size() >= MINUSER && userManager.size() <= MAXUSER)
			return true;
		return false;
	}
	


	public boolean updateCharacter() {
		Integer numberOfUsers = userManager.size();
		Integer[] caracterDivision = numberOfCharacter.get(numberOfUsers);

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
					user.setCharacter(new CharacterMafia());
					numberOfMafias++;
					break;
				}

				else if (random == 1 && numberOfCops < maxOfCops) {
					user.setCharacter(new CharacterCop());
					numberOfCops++;
					break;
				}

				else if (random == 2 && numberOfDoctors < maxOfDoctors) {
					user.setCharacter(new CharacterDoctor());
					numberOfDoctors++;
					break;
				}

				else if (random == 3 && numberOfCivils < maxOfCivils) {
					user.setCharacter(new CharacterCivil());
					numberOfCivils++;
					break;
				}
			}
		}
		return true;

	}

}
