/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.qa3web.services;

import edu.ucla.cs.scai.linkedspending.AnnotatedText;
import edu.ucla.cs.scai.linkedspending.QA;
import edu.ucla.cs.scai.linkedspending.Triple;
import edu.ucla.cs.scai.linkedspending.template.AnnotatedTokens;
import edu.ucla.cs.scai.linkedspending.template.FlatPattern;
import edu.ucla.cs.scai.qa3web.dto.KBTaggingResult;
import edu.ucla.cs.scai.qa3web.dto.TaggedChunk;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class TemplateFinderAndFiller {

    static HashMap<String, HashMap<FlatPattern, AnnotatedTokens>> patternsCache = new HashMap<>();
    static HashMap<String, String> sparqlCache = new HashMap<>();
    static HashMap<String, HashMap<AnnotatedText, ArrayList<Triple>>> taggingCache = new HashMap<>();

    private static QA qa;

    static {
        try {
            qa = new QA(System.getProperty("lsDatasetPath", "/home/massimo/Downloads/benchmarkdatasets"), null);
        } catch (Exception ex) {
            Logger.getLogger(TemplateFinderAndFiller.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    static synchronized HashMap<FlatPattern, AnnotatedTokens> getPatternsFromCache(String question) {
        return patternsCache.get(question);
    }

    static synchronized void savePatternsToCache(String question, HashMap<FlatPattern, AnnotatedTokens> patterns) {
        patternsCache.put(question, patterns);
    }
    
    static synchronized HashMap<AnnotatedText, ArrayList<Triple>> getTaggingFromCache(String question) {
        return taggingCache.get(question);
    }

    static synchronized void saveTaggingToCache(String question, HashMap<AnnotatedText, ArrayList<Triple>> tagging) {
        taggingCache.put(question, tagging);
    }
    

    static synchronized String getSparqlFromCache(String question) {
        return sparqlCache.get(question);
    }

    static synchronized void saveSparqlToCache(String question, String sparql) {
        sparqlCache.put(question, sparql);
    }

    public FlatPattern findTemplate(String question) throws Exception {
        HashMap<FlatPattern, AnnotatedTokens> patterns = getPatternsFromCache(question);
        if (patterns != null) {
            if (patterns.isEmpty()) {
                return null;
            }
            return patterns.keySet().iterator().next();
        }
        KBTaggingResult tags = ExternalPythonAnnotator.getFromCache(question);
        String dataset = tags.getDataset();
        HashMap<AnnotatedText, ArrayList<Triple>> tagging = new HashMap<>();

        for (TaggedChunk tc : tags.getAnnotatedChunks()) {
            String key = tc.getText();
            int begin = question.indexOf(key);
            int end = begin + key.length() - 1;
            ArrayList<Triple> triples = new ArrayList<>();
            String k = key.toLowerCase().trim();
            if (k.equals("years") || k.equals("year")) {
                triples.add(new Triple("http://linkedspending.aksw.org/ontology/refYear", "http://www.w3.org/2000/01/rdf-schema#label", "year"));
            } else if (k.equals("date") || k.equals("dates") || k.equals("day") || k.equals("days")) {
                triples.add(new Triple("http://linkedspending.aksw.org/ontology/refDate", "http://www.w3.org/2000/01/rdf-schema#label", "date"));
            } else if (tc.getSubject() == null) {
                //try to find a missing year/date annotation - very rough implementaion
                if (k.contains("years")) {
                    begin = key.indexOf("years");
                    end = begin + 4;
                    key = "years";
                    triples.add(new Triple("http://linkedspending.aksw.org/ontology/refYear", "http://www.w3.org/2000/01/rdf-schema#label", "year"));
                } else if (k.contains("year")) {
                    begin = key.indexOf("year");
                    end = begin + 3;
                    key = "year";
                    triples.add(new Triple("http://linkedspending.aksw.org/ontology/refYear", "http://www.w3.org/2000/01/rdf-schema#label", "year"));
                } else if (k.contains("dates")) {
                    begin = key.indexOf("dates");
                    end = begin + 4;
                    key = "dates";
                    triples.add(new Triple("http://linkedspending.aksw.org/ontology/refDate", "http://www.w3.org/2000/01/rdf-schema#label", "date"));
                } else if (k.contains("date")) {
                    begin = key.indexOf("date");
                    end = begin + 3;
                    key = "date";
                    triples.add(new Triple("http://linkedspending.aksw.org/ontology/refDate", "http://www.w3.org/2000/01/rdf-schema#label", "date"));
                } else if (k.contains("days")) {
                    begin = key.indexOf("days");
                    end = begin + 3;
                    key = "days";
                    triples.add(new Triple("http://linkedspending.aksw.org/ontology/refDate", "http://www.w3.org/2000/01/rdf-schema#label", "date"));
                } else if (k.contains("day")) {
                    begin = key.indexOf("day");
                    end = begin + 2;
                    key = "day";
                    triples.add(new Triple("http://linkedspending.aksw.org/ontology/refDate", "http://www.w3.org/2000/01/rdf-schema#label", "date"));
                }
            } else {
                triples.add(new Triple(tc.getSubject(), tc.getProperty(), tc.getValue()));
            }
            if (!triples.isEmpty()) {
                tagging.put(new AnnotatedText(key, begin, end), triples);
            }
        }
        saveTaggingToCache(question, tagging);
        //qa.printTags(question, dataset, tagging);
        try {
            patterns = qa.findTemplate(question, dataset, tagging);
            savePatternsToCache(question, patterns);
            if (patterns.isEmpty()) {
                return null;
            }
            return patterns.keySet().iterator().next();

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public String fillTemplate(String question) throws Exception {
        String res = getSparqlFromCache(question);
        if (res != null) {
            return res;
        }
        //fill the template
        KBTaggingResult tags = ExternalPythonAnnotator.getFromCache(question);
        String dataset = tags.getDataset();
        res = TemplateFinderAndFiller.qa.translateToSparql(question, dataset, getTaggingFromCache(question), getPatternsFromCache(question));
        saveSparqlToCache(question, res);
        return res;
    }

}
