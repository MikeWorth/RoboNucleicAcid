package rna;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RobotLeague {
	
	private int numberOfChallengers;
	private GeneticCode[] botGenomes;
	private List<RobotScore> scores;
	private String[] botNames;
	
	public RobotLeague(Generation generation,String[] yardstickBots,int numberOfYardstickBotsToChallenge,boolean challengersBattleEachOther) throws FileNotFoundException, UnsupportedEncodingException{
		
		botGenomes=generation.getBots();
		numberOfChallengers=botGenomes.length;//We have to work up to fighting all of them - robotbreeder tells us how many to include TODO: check this isn't too high
		botNames=new String[numberOfChallengers];
		
		scores = new ArrayList<RobotScore>();
		for (int i=0;i<botGenomes.length;i++){
			botNames[i]=botGenomes[i].getName();
			scores.add(new RobotScore(botNames[i]));
		}
		
		commitToBots();
		
		RobotBattle[][] interChallengerBattles = new RobotBattle[botGenomes.length][botGenomes.length-1];

		//run interchallenger battles
		if(challengersBattleEachOther){
			for(int i=0; i<botGenomes.length;i++){
				for (int j=i+1;j<botGenomes.length;j++){

					String pairingNames[]={botGenomes[i].getName(), botGenomes[j].getName()};
					int pairingGenomeLengths[]={botGenomes[i].toString().length(),botGenomes[j].toString().length()};
					interChallengerBattles[i][j] = new RobotBattle(pairingNames,pairingGenomeLengths);
					interChallengerBattles[i][j].start();
					try {
						interChallengerBattles[i][j].join();
						scores.get(i).addPoints(interChallengerBattles[i][j].getScores()[0]);
						scores.get(j).addPoints(interChallengerBattles[i][j].getScores()[1]);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}

		RobotBattle[][] yardstickBattles = new RobotBattle[yardstickBots.length][botGenomes.length];
		
		//Now make each challengerBot fight each yardstickBot 
		for(int i=0;i<numberOfYardstickBotsToChallenge;i++){
			for (int j=0;j<botGenomes.length;j++){
				String pairingNames[]={yardstickBots[i], botGenomes[j].getName()};
				int pairingGenomeLengths[]={0,botGenomes[j].toString().length()};
				yardstickBattles[i][j] = new RobotBattle(pairingNames,pairingGenomeLengths);
				yardstickBattles[i][j].run();
				scores.get(j).addPoints(yardstickBattles[i][j].getScores()[1]);
			}				
		}

		//A newline after the series of dots for each battle, otherwise we run the next output onto it
		System.out.print('\n');
	}

	public GeneticCode[] getChallengersInOrder(){
		RobotScoreComparator comparator=new RobotScoreComparator();
		Collections.sort(scores,comparator);
		GeneticCode[] botsInOrder=new GeneticCode[numberOfChallengers];
		
		int i=0;
		for(RobotScore score:scores){
			int botIndex=Arrays.asList(botNames).indexOf(score.getName());
			botsInOrder[i]=botGenomes[botIndex];
			i++;
		}
		return botsInOrder;
	}
	
	public int getScorePercentage(GeneticCode bot){
		for(int i=0;i<numberOfChallengers;i++){
			if(scores.get(i).getName()==bot.getName())//This reference match check is ok, all names are created at compile time from the evolveBotNames array
				return scores.get(i).getScorePercentage();
		}
		System.err.println("Score not found:"+bot.getName());
		return 0;
	}

	public int getAverageScore(){
		int runningTotal=0;
		for(int i=0;i<numberOfChallengers;i++){
			runningTotal+=getScorePercentage(botGenomes[i]);
		}
		return runningTotal/numberOfChallengers;
	}
	private void commitToBots() throws FileNotFoundException, UnsupportedEncodingException{
		for(int i=0;i<numberOfChallengers;i++){
			botGenomes[i].commitToRobot();
		}
		
	}
	private class RobotScore{
		String botName;
		float score;
		int battleCount;
	
		public RobotScore (String name){
			botName=name;
			score=0;
			battleCount=0;
		}
		public void addPoints(float points){
			score+=points;
			battleCount++;
		}
		public String getName(){
			return botName;
		}
		public int getScorePercentage(){
			return Math.round(score/battleCount*100);
		}
	}
	
	private class RobotScoreComparator implements Comparator<RobotScore>{
		public int compare(RobotScore rs1,RobotScore rs2){
			float score1=rs1.getScorePercentage();
			float score2=rs2.getScorePercentage();
			if(score1>score2){
				return -1;
			}else if(score1<score2){
				return 1;
			}else{
				return 0;
			}
		}
	}	
	
	
}
