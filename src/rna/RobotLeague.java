package rna;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

import robocode.control.BattleSpecification;
import robocode.control.BattlefieldSpecification;
import robocode.control.RobocodeEngine;
import robocode.control.RobotSpecification;

public class RobotLeague {
	
	private ScoreKeeper scoreKeeper;
	private int numberOfChallengers;
	private GeneticCode[] botGenomes;
	
	public RobotLeague(Generation generation,String[] yardstickBots,boolean interChallengerBattles) throws FileNotFoundException, UnsupportedEncodingException{
		
		botGenomes=generation.getBots();
		numberOfChallengers=botGenomes.length;//TODO is this a good thing?
		scoreKeeper=new ScoreKeeper(botGenomes);
		
		commitToBots();
		
		//run interchallenger battles
		if(interChallengerBattles){
			for(int i=0; i<botGenomes.length;i++){
				for (int j=i+1;j<botGenomes.length;j++){
					String pairingString=botGenomes[i].getName()+","+botGenomes[j].getName();
					RobotBattle battle = new RobotBattle(pairingString,scoreKeeper);
					battle.run();
				}
			}
		}

		//Now make each challengerBot fight each yardstickBot 
		for(int i=0;i<yardstickBots.length;i++){
			for (int j=0;j<botGenomes.length;j++){
				String pairingString=yardstickBots[i]+","+botGenomes[j].getName();
				RobotBattle battle = new RobotBattle(pairingString,scoreKeeper);
				battle.run();
			}				
		}
		//A newline after the series of dots for each battle, otherwise we run the next output onto it
		System.out.print('\n');
	}

	public GeneticCode[] getChallengersInOrder(){
		return scoreKeeper.getBotsInOrder();
	}
	
	public int getScore(GeneticCode bot){
		return scoreKeeper.getScorePercentage(bot);
	}
	
	public int getAverageScore(){
		int runningTotal=0;
		for(int i=0;i<numberOfChallengers;i++){
			runningTotal+=scoreKeeper.getScorePercentage(botGenomes[i]);
		}
		return runningTotal/numberOfChallengers;
	}
	private void commitToBots() throws FileNotFoundException, UnsupportedEncodingException{
		for(int i=0;i<numberOfChallengers;i++){
			botGenomes[i].commitToRobot();
		}
		
	}
	
}
