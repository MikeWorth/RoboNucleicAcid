package rna;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/*
 * GeneticCodes are a series of strings detailing actions for each of the possible events
 * Each string breaks down into blocks of 2 characters, this is a hexidecimal value for an
 * instruction. Sometimes the instruction refers to the following 2 characters, in this case
 * they are not also an instruction. 
 */
class GeneticCode {

	private static String robotPath="bin/rna/";
	private static int genomeLength = EvolveBot.numberOfGenes;
	private String[] genome;
	private String botName;
	private Lineage lineage;	
	private static double seedLength=0.9;//this is the chance of adding an additional command to the seed genes; it iterates until it gets lower than this value
	
	//These could be placed into the appropriate methods, but having them here makes tweaking easier. I think the difference is compiled out anyway
	private static double mutationBreedSwapChromosomeRate=0.01;//This is the probability that the 'wrong' string will be picked from a genome when breeding 
	private static double mutationBreedWrongOrderRate=0.01;//This is the probability that the beginning and end taken from the parents will be assembled the wrong way round when breeding
	private static double mutationSmallDeleteRate=0.001;
	private static double mutationSmallAddRate=0.001;
	private static double mutationSmallModifyRate=0.001;
	private static double mutationSmallModifyMagnitude=20;
	private static double mutationLargeDeleteRate=0.01;
	private static double mutationLargeReverseRate=0.01;
	private static double mutationLargeMoveRate=0.01;
	private static double mutationLargeRepeatRate=0.01;

	
	//This creates a random seed genome
	public GeneticCode(String name){
		botName=name;
		lineage=new Lineage();
		genome = new String[genomeLength];
		for(int i=0;i<genomeLength;i++){
			genome[i]="";
			while(RobotBreeder.generator.nextDouble()<seedLength){
				for(int j=0;j<4;j++){
					String gene=Integer.toString(RobotBreeder.generator.nextInt(16),16);
					genome[i]+=gene;
				}
			}
			if((genome[i].length() % 4)!=0)System.err.println("Length error: "+genome[i]);
		}
	}

	//This breeds a new genome from 2 parents 
	public GeneticCode(String name,GeneticCode parent1,GeneticCode parent2){

		botName=name;
		
		lineage=new Lineage(parent1.getLineage(),parent2.getLineage());
		
		genome = new String[genomeLength];

		for (int i=0; i<genomeLength;i++){
			genome[i]="";
			
			
			//Occasionally get the 'wrong' Chromosome
			String[] parentGenome = new String[2]; 
			for(int j=0;j<2;j++){
				if(RobotBreeder.generator.nextDouble()<mutationBreedSwapChromosomeRate){
					parentGenome[j]=parent1.genome[RobotBreeder.generator.nextInt(genomeLength)];
				}else{
					parentGenome[j]=parent1.genome[i];
				}
			}
						
			//make genomeSmall the shorter of the two TODO: use library sort?
			String genomeSmall="";
			String genomeLarge="";
			if(parentGenome[0].length() < parentGenome[1].length()){
				genomeSmall=parentGenome[0];
				genomeLarge=parentGenome[1];
			}else{
				genomeSmall=parentGenome[1];
				genomeLarge=parentGenome[0];
			}
			
			int chopPlace=0;
			if (genomeSmall.length()>0)
				chopPlace=RobotBreeder.generator.nextInt(genomeSmall.length());
			
			while((chopPlace % 2) != 0)
				chopPlace-=1;
			int avgLength=(genomeSmall.length()+genomeLarge.length())/2;
			while((avgLength % 2) != 0)
				avgLength+=1;
			
			//select the 2 constituent bits from the parent strings
			String firstBit="";
			String secondBit="";
			if(genomeSmall.length()>0)
				firstBit=genomeSmall.substring(0,chopPlace);
			if(genomeLarge.length()>0)
				secondBit=genomeLarge.substring(genomeLarge.length()-(avgLength-chopPlace));
			
			//Put them together, occasionally putting them back to front
			if(RobotBreeder.generator.nextDouble()<mutationBreedWrongOrderRate){
				genome[i] = secondBit + firstBit;
			}else{
				genome[i] = firstBit + secondBit;
			}
			
			if((genome[i].length() % 2)!=0)
				System.err.println("Breeding Length error: "+genome[i]+" "+parent1.genome[i]+" "+parent2.genome[i]);
		}
		mutateSmallScale();
		mutateLargeScale();
		
		//This prevents problems with things like 'add the following value' on the end of genomes
		for(int i=0;i<genomeLength;i++){
			if (genome[i].length()>0){
				String lastByte=genome[i].substring(genome[i].length()-2,genome[i].length());
				int lastVal=Integer.parseInt(lastByte, 16);
				if (GeneticCode.expectsFollowingValue(lastVal))
					genome[i]+="00";
			}
		}

	}
	
	public String toString(){
		String genomeString="";
		for(int i=0;i<genomeLength;i++){
			genomeString+=genome[i] + ",";
		}
		genomeString=genomeString.substring(0,genomeString.length()-2);//strip the trailing ,
		return genomeString;
	}
	
	public String toJavaCode(){
		String javaCode="package rna;" +
				"import robocode.*;" +
				"public class " + getLineage().getLongName(true) + " extends AdvancedRobot{" +
						"public void run() {";
		String[] eventValues={};
		javaCode+=GeneToJava(genome[0], eventValues);
		javaCode+="while(true){ahead(1);";
		javaCode+=GeneToJava(genome[1], eventValues);
		javaCode+="}}public void onScannedRobot(ScannedRobotEvent e) {";
		String[] eventValues2={"e.getBearing()","e.getDistance()","e.getEnergy()","e.getHeading()","e.getVelocity()"};
		javaCode+=GeneToJava(genome[2], eventValues2);
		javaCode+="}public void onHitByBullet(HitByBulletEvent e) {";
		String[] eventValues3={"e.getBearing()","e.getHeading()","e.getPower()","e.getVelocity()"};
		javaCode+=GeneToJava(genome[3], eventValues3);
		javaCode+="}public void onHitRobot(HitRobotEvent e) {";
		String[] eventValues4={"e.getBearing()","e.getEnergy()"};
		javaCode+=GeneToJava(genome[4], eventValues4);
		javaCode+="}public void onHitWall(HitWallEvent e) {";
		String[] eventValues5={"e.getBearing()"};
		javaCode+=GeneToJava(genome[5], eventValues5);
		javaCode+="}public void onBulletHitBullet(BulletHitBulletEvent e) {";
		String[] eventValues6={};
		javaCode+=GeneToJava(genome[6], eventValues6);
		javaCode+="}public void onBulletHit(BulletHitEvent e) {";
		String[] eventValues7={"e.getEnergy()"};
		javaCode+=GeneToJava(genome[7], eventValues7);
		javaCode+="}public void onBulletMissed(BulletMissedEvent e) {";
		String[] eventValues8={};
		javaCode+=GeneToJava(genome[8], eventValues8);
		javaCode+="}}";
		return javaCode;
	}
	
	String GeneToJava(String gene,String[] eventValues){//TODO:I'm fairly sure commands like setAhead(0) don't do anything; if so do't bother parsing them
		String javaCode="";
		String workingValue="0";//reset this each time
		int loopDepth=0;
		for(int i=0;i< gene.length();i+=2){
			int command=Integer.parseInt(gene.substring(i,i+2), 16);
			String value="";
			if (GeneticCode.expectsFollowingValue(command) && i + 4 <= gene.length()){
				value=String.valueOf( Integer.parseInt( gene.substring(i+2,i+4) , 16) );
				i+=2;			
			}
			switch(command){
			case 0:
				if(loopDepth>0){
					javaCode+="}";
					loopDepth-=1;
				}
				break;
			case 1:
				javaCode+="ahead("+workingValue+");";
				break;
			case 2:
				javaCode+="back("+workingValue+");";
				break;
			case 3:
				javaCode+="fire("+workingValue+"/80);";//TODO scale?
				break;
			case 4:
				javaCode+="turnGunLeft("+workingValue+");";
				break;
			case 5:
				javaCode+="turnGunRight("+workingValue+");";
				break;
			case 6:
				//javaCode+="turnRadarLeft("+workingValue+");";
				break;
			case 7:
				//javaCode+="turnRadarRight("+workingValue+");";
				break;
			case 8:
				javaCode+="turnLeft("+workingValue+");";
				break;
			case 9:
				javaCode+="turnRight("+workingValue+");";
				break;
			case 10:
				javaCode+="execute();";
				break;
			case 11:
				javaCode+="setAhead("+workingValue+");";
				break;
			case 12:
				javaCode+="setBack("+workingValue+");";
				break;
			case 13:
				javaCode+="setFire("+workingValue+"/80);";//TODO scale?
				break;
			case 14:
				javaCode+="setMaxTurnRate("+workingValue+"/25);";
				break;
			case 15:
				javaCode+="setMaxVelocity("+workingValue+"/32);";
				break;
			case 16:
				javaCode+="setTurnGunLeft("+workingValue+");";
				break;
			case 17:
				javaCode+="setTurnGunRight("+workingValue+");";
				break;
			case 18:
				javaCode+="setTurnLeft("+workingValue+");";
				break;
			case 19:
				javaCode+="setTurnRight("+workingValue+");";
				break;
			case 20:
				//javaCode+="setTurnRadarLeft("+workingValue+");";
				break;
			case 21:
				//javaCode+="setTurnRadarRight("+workingValue+");";
				break;
			case 22:
				workingValue = "("+workingValue+"+"+value+")";
				break;
			case 23:
				workingValue = "("+workingValue+"-"+value+")";
				break;
			case 24:
				workingValue = "("+workingValue+"*"+value+")";
				break;
			case 25:
				workingValue = "("+workingValue+"/"+value+")";
				break;
			case 26:
				workingValue=value;
				break;
			case 27:
				javaCode+="if (!("+workingValue+">"+value+")){";
				loopDepth+=1;
				break;
			case 28:
				javaCode+="if (!("+workingValue+"<"+value+")){";
				loopDepth+=1;
				break;
			case 29:
				javaCode+="if (!("+workingValue+"=="+value+")){";
				loopDepth+=1;
				break;
			default:
				int staticCommands=30;
				if(command<staticCommands+eventValues.length){
					workingValue=eventValues[command-staticCommands]; 
				}else if(command<staticCommands+2*eventValues.length){
					workingValue="("+workingValue+"+"+eventValues[command-(staticCommands+eventValues.length)]+")"; 
				}else if(command<staticCommands+3*eventValues.length){
					workingValue="("+workingValue+"+"+eventValues[command-(staticCommands+2*eventValues.length)]+")"; 
				}else if(command<staticCommands+4*eventValues.length){
					workingValue="("+workingValue+"+"+eventValues[command-(staticCommands+3*eventValues.length)]+")"; 
				}else if(command<staticCommands+5*eventValues.length){
					workingValue="("+workingValue+"+"+eventValues[command-(staticCommands+4*eventValues.length)]+")"; 
				}
				break;//otherwise, do nothing- junk rna
			}
		}
		for (;loopDepth>0;loopDepth-=1)
			javaCode+="}";
		return javaCode;
	}
	
	//Apply small scale genetic mutations to the genome
	void mutateSmallScale(){
		//TODO: rewrite different mutations as independent?
		for (int i=0; i<genomeLength;i++){

			//Delete some bits
			int numberOfDeletions=(int) (genome[i].length() * mutationSmallDeleteRate);//TODO: add a gausian for varying numbers of mutations
			for(int j=0;j<numberOfDeletions;j++){
				int mutationPosition=RobotBreeder.generator.nextInt(genome[i].length()-2);//This cannot be too close to the end; it's the start of the mutation
				genome[i]=genome[i].substring(0,mutationPosition) + genome[i].substring(mutationPosition+2);
			}
			
			//Add some bits
			int numberOfAdditions=(int) (genome[i].length() * mutationSmallAddRate);//TODO: add a gausian for varying numbers of mutations
			for(int j=0;j<numberOfAdditions;j++){
				int mutationPosition=RobotBreeder.generator.nextInt(genome[i].length()-2);//This cannot be too close to the end; it's the start of the mutation				
				String newBase=Integer.toHexString(RobotBreeder.generator.nextInt(256));
				while(newBase.length()<2){
					newBase="0"+newBase;
				}
				
				genome[i]=genome[i].substring(0,mutationPosition) + newBase + genome[i].substring(mutationPosition);
			}
			
			//Change the value of some bits
			int numberOfModifications=(int) (genome[i].length() * mutationSmallModifyRate);//TODO: add a gausian for varying numbers of mutations
			for(int j=0;j<numberOfModifications;j++){
				int mutationPosition=RobotBreeder.generator.nextInt(genome[i].length()-2);//This cannot be too close to the end; it's the start of the mutation
				
				int oldBaseVal = Integer.valueOf(genome[i].substring(mutationPosition,mutationPosition+2),16);
				int newBaseVal= oldBaseVal + (int)(RobotBreeder.generator.nextGaussian()*mutationSmallModifyMagnitude);
				while (newBaseVal>255)newBaseVal-=256;//a gaussian has a very small but finite chance of being bloody miles away from 0
				while (newBaseVal<0)newBaseVal+=256;

				String newBase=Integer.toHexString(newBaseVal);
				while(newBase.length()<2){
					newBase="0"+newBase;
				}
				
				genome[i]=genome[i].substring(0,mutationPosition) + newBase + genome[i].substring(mutationPosition+2);
			}


			if((genome[i].length()% 2)!=0)System.err.println("Mutated Length error: "+genome[i]+" "+genome[i]);
		}
	}
	
	void mutateLargeScale(){
		for (int i=0; i<genomeLength;i++){
			//split the chromosome into pre,mutation,post. Potential for frame misalignment is intentional
			int[] boundaries = new int[2];
			if(genome[i].length()>0){
				for(int j=0;j<2;j++){
					boundaries[j]=RobotBreeder.generator.nextInt(genome[i].length()-1);//TODO: control relative positions of boundaries in some way?
				}
				Arrays.sort(boundaries);
				
				String pre=genome[i].substring(0,boundaries[0]);
				String mutation=genome[i].substring(boundaries[0],boundaries[1]);
				String post=genome[i].substring(boundaries[1]);
				
				//only do at most one; this makes things a bit simpler and these probabilities coming up together is rare enough to make a negligible difference
				if(RobotBreeder.generator.nextDouble()<mutationLargeDeleteRate){
					//delete a chunk
					genome[i]=pre + post;
				}else if(RobotBreeder.generator.nextDouble()<mutationLargeReverseRate){
					//reverse order of bytes in chunk
					String flippedMutation="";
					for (int k=mutation.length()-1;k>=0;k--){
						flippedMutation+=mutation.charAt(k);
					}
					genome[i] = pre + flippedMutation + post;
				}else if(RobotBreeder.generator.nextDouble()<mutationLargeMoveRate){
					//move a chunk
					StringBuffer genomeBuffer = new StringBuffer(pre + post);
					int insertLocation=RobotBreeder.generator.nextInt(genomeBuffer.length()-1);
					genomeBuffer.insert(insertLocation, mutation);
					genome[i]=genomeBuffer.toString();
				}else if(RobotBreeder.generator.nextDouble()<mutationLargeRepeatRate){
					//repeat chunk
					genome[i]=pre + mutation + mutation + post;
				}

				//Make sure length is made of an even number of characters(nybbles)
				if((genome[i].length() % 2)!=0)
					genome[i]+="0";
	
			}
		}
	}
	
	
	
	//write this genome to a file so a robot can use it
	public void commitToRobot() throws FileNotFoundException, UnsupportedEncodingException{//TODO: strip out junk rna here to improve efficiency
		commitToRobot(botName,false);
	}	
	public void commitToRobot(String name,boolean javaCode) throws FileNotFoundException, UnsupportedEncodingException{//TODO: strip out junk rna here to improve efficiency

		String shortName=name.substring(name.lastIndexOf('.')+1);//This doesn't have the package prefix
		if (shortName.substring(shortName.length()-1).equals("*"))shortName=shortName.substring(0,shortName.length()-1);//TODO fix the bastard * issue
		
		File robotDataDirectory=new File(robotPath + shortName + ".data");
		if(!robotDataDirectory.exists())
			robotDataDirectory.mkdir();
		
		PrintWriter rnaWriter = new PrintWriter(robotPath + shortName + ".data/geneticcode.rna");
		
		rnaWriter.println(getPersonifiedName());
		
		for (int i=0; i<genomeLength;i++){
			rnaWriter.println(genome[i]);
		}
		rnaWriter.close();
		
		if(javaCode){
			PrintWriter sourceWriter = new PrintWriter(robotPath + shortName + ".data/source.java");//TODO: I think this is very expensive for large genomes; do it more sparingly? 
			sourceWriter.println(toJavaCode());
			sourceWriter.close();
		}
	}
	
	public String getName(){
		return botName;
	}
	public Lineage getLineage(){
		return lineage;
	}
	public String getPersonifiedName(){
		return lineage.getLongName();
	}
	
	private static boolean expectsFollowingValue(int instruction){
		return (
				(instruction==22) ||
				(instruction==23) ||
				(instruction==24) ||
				(instruction==25) ||
				(instruction==26) ||	
				(instruction==27) ||	
				(instruction==28) ||	
				(instruction==29) ||	
		false);//false is here to make above lines all end with ||
	}
}
