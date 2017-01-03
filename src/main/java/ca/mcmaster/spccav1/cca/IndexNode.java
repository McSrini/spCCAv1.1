/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.mcmaster.spccav1.cca;

import static ca.mcmaster.spccav1.Main.*;
import ca.mcmaster.spccav1.cplex.datatypes.BranchingInstruction;
import ca.mcmaster.spccav1.cplex.datatypes.NodeAttachment;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author tamvadss
 */
public class IndexNode {
    
    public String nodeID = EMPTY_STRING + -ONE; //default for subtree root 
    public int depthFromSubtreeRoot = ZERO;
    //is this node itself a leaf?
    public    boolean isLeaf = false;
    public    boolean isMigratableLeaf = false;
    
    public boolean isSelectedAsCCA = false;
    
    public List<NodeAttachment> migratableLeafNodesToTheLeft = new ArrayList<NodeAttachment>();
    public List<NodeAttachment> migrateableLeafNodesToTheRight = new ArrayList<NodeAttachment>();

    public List<NodeAttachment> leafNodesToTheLeft = new ArrayList<NodeAttachment>();
    public List<NodeAttachment> leafNodesToTheRight = new ArrayList<NodeAttachment>();
                
    public IndexNode nodeLeft =null, nodeRight=null ;
        
    //branching instructions needed to create this node
    //only populated if this becomes a cca node or is a leaf node
    public List <BranchingInstruction> cumulativeBranchingInstructions = new ArrayList <BranchingInstruction>();
    
    //use this variable if node marked as CCA
    public int numNodeLPsToSolveToArriveAtLeafs = -ONE;
   
 
    public   IndexNode(  List<NodeAttachment> allLeafNodes , int depthFromSubtreeRoot){
        
        
        this. depthFromSubtreeRoot=   depthFromSubtreeRoot;
        
        for (NodeAttachment leafNode : allLeafNodes) {
            //
            
            if (leafNode.cumulativeIsRightChild.size()==depthFromSubtreeRoot){
                //this node is itself a leaf, all the lists can be left at default values
                this.nodeID = leafNode.nodeID;
                this.isLeaf= true;
                isMigratableLeaf=leafNode.isMigrateable;
                this.cumulativeBranchingInstructions.addAll( leafNode.cumulativeBranchingInstructions);
            } else {
                if (!leafNode.cumulativeIsRightChild.get(ZERO+depthFromSubtreeRoot)) {
                    this.leafNodesToTheLeft. add(leafNode  );
                    if (  leafNode.isMigrateable ) this.migratableLeafNodesToTheLeft. add(leafNode  );
                }
                if ( leafNode.cumulativeIsRightChild.get(ZERO+depthFromSubtreeRoot)) {
                    this.leafNodesToTheRight.add(leafNode  );
                    if (  leafNode.isMigrateable ) this.migrateableLeafNodesToTheRight.add(leafNode  );
                }                  
               
            } 
        }
        
        if ( depthFromSubtreeRoot > ZERO && !  isLeaf) this.nodeID=calculateNodeID( allLeafNodes.get(ZERO)) ;
        
    }
    
    //construct the tree index by recursively splitting every subtree  
    public void split ( ) {
        
        //if  either side of subtree root has >= NUM_LEAFS_FOR_MIGRATION_IN_CCA_SUBTREE , split
        //else if one side has 90% and the other side has 10%, then also split
        boolean isSplitNeeded = this.migratableLeafNodesToTheLeft.size() >=NUM_LEAFS_FOR_MIGRATION_IN_CCA_SUBTREE;
        isSplitNeeded = isSplitNeeded || this.migrateableLeafNodesToTheRight.size() >=NUM_LEAFS_FOR_MIGRATION_IN_CCA_SUBTREE;
        
        boolean isSplitDesirable = this.migratableLeafNodesToTheLeft.size() >=NUM_LEAFS_FOR_MIGRATION_IN_CCA_SUBTREE *(ONE-CCA_TOLERANCE_FRACTION);
        isSplitDesirable = isSplitDesirable &&  this.migrateableLeafNodesToTheRight.size() < NUM_LEAFS_FOR_MIGRATION_IN_CCA_SUBTREE * CCA_TOLERANCE_FRACTION;
        
        if (!isSplitDesirable) {
            //check the reverse
            isSplitDesirable =    this.migrateableLeafNodesToTheRight.size() >= NUM_LEAFS_FOR_MIGRATION_IN_CCA_SUBTREE * (ONE-CCA_TOLERANCE_FRACTION);
            isSplitDesirable = isSplitDesirable && this.migratableLeafNodesToTheLeft.size() <NUM_LEAFS_FOR_MIGRATION_IN_CCA_SUBTREE * CCA_TOLERANCE_FRACTION;
        }
        
        isSplitNeeded = isSplitNeeded ||isSplitDesirable;
        
        if (isSplitNeeded) {
            if (this.migratableLeafNodesToTheLeft.size()>ZERO) {
                //create left subtree
                this.nodeLeft = new IndexNode(this.leafNodesToTheLeft, ONE + this.depthFromSubtreeRoot);
                //recursive call
                this.nodeLeft.split();
            }
            if (this.migrateableLeafNodesToTheRight.size()>ZERO) {
                //create right sub tree
                this.nodeRight = new IndexNode(this.leafNodesToTheRight, ONE + this.depthFromSubtreeRoot);
                //recursive call
                this.nodeRight.split();
            }
        } else {
            //if number of migratable leafs under this node >=NUM_LEAFS_FOR_MIGRATION_IN_CCA_SUBTREE, mark as CCA
            //in case NUM_LEAFS_FOR_MIGRATION_IN_CCA_SUBTREE=1 and this is a migratable leaf , then also mark as CCA
            if (this.migrateableLeafNodesToTheRight.size()+ this.migratableLeafNodesToTheLeft.size()>=NUM_LEAFS_FOR_MIGRATION_IN_CCA_SUBTREE*(ONE-CCA_TOLERANCE_FRACTION)) 
                this.isSelectedAsCCA=true;
            if (this.isLeaf && NUM_LEAFS_FOR_MIGRATION_IN_CCA_SUBTREE==ONE && this.isMigratableLeaf) this.isSelectedAsCCA=true;
            
            //if marked CCA, populate cumulative branching instructions needed to create this CCA node
            if (isSelectedAsCCA) {
                //take any leaf, and choose the first depthFromSubtreeRoot branching instructions
                //be careful that this node itself can be a leaf
                if (this.leafNodesToTheLeft.size()>ZERO) {
                    NodeAttachment leafNode  = this.leafNodesToTheLeft.get(ZERO);
                    this.cumulativeBranchingInstructions.addAll(leafNode.cumulativeBranchingInstructions.subList(ZERO, this.depthFromSubtreeRoot));
                } else  if (this.leafNodesToTheRight.size()>ZERO) {
                    NodeAttachment leafNode  = this.leafNodesToTheRight.get(ZERO);
                    this.cumulativeBranchingInstructions.addAll(leafNode.cumulativeBranchingInstructions.subList(ZERO, this.depthFromSubtreeRoot));
                }else {
                    //node is a leaf
                    //cumulative branching instructions would have been already set during node creation
                }
            }
        }
         
    }
    
    //how many leafs which we do not want to migrate
    public int getUnnecessaryLeafCount() {
        return -migratableLeafNodesToTheLeft .size()-migrateableLeafNodesToTheRight .size()+leafNodesToTheLeft.size() + leafNodesToTheRight.size();
    }
    
    public List<IndexNode> getCCATrees () {
        List<IndexNode> retval = new ArrayList<IndexNode> ();
        
        if (  this.isSelectedAsCCA  )         {
            retval.add(this);
        }else {
            if (nodeLeft!=null)  retval.addAll( this.nodeLeft. getCCATrees());
            if (nodeRight!=null) retval.addAll( this.nodeRight.getCCATrees() );            
        }
         
        return retval;
    }
    
    public String toString () {
        String result = EMPTY_STRING+"\n";
        
        result = "\n CCA node is "+this.nodeID+"\n";
        
        result+= " Migratable Leaf nodes to Left:\n(";
        for (NodeAttachment leafNode  : this.migratableLeafNodesToTheLeft  ){
            result+=leafNode.nodeID+" ";
        }
        result+= ")\n";
        
        result+= " Migratable Leaf nodes to Right:\n(";
        for (NodeAttachment leafNode  : this.migrateableLeafNodesToTheRight){
            result+=leafNode.nodeID+" ";
        }
        result+= ")\n";
        
        result+= " All Leaf nodes to Left:\n(";
        for (NodeAttachment leafNode  : this.leafNodesToTheLeft){
            result+=leafNode.nodeID+" ";
        }
        result+= ")\n";
        
        result+= " All Leaf nodes to Right:\n(";
        for (NodeAttachment leafNode  : this.leafNodesToTheRight){
            result+=leafNode.nodeID+" ";
        }
        result+= ")\n";
        
        return result;
    }
    
    //take any leaf , and use list of parent node IDs and depthFromSubtreeRoot to find self node ID
    private String calculateNodeID(NodeAttachment someLeaf){
       
        int size = someLeaf.cumulativeParentNodeIDs.size();
        return someLeaf.cumulativeParentNodeIDs.get( size - ONE - depthFromSubtreeRoot);
    }
    

}

