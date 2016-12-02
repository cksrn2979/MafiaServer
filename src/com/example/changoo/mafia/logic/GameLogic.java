package com.example.changoo.mafia.logic;

import java.util.HashMap;

import com.example.changoo.mafia.model.CharacterCivil;
import com.example.changoo.mafia.model.CharacterCop;
import com.example.changoo.mafia.model.CharacterDoctor;
import com.example.changoo.mafia.model.CharacterMafia;
import com.example.changoo.mafia.model.UserManager;
import com.example.changoo.mafia.model.UserInfo;

public class GameLogic {
	private final static int MINUSER=1;
	private final static int MAXUSER=10;
	
	//MAFIA, DOCTOR, COP;
	private HashMap<Integer, Integer[]> numberOfCharacter;
	
	private UserManager userManager;
	
	public GameLogic(UserManager userManager) {
		this.userManager=userManager;
		numberOfCharacter=new HashMap<>();
		numberOfCharacter.put(1, new Integer[]{0,1,0,0});
		numberOfCharacter.put(2, new Integer[]{1,1,0,0});
		//numberOfCharacter.put(3, new Integer[]{1,0,0,0});
		//numberOfCharacter.put(4, new Integer[]{1,0,0,0});
		numberOfCharacter.put(5, new Integer[]{1,1,1,2});
		numberOfCharacter.put(6, new Integer[]{2,1,1,2});
		numberOfCharacter.put(7, new Integer[]{2,1,1,3});
		numberOfCharacter.put(8, new Integer[]{2,1,1,4});		
		numberOfCharacter.put(9, new Integer[]{3,1,1,4});
		numberOfCharacter.put(10, new Integer[]{3,1,1,5});
	}
	
	public boolean isPlayAvailable(){
		if(userManager.isAllUserReady() && userManager.size()>=MINUSER && userManager.size() <= MAXUSER)
			return true;
		else
			return false;
	}
	
	
	
	public boolean isSunnyTimerAvailable(){
		if(userManager.isAllUserPlay() && userManager.isAllUserInSunny())
			return true;
		else
			return false;		
	}
	
	public boolean updateCharacter(){
		Integer numberOfUsers=userManager.size();
		Integer[] caracterDivision=numberOfCharacter.get(numberOfUsers);
		
		int numberOfMafias=0;
		int numberOfCops=0;
		int numberOfDoctors=0;
		int numberOfCivils=0;
		
		int maxOfMafias=caracterDivision[0];
		int maxOfCops=caracterDivision[1];
		int maxOfDoctors=caracterDivision[2];
		int maxOfCivils=caracterDivision[3];
		
		for(int i=0; i<numberOfUsers; i++){
			UserInfo user=userManager.getUser(i);
			while(true){
				int random=(int)(Math.random()*4);
				if(random==0 && numberOfMafias < maxOfMafias){
					user.setCharacter(new CharacterMafia());
					numberOfMafias++;
					break;
				}
				
				else if(random==1 && numberOfCops < maxOfCops){
					user.setCharacter(new CharacterCop());
					numberOfCops++;
					break;
				}
				
				else if(random==2 && numberOfDoctors < maxOfDoctors){
					user.setCharacter(new CharacterDoctor());
					numberOfDoctors++;
					break;
				}
				
				else if(random==3 && numberOfCivils < maxOfCivils){
					user.setCharacter(new CharacterCivil());
					numberOfCivils++;
					break;
				}					
			}	
		}
		return true;
		
	}
	
}
