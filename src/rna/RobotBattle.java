package rna;

import java.io.File;

import robocode.control.BattleSpecification;
import robocode.control.BattlefieldSpecification;
import robocode.control.RobocodeEngine;
import robocode.control.RobotSpecification;

public class RobotBattle extends Thread{
	private static boolean VisibleBattles = false;
	private static int rounds=3;
	private RobocodeEngine engine;
	private BattleSpecification battleSpec;
	private float[] scores;
	private ScoreKeeper scoreKeeper;
	
	public RobotBattle(String[] botNames, int[] botGenomeLengths){
		
		scoreKeeper=new ScoreKeeper(botNames,botGenomeLengths);
		
		engine = new RobocodeEngine((new File("robocode")));//TODO: automatically detect dir?
		engine.setVisible(VisibleBattles);
		BattlefieldSpecification defaultBattlefield = new BattlefieldSpecification();
		engine.addBattleListener(scoreKeeper);//TODO: do I still have to remove this manually?
		RobotSpecification[] pairing;
		pairing=engine.getLocalRepository(botNames[0]+","+botNames[1]);
		battleSpec = new BattleSpecification(rounds, defaultBattlefield, pairing);
	}

	public void run(){
		engine.runBattle(battleSpec,true);
		scores=scoreKeeper.getScores();
		engine.removeBattleListener(scoreKeeper);
		System.out.print('.');
		engine.close();
	}
	public float[] getScores(){
		return scores;
	}
}