package rna;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Random;

/*
 * GeneticCodes are a series of strings detailing actions for each of the possible events
 * Each string breaks down into blocks of 2 characters, this is a hexidecimal value for an
 * instruction. Sometimes the instruction refers to the following 2 characters, in this case
 * they are not also an instruction. 
 */
class GeneticCode {

	private static String robotPath="/home/mike/workspace/RoboNucleicAcid/bin/rna/";
	private static int genomeLength = EvolveBot.numberOfGenes;
	private String[] genome;
	private static double mutationRate=0.001;
	private static double mutationMagnitude=20;
	private static double seedLength=0.995;//this is the chance of adding an aditional command to the seed genes; it iterates until it gets lower than this value
	private String botName;
	private Lineage lineage;
	
	//This creates a random seed genome
	public GeneticCode(){
		lineage=new Lineage();
		genome = new String[genomeLength];
		Random generator=new Random();
		for(int i=0;i<genomeLength;i++){
			genome[i]="";
			while(generator.nextDouble()<seedLength){
				for(int j=0;j<4;j++){
					String gene=Integer.toString(generator.nextInt(16),16);
					genome[i]+=gene;
				}
			}
			if((genome[i].length() % 4)!=0)System.err.println("Length error: "+genome[i]);
		}
	}

	//This breeds a new genome from 2 parents 
	public GeneticCode(GeneticCode parent1,GeneticCode parent2){
		
		//We can only provide one lineage; pick the first for no particular reason
		lineage=new Lineage(parent1.getLineage(),parent2.getLineage());
		
		genome = new String[genomeLength];
		Random generator=new Random();

		for (int i=0; i<genomeLength;i++){
			genome[i]="";
			String genome1="";
			String genome2="";

			//make genome1 the shorter of the two
			if(parent1.genome[i].length() < parent2.genome[i].length()){
				genome1=parent1.genome[i];
				genome2=parent2.genome[i];
			}else{
				genome1=parent2.genome[i];
				genome2=parent1.genome[i];
			}
			
			int chopPlace=0;
			if (genome1.length()>0)
				chopPlace=generator.nextInt(genome1.length());
			
			while((chopPlace % 2) != 0)
				chopPlace-=1;
			int avgLength=(genome1.length()+genome2.length())/2;
			while((avgLength % 2) != 0)
				avgLength+=1;
			
			if(genome1.length()>0)
				genome[i]+=genome1.substring(0,chopPlace);
			if(genome2.length()>0)
				genome[i]+=genome2.substring(genome2.length()-(avgLength-chopPlace));
			if((genome[i].length() % 2)!=0)
				System.err.println("Breeding Length error: "+genome[i]+" "+parent1.genome[i]+" "+parent2.genome[i]);
		}
		mutate();
		
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
		javaCode+="while(true){";
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
			if (GeneticCode.expectsFollowingValue(command)){
				value=String.valueOf( Integer.parseInt( gene.substring(i,i+2) , 16) );
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
				workingValue+="+"+value;
				break;
			case 23:
				workingValue+="-"+value;
				break;
			case 24:
				workingValue+="*"+value;
				break;
			case 25:
				workingValue+="/"+value;
				break;
			case 26:
				workingValue=value;
				break;
			case 27:
				javaCode+="if("+workingValue+">"+value+"){";
				loopDepth+=1;
				break;
			case 28:
				javaCode+="if("+workingValue+"<"+value+"){";
				loopDepth+=1;
				break;
			case 29:
				javaCode+="if("+workingValue+"=="+value+"){";
				loopDepth+=1;
				break;
			default:
				int staticCommands=30;
				if(command<staticCommands+eventValues.length){
					workingValue=eventValues[command-staticCommands]; 
				}else if(command<staticCommands+2*eventValues.length){
					workingValue="("+workingValue+")+"+eventValues[command-(staticCommands+eventValues.length)]; 
				}else if(command<staticCommands+3*eventValues.length){
					workingValue="("+workingValue+")+"+eventValues[command-(staticCommands+2*eventValues.length)]; 
				}else if(command<staticCommands+4*eventValues.length){
					workingValue="("+workingValue+")+"+eventValues[command-(staticCommands+3*eventValues.length)]; 
				}else if(command<staticCommands+5*eventValues.length){
					workingValue="("+workingValue+")+"+eventValues[command-(staticCommands+4*eventValues.length)]; 
				}
				break;//otherwise, do nothing- junk rna
			}
		}
		for (;loopDepth>0;loopDepth-=1)
			javaCode+="}";
		return javaCode;
	}
	
	//Apply genetic mutations to the genome
	void mutate(){
		//TODO: different types of mutation, copying/moving genes from one to another?, swapping the order of genes? 
		Random generator=new Random();
		for (int i=0; i<genomeLength;i++){
			String mutatedGene="";
			
			for(int j=0;j<genome[i].length();j+=2){
				String base=genome[i].substring(j,j+2);
				int oldBaseVal=Integer.valueOf(base,16);
				int newBaseVal=0;
				if(generator.nextDouble()<mutationRate){

					double mutationType=generator.nextDouble();
					if(mutationType<0.5){//TODO:tweak probabilities?
						//Change the value of a gene
						newBaseVal= oldBaseVal + (int)(generator.nextGaussian()*mutationMagnitude);
						while (newBaseVal>255)newBaseVal-=256;//a gaussian has a very small but finite chance of being bloody miles away from 0
						while (newBaseVal<0)newBaseVal+=256;
						base=Integer.toHexString(newBaseVal);
						if(base.length()==1)base="0"+base;
					}else if(mutationType<0.75){//TODO:tweak probabilities?
						//Add a gene, potential to misalign operations/arguements is intentional
						String newBase=Integer.toHexString(generator.nextInt(256));
						while(newBase.length()<2){
							newBase="0"+newBase;
						}
						base+=newBase;
					}else{
						//Remove a gene
						base="";
					}
				}
				if(base.length()!=2&&base.length()!=4&&base.length()!=0) System.err.println("Baselength wrong:"+base+":"+Integer.valueOf(oldBaseVal)+":"+Integer.valueOf(newBaseVal));
				mutatedGene+=base;
			}
			//Even if the genome is empty, we could add one to the end
			if(generator.nextDouble()<mutationRate){
				String newBase=Integer.toHexString(generator.nextInt(256));
				while(newBase.length()<2){
					newBase="0"+newBase;
				}
				mutatedGene+=newBase;				
			}

			if((mutatedGene.length()% 2)!=0)System.err.println("Mutated Length error: "+genome[i]+" "+mutatedGene);
			genome[i]=mutatedGene;
		}
	}
	
	//write this genome to a file so a robot can use it
	public void commitToRobot(String name) throws FileNotFoundException, UnsupportedEncodingException{//TODO: strip out junk rna here to improve efficiency

		botName=name;
		String shortName=name.substring(name.lastIndexOf('.')+1);//This doesn't have the package prefix
		
		File robotDataDirectory=new File(robotPath + shortName + ".data");
		if(!robotDataDirectory.exists())
			robotDataDirectory.mkdir();
		
		PrintWriter rnaWriter = new PrintWriter(robotPath + shortName + ".data/geneticcode.rna");
		
		rnaWriter.println(getPersonifiedName());
		
		for (int i=0; i<genomeLength;i++){
			rnaWriter.println(genome[i]);
		}
		rnaWriter.close();

		PrintWriter sourceWriter = new PrintWriter(robotPath + shortName + ".data/source.java");
		sourceWriter.println(toJavaCode());
		sourceWriter.close();
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