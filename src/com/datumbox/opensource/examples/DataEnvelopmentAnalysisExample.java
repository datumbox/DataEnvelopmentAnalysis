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
package com.datumbox.opensource.examples;

import com.datumbox.opensource.algorithms.DataEnvelopmentAnalysis;
import com.datumbox.opensource.applications.SocialMediaPopularity;
import com.datumbox.opensource.dataobjects.DeaRecord;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import lpsolve.LpSolveException;

/**
 * Examples of using DEA algorithm.
 * 
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public class DataEnvelopmentAnalysisExample {

    /**
     * @param args the command line arguments
     * @throws lpsolve.LpSolveException
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws LpSolveException, IOException {
        //Depots Efficiency example
        depotsEfficiency();
        
        //Page Popularity example
        pageSocialMediaPopularity();
    }
    
    /**
     * Page Social MEdiaPopularity example. Estimates the popularity of a page by using data
     * from Social Media such as Facebook Likes, Google +1s and Tweets. The training
     * data are provided by WebSEOAnalytics.com.
     * 
     * @throws IOException 
     */
    public static void pageSocialMediaPopularity() throws IOException {
        SocialMediaPopularity rank = new SocialMediaPopularity();
        rank.loadFile(DataEnvelopmentAnalysisExample.class.getResource("/datasets/socialcounts.txt"));
        Double popularity = rank.getPopularity(135, 337, 9079); //Facebook likes, Google +1s, Tweets
        System.out.println("Page Social Media Popularity: "+popularity.toString());
    }
    
    /**
     * Depots Efficiency example. Estimates the efficiency of organizational units
     * based on their output (ISSUES, RECEIPTS, REQS) and input (STOCK, WAGES). 
     * This example was taken from http://deazone.com/en/resources/tutorial/introduction
     * 
     * @throws LpSolveException 
     */
    public static void depotsEfficiency() throws LpSolveException {
        Map<String, DeaRecord> records = new LinkedHashMap<>();
        
        records.put("Depot1", new DeaRecord(new double[]{40.0,55.0,30.0}, new double[]{3.0,5.0}));
        records.put("Depot2", new DeaRecord(new double[]{45.0,50.0,40.0}, new double[]{2.5,4.5}));
        records.put("Depot3", new DeaRecord(new double[]{55.0,45.0,30.0}, new double[]{4.0,6.0}));
        records.put("Depot4", new DeaRecord(new double[]{48.0,20.0,60.0}, new double[]{6.0,7.0}));
        records.put("Depot5", new DeaRecord(new double[]{28.0,50.0,25.0}, new double[]{2.3,3.5}));
        records.put("Depot6", new DeaRecord(new double[]{48.0,20.0,65.0}, new double[]{4.0,6.5}));
        records.put("Depot7", new DeaRecord(new double[]{80.0,65.0,57.0}, new double[]{7.0,10.0}));
        records.put("Depot8", new DeaRecord(new double[]{25.0,48.0,30.0}, new double[]{4.4,6.4}));
        records.put("Depot9", new DeaRecord(new double[]{45.0,64.0,42.0}, new double[]{3.0,5.0}));
        records.put("Depot10", new DeaRecord(new double[]{70.0,65.0,48.0}, new double[]{5.0,7.0}));
        records.put("Depot11", new DeaRecord(new double[]{45.0,65.0,40.0}, new double[]{5.0,7.0}));
        records.put("Depot12", new DeaRecord(new double[]{45.0,40.0,44.0}, new double[]{2.0,4.0}));
        records.put("Depot13", new DeaRecord(new double[]{65.0,25.0,35.0}, new double[]{5.0,7.0}));
        records.put("Depot14", new DeaRecord(new double[]{38.0,18.0,64.0}, new double[]{4.0,4.0}));
        records.put("Depot15", new DeaRecord(new double[]{20.0,50.0,15.0}, new double[]{2.0,3.0}));
        records.put("Depot16", new DeaRecord(new double[]{38.0,20.0,60.0}, new double[]{3.0,6.0}));
        records.put("Depot17", new DeaRecord(new double[]{68.0,64.0,54.0}, new double[]{7.0,11.0}));
        records.put("Depot18", new DeaRecord(new double[]{25.0,38.0,20.0}, new double[]{4.0,6.0}));
        records.put("Depot19", new DeaRecord(new double[]{45.0,67.0,32.0}, new double[]{3.0,4.0}));
        records.put("Depot20", new DeaRecord(new double[]{57.0,60.0,40.0}, new double[]{5.0,6.0}));
        
        DataEnvelopmentAnalysis dea = new DataEnvelopmentAnalysis();
        Map<String, Double> results = dea.estimateEfficiency(records);
        System.out.println((new TreeMap<>(results)).toString());
    }
}
