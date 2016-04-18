package rna;

import java.io.File;

import robocode.control.BattleSpecification;
import robocode.control.BattlefieldSpecification;
import robocode.control.RobocodeEngine;
import robocode.control.RobotSpecification;

public class RobotBattle {
	private static boolean VisibleBattles = false;
	private static int rounds=3;
	private RobocodeEngine engine;
	private BattleSpecification battleSpec;
	
	public RobotBattle(String pairingString, ScoreKeeper scoreKeeper){
		
		engine = new RobocodeEngine((new File("robocode")));//TODO: automatically detect dir?
		engine.setVisible(VisibleBattles);
		BattlefieldSpecification defaultBattlefield = new BattlefieldSpecification();
		engine.addBattleListener(scoreKeeper);//TODO: do I still have to remove this manually?
		RobotSpecification[] pairing;
		pairing=engine.getLocalRepository(pairingString);
		battleSpec = new BattleSpecification(rounds, defaultBattlefield, pairing);
	}
	public void run(){
		engine.runBattle(battleSpec,true);
		System.out.print('.');
	}
	
}