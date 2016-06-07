package rna;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RobotBreeder {

	private static int BOTCOUNT=50;//Don't make this larger than 50 without making more EB*.class
	private static int CLOSESTALLOWEDINCEST=2;
	
	private static int FEASTMAXLENGTH=5000;
	private static int FAMINEMAXLENGTH=2000;
	private static int FEASTDURATION=1000;
	private static int FAMINEDURATION=1000;
	private static int FFTRANSITIONRATE=10;//this is the amount that the max length will change per generation
	
	private static int feastGenerationsRemaining = FEASTDURATION;//start with a feast period
	private static int famineGenerationsRemaining = 0;
	private static boolean transitionToFamine = false;
	private static boolean transitionToFeast = false;
	
	private static String LogfileName="RNA log.csv";

	private static int randomSeed;
	public static Random generator = new Random();

	private static String[] evolveBotNames(){
		String names[] = new String[BOTCOUNT];
		for(int i=0;i<BOTCOUNT;i++){
			names[i]="rna.EB"+String.valueOf(i)+"*";//Dunno why this * is now required
		}
		return names;
	}

	private static String[] manualBotNames={
		"sample.SpinBot",
		"sample.Corners",
		"sample.Crazy",
		"sample.RamFire",
		"sample.Walls",
		"supersample.SuperSpinBot*"
	};
	private static int numberOfManualBotsToChallenge=1;
	private static int newChallengerThreashold = 75;
	private static boolean evolveBotsFightEachOther = false;
	
	public static void main(String[] args) throws IOException{

		//Use a constant default if user doesn't enter a seed
		if (args.length==0){
			randomSeed=0;
		}else{
			randomSeed=Integer.parseInt(args[0]);
		}
		
		// seed RNG with command line value
		generator = new Random(randomSeed);
		ScoreKeeper.setMaxGenomeLength(FEASTMAXLENGTH);

		Generation currentGeneration = new Generation(evolveBotNames());

		PrintWriter log = new PrintWriter(LogfileName);

		String logHeaderRow="\"Generation number\",\"Winning robot\",\"Winner Name\",\"Winning score\",\"Generation average score\",\"Generation average cousinality\",\"Generation time/s\",\"Winning genome length\",\"Maximum allowed genome length\"";
		log.println(logHeaderRow);
		log.flush();

		double generationTimer=System.currentTimeMillis();
		GeneticCode[] rankedBots=new GeneticCode[currentGeneration.getBots().length];

		for (int generation=0;true;generation++){


			double fightingStartTime=System.currentTimeMillis();
			RobotLeague league = new RobotLeague(currentGeneration, manualBotNames, numberOfManualBotsToChallenge, evolveBotsFightEachOther); 
			rankedBots=league.getChallengersInOrder();

			System.out.println("Fighting took:" + Double.valueOf((System.currentTimeMillis() - fightingStartTime)/1000) + "s");

			GeneticCode winner=rankedBots[0];
			int winnerScore=league.getScorePercentage(winner);
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
			logLine += String.valueOf(generationTime/1000) + ",";
			logLine += String.valueOf(winner.toString().length()) + ",";
			logLine += String.valueOf(ScoreKeeper.getMaxGenomeLength());
			log.println(logLine);
			log.flush();


			//Save copies of the winning bot for external evaluation/analysis
			winner.commitToRobot("EvolveBot",false);//put the winner here for external viewing

			double breedingStartTime=System.currentTimeMillis();
			currentGeneration = new Generation(evolveBotNames(),rankedBots,CLOSESTALLOWEDINCEST);
			System.out.println("Breeding took:" + Double.valueOf((System.currentTimeMillis() - breedingStartTime)/1000) + "s");
			
			if (feastGenerationsRemaining>0){
				if(--feastGenerationsRemaining==0){
					transitionToFamine=true;
				}
			}

			if (famineGenerationsRemaining>0){
				if(--famineGenerationsRemaining==0){
					transitionToFeast=true;
				}
			}
			
			if (transitionToFamine){
				int newLength=ScoreKeeper.getMaxGenomeLength() - FFTRANSITIONRATE;
				
				if(newLength<=FAMINEMAXLENGTH){
					newLength=FAMINEMAXLENGTH;
					transitionToFamine=false;
					famineGenerationsRemaining=FAMINEDURATION;
				}
				ScoreKeeper.setMaxGenomeLength(newLength);
			}

			if (transitionToFeast){
				int newLength=ScoreKeeper.getMaxGenomeLength() + FFTRANSITIONRATE;
				
				if(newLength>=FEASTMAXLENGTH){
					newLength=FEASTMAXLENGTH;
					transitionToFeast=false;
					feastGenerationsRemaining=FEASTDURATION;
				}
				ScoreKeeper.setMaxGenomeLength(newLength);
			}
			
			//Introduce a new challenger bot if the current generation average is high enough
			if (newChallengerThreashold<averageScore){
				if(manualBotNames.length>numberOfManualBotsToChallenge){
					numberOfManualBotsToChallenge++;
				}else{
					//once the evolvebots can beat all opposition, set them loose on each other! 
					//TODO: fix evolveBotsFightEachOther=true;
				}
			}

		}
	}

}

