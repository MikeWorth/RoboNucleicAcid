package rna;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import robocode.control.events.BattleAdaptor;
import robocode.control.events.BattleCompletedEvent;

class ScoreKeeper extends BattleAdaptor {
	List<RobotScore> scores;
	public ScoreKeeper(){
		scores = new ArrayList<RobotScore>();
		scores.add(new RobotScore(0));
		scores.add(new RobotScore(1));
		scores.add(new RobotScore(2));
		scores.add(new RobotScore(3));
		scores.add(new RobotScore(4));
		scores.add(new RobotScore(5));
		scores.add(new RobotScore(6));
		scores.add(new RobotScore(7));
		scores.add(new RobotScore(8));
		scores.add(new RobotScore(9));
		scores.add(new RobotScore(10));
		scores.add(new RobotScore(11));
		scores.add(new RobotScore(12));
		scores.add(new RobotScore(13));
		scores.add(new RobotScore(14));
		scores.add(new RobotScore(15));
	}
	public void onBattleCompleted(BattleCompletedEvent e){
		System.out.print('.');
		int[] battleScore=new int[2];
		battleScore[0]=e.getIndexedResults()[0].getScore();
		battleScore[1]=e.getIndexedResults()[1].getScore();
		for (int i=0;i<2;i++){
			robocode.BattleResults result=e.getIndexedResults()[i];
			if(result.getTeamLeaderName().substring(0,6).equals("rna.EB")){
				int botId=Integer.parseInt(result.getTeamLeaderName().substring(6))-1;
				float weightedscore=(float)(1+battleScore[i])/(1+battleScore[0]+battleScore[1]);//Use this to differentiate between really shit and really really shit bots
				scores.get(botId).addPoints(weightedscore);
			}

		}
	}
	

	
	public int[][] getRankings(){
		RobotScoreComparator comparator=new RobotScoreComparator();
		Collections.sort(scores,comparator);
		int[][] rankings = new int[16][2];
		int i=0;
		for (RobotScore score:scores){
			rankings[i]=new int[]{score.getId(),score.getScorePercentage()};
			i++;
		}
		return rankings;
	}


	private class RobotScore{
		int botId;
		float score;
		int battleCount;
	
		public RobotScore (int id){
			botId=id;
			score=0;
			battleCount=0;
		}
		public void addPoints(float points){
			score+=points;
			battleCount++;
		}
		public int getId(){
			return botId;
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
