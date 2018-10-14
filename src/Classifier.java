import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;


public class Classifier {

	public static void main(String args[]){
		
		Trainer.run();
		
		String csvFile = "C:/Users/Ibrahim/Downloads/testTweets.csv";
		BufferedReader br = null;
		String line = "";
		
		String cvsSplitBy = ",";
		Double accurate = 0.0;
		Double total = 0.0;
		
		HashMap<String, Double> posTopics = new HashMap<String, Double>();
		HashMap<String, Double> negTopics = new HashMap<String, Double>();
		HashMap<String, Double> neuTopics = new HashMap<String, Double>();
		
		int totalPos = 0, totalNeg = 0, totalNeu = 0;
		
		try {			 
			br = new BufferedReader(new FileReader(csvFile));
			while ((line = br.readLine()) != null) {
				String[] tweet = line.split(cvsSplitBy);
				
				ArrayList<String> ngrams = new ArrayList<String>();
				HashMap<String, Double> posProb = new HashMap<String, Double>();
				HashMap<String, Double> negProb = new HashMap<String, Double>();
				HashMap<String, Double> neuProb = new HashMap<String, Double>();
				Double pProb = 0.0;
				Double nProb = 0.0;
				Double eProb = 0.0;
				String classification = "";
				
				try{
					ngrams = Trainer.extractForClassification(tweet[4]);
					
					for(int i=0; i<ngrams.size(); i++){
						
						if(Trainer.positiveProb.containsKey(ngrams.get(i))){
							posProb.put(ngrams.get(i), Trainer.positiveProb.get(ngrams.get(i)));
						}
						if(Trainer.negativeProb.containsKey(ngrams.get(i))){
							negProb.put(ngrams.get(i), Trainer.negativeProb.get(ngrams.get(i)));
						}
						if(Trainer.neutralProb.containsKey(ngrams.get(i))){
							neuProb.put(ngrams.get(i), Trainer.neutralProb.get(ngrams.get(i)));
						}
					}
					
					if(posProb.size()>0){
						for(Entry<String, Double> temp : posProb.entrySet()){
					    	  pProb = pProb+temp.getValue();
					    }
						pProb = pProb*Trainer.posProb;
					}
					else{
						pProb = 0.0;
					}
					
					if(negProb.size()>0){
						for(Entry<String, Double> temp : negProb.entrySet()){
					    	  nProb = nProb+temp.getValue();
					    }
						nProb = nProb*Trainer.negProb;
					}
					else{
						nProb = 0.0;
					}

					if(neuProb.size()>0){
						for(Entry<String, Double> temp : neuProb.entrySet()){
					    	  eProb = eProb+temp.getValue();
					    }
						eProb = eProb*Trainer.neuProb;
					}
					else{
						eProb = 0.0;
					}
					
					//System.out.println("Positive Probability for tweet "+tweet[2]+": "+pProb);
					//System.out.println("Negative Probability for tweet "+tweet[2]+": "+nProb);
					//System.out.println("Neutral Probability for tweet "+tweet[2]+": "+eProb);
					
					double max = Math.max(pProb, Math.max(nProb, eProb));
					if(max == pProb){
						System.out.println("Classification for tweet "+tweet[2]+": positive");
						classification = "positive";
						
						if(posTopics.containsKey(tweet[0])){
							posTopics.put(tweet[0], posTopics.get(tweet[0])+1);
						}
						else
							posTopics.put(tweet[0], 1.0);
					}
					else if(max == nProb){
						System.out.println("Classification for tweet "+tweet[2]+": negative");
						classification = "negative";
						
						if(negTopics.containsKey(tweet[0])){
							negTopics.put(tweet[0], negTopics.get(tweet[0])+1);
						}
						else
							negTopics.put(tweet[0], 1.0);
					}
					else if(max == eProb){
						System.out.println("Classification for tweet "+tweet[2]+": neutral");
						classification = "neutral";
						
						if(neuTopics.containsKey(tweet[0])){
							neuTopics.put(tweet[0], neuTopics.get(tweet[0])+1);
						}
						else
							neuTopics.put(tweet[0], 1.0);
					}
					
					if(tweet[1].trim().equalsIgnoreCase(classification))
					{
						System.out.println("Classification was accurate \n");
						accurate++;
					}
					else
						System.out.println("Classification was inaccurate \n");
					
					total++;
					
					if(tweet[1].trim().equalsIgnoreCase("positive")){
						totalPos++;
					}
					else if(tweet[1].trim().equalsIgnoreCase("negative")){
						totalNeg++;
					}
					else if(tweet[1].trim().equalsIgnoreCase("neutral")){
						totalNeu++;
					}
				}
				catch(ArrayIndexOutOfBoundsException ae)
				{
					ae.printStackTrace();
				}
				
			}
			
			total = total - 67; //67 tweets were not parse-able and were causing exceptions, hence removed from the total
			System.out.println("Accurate: "+accurate);
			System.out.println("Total: "+total);	
			
			Double accuracy = (accurate/total)*100; 
			System.out.println("Accuracy: "+accuracy);
			
			System.out.println("Positive tweets: "+totalPos);
			System.out.println("Neutral tweets: "+totalNeu);
			System.out.println("Negative tweets: "+totalNeg);
			
			System.out.println("\n");
			System.out.println("For positive: ");
			for(Entry<String, Double> temp : posTopics.entrySet()){
				 System.out.println(temp.getKey() +": "+temp.getValue());
		     }
			 System.out.println("\n");
			 
			 System.out.println("For negative: ");
		     for(Entry<String, Double> temp : negTopics.entrySet()){
		    	 System.out.println(temp.getKey() +": "+temp.getValue());
		     }
		     System.out.println("\n");
		    
			 System.out.println("For neutral: ");
		     for(Entry<String, Double> temp : neuTopics.entrySet()){
		    	 System.out.println(temp.getKey() +": "+temp.getValue());
		     }
		     System.out.println("\n");
		}
		catch(FileNotFoundException fe){
			fe.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
