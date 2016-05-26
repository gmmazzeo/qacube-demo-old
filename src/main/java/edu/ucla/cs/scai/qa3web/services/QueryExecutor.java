/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.qa3web.services;

import edu.ucla.cs.scai.linkedspending.qald.JsonAnswer;
import edu.ucla.cs.scai.linkedspending.qald.JsonResults;
import edu.ucla.cs.scai.linkedspending.qald.JsonValue;
import edu.ucla.cs.scai.linkedspending.qald.SparqlEndpoint;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class QueryExecutor {

    static HashMap<String, ArrayList<String>> cache = new HashMap<>();

    static synchronized ArrayList<String> getFromCache(String question) {
        return cache.get(question);
    }

    static synchronized void saveToCache(String question, ArrayList<String> results) {
        cache.put(question, results);
    }

    public ArrayList<String> executeQuery(String question) throws Exception {
        ArrayList<String> res = getFromCache(question);
        if (res != null) {
            return res;
        }
        String sparql = TemplateFinderAndFiller.getSparqlFromCache(question);
        try {
            //fill the results
            JsonAnswer[] anss = new SparqlEndpoint("http://cubeqa.aksw.org/sparql").executeQuery(sparql);
            res = new ArrayList<>();
            for (JsonAnswer ans : anss) {
                if (ans.getBooleanResult() != null) {
                    res.add(ans.getBooleanResult().toString());
                } else {
                    for (HashMap<String, JsonValue> map : ans.getResults().getBindings()) {
                        StringBuilder sb = new StringBuilder();
                        for (JsonValue jv : map.values()) {
                            if (sb.length() > 0) { //this should never happen, since the query selects always one only column
                                sb.append("\t");
                            }
                            if (jv.getType().equals(JsonValue.URI)) {
                                sb.append("<").append(jv.getValue()).append(">");
                            } else if (jv.getType().equals(JsonValue.TYPED_LITERAL)) {
                                sb.append("\"").append(jv.getValue()).append("\"");
                                sb.append("^^<").append(jv.getDatatype()).append(">");
                            } else if (jv.getType().equals(JsonValue.LITERAL)) {
                                sb.append(jv.getValue());
                            }
                        }
                        res.add(sb.toString());
                    }
                }
            }
            saveToCache(question, res);
            return res;
        } catch (Exception ex) {
            Logger.getLogger(QueryExecutor.class.getName()).log(Level.SEVERE, null, ex);
            throw new Exception("Error while running the query");
        }
    }

}
