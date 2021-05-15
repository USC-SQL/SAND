package sql.sand.test;

import sql.sand.main.Main;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class MainTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public MainTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( MainTest.class );
    }

    /**
     * Rigourous Test :-)
     */ 
    public void testApp()
    {
    	String[] args = {"example/android.jar","example/com.a1.quiz.asvab.free.apk",
    			"UnbatchedWrites:NotMergingProjectionPredicates:NotMergingSelectionPredicates:TaintedQuery:NotUsingParameterizedQueries:LoopToJoin:NotCaching:ReadablePassword:UnnecessaryColumnRetrieval:UnnecessaryRowRetrieval:UnboundedQuery:IgnoreReturn",
    			"result.txt"};
        //Main.main(args);
    }
    
}
