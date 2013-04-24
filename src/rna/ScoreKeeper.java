package rna;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import robocode.control.events.BattleAdaptor;
import robocode.control.events.BattleCompletedEvent;

class ScoreKeeper extends BattleAdaptor {
	
	List<RobotScore> scores;
	String[] botNames;
	GeneticCode[] botGenomes;
	int botCount;
	
	public ScoreKeeper(GeneticCode[] bots){
		botGenomes=bots;
		botCount=botGenomes.length;
		botNames=new String[botCount];
		scores = new ArrayList<RobotScore>();
		for(int i=0;i<botCount;i++){
			botNames[i]=bots[i].getName();
			scores.add(new RobotScore(botNames[i]));

		}
	}

	public void onBattleCompleted(BattleCompletedEvent e){
		int[] battleScore=new int[2];
		battleScore[0]=e.getIndexedResults()[0].getScore();
		battleScore[1]=e.getIndexedResults()[1].getScore();
		for (int i=0;i<2;i++){
			robocode.BattleResults result=e.getIndexedResults()[i];
			String botName=result.getTeamLeaderName();
			if(Arrays.asList(botNames).contains(botName)){
				int botIndex=Arrays.asList(botNames).indexOf(botName);
				float weightedscore=(float)(1+battleScore[i])/(1+battleScore[0]+battleScore[1]);//Use this to differentiate between really shit and really really shit bots
				
				//Add selective pressure for genomes smaller than 250 bytes
				int genomeLength = botGenomes[botIndex].toString().length();
				weightedscore = adjustForGenomeLength(weightedscore,genomeLength);
				
				scores.get(botIndex).addPoints(weightedscore);
			}

		}
	}
	
	private float adjustForGenomeLength(float basicScore,int genomeLength){
		if (genomeLength > 250 ){
			int extraBytes = genomeLength - 250;
			System.out.println("Genome too big, genomeLength:"+genomeLength+" Penalty applied:"+Float.toString((float)extraBytes/1000));
			 basicScore -= ((float)extraBytes/1000);
		}
		return basicScore;
	}
	
	public GeneticCode[] getBotsInOrder(){
		RobotScoreComparator comparator=new RobotScoreComparator();
		Collections.sort(scores,comparator);
		GeneticCode[] botsInOrder=new GeneticCode[botCount];
		
		int i=0;
		for(RobotScore score:scores){
			int botIndex=Arrays.asList(botNames).indexOf(score.getName());
			botsInOrder[i]=botGenomes[botIndex];
			i++;
		}
		return botsInOrder;
	}
	
	public int getScorePercentage(GeneticCode bot){
		for(int i=0;i<botCount;i++){
			if(scores.get(i).getName()==bot.getName())//This reference match check is ok, all names are created at compile time from the evolveBotNames array
				return scores.get(i).getScorePercentage();
		}
		System.err.println("Score not found:"+bot.getName());
		return 0;
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
		public float getScoreFloat(){
			return score;
		}
	}
	
	private class RobotScoreComparator implements Comparator<RobotScore>{
		public int compare(RobotScore rs1,RobotScore rs2){
			float score1=rs1.getScoreFloat();
			float score2=rs2.getScoreFloat();
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
