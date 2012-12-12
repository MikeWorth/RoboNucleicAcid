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
	
	public String getLongName(){
		
		String longName=name;
		
		Lineage father;
		Lineage grandFather;
		if(ancestors.size()>=1){
			father=ancestors.get(0);
			longName+=", son of "+ father.name;
			if(father.ancestors.size()>=1){
				grandFather=father.ancestors.get(0);
				longName+=", grandson of "+ grandFather.name;
			}
		}
		
		return longName;
	}
	
	private static String randomName(){
		Random generator=new Random();
		return names()[generator.nextInt(names().length)];
		
	}
}
