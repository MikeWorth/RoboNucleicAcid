package rna;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

public class RobotBreeder {
	
	private static int BOTCOUNT=50;//Don't make this larger than 50 without making more EB*.class
	private static int CLOSESTALLOWEDINCEST=2;
	private static int SeparationTime=1000;
	
	private static String[] evolveBotNames(){
		String names[] = new String[BOTCOUNT];
		for(int i=0;i<BOTCOUNT;i++){
			names[i]="rna.EB"+String.valueOf(i)+"*";//Dunno why this * is now required
		}
		return names;
	}
	
	private static String[] manualBotNames={
			"supersample.SuperSpinBot*"
	};		
	
	public static void main(String[] args) throws IOException{
		

		GeneticCode[] EvolvedA=BreedPopulation(SeparationTime,"Population.0a.csv");
		GeneticCode[] EvolvedB=BreedPopulation(SeparationTime,"Population.0b.csv");

		for(int population=1;true;population++){
			
			//Combine the two populations; this slightly favours A, TODO: maybe tweak later?
			GeneticCode[] combinedBots = new GeneticCode[2*BOTCOUNT];
			int i=0;
			for (int j=0;j<BOTCOUNT;j++){
				combinedBots[i]=EvolvedA[j];
				combinedBots[i+1]=EvolvedB[j];
				i+=2;
			}

			//Breed 2 populations from this combination
			Generation populationA = new Generation(evolveBotNames(), combinedBots, CLOSESTALLOWEDINCEST);
			Generation populationB = new Generation(evolveBotNames(), combinedBots, CLOSESTALLOWEDINCEST);
			
			String popALog="Population."+String.valueOf(population)+"a.csv";
			 EvolvedA = BreedPopulation(SeparationTime,popALog ,populationA );

			String popBLog="Population."+String.valueOf(population)+"b.csv";
			 EvolvedB = BreedPopulation(SeparationTime,popBLog ,populationB );
			
			
		}
		
	}
	
	private static GeneticCode[] BreedPopulation(int generations,String logFileName) throws FileNotFoundException, UnsupportedEncodingException{
		Generation seedGeneration = new Generation(evolveBotNames());
		return BreedPopulation(generations, logFileName,seedGeneration);
	}
	
	private static GeneticCode[] BreedPopulation(int generations,String logFileName, Generation initialPopulation) throws FileNotFoundException, UnsupportedEncodingException{
		PrintWriter log = new PrintWriter(logFileName);
		
		Generation currentGeneration = initialPopulation;
		double generationTimer=System.currentTimeMillis();
		GeneticCode[] rankedBots=new GeneticCode[initialPopulation.getBots().length];
		
		for (int generation=0;generation<generations;generation++){

		
			double fightingStartTime=System.currentTimeMillis();
			RobotLeague league = new RobotLeague(currentGeneration, manualBotNames, false); 
			rankedBots=league.getChallengersInOrder();
			
			System.out.println("Fighting took:" + Double.valueOf((System.currentTimeMillis() - fightingStartTime)/1000) + "s");
			
			GeneticCode winner=rankedBots[0];
			int winnerScore=league.getScore(winner);
			int averageScore=league.getAverageScore();
			
			System.out.println(winner.getName() + "(" + winner.getPersonifiedName() + ") wins generation " + String.valueOf(generation) + "!");
			
			/*TODO fix excess incest
			int leastRelated=0;
			double IncestStartTime=System.currentTimeMillis();
			System.out.println("Incest check took:"+String.valueOf(System.currentTimeMillis() - IncestStartTime)+"ms");
			if(leastRelated<CLOSESTALLOWEDINCEST){
				System.err.println("Population has become too inbred to proceed, replacing bottom 10% with random bots");
				for (int i=(BOTCOUNT*9)/10;i<BOTCOUNT;i++)
					rankedBots[i]=new GeneticCode();
			}
			*/

			double generationTime=System.currentTimeMillis() - generationTimer;
			generationTimer=System.currentTimeMillis();

			String logLine="";
			logLine += String.valueOf(generation) + ",";
			logLine += winner.getName() + ",";
			logLine += '"' + winner.getPersonifiedName() + "\",";
			logLine += winnerScore + ",";
			logLine += averageScore + ",";
			logLine += String.valueOf(currentGeneration.getAverageCousinality()) + ",";
			logLine += String.valueOf(generationTime);
			System.out.println(logLine);//TODO why doesn't this go to file?
			log.println(logLine);
			log.flush();
			
			
			//Save copies of the winning bot for external evaluation/analysis
			rankedBots[0].commitToRobot("EvolveBot");//put the winner here for external viewing
			String genString=String.valueOf(generation);
			while(genString.length()<5)
				genString="0"+genString;
			rankedBots[0].commitToRobot("winner"+genString);//These bots don't exist, but it will save a copy of the genomes regardless

			double breedingStartTime=System.currentTimeMillis();
			currentGeneration = new Generation(evolveBotNames(),rankedBots,CLOSESTALLOWEDINCEST);
			System.out.println("Breeding took:" + Double.valueOf((System.currentTimeMillis() - breedingStartTime)/1000) + "s");

		}
		return rankedBots;
	}
	
	
}

