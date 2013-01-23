package rna;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

public class RobotBreeder {
	
	public static void main(String[] args) throws IOException{
		
		final int BOTCOUNT=50;//Don't make this larger than 50 without making more EB*.class
		final int CLOSESTALLOWEDINCEST=2;
		
		String[] evolveBotNames=new String[BOTCOUNT];
		for(int i=0;i<BOTCOUNT;i++){
			evolveBotNames[i]="rna.EB"+String.valueOf(i);
		}

		
		GeneticCode[] currentGeneration;
		String[] manualBotNames={
				"sample.SpinBot"
				/*
				"sample.Corners",
				"sample.Crazy",
				"sample.Fire",
				"sample.MyFirstJuniorRobot",
				"sample.MyFirstRobot",
				"sample.RamFire",
				"sample.SittingDuck",
				"sample.SpinBot",
				"sample.Target",
				"sample.Tracker",
				"sample.Trackfire",
				"sample.Walls"
*/
		};		
		currentGeneration = new GeneticCode[evolveBotNames.length];
		
		for(int i=0;i<BOTCOUNT;i++){
			currentGeneration[i] = new GeneticCode();
			currentGeneration[i].commitToRobot(evolveBotNames[i]);
		}
		
		int generation=1;
		PrintWriter log = new PrintWriter("RNA log.csv");
		
		
		while(true){
			
			double generationStartTime=System.currentTimeMillis();

			RobotLeague league=new RobotLeague(currentGeneration, manualBotNames, false);
			
			GeneticCode[] rankedBots=league.getChallengersInOrder();
			
			GeneticCode[] newGeneration = new GeneticCode[BOTCOUNT];
			GeneticCode winner=rankedBots[0];
			System.out.println(winner.getName() + "(" + winner.getPersonifiedName() + ") wins generation " + String.valueOf(generation) + "!");
			double generationTime=System.currentTimeMillis() - generationStartTime;
			
			int leastRelated=0;
			double generationCousinality=0;
			double IncestStartTime=System.currentTimeMillis();
			int averageDenominator=BOTCOUNT*(BOTCOUNT-1)/2;
			for(int i=0;i<BOTCOUNT;i++){
				for(int j=i+1;j<BOTCOUNT;j++){
					int cousinality=Lineage.getCousinality(currentGeneration[i].getLineage(), currentGeneration[j].getLineage(),10);//TODO: how deep to search? A long term limit exists, calculate that? log2(BOTCOUNT) round up
					generationCousinality+= ((double) cousinality)/averageDenominator;
					leastRelated=Math.max(leastRelated,cousinality);
				}
			}
			System.out.println("Incest check ended after "+String.valueOf(System.currentTimeMillis() - IncestStartTime)+"ms");
			if(leastRelated<CLOSESTALLOWEDINCEST){
				System.err.println("Population has become too inbred to proceed, replacing bottom 10% with random bots");
				for (int i=(BOTCOUNT*9)/10;i<BOTCOUNT;i++)
					rankedBots[i]=new GeneticCode();
			}

			String logLine="";
			logLine += String.valueOf(generation) + ",";
			logLine += winner.getName() + ",";
			logLine += '"' + winner.getPersonifiedName() + "\",";
			logLine += league.getScore(winner) + ",";
			logLine += league.getAverageScore() + ",";
			logLine += String.valueOf(generationCousinality) + ",";
			logLine += String.valueOf(generationTime);
			log.println(logLine);
			log.flush();
			
			rankedBots[0].commitToRobot("EvolveBot");//put the winner here for external viewing
			String genString=String.valueOf(generation);
			while(genString.length()<5)
				genString="0"+genString;
			rankedBots[0].commitToRobot("winner"+genString);//These bots don't exist, but it will save a copy of the genomes regardless

			for(int i=0;i<BOTCOUNT;i++){
				GeneticCode parent1=getWeightedRandomBot(rankedBots);
				GeneticCode parent2=getWeightedRandomBot(rankedBots);
				
				//promote genetic diversity by prohibiting incest:
				while(Lineage.getCousinality(parent1.getLineage(), parent2.getLineage(),CLOSESTALLOWEDINCEST+1)<CLOSESTALLOWEDINCEST){
					System.out.println("Incest prevented while parenting new bot "+String.valueOf(i));
					parent1=getWeightedRandomBot(rankedBots);
					parent2=getWeightedRandomBot(rankedBots);
				}
				
				newGeneration[i] = new GeneticCode(parent1,parent2);
			}

			
			//Finally ditch the old generation, replacing it with the current
			currentGeneration=newGeneration;
			for(int i=0;i<BOTCOUNT;i++){
				currentGeneration[i].commitToRobot(evolveBotNames[i]);
			}
			generation++;
		}
	}
	
	private static GeneticCode getWeightedRandomBot(GeneticCode[] rankedBots){
		int botCount = rankedBots.length;//This shouldn't vary from the main botCount, but I'd rather not constrain it in case things change later
		Random generator=new Random();
		
		int cumulativeProbabilities[]=new int[botCount];
		int runningTotal=0;
		for(int i=0;i<botCount;i++){
			int points=(botCount-i);
			cumulativeProbabilities[i]=runningTotal;
			cumulativeProbabilities[i]+=points;//This line can be changed to reweight probabilities without affecting anything else
			runningTotal=cumulativeProbabilities[i];
		}
		
		double rnd=generator.nextInt(runningTotal);
		for(int i=0;i<botCount;i++){
			if (rnd<=cumulativeProbabilities[i])
				return rankedBots[i];
		}
		
		return rankedBots[0];
	}
	
}

