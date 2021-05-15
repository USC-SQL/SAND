package sql.sand.abstraction;

public class Transaction extends BaseAbstraction{

	
	public Transaction(Location location)
	{
		super(location);
	}

	public String toString() {
		return getLocation().toString();
	}
}
