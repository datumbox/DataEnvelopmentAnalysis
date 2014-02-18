/* 
 * Copyright (C) 2014 Vasilis Vryniotis <bbriniotis at datumbox.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.datumbox.opensource.applications;

import com.datumbox.opensource.algorithms.DataEnvelopmentAnalysis;
import com.datumbox.opensource.dataobjects.DeaRecord;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import lpsolve.LpSolveException;

/**
 * Class which uses DEA to estimate the Social Media Popularity of a page by 
 * using a scale from 0-100 (percentiles).
 * 
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public class SocialMediaPopularity {
    /**
     * The knowledgeBase stores a large number of page statistics such as:
     * facebook likes, Google +1s and tweets.
     * We use those statistics in DEA, compare our evaluated page statistics with
     * the ones of the knowledgeBase and this way we compute its popularity.
     */
    private Map<String, DeaRecord> knowledgeBase; 
    
    /**
     * Constructor without arguments
     */
    public SocialMediaPopularity() {
        this(null);
    }
    
    /**
     * Constructor with knowledgeBase argument
     * 
     * @param knowledgeBase The Map of points which are used as reference while performing DEA.
     */
    public SocialMediaPopularity(Map<String, DeaRecord> knowledgeBase) {
        this.knowledgeBase=knowledgeBase;
    }
    
    /**
     * Gets the knowledgebase parameter
     * 
     * @return  The knowledgeBase map which contains the statistics of our pages.
     */
    public Map<String, DeaRecord> getKnowledgeBase() {
        return knowledgeBase;
    }
    
    /**
     * Reads a list of page statistics from a file and uses them to build the
     * knowledgeBase. 
     * 
     * @param url   The path of the dataset.
     * @return  The number of data records added in the knowledgeBase.
     * @throws IOException 
     */
    public int loadFile(URL url) throws IOException {
        knowledgeBase = new HashMap<>();
        
        int n=0;
        Reader fileReader = new InputStreamReader(url.openStream(), Charset.forName("UTF-8"));
        try (BufferedReader bufferedReader = new BufferedReader(fileReader)) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                //read metrics, store them in a double[] array ad store it in knowledgeBase
                String[] metricsStr=line.trim().split("\t");
                double[] metrics = new double[metricsStr.length];
                for(int i=0;i<metricsStr.length;++i) {
                    metrics[i]=Double.valueOf(metricsStr[i]);
                }
                
                knowledgeBase.put(Integer.toString(n), new DeaRecord(metrics));
                ++n;
            }
        }
        
        return n;
    }
    
    /**
     * Converts the DEA scores in percentiles. The DEA scores are in linear scale. 
     * Thus if in our dataset we have an extremum (a page with thousands of tweets,
     * likes and +1s) then all the other scores of the pages will be really small, 
     * making them hard to understand and interpret. With this function we take 
     * those scores and we estimate their percentiles. Thus when we say that the
     * popularity score of a page is 70% it means that the particular page is more 
     * popular than the 70% of the pages.
     * 
     * @param scores    The Map with DEA scores.
     * @return 
     */
    protected Map<String, Double> estimatePercentiles(Map<String, Double> scores) {
        
        int n = scores.size();
        
        //create a map that holds the score=>id list
        Map<Double, List<String>> score2ids = new TreeMap<>(Collections.reverseOrder()); //TreeMap is required to keep the map sorted by key
        for(Map.Entry<String, Double> entry : scores.entrySet()) {
            String key = entry.getKey();
            Double score = entry.getValue();
            
            List<String> idList = score2ids.get(score);
            if(idList==null) { 
                //initialize the list
                idList=new ArrayList<>();
                score2ids.put(score, idList);
            }
            
            idList.add(key); //add the key in the idList
        }
        
        Map<String, Double> percentiles = new HashMap<>();
        Double rank = 1.0;
        for(List<String> idList : score2ids.values()) {
            Integer ties = idList.size();
            /*
            //Method that uses the average rank of all the ties.
            //This method is good for particular statistical tests, but not very
            //good for interpreting the popularity of pages.
            
            //using arithmetic progression formula (a1+an)*n/2
            Double sumRank = (rank+(rank+ties-1))*ties/2.0;
            Double avgRank = sumRank/ties;
            Double percentile = 100*(n-(avgRank-1))/n;
            */
            
            //Method that uses the same rank (minimum one) in every tie
            Double percentile = 100*(n-(rank-1))/n;
            for(String key : idList) {
                percentiles.put(key, percentile); //add the percentile score in the map
            }
            
            rank += ties; //increase the rank by the number of records that you updated in this iteration
        }
        score2ids=null;
        
        return percentiles;
    }
    
    /**
     * Gets an array of social media statistics and returns the Popularity score
     * (percentile) of the page.
     * 
     * @param socialCounts  An array with all the social media statistics that we use in our analysis.
     * @return              Returns the popularity score (percentile) of the page.
     * @throws LpSolveException 
     */
    protected Double calculatePopularity(double[] socialCounts) throws LpSolveException {
        String newId=String.valueOf(socialCounts.length);
        //important! In this problem, we don't define an input. All the metrics
        //are considered output of the DeaRecord.
        knowledgeBase.put(newId, new DeaRecord(socialCounts)); //add the new point in the database
        
        //Run DEA to evaluate its popularity
        DataEnvelopmentAnalysis dea = new DataEnvelopmentAnalysis();
        Map<String, Double> results = dea.estimateEfficiency(knowledgeBase);
        
        knowledgeBase.remove(newId); //remove point from the list. We could also leave it in and make our DEA learn as we evaluate more points
        
        //Convert DEA score to percintile
        Map<String, Double> percintiles = estimatePercentiles(results);
        Double popularity = percintiles.get(newId); //fetch popularity for the particular record
        results=null;
        percintiles=null;
        
        return popularity;
    }
    
    /**
     * Public method which gets the facebook likes, Google +1s and the number of
     * tweets and evaluates the popularity of the page.
     * 
     * @param facebook  Facebook likes
     * @param plusone   Google +1s
     * @param tweets    Tweets
     * @return          Popularity score from 0-100 (percentile)
     */
    public Double getPopularity(int facebook, int plusone, int tweets) {
        double[] socialCounts = new double[]{(double)facebook, (double)plusone, (double)tweets};
        Double popularity=null;
        try {
            popularity = calculatePopularity(socialCounts);
        } catch (LpSolveException ex) {
            Logger.getLogger(SocialMediaPopularity.class.getName()).log(Level.SEVERE, null, ex);
        }
        //round the score and keep only the 2 last digits
        return Math.round(popularity*100.0)/100.0;
    }
}
