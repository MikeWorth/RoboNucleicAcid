package rna;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Lineage {
	
	private List<Lineage> ancestors=new ArrayList<Lineage>();
	private String name;
	private static final int MAXIMUMCOUSINALITYDEPTH=5;//this is the deepest a cousinality search will go, it is the value that is returned if no match is found
	
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
		ancestors.add(parent1);
		ancestors.add(parent2);
	}
	
	/*
	 * This is slightly different to the way in which society defines cousinality:
	 * sharing both parents gives 0
	 * sharing a pair of (great)^n grandparents gives n+1
	 * 
	 * This defines what people would consider half siblings as cousins, with similar for other 'half' relationships
	 */
	public static int getCousinality(Lineage line1,Lineage line2){
		
		if(line1.ancestors.size()==0)//they are from the same generation, don't need to check the other one
			return MAXIMUMCOUSINALITYDEPTH;
		
		if (line1.ancestors.get(0)==line2.ancestors.get(0) && line1.ancestors.get(1)==line2.ancestors.get(1)){
			return 0;
		}else{
			
			int closestRelationship=MAXIMUMCOUSINALITYDEPTH;
			for(int i=0;i<2;i++){
				for(int j=0;j<2;j++){
					closestRelationship=Math.min(closestRelationship ,getCousinality(line1.ancestors.get(i), line2.ancestors.get(j)));
				}
			}
			return closestRelationship + 1; 
		}
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
		if(ancestors.size()>=1){
			father=ancestors.get(0);
			longName+= sonOf + father.name;
			if(father.ancestors.size()>=1){
				grandFather=father.ancestors.get(0);
				longName+=grandsonOf+ grandFather.name;
			}
		}

		return longName;

	}

	
	public String getLongName(){
		return getLongName(false);
	}
	
	private static String randomName(){
		Random generator=new Random();
		return names()[generator.nextInt(names().length)];
		
	}
}
