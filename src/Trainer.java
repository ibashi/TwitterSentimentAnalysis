import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;


public class Trainer {
	
	static final int N = 2;
	static TokenizerModel tm = null;
	static TokenizerME wordBreaker = null;
	static POSModel pm = null;
	static POSTaggerME posme = null;
	static InputStream modelIn = null;
	static ChunkerModel chunkerModel = null;
	static ChunkerME chunkerME = null;
	
	public static double posProb = 0.0;
	public static double negProb = 0.0;
	public static double neuProb = 0.0;
	
	public static HashMap<String, Double> positive = new HashMap<String, Double>();
	public static HashMap<String, Double> negative = new HashMap<String, Double>();
	public static HashMap<String, Double> neutral = new HashMap<String, Double>();
	
	public static HashMap<String, Double> positiveProb = new HashMap<String, Double>();
	public static HashMap<String, Double> negativeProb = new HashMap<String, Double>();
	public static HashMap<String, Double> neutralProb = new HashMap<String, Double>();

	public static Double totalPos = 0.0;
	public static Double totalNeg = 0.0;
	public static Double totalNeu = 0.0;
	public static Double totalTweets = 0.0;
	
	public static void run()
	{
		train();
		String csvFile = "C:/Users/Ibrahim/Downloads/full-sentiment.csv";
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";
		
		double tweetsProcessed = 0.0;

		try {
	 
			br = new BufferedReader(new FileReader(csvFile));
			while ((line = br.readLine()) != null) {
				String[] tweet = line.split(cvsSplitBy);
				try{
					//System.out.println("Tweet [ID= " + tweet[2] + " , text=" + tweet[4] + ", sentiment= " + tweet[1]+"]");
					if(tweet[1].trim().equalsIgnoreCase("positive")){
						totalPos++;
						extract(tweet[4], 1);
					}
					else if(tweet[1].trim().equalsIgnoreCase("neutral")){
						totalNeu++;
						extract(tweet[4], 0);
					}
					else if(tweet[1].trim().equalsIgnoreCase("negative")){
						totalNeg++;
						extract(tweet[4], 2);
					}
					tweetsProcessed++;
					System.out.println("Processing..."+(tweetsProcessed/5122)*100+"%");
				}
				catch (ArrayIndexOutOfBoundsException e) {
					e.printStackTrace();
					tweetsProcessed++;
					System.out.println("Processing..."+(tweetsProcessed/5122)*100+"%");
				}
			}
	 
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		  ArrayList<String> maxKeysPos= new ArrayList<String>();
		  Double maxValuePos = Double.MIN_VALUE; 
	      for(Entry<String, Double> temp : positive.entrySet()){
	    	  positiveProb.put(temp.getKey(), temp.getValue()/totalPos);
	    	     if(temp.getValue() > maxValuePos) {
	    	         maxKeysPos.clear(); /* New max remove all current keys */
	    	         maxKeysPos.add(temp.getKey());
	    	         maxValuePos = temp.getValue();
	    	     }
	    	     else if(temp.getValue() == maxValuePos)
	    	     {
	    	       maxKeysPos.add(temp.getKey());
	    	     }
	      }
	      
	      ArrayList<String> maxKeysNeu= new ArrayList<String>();
		  Double maxValueNeu = Double.MIN_VALUE; 
	      for(Entry<String, Double> temp : neutral.entrySet()){
	    	  neutralProb.put(temp.getKey(), temp.getValue()/totalNeu);
	    	     if(temp.getValue() > maxValueNeu) {
	    	    	 maxKeysNeu.clear(); /* New max remove all current keys */
	    	    	 maxKeysNeu.add(temp.getKey());
	    	    	 maxValueNeu = temp.getValue();
	    	     }
	    	     else if(temp.getValue() == maxValueNeu)
	    	     {
	    	    	 maxKeysNeu.add(temp.getKey());
	    	     }
	      }
	      
	      ArrayList<String> maxKeysNeg= new ArrayList<String>();
		  Double maxValueNeg = Double.MIN_VALUE; 
	      for(Entry<String, Double> temp : negative.entrySet()){
	    	  negativeProb.put(temp.getKey(), temp.getValue()/totalNeg);
	    	  if(temp.getValue() > maxValueNeg) {
	    		  	 maxKeysNeg.clear(); /* New max remove all current keys */
	    		  	 maxKeysNeg.add(temp.getKey());
	    	    	 maxValueNeg = temp.getValue();
	    	     }
	    	     else if(temp.getValue() == maxValueNeg)
	    	     {
	    	    	 maxKeysNeg.add(temp.getKey());
	    	     }
	      }
	      
	      for(Entry<String, Double> temp : positiveProb.entrySet()){
	    	 System.out.println(temp.getKey() +": "+temp.getValue());
	      }
	      for(Entry<String, Double> temp : neutralProb.entrySet()){
	    	  System.out.println(temp.getKey() +": "+temp.getValue());
	      }
	      for(Entry<String, Double> temp : negativeProb.entrySet()){
	    	  System.out.println(temp.getKey() +": "+temp.getValue());
	      }
		
	    totalTweets = totalPos+totalNeu+totalNeg;
		System.out.println("Positive tweets: "+totalPos);
		System.out.println("Neutral tweets: "+totalNeu);
		System.out.println("Negative tweets: "+totalNeg);
		System.out.println("Total tweets: "+totalTweets);
		
		System.out.println("Number of positive ngrams: "+positiveProb.size());
		System.out.println("Number of neutral ngrams: "+neutralProb.size());
		System.out.println("Number of negative ngrams: "+negativeProb.size());
		
		posProb = totalPos/totalTweets;
		neuProb = totalNeu/totalTweets;
		negProb = totalNeg/totalTweets;
		
		System.out.println("Positive probability: "+posProb);
		System.out.println("Negative probability: "+negProb);
		System.out.println("Neutral probability: "+neuProb);
		
		for(int i=0; i<maxKeysPos.size(); i++){
			System.out.println("Max positive probability: "+maxKeysPos.get(i));
		}
		
		for(int i=0; i<maxKeysNeg.size(); i++){
			System.out.println("Max negative probability: "+maxKeysNeg.get(i));
		}
		
		for(int i=0; i<maxKeysNeu.size(); i++){
			System.out.println("Max neutral probability: "+maxKeysNeu.get(i));
		}
		
		System.out.println("Done");
	  }
	
	public static void train(){
		try{
			tm = new TokenizerModel(new FileInputStream(new File("en-token.bin")));
		    wordBreaker = new TokenizerME(tm);
		    pm = new POSModel(new FileInputStream(new File("en-pos-maxent.bin")));
		    posme = new POSTaggerME(pm);
		    modelIn = new FileInputStream("en-chunker.bin");
		    chunkerModel = new ChunkerModel(modelIn);
		    chunkerME = new ChunkerME(chunkerModel);
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public static ArrayList<String> extractForClassification(String tweet){      
		  ArrayList<String> tweetNgrams = new ArrayList<String>();
		  String sentence = tweet;
	      sentence = sentence.toLowerCase();
	      
	      if(sentence.contains(" a ")){
	    	  sentence = sentence.replace(" a ", " ");
	      }
	      else if(sentence.contains(" an ")){
	    	  sentence = sentence.replace(" an ", " ");
	      }
	      else if(sentence.contains(" as ")){
	    	  sentence = sentence.replace(" as ", " ");
	      }
	      else if(sentence.contains(" the ")){
	    	  sentence = sentence.replace(" the ", " ");
	      }
	      else if(sentence.contains(" i ")){
	    	  sentence = sentence.replace(" i ", " ");
	      }
	      else if(sentence.contains(" he ")){
	    	  sentence = sentence.replace(" he ", " ");
	      }
	      else if(sentence.contains(" she ")){
	    	  sentence = sentence.replace(" she ", " ");
	      }
	      else if(sentence.contains(" their ")){
	    	  sentence = sentence.replace(" their ", " ");
	      }
	      else if(sentence.contains(" our ")){
	    	  sentence = sentence.replace(" our ", " ");
	      }
	      sentence = sentence.replaceAll("[^a-zA-Z ]", "");
	           
	      //words is the tokenized sentence
	      String[] words = wordBreaker.tokenize(sentence);
	      //posTags are the parts of speech of every word in the sentence (The chunker needs this info of course)
	      String[] posTags = posme.tag(words);
	      //chunks are the start end "spans" indices to the chunks in the words array
	      Span[] chunks = chunkerME.chunkAsSpans(words, posTags);
	      //chunkStrings are the actual chunks
	      String[] chunkStrings = Span.spansToStrings(chunks, words);
	      for (int i = 0; i < chunks.length; i++) {
	        //if (chunks[i].getType().equals("NP")) {
	          String[] split = chunkStrings[i].split(" ");

	          List<String> ngrams = ngram(Arrays.asList(split), N, " ");
	          //System.out.println("ngrams:");
	          for (String gram : ngrams) {
	        	  tweetNgrams.add(gram);
	          }
	      }
	      
	      return tweetNgrams;
	}
	
	public static void extract(String tweet, Integer emotion){
	      
		//this is your sentence
	      String sentence = tweet;
	      sentence = sentence.toLowerCase();
	      
	      if(sentence.contains(" a ")){
	    	  sentence = sentence.replace(" a ", " ");
	      }
	      else if(sentence.contains(" an ")){
	    	  sentence = sentence.replace(" an ", " ");
	      }
	      else if(sentence.contains(" as ")){
	    	  sentence = sentence.replace(" as ", " ");
	      }
	      else if(sentence.contains(" the ")){
	    	  sentence = sentence.replace(" the ", " ");
	      }
	      else if(sentence.contains(" i ")){
	    	  sentence = sentence.replace(" i ", " ");
	      }
	      else if(sentence.contains(" he ")){
	    	  sentence = sentence.replace(" he ", " ");
	      }
	      else if(sentence.contains(" she ")){
	    	  sentence = sentence.replace(" she ", " ");
	      }
	      else if(sentence.contains(" their ")){
	    	  sentence = sentence.replace(" their ", " ");
	      }
	      else if(sentence.contains(" our ")){
	    	  sentence = sentence.replace(" our ", " ");
	      }
	      sentence = sentence.replaceAll("[^a-zA-Z ]", "");
	      
	      //words is the tokenized sentence
	      String[] words = wordBreaker.tokenize(sentence);
	      //posTags are the parts of speech of every word in the sentence (The chunker needs this info of course)
	      String[] posTags = posme.tag(words);
	      //chunks are the start end "spans" indices to the chunks in the words array
	      Span[] chunks = chunkerME.chunkAsSpans(words, posTags);
	      //chunkStrings are the actual chunks
	      String[] chunkStrings = Span.spansToStrings(chunks, words);
	      for (int i = 0; i < chunks.length; i++) {
	        //if (chunks[i].getType().equals("NP")) {
	          String[] split = chunkStrings[i].split(" ");

	          List<String> ngrams = ngram(Arrays.asList(split), N, " ");
	          //System.out.println("ngrams:");
	          for (String gram : ngrams) {
	        	if(emotion == 1){
	        		if(positive.containsKey(gram) && !(neutral.containsKey(gram)) && !(negative.containsKey(gram)) ){
	        			positive.put(gram, positive.get(gram)+1);
	        		}
	        		else
	        			positive.put(gram, 1.0);
	        	}
	        	else if(emotion == 0){
		        		if(neutral.containsKey(gram) && !(positive.containsKey(gram)) && !(negative.containsKey(gram)) ){
		        			neutral.put(gram, neutral.get(gram)+1);
		        		}
		        		else
		        			neutral.put(gram, 1.0);	
		        }
	        	else if(emotion == 2){
		        		if(negative.containsKey(gram) && !(neutral.containsKey(gram)) && !(positive.containsKey(gram)) ){
		        			negative.put(gram, negative.get(gram)+1);
		        		}
		        		else
		        			negative.put(gram, 1.0);			
		        }
	            //System.out.println("\t" + gram);
	          }

	        //}
	      }
	  }
	
	  public static List<String> ngram(List<String> input, int n, String separator) {
		    if (input.size() <= n) {
		      return input;
		    }
		    List<String> outGrams = new ArrayList<String>();
		    for (int i = 0; i < input.size() - (n - 2); i++) {
		      String gram = "";
		      if ((i + n) <= input.size()) {
		        for (int x = i; x < (n + i); x++) {
		          gram += input.get(x) + separator;
		        }
		        gram = gram.substring(0, gram.lastIndexOf(separator));
		        outGrams.add(gram);
		      }
		    }
		    return outGrams;
	  }

}
