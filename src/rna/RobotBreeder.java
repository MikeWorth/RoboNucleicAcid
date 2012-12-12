package rna;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

public class RobotBreeder {
	
	public static void main(String[] args) throws IOException{
		
		GeneticCode[] currentGeneration;
		String[] evolveBotNames={"rna.EB1","rna.EB2","rna.EB3","rna.EB4","rna.EB5","rna.EB6","rna.EB7","rna.EB8","rna.EB9","rna.EB10","rna.EB11","rna.EB12","rna.EB13","rna.EB14","rna.EB15","rna.EB16"};
		String[] manualBotNames={"sample.Corners","sample.Crazy","sample.Fire","sample.MyFirstJuniorRobot","sample.MyFirstRobot","sample.RamFire","sample.SittingDuck","sample.SpinBot","sample.Target","sample.Tracker","sample.Trackfire","sample.Walls"};		
		currentGeneration = new GeneticCode[evolveBotNames.length];
		int botCount=evolveBotNames.length;
		final int CLOSESTALLOWEDINCEST=1;
		
		for(int i=0;i<botCount;i++){
			currentGeneration[i] = new GeneticCode();
			currentGeneration[i].commitToRobot(evolveBotNames[i]);
		}
		
		int generation=1;
		PrintWriter log = new PrintWriter("RNA log.csv");
		while(true){

			int leastRelated=0;
			for(int i=0;i<botCount;i++){
				for(int j=i;j<botCount;j++){
					leastRelated=Math.max(leastRelated,Lineage.getCousinality(currentGeneration[i].getLineage(), currentGeneration[j].getLineage()));
				}
			}
			if(leastRelated<CLOSESTALLOWEDINCEST){
				System.err.println("Population has become too inbred to proceed");
				break;
			}

			RobotLeague league=new RobotLeague(currentGeneration, manualBotNames, false);
			
			GeneticCode[] rankedBots=league.getChallengersInOrder();
			
			GeneticCode[] newGeneration = new GeneticCode[botCount];
			GeneticCode winner=rankedBots[0];
			System.out.println(winner.getName() + "(" + winner.getPersonifiedName() + ") wins generation " + String.valueOf(generation) + "!");

			String logLine="";
			logLine += String.valueOf(generation) + ",";
			logLine += winner.getName() + ",";
			logLine += '"' + winner.getPersonifiedName() + "\",";
			logLine += league.getScore(winner) + ",";
			logLine += league.getAverageScore();
			log.println(logLine);
			log.flush();
			
			for(int i=0;i<botCount;i++){
				GeneticCode parent1=getWeightedRandomBot(rankedBots);
				GeneticCode parent2=getWeightedRandomBot(rankedBots);
				
				//promote genetic diversity by prohibiting incest:
				while(Lineage.getCousinality(parent1.getLineage(), parent2.getLineage())<CLOSESTALLOWEDINCEST){
					parent1=getWeightedRandomBot(rankedBots);
					parent2=getWeightedRandomBot(rankedBots);
				}
				
				newGeneration[i] = new GeneticCode(parent1,parent2);
			}

			//Finally ditch the old generation, replacing it with the current
			currentGeneration=newGeneration;
			for(int i=0;i<botCount;i++){
				currentGeneration[i].commitToRobot(evolveBotNames[i]);
			}
			generation++;
		}
	}
	
	private static GeneticCode getWeightedRandomBot(GeneticCode[] rankedBots){
		int botCount = rankedBots.length;
		Random generator=new Random();
		double smallestProbability= (2.0/(botCount*(botCount+1)));//2 is an int, 2.0 is a double
		double[] probabilities = new double[botCount]; 

		for(int i=0;i<botCount;i++)
			probabilities[i]=smallestProbability * (botCount-i);
		
		double rnd=generator.nextDouble();
		double cumulativeProbability=0;
		for(int i=0;i<botCount;i++){
			cumulativeProbability+=probabilities[i];
			if (rnd<cumulativeProbability)
				return rankedBots[i];
		}
		return rankedBots[botCount-1];//the cumulative probability can round to 0.9999999999999999
	}
	
}

