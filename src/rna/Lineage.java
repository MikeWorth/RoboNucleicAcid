package rna;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Lineage {
	
	private List<Lineage> parents=new ArrayList<Lineage>(2);
	private String name;
	private static int NumberOfPreviousGenerationsToRemember=10; //Keeping thousands of generations starts to gum things up

	private static String[] names() {
		FileReader nameFile=null;
		try {
			nameFile = new FileReader("names.txt");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BufferedReader br = new BufferedReader(nameFile);
		List<String> names = new ArrayList<String>();
		String name=null;
		try {
			while((name=br.readLine())!=null){
				if(!name.subSequence(0,2).equals("//"))
					names.add(name);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return names.toArray(new String[names.size()]);
	}

	public Lineage(){
		name = randomName();
	}

	public Lineage(Lineage parent1,Lineage parent2){
		name = randomName();
		parents.add(parent1);
		parents.add(parent2);

		pruneFamilyTree (NumberOfPreviousGenerationsToRemember);//We don't do anything with them, so delete them to prevent ever-increasing storage of crap
	}
	
	/*
	 * This is slightly different to the way in which society defines cousinality:
	 * sharing both parents gives 0
	 * sharing a pair of (great)^n grandparents gives n+1
	 * 
	 * This defines what people would consider half siblings as cousins, with similar for other 'half' relationships
	 */
	private static int getCousinality(Lineage line1,Lineage line2,int maxDepth,int currentDepth){
		
		if(currentDepth==maxDepth-1)
			return maxDepth;
		
		if(line1.parents.size()==0 || line2.parents.size()==0)
			return maxDepth;
		
		if (line1.parents.get(0)==line2.parents.get(0) && line1.parents.get(1)==line2.parents.get(1)){
			return 0;
		}else{
			
			int closestRelationship=maxDepth-1;
			for(int i=0;i<2;i++){
				for(int j=0;j<2;j++){
					closestRelationship=Math.min(closestRelationship ,getCousinality(line1.parents.get(i), line2.parents.get(j),maxDepth,currentDepth+1));
				}
			}
			return closestRelationship + 1; 
		}
	}
	public static int getCousinality(Lineage line1,Lineage line2,int maxDepth){
		return getCousinality( line1, line2,maxDepth,0);
	}
	
	public String getLongName(boolean camelCase){

		String sonOf=", son of ";
		String grandsonOf=", grandson of ";
		if (camelCase){
			sonOf="SonOf";
			grandsonOf="GrandsonOf";
		}


		String longName=name;

		Lineage father;
		Lineage grandFather;
		if(parents.size()>=1){
			father=parents.get(0);
			longName+= sonOf + father.name;
			if(father.parents.size()>=1){
				grandFather=father.parents.get(0);
				longName+=grandsonOf+ grandFather.name;
			}
		}

		return longName;

	}

	
	public String getLongName(){
		return getLongName(false);
	}
	
	private static String randomName(){
		return names()[RobotBreeder.generator.nextInt(names().length)];
		
	}

	private void pruneFamilyTree (int depth){
		if (depth==0){
			parents=new ArrayList<Lineage>();//overwrite ancestors list with nothing
		}else{
			for (Lineage parent : parents){
				parent.pruneFamilyTree(depth-1);
			}
		}
	}
}
