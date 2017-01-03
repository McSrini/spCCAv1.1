/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.mcmaster.spccav1.cplex.handlers;

import static ca.mcmaster.spccav1.Main.*;
import ca.mcmaster.spccav1.cplex.datatypes.BranchingInstruction;
import ca.mcmaster.spccav1.cplex.datatypes.NodeAttachment;
import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.BranchCallback;
import ilog.cplex.IloCplex.BranchDirection;
import static java.lang.System.exit;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;

/**
 *
 * @author tamvadss
 */
public class BranchHandler extends BranchCallback {
    
    private static Logger logger=Logger.getLogger(BranchHandler.class);
    
    public List<String> pruneList = new ArrayList<String>();
    
    public double bestObjValue = BILLION ;
    public double cutoff = BILLION ;
    
    public int numBranches = ZERO;
    
    static {
        logger.setLevel(Level.DEBUG);
        PatternLayout layout = new PatternLayout("%5p  %d  %F  %L  %m%n");     
        try {
            logger.addAppender(new  RollingFileAppender(layout,LOG_FOLDER+BranchHandler.class.getSimpleName()+ LOG_FILE_EXTENSION));
            logger.setAdditivity(false);
        } catch (Exception ex) {
            exit(1);
        }
          
    }

    @Override
    protected void main() throws IloException {
        
        if ( getNbranches()> 0 ){  
            
            numBranches +=getNbranches();
                        
            //get the node attachment for this node, any child nodes will accumulate the branching conditions
            NodeAttachment nodeData = (NodeAttachment) getNodeData();
            if (nodeData==null ) { //it will be null for subtree root
               
                nodeData=new NodeAttachment (      );  
                setNodeData(nodeData);                
                
            } 
            
            if (pruneList.contains(nodeData.nodeID) ) {
                //
                prune();
            } else if (IS_MAXIMIZATION && getObjValue() < cutoff){
                prune();
            }else if (!IS_MAXIMIZATION && getObjValue() > cutoff){
                prune();
            }else {
                
                //get the branches about to be created
                IloNumVar[][] vars = new IloNumVar[2][] ;
                double[ ][] bounds = new double[2 ][];
                BranchDirection[ ][]  dirs = new  IloCplex.BranchDirection[ 2][];
                getBranches(  vars, bounds, dirs);

                //now allow  both kids to spawn
                for (int childNum = ZERO ;childNum<getNbranches();  childNum++) {    
                    //apply the bound changes specific to this child
                    NodeAttachment thisChild  =  createChildNode( nodeData,
                            dirs[childNum], bounds[childNum], vars[childNum]  , childNum ); 

                    IloCplex.NodeId nodeid = makeBranch(childNum,thisChild );
                    thisChild.nodeID =nodeid.toString();
                    
                    logger.debug(" Node "+nodeData.nodeID + " created child "+  thisChild.nodeID + " varname " +
                           vars[childNum][0].getName() + " bound " + bounds[childNum][0] +   (dirs[childNum][0].equals( BranchDirection.Down) ? " U":" L") ) ;

                }//end for 2 kids
                
            }
            
            bestObjValue = getBestObjValue();
              
        }
          
    }
    
    private NodeAttachment createChildNode (NodeAttachment parentNodeData, BranchDirection[ ]  dirs,double[ ] bounds, IloNumVar[] vars ,int childNum ){
        NodeAttachment thisChild  = new NodeAttachment (); 
        
        //accumulate cumulativeBranchingInstructions  , cumulativeBranchingDirections  , cumulativeParentNodeIDs
        BranchingInstruction bi = new  BranchingInstruction(  getVarnames (  vars),   getVarDirs(dirs),   bounds);
        thisChild.cumulativeBranchingInstructions.addAll(parentNodeData.cumulativeBranchingInstructions);
        thisChild.cumulativeBranchingInstructions.add(bi);
        
        thisChild.cumulativeIsRightChild.addAll(parentNodeData.cumulativeIsRightChild );
        thisChild.cumulativeIsRightChild.add( childNum == ONE);
        
        thisChild.cumulativeParentNodeIDs.addAll(parentNodeData.cumulativeParentNodeIDs);
        thisChild.cumulativeParentNodeIDs.add( ZERO, parentNodeData.nodeID );
        
        thisChild.depthFromSubtreeRoot=parentNodeData.depthFromSubtreeRoot + ONE;
                
        return     thisChild;    
    }
    
    private String[] getVarnames (IloNumVar[] vars) {
        String[] varnames = new  String[vars.length];
        
        int index = ZERO;
        for (IloNumVar var : vars) {
            varnames[index ++] = var.getName();
        }
        return varnames;
    }
    
    private Boolean[] getVarDirs (BranchDirection[ ]  dirs) {
        Boolean[] vardirs = new  Boolean[dirs.length];
        
        int index = ZERO;
        for (BranchDirection dir : dirs) {
            vardirs[index ++] = dir.equals( BranchDirection.Down);
        }
        return vardirs;
    }
    
}
