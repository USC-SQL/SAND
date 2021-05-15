package sql.sand.analysis.helper;

public class Pair<A, B> {
    private A first;
    private B second;

    public Pair(A first, B second) {
    	super();
    	this.first = first;
    	this.second = second;
    }

    public int hashCode() {
    	int hashFirst = first != null ? first.hashCode() : 0;
    	int hashSecond = second != null ? second.hashCode() : 0;

    	return (hashFirst + hashSecond) * hashSecond + hashFirst;
    }

    public boolean equals(Object other) {
    	return this.hashCode() == other.hashCode();
    	/*
    	if (other instanceof Pair) {
    		Pair otherPair = (Pair) other;
    		boolean isEqual =
    		
    		(
    		 (  
    			( this.first != null && otherPair.first != null &&
    			  this.first.equals(otherPair.first))) &&
    		 (	
    			( this.second != null && otherPair.second != null &&
    			  this.second.equals(otherPair.second))) 
    		);
    		System.out.println(this.first.hashCode() + " " + otherPair.first.hashCode() + " " + isEqual);
    		return isEqual;
    	}
    	return false;
    	*/
    }
    public String toString()
    { 
           return "(" + first + ", " + second + ")"; 
    }

    public A getFirst() {
    	return first;
    }

    public void setFirst(A first) {
    	this.first = first;
    }
    
    public B getSecond() {
    	return second;
    }

    public void setSecond(B second) {
    	this.second = second;
    }
}