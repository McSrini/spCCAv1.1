/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.mcmaster.spccav1.cplex.datatypes;

import ca.mcmaster.spccav1.cplex.datatypes.BranchingInstruction;
import static ca.mcmaster.spccav1.Main.*;
import java.util.*;

/**
 *
 * @author tamvadss
 */
public class NodeAttachment {
    
    public String nodeID = EMPTY_STRING + -ONE; //default for subtree root 
    public int depthFromSubtreeRoot = ZERO;
    
    //random for now, this will be determined by node metrics
    public boolean isMigrateable = true;
    
    public List <BranchingInstruction> cumulativeBranchingInstructions = new ArrayList <BranchingInstruction>();
    
    //list like LLLRRLRLRRLRLR=00011010110101, 0   indicates left child
    public List<Boolean> cumulativeIsRightChild = new ArrayList<Boolean>();
    
    public List<String> cumulativeParentNodeIDs = new ArrayList<String> ();
                        
    //private static Random rand = new Random(ONE) ;    
 
    public String toString(){
        String result = EMPTY_STRING;
        result += "NodeID "+ nodeID;
        result += isMigrateable? "Mig":"Un";
        result += "\n";
        return result;
    }
  
    
}
