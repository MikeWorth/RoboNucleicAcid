package rna;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

import robocode.control.BattleSpecification;
import robocode.control.BattlefieldSpecification;
import robocode.control.RobocodeEngine;
import robocode.control.RobotSpecification;

public class RobotLeague {
	
	private final static int rounds=10;
	private ScoreKeeper scoreKeeper;
	private int numberOfChallengers;
	private GeneticCode[] botGenomes;
	
	public RobotLeague(Generation generation,String[] yardstickBots,boolean interChallengerBattles) throws FileNotFoundException, UnsupportedEncodingException{
		
		botGenomes=generation.getBots();
		numberOfChallengers=botGenomes.length;//TODO is this a good thing?
		scoreKeeper=new ScoreKeeper(botGenomes);
		
		RobocodeEngine engine = new RobocodeEngine((new File("robocode")));//TODO: automatically detect dir?
		//engine.setVisible(true);
		BattlefieldSpecification defaultBattlefield = new BattlefieldSpecification();
		engine.addBattleListener(scoreKeeper);
		RobotSpecification[] pairing;
		
		commitToBots();
		
		//run interchallenger battles
		if(interChallengerBattles){
			for(int i=0; i<botGenomes.length;i++){
				for (int j=i+1;j<botGenomes.length;j++){
					pairing=engine.getLocalRepository(botGenomes[i].getName()+","+botGenomes[j].getName());
					BattleSpecification battleSpec = new BattleSpecification(rounds, defaultBattlefield, pairing);
					engine.runBattle(battleSpec,true);//TODO multithreading?
					System.out.print('.');
				}
			}
		}

		//Now make each challengerBot fight each yardstickBot 
		for(int i=0;i<yardstickBots.length;i++){
			for (int j=0;j<botGenomes.length;j++){
				pairing=engine.getLocalRepository(yardstickBots[i]+","+botGenomes[j].getName());
				BattleSpecification battleSpec = new BattleSpecification(rounds, defaultBattlefield ,pairing);
				engine.runBattle(battleSpec,true);//TODO multithreading?
				System.out.print('.');
			}				
		}
		engine.removeBattleListener(scoreKeeper);//If I don't remove this here it hangs around somehow and the nth generation counts the scores n times
		engine.close();
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
