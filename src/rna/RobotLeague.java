package rna;

import java.io.File;

import robocode.control.BattleSpecification;
import robocode.control.BattlefieldSpecification;
import robocode.control.RobocodeEngine;
import robocode.control.RobotSpecification;

public class RobotLeague {
	
	private final static int rounds=10;
	private ScoreKeeper scoreKeeper;
	private int numberOfChallengers;
	private GeneticCode[] botGenomes;
	
	public RobotLeague(GeneticCode[] challengerBots,String[] yardstickBots,boolean interChallengerBattles){
		
		botGenomes=challengerBots;
		numberOfChallengers=botGenomes.length;
		scoreKeeper=new ScoreKeeper(challengerBots);
		
		RobocodeEngine engine = new RobocodeEngine((new File("/home/mike/.robocode")));//TODO: automatically detect dir?
		engine.addBattleListener(scoreKeeper);
		RobotSpecification[] pairing;
		
		//run interchallenger battles
		if(interChallengerBattles){
			for(int i=0; i<challengerBots.length;i++){
				for (int j=i+1;j<challengerBots.length;j++){
					pairing=engine.getLocalRepository(botGenomes[i].getName()+","+botGenomes[j].getName());
					BattleSpecification battleSpec = new BattleSpecification(rounds, new BattlefieldSpecification(),pairing);
					engine.runBattle(battleSpec,true);//TODO multithreading?
				}
			}
		}

		//Now make each challengerBot fight each yardstickBot 
		for(int i=0;i<yardstickBots.length;i++){
			for (int j=0;j<challengerBots.length;j++){
				pairing=engine.getLocalRepository(yardstickBots[i]+","+botGenomes[j].getName());
				BattleSpecification battleSpec = new BattleSpecification(rounds, new BattlefieldSpecification(),pairing);
				engine.runBattle(battleSpec,true);//TODO multithreading?
			}				
		}
		engine.close();
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
}
