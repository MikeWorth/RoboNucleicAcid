package rna;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

public class RobotBreeder {
	
	public static void main(String[] args) throws IOException{
		
		GeneticCode[] currentGeneration;
		String[] evolveBotNames={"rna.EB1","rna.EB2","rna.EB3","rna.EB4","rna.EB5","rna.EB6","rna.EB7","rna.EB8","rna.EB9","rna.EB10","rna.EB11","rna.EB12","rna.EB13","rna.EB14","rna.EB15","rna.EB16"};
		String[] manualBotNames={"sample.Corners","sample.Crazy","sample.Fire","sample.MyFirstJuniorRobot","sample.MyFirstRobot","sample.RamFire","sample.SittingDuck","sample.SpinBot","sample.Target","sample.Tracker","sample.Trackfire","sample.Walls"};		currentGeneration = new GeneticCode[evolveBotNames.length];
		int botCount=evolveBotNames.length;
		
		for(int i=0;i<botCount;i++){
			currentGeneration[i] = new GeneticCode();
			currentGeneration[i].commitToRobot(evolveBotNames[i]);
		}
		
		int generation=1;
		PrintWriter log = new PrintWriter("RNA log.csv");
		while(true){

			RobotLeague league=new RobotLeague(currentGeneration, manualBotNames, false);
			
			GeneticCode[] rankedBots=league.getChallengersInOrder();
			
			GeneticCode[] newGeneration = new GeneticCode[botCount];
			GeneticCode winner=rankedBots[0];
			System.out.println(winner.getName()+" wins generation " + String.valueOf(generation) + "!");

			String logLine="";
			logLine += String.valueOf(generation) + ",";
			logLine += winner.getName() + ",";
			logLine += league.getScore(winner) + ",";
			logLine += league.getAverageScore();
			log.println(logLine);
			log.flush();
			
			Random generator=new Random();
			for(int i=0;i<botCount;i++){
				//first keep the top 1/2:
				if (i<botCount/2){
					newGeneration[i] = rankedBots[i];
					System.out.println(rankedBots[i].getName());
				//Kill off the bottom half and replace them with crossbreeds
				//1/4 from the top 1/4
				}else if(i< ((botCount*3)/4) ){
					int rnd1=generator.nextInt(botCount/4);
					int rnd2=generator.nextInt(botCount/4);
					newGeneration[i] = new GeneticCode(rankedBots[rnd1],rankedBots[rnd2]);
				//and the last 1/4 from the top 1/2
				}else{
					int rnd1=generator.nextInt(botCount/2);
					int rnd2=generator.nextInt(botCount/2);
					newGeneration[i] = new GeneticCode(rankedBots[rnd1],rankedBots[rnd2]);
				}
			}

			//Finally ditch the old generation, replacing it with the current
			currentGeneration=newGeneration;
			for(int i=0;i<botCount;i++){
				currentGeneration[i].commitToRobot(evolveBotNames[i]);
			}
			generation++;
		}
	}
}

