/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.qa3web.services;

import com.google.gson.Gson;
import edu.ucla.cs.scai.qa3web.dto.KBTaggingResult;
import edu.ucla.cs.scai.qa3web.dto.TaggedChunk;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class ExternalPythonAnnotator implements KBTagger {

    String urlService;
    static HashMap<String, KBTaggingResult> cache = new HashMap<>();

    static synchronized KBTaggingResult getFromCache(String question) {
        return cache.get(question);
    }

    static synchronized void saveToCache(String question, KBTaggingResult tags) {
        cache.put(question, tags);
    }

    public ExternalPythonAnnotator(String url) {
        this.urlService = url;
    }

    public ExternalPythonAnnotator() {
        this.urlService = System.getProperty("taggerUrl", "http://swipe.unica.it/apps/qa3/?q=");
    }

    @Override
    public KBTaggingResult tag(String question) throws Exception {
        KBTaggingResult res = getFromCache(question);
        if (res != null) {
            return res;
        }
        URL url;
        InputStream is = null;
        BufferedReader br;

        try {
            url = new URL(urlService + URLEncoder.encode(question, "UTF-8"));
            is = url.openStream();
            br = new BufferedReader(new InputStreamReader(is));
            Gson gson = new Gson();
            String json = "";
            String line;
            while ((line = br.readLine()) != null) {
                json += line;
            }
            TaggerResult tr = gson.fromJson(json, TaggerResult.class);
            res = new KBTaggingResult();
            res.setDataset(tr.getDataset());
            res.setAnnotatedChunks(new ArrayList<TaggedChunk>());
            String result = tr.getResult();
            for (String t : result.split("\n")) {
                String[] p = t.split("\t");
                if (p.length == 1) {
                    res.getAnnotatedChunks().add(new TaggedChunk(p[0]));
                } else if (p.length == 4) {
                    res.getAnnotatedChunks().add(new TaggedChunk(p[0], p[1], p[2], p[3]));
                }
            }
            saveToCache(question, res);
            return res;
        } catch (MalformedURLException mue) {
            mue.printStackTrace();
            throw mue;
        } catch (IOException ioe) {
            ioe.printStackTrace();
            throw ioe;
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException ioe) {
                throw ioe;
            }
        }
    }

    class TaggerResult {

        String dataset;
        String result;

        public String getDataset() {
            return dataset;
        }

        public void setDataset(String dataset) {
            this.dataset = dataset;
        }

        public String getResult() {
            return result;
        }

        public void setResult(String result) {
            this.result = result;
        }

    }

}
