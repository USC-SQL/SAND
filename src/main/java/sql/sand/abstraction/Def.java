package sql.sand.abstraction;

public class Def extends BaseAbstraction{

	//type can be METHOD, FIELD, or PARAMETER
	private String type;
	private String name;

	public Def(String type, String name, Codepoint codepoint)
	{
		super(codepoint);
		this.type = type;
		this.name = name;
	}
	
	public String getType()
	{
		return type;
	}
	
	public String getName()
	{
		return name;
	}
	
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append(type);
		sb.append("@");
		if(type.equals("METHOD"))
		{
			sb.append("METHOD:");
			sb.append(name);
			sb.append("@");
			sb.append("Location");
			sb.append(getLocation().toString());
			return sb.toString();
		}
		else if(type.equals("FIELD"))
		{
			sb.append("FIELD:");
			sb.append(name);
			return sb.toString();
		}
		else if(type.equals("PARAMETER"))
		{
			sb.append("PARAMETER:");
			sb.append(name);
			return sb.toString();
		}
		else
		{
			sb.append("OTHER:");
			sb.append(name);
			sb.append("@");
			sb.append("Location");
			sb.append(getLocation().toString());
			return sb.toString();
		}
	}
}
