/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.qa3web.services;

import edu.ucla.cs.scai.qa3web.dto.KBTaggingResult;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public interface KBTagger {
    
    public KBTaggingResult tag(String question) throws Exception;
    
}
