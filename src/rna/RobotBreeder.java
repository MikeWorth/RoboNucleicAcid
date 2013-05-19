package rna;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Random;

public class RobotBreeder {

	private static int BOTCOUNT=50;//Don't make this larger than 50 without making more EB*.class
	private static int CLOSESTALLOWEDINCEST=2;

	public static Random generator = new Random();

	private static String[] evolveBotNames(){
		String names[] = new String[BOTCOUNT];
		for(int i=0;i<BOTCOUNT;i++){
			names[i]="rna.EB"+String.valueOf(i)+"*";//Dunno why this * is now required
		}
		return names;
	}

	private static String[] manualBotNames={
		"sample.SpinBot"
	};		

	public static void main(String[] args) throws IOException{

		// seed RNG with command line value
		generator = new Random(Integer.parseInt(args[0]));

		Generation currentGeneration = new Generation(evolveBotNames());

		PrintWriter log = new PrintWriter("RNA log.csv");

		double generationTimer=System.currentTimeMillis();
		GeneticCode[] rankedBots=new GeneticCode[currentGeneration.getBots().length];

		for (int generation=0;true;generation++){


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
			log.println(logLine);
			log.flush();


			//Save copies of the winning bot for external evaluation/analysis
			rankedBots[0].commitToRobot("EvolveBot",false);//put the winner here for external viewing
			String genString=String.valueOf(generation);
			while(genString.length()<6)
				genString="0"+genString;
			rankedBots[0].commitToRobot("winner"+genString,true);//These bots don't exist, but it will save a copy of the genomes regardless

			double breedingStartTime=System.currentTimeMillis();
			currentGeneration = new Generation(evolveBotNames(),rankedBots,CLOSESTALLOWEDINCEST);
			System.out.println("Breeding took:" + Double.valueOf((System.currentTimeMillis() - breedingStartTime)/1000) + "s");

		}
	}

}

