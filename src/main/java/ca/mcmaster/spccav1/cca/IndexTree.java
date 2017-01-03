/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.mcmaster.spccav1.cca;

import static ca.mcmaster.spccav1.Main.*;
import static ca.mcmaster.spccav1.Main.ZERO;
import ca.mcmaster.spccav1.cplex.datatypes.NodeAttachment;
import java.util.*;

/**
 *
 * @author tamvadss
 */
public class IndexTree {
    
    private IndexNode subtreeRoot ;
    
    private List<NodeAttachment> allLeafNodes;
    
    public IndexTree (List<NodeAttachment> allLeafNodes) {
        this.allLeafNodes= allLeafNodes;
        subtreeRoot = new IndexNode(   allLeafNodes, ZERO);
    }
    
    //kick off recursive split 
    public void split ( ) {
        subtreeRoot.split();
    }
    
    //return the CCA subtrees found  
    public List<IndexNode> getCCANodes () {
        List<IndexNode> ccaList =  this.subtreeRoot.getCCATrees();
        for (IndexNode node: ccaList) {
            setNumberOfNodeLPsRequiredToConstructAllLeafs(node);
        }
        return ccaList;
    }
    
    
    //this number , when multiplied by average node LP solve time, should be much less than expected solution time of CCA node
    private void setNumberOfNodeLPsRequiredToConstructAllLeafs(IndexNode ccaNode){
        //move up from every leaf towards CCA node, and count the number of 
        //unique node IDs encountered along the way, including the CCA node itself
        //
        //Be careful of the case when the CCA node is itself a leaf, in which case no need to solve any node LPs
        
        ccaNode.numNodeLPsToSolveToArriveAtLeafs = ZERO ;
        
        if (ccaNode.leafNodesToTheLeft.size() + ccaNode.leafNodesToTheRight.size() > ZERO){
            
            Map<String, Integer> uniqueNodeMap = new HashMap<String, Integer>();
            
            for (NodeAttachment node : ccaNode.leafNodesToTheLeft) {
                for (String parentNodeId : node.cumulativeParentNodeIDs){
                    uniqueNodeMap.put(parentNodeId, ONE);
                }
            }
            for (NodeAttachment node : ccaNode.leafNodesToTheRight) {
                for (String parentNodeId : node.cumulativeParentNodeIDs){
                    uniqueNodeMap.put(parentNodeId, ONE);
                }
            }
             
            ccaNode.numNodeLPsToSolveToArriveAtLeafs =uniqueNodeMap.keySet().size();
        } 
        
    }
    
  
}
