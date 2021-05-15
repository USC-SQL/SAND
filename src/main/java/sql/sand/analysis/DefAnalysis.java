package sql.sand.analysis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import edu.usc.sql.graphs.Node;
import edu.usc.sql.graphs.NodeInterface;
import edu.usc.sql.graphs.cfg.CFGInterface;
import soot.Unit;
import soot.jimple.IdentityStmt;
import soot.tagkit.BytecodeOffsetTag;
import soot.tagkit.Tag;
import sql.sand.abstraction.Codepoint;
import sql.sand.abstraction.Def;
import sql.sand.abstraction.Location;
import sql.sand.abstraction.Silica;

public class DefAnalysis {

	private Set<Def> defSet = new HashSet<>();
	
	public DefAnalysis(Silica silica)
	{
		for(String query : silica.getStringQueries())
		{
			int id = 0;
			Map<Integer,Def> idToDef = new HashMap<>();
			while(query.contains("unknown@"))
			{
				int a1 = query.indexOf("unknown@");
				int a2 = query.indexOf("!!!");
				
				String defString = query.substring(a1,a2);			
				query = query.substring(0, a1) + "$" + id + "$" + query.substring(a2+3, query.length());  
				
				Def def = null;
				String[] info = defString.split("@");
				
				//FORMAT:Unknown@METHOD@<invoking_method_signature>@<containing_method_signature>@source_line_number@bytecode_offset!!!
				if(defString.startsWith("unknown@method"))
				{
					String apiSignature = info[2];
					String containingMethod = info[3];
					int sourceLineNum = Integer.parseInt(info[4]);
					int byteCodeOffset = Integer.parseInt(info[5]);
					def = new Def("METHOD", apiSignature, findDefCodepoint(silica, containingMethod, sourceLineNum, byteCodeOffset));
				}
				//FORMAT:!Unknown@PARA@<method_signature>@parameter_index!!!
				else if(defString.startsWith("unknown@para"))
				{
					def = new Def("PARAMETER", info[2], findParaDefCodepoint(silica, info[2]));
				}
				//FORMAT:Unknown@FIELD@<field_signature>!!!
				else if(defString.startsWith("unknown@field"))
				{
					def = new Def("FIELD", info[2], silica.getCodepoint());
				}
				else if(defString.startsWith("unknown@interpret"))
				{
					//do nothing
				}
				//FORMAT:Unknown@XX@<containing_method_signature@source_line_number@bytecode_offset!!!
				else
				{
					String name = info[2];
					String containingMethod = info[3];
					int sourceLineNum = Integer.parseInt(info[4]);
					int byteCodeOffset = Integer.parseInt(info[5]);
					def = new Def("DYNAMIC_VARIABLE", name, findDefCodepoint(silica, containingMethod, sourceLineNum, byteCodeOffset));
				}
				if(def != null)
				{
					idToDef.put(id, def);
					defSet.add(def);
					id ++;
				}

				
			}
			

		}
	}
	
	public Set<Def> getDefSet()
	{
		return defSet;
	}
	private Codepoint findParaDefCodepoint(Silica silica, String defString)
	{
		int splitIndex = defString.indexOf(":");
		String para = defString.substring(0, splitIndex);
		String sig = defString.substring(defString.indexOf("<"));
		Map<NodeInterface, CFGInterface> callChain = silica.getCallChain();
		NodeInterface targetNode = null;
		Map<NodeInterface, CFGInterface> codePointCallChain = new LinkedHashMap<>();
		for(NodeInterface callSiteNode : callChain.keySet())
		{
			CFGInterface cfg = callChain.get(callSiteNode);
			codePointCallChain.put(callSiteNode, cfg);

			if(cfg.getSignature().toLowerCase().equals(sig))
			{
				for(NodeInterface n : cfg.getAllNodes())
				{
					Unit actualNode = ((Node<Unit>)n).getActualNode();
					if(actualNode != null)
					{
						if(actualNode instanceof IdentityStmt)
						{
							if(actualNode.toString().contains(para))
							{
								targetNode = n;
								break;
							}
						
						}
					}
				}
				break;
			}
		}
		if(targetNode != null && !codePointCallChain.isEmpty())
			return new Codepoint(targetNode, codePointCallChain);
		else
			return silica.getCodepoint();
	}
	private Codepoint findDefCodepoint(Silica silica, String sig, int sourceLineNum, int byteCodeOffset) 
	{
		Map<NodeInterface, CFGInterface> callChain = silica.getCallChain();
		NodeInterface targetNode = null;
		Map<NodeInterface, CFGInterface> codePointCallChain = new LinkedHashMap<>();
		for(NodeInterface callSiteNode : callChain.keySet())
		{
			CFGInterface cfg = callChain.get(callSiteNode);
			codePointCallChain.put(callSiteNode, cfg);

			if(cfg.getSignature().toLowerCase().equals(sig))
			{
		
				for(NodeInterface n : cfg.getAllNodes())
				{
					Unit actualNode = ((Node<Unit>)n).getActualNode();
					if(actualNode != null)
					{
						if(actualNode.getJavaSourceStartLineNumber() == sourceLineNum && 
								getBytecodeOffset(actualNode) == byteCodeOffset)
						{
							targetNode = n;
							break;
						}
					}
				}
				break;
			}
		}
		
		if(targetNode != null && !codePointCallChain.isEmpty())
			return new Codepoint(targetNode, codePointCallChain);
		else
			return silica.getCodepoint();
	}
	
	private int getBytecodeOffset(Unit unit)
	{
		int bytecodeOffset = -1;
		for (Tag t : unit.getTags()) {
			if (t instanceof BytecodeOffsetTag)
				bytecodeOffset = ((BytecodeOffsetTag) t).getBytecodeOffset();
		}
		if(bytecodeOffset == -1)
			bytecodeOffset = unit.hashCode();
		return bytecodeOffset;
	}
}
