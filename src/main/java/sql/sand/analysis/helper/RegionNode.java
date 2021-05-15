package sql.sand.analysis.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.usc.sql.graphs.EdgeInterface;
import edu.usc.sql.graphs.NodeInterface;
import edu.usc.sql.graphs.cfg.CFGInterface;

public class RegionNode {

	private List<NodeInterface> nodeList = new ArrayList<>();
	private RegionNode parent;
	private List<RegionNode> children = new ArrayList<>();
	private EdgeInterface backedge;
	private int regionNumber;
	private CFGInterface cfg;
	private NodeInterface beginNode;
	private NodeInterface beginTryNode;
	private NodeInterface insertedBeginTryNode;
	private Set<NodeInterface> endNode;
	private String toDot;
	public RegionNode(int i, EdgeInterface b,CFGInterface cfg) {
		backedge = b;
		regionNumber = i;
		this.cfg = cfg;
		toDot = cfg.toDot();
		

		
	}

	public CFGInterface getCFG()
	{
		return cfg;
	}
	public String getCFGToDot()
	{
		return toDot;
	}

	public int getRegionNumber() {
		return regionNumber;
	}

	public EdgeInterface getBackEdge() {
		return backedge;
	}

	public void addToNodeList(NodeInterface n) {
		nodeList.add(n);
	}
	public void setBeginNode(NodeInterface n)
	{
		beginNode = n;
	}
	public void setEndNode(Set<NodeInterface> n)
	{
		endNode = n;
	}
	
	public void addToChildrenList(RegionNode n) {
		children.add(n);
	}

	public List<NodeInterface> getNodeList() {
		return nodeList;
	}

	public NodeInterface getBeginNode()
	{

		return beginNode;
	}
	public Set<NodeInterface> getEndNode()
	{

		return endNode;
	}
	
	public String getSignature()
	{
		return cfg.getSignature()+regionNumber;
	}
	public RegionNode getParent() {
		return parent;
	}

	public void setParent(RegionNode parent) {
		this.parent = parent;
	}

	public List<RegionNode> getChildren() {
		return children;
	}

	public void setChildren(List<RegionNode> children) {
		this.children = children;
	}

	public boolean contain(RegionNode rn) {
		if (nodeList.containsAll(rn.getNodeList()))
			return true;
		else
			return false;
	}

	public NodeInterface getBeginTryNode() {
		return beginTryNode;
	}

	public void setBeginTryNode(NodeInterface beginTryNode) {
		this.beginTryNode = beginTryNode;
	}

	public NodeInterface getInsertedBeginTryNode() {
		return insertedBeginTryNode;
	}

	public void setInsertedBeginTryNode(NodeInterface insertedBeginTryNode) {
		this.insertedBeginTryNode = insertedBeginTryNode;
	}
}
