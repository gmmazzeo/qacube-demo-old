/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.qa3web.dto;

import java.util.ArrayList;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class KBTaggingResult {

    String dataset;

    ArrayList<TaggedChunk> annotatedChunks;

    public String getDataset() {
        return dataset;
    }

    public void setDataset(String dataset) {
        this.dataset = dataset;
    }

    public ArrayList<TaggedChunk> getAnnotatedChunks() {
        return annotatedChunks;
    }

    public void setAnnotatedChunks(ArrayList<TaggedChunk> annotatedChunks) {
        this.annotatedChunks = annotatedChunks;
    }

}
