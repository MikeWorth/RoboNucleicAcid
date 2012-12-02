package rna;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

import robocode.control.BattleSpecification;
import robocode.control.BattlefieldSpecification;
import robocode.control.RobocodeEngine;
import robocode.control.RobotSpecification;

public class RobotBreeder {
	public static int rounds=10;
	static GeneticCode[] currentGeneration;
	
	public static void main(String[] args) throws IOException{

		String[] evolveBotNames={"rna.EB1","rna.EB2","rna.EB3","rna.EB4","rna.EB5","rna.EB6","rna.EB7","rna.EB8","rna.EB9","rna.EB10","rna.EB11","rna.EB12","rna.EB13","rna.EB14","rna.EB15","rna.EB16"};
		currentGeneration = new GeneticCode[evolveBotNames.length];
		
		for(int i=0;i<evolveBotNames.length;i++){
			currentGeneration[i] = new GeneticCode(/*"EB"+String.valueOf(i+1)*/);
			currentGeneration[i].commitToRobot("EB"+String.valueOf(i+1));
		}
		
		int generation=1;
		PrintWriter log = new PrintWriter("RNA.log");
		while(true){
			RobocodeEngine engine = new RobocodeEngine((new File("/home/mike/.robocode")));
			ScoreKeeper scoreKeeper=new ScoreKeeper();
			engine.addBattleListener(scoreKeeper);
			RobotSpecification[] pairing;
			
			//Pit all evolvebots against each other
			/*for(int i=0; i<evolveBotNames.length;i++){
				for (int j=i+1;j<evolveBotNames.length;j++){
					pairing=engine.getLocalRepository(evolveBotNames[i]+","+evolveBotNames[j]);
					BattleSpecification battleSpec = new BattleSpecification(rounds, new BattlefieldSpecification(),pairing);
					engine.runBattle(battleSpec,true);//TODO multithreading?
				}
			}*/

			String[] manualBotNames={"sample.Corners","sample.Crazy","sample.Fire","sample.MyFirstJuniorRobot","sample.MyFirstRobot","sample.RamFire","sample.SittingDuck","sample.SpinBot","sample.Target","sample.Tracker","sample.Trackfire","sample.Walls"};
			//Now make each manualbot fight each evolvebot 
			for(int i=0;i<manualBotNames.length;i++){
				for (int j=0;j<evolveBotNames.length;j++){
					pairing=engine.getLocalRepository(manualBotNames[i]+","+evolveBotNames[j]);
					BattleSpecification battleSpec = new BattleSpecification(rounds, new BattlefieldSpecification(),pairing);
					engine.runBattle(battleSpec,true);//TODO multithreading?
				}				
			}
			int[][] rankings=scoreKeeper.getRankings();
			GeneticCode[] newGeneration = new GeneticCode[16];
			System.out.println("Bot" + String.valueOf(rankings[0][0]+1)+" wins generation " + String.valueOf(generation) + "! "+String.valueOf(rankings[0][1])+" "+currentGeneration[rankings[0][0]].toJavaCode());
			log.println(String.valueOf(generation) + " "+String.valueOf(rankings[0][1])+" "+currentGeneration[rankings[0][0]].toStrippedString());
			log.flush();
			
			//first keep the top 8 scorers:
			for(int i=0;i<8;i++){
			newGeneration[i] = currentGeneration[rankings[i][0]];
			}
			
			//Kill off the bottom 8 and replace them with crossbreeds
			//4 from the top 4
			for(int i=0;i<4;i++){
				Random generator=new Random();
				newGeneration[8+i] = new GeneticCode(currentGeneration[rankings[generator.nextInt(4)][0]],currentGeneration[rankings[generator.nextInt(4)][0]]);
			}

			//and 4 from the top 8
			for(int i=0;i<4;i++){
				Random generator=new Random();
				newGeneration[12+i] = new GeneticCode(currentGeneration[rankings[generator.nextInt(8)][0]],currentGeneration[rankings[generator.nextInt(8)][0]]);
			}

			//then add random mutations to and commit to file all of the new generation
			for(int i=8;i<16;i++){
				newGeneration[i].mutate();
			}
			for(int i=0;i<16;i++){
				String botName="EB"+String.valueOf(i+1);
				newGeneration[i].commitToRobot(botName);
			}
			
			//Finally ditch the old generation, replacing it with the current
			currentGeneration=newGeneration;
			generation++;
		}
		//engine.close();
		

	}
}

