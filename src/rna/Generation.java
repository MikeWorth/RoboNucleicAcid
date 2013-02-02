package rna;

import java.util.Random;

public class Generation {

	private GeneticCode[] bots;
	private int botCount;
	
	public Generation(String[] botNames){
		botCount=botNames.length;
		bots=new GeneticCode[botCount];
		for(int i=0;i<botCount;i++){
			bots[i] = new GeneticCode(botNames[i]);
		}

	}
	
	public Generation(String[] botNames,GeneticCode[] rankedParents,int ClosestAllowedIncest){

		botCount=botNames.length;
		bots=new GeneticCode[botCount];

		for(int i=0;i<botCount;i++){
			GeneticCode parent1=getWeightedRandomBot(rankedParents);
			GeneticCode parent2=getWeightedRandomBot(rankedParents);
			
			//promote genetic diversity by prohibiting incest:
			while(Lineage.getCousinality(parent1.getLineage(), parent2.getLineage(),ClosestAllowedIncest+1)<ClosestAllowedIncest){
				parent1=getWeightedRandomBot(rankedParents);
				parent2=getWeightedRandomBot(rankedParents);
			}
			
			bots[i] = new GeneticCode(botNames[i],parent1,parent2);
		}

	}
	
	public GeneticCode[] getBots(){
		return bots;
	}
	
	public double getAverageCousinality(){
		double generationCousinality=0;
		int averageDenominator=botCount*(botCount-1)/2;
		int maxSearchDepth=(int) ((Math.log(botCount)/Math.log(2))+0.5);//log2(BOTCOUNT) round up; it is impossible to be any less related than this
		for(int i=0;i<botCount;i++){
			for(int j=i+1;j<botCount;j++){
				int cousinality=Lineage.getCousinality(bots[i].getLineage(), bots[j].getLineage(),maxSearchDepth);
				generationCousinality+= ((double) cousinality)/averageDenominator;
			}
		}
		return generationCousinality;
	}
	
	private GeneticCode getWeightedRandomBot(GeneticCode[] rankedBots){
		int botCount = rankedBots.length;//This can be different from the size of the generation 
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
		
		//We shouldn't get here, but if we do the winner will do
		return rankedBots[0];
	}
}
