package sql.sand.analysis.helper;

import java.util.Set;

public class Parameter {

	String methodSig;
	int index;
	Set<String> values;
	
	public Parameter(String methodSig, int index, Set<String> values) {
		this.methodSig = methodSig;
		this.index = index;
		this.values = values;
	}
}
