package rna;

import java.util.Arrays;
import robocode.BattleResults;
import robocode.control.events.BattleAdaptor;
import robocode.control.events.BattleCompletedEvent;

class ScoreKeeper extends BattleAdaptor {
	
	float[] scores;
	String[] botNames;
	int[] botGenomeLengths;
	GeneticCode[] botGenomes;
	int botCount;

	//Start off using default total score
	private static float[] scoreWeightings = {1,0,0,0,0,0,0,0};
	private static boolean scoresAsFractionOfTotal=true;
	private static int maxGenomeLength;
	
	public ScoreKeeper(String[] names, int[] genomeLengths){
		botNames=names;
		botGenomeLengths=genomeLengths;
		botCount=botNames.length;
		scores = new float[botCount];
	}

	public void onBattleCompleted(BattleCompletedEvent e){
		float[] battleScore=new float[2];
		battleScore[0]=calculateScore(e.getIndexedResults()[0]);
		battleScore[1]=calculateScore(e.getIndexedResults()[1]);
		for (int i=0;i<2;i++){
			robocode.BattleResults result=e.getIndexedResults()[i];
			String botName=result.getTeamLeaderName();
			if(Arrays.asList(botNames).contains(botName)){
				int botIndex=Arrays.asList(botNames).indexOf(botName);
				
				//TODO: this is ugly; rewrite it in a more sensible way
				float weightedscore;
				if(scoresAsFractionOfTotal){
					weightedscore=(float)(1+battleScore[i])/(1+battleScore[0]+battleScore[1]);//Use this to differentiate between really shit and really really shit bots
				}else{
					weightedscore=battleScore[i];
				}
				
				//Add selective pressure to stop genomes growing too big
				weightedscore = adjustForGenomeLength(weightedscore,botGenomeLengths[botIndex]);

				scores[botIndex] = weightedscore;
			}

		}
	}
	
	public float[] getScores(){
		return scores;
	}
	
	private float calculateScore(BattleResults result){
		float total=0;
		
		total+= scoreWeightings[0] * result.getScore();
		total+= scoreWeightings[1] * result.getSurvival();
		total+= scoreWeightings[2] * result.getLastSurvivorBonus();
		total+= scoreWeightings[3] * result.getBulletDamage();
		total+= scoreWeightings[4] * result.getBulletDamageBonus();
		total+= scoreWeightings[5] * result.getRamDamage();//TODO: has this already been doubled?
		total+= scoreWeightings[6] * result.getRamDamageBonus();
		total+= scoreWeightings[7] * result.getFirsts();
		
		return total;
	}

	/*
	 * Score weightings are an array of coefficients for (in order):
	 * default robocode total score
	 * survival bonus
	 * last surviver bonus
	 * bullet damage
	 * bullet damage kill bonus
	 * ram damage
	 * ram damage kill bonus
	 * number of rounds won
	 */
	public static void alterScoreWeightings(float[] newWeightings){
		if (newWeightings.length==scoreWeightings.length){
			scoreWeightings=newWeightings;
		}else{
			System.err.println("Weightings array wrong length, weightings not updated");
		}
	}
	
	public static void setScoresAsFractionOfTotal(boolean newSetting){
		scoresAsFractionOfTotal=newSetting;
	}
	
	public static void setMaxGenomeLength(int newLength){
		maxGenomeLength=newLength;
	}
	
	public static int getMaxGenomeLength(){
		return maxGenomeLength;
	}
	
	private float adjustForGenomeLength(float basicScore,int genomeLength){
		if (genomeLength > maxGenomeLength ){
			int extraBytes = genomeLength - maxGenomeLength;
			float penaltyProportion = (float)extraBytes/maxGenomeLength;
			penaltyProportion = Math.min(penaltyProportion,1);//Don't allow more than all the points to be taken away
			basicScore -= penaltyProportion * basicScore;
		}
		return basicScore;
	}
	

}
