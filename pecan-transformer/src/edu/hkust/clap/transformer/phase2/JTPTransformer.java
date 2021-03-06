package edu.hkust.clap.transformer.phase2;

import java.util.Iterator;
import java.util.Map;

import edu.hkust.clap.Parameters;
import edu.hkust.clap.Util;
import edu.hkust.clap.transformer.*;
import soot.Body;
import soot.BodyTransformer;
import soot.Modifier;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.Jimple;
import soot.jimple.Stmt;
import soot.util.Chain;

public class JTPTransformer extends BodyTransformer {
	private Visitor visitor;
	public JTPTransformer()
	{
        RecursiveVisitor2 vv = new RecursiveVisitor2(null);
        CLAPVisitor2 pv = new CLAPVisitor2(vv);
        vv.setNextVisitor(pv);
        visitor = pv;
	}
	protected void internalTransform(Body body, String pn, Map map) {
		
		Util.resetParameters();
		SootMethod thisMethod = body.getMethod();
		
//		if(thisMethod.getDeclaringClass().getName().equals("example.URLParse")
//				&& thisMethod.getName().equals("getVal"))
//		{
//			System.out.println(thisMethod.getActiveBody());
//		}
		
//		if(!Util.shouldInstruThisMethod(thisMethod.getName()))
//			return;
		String smname = thisMethod.getName();
		if (smname.contains("<clinit>"))
			
			{
			return;
			}
		
	
		if(smname.contains("<init>"))
		{
//			if(thisMethod.getDeclaringClass().getName().contains("$HashIterator"))
//				System.out.print(true);
			
			Parameters.isInsideConstructor = false;//true;
		}
		else
		{
			Parameters.isInsideConstructor = false;
		}
		
		SootClass thisClass = thisMethod.getDeclaringClass();
		String scname = thisClass.getName();
		//System.out.println("scname: "+scname);
		if(!Util.shouldInstruThisClass(scname)) 
			return;
		

		
		if(thisMethod.toString().contains("void main(java.lang.String[])"))
		{
			Parameters.isMethodMain = true;
		}
		else if(thisMethod.toString().contains("void run()")&&Util.isRunnableSubType(thisClass))
		{
			Parameters.isMethodRunnable = true;
		}
		if(thisMethod.isSynchronized())
		{
			Parameters.isMethodSynchronized = true;
		}
		
		if(thisMethod.isPrivate())
		{
			Parameters.isMethodNonPrivate = false;
		}
				
		Chain units = body.getUnits();
		
	//	System.err.println(body);
		//NO IDEA WHY THIS
		//To enable insert tid
		if(thisMethod.isStatic()&&thisMethod.getParameterCount()==0)
		{
			Parameters.isMethodStaticNonPara = true;
			Stmt nop=Jimple.v().newNopStmt();
			//insert the nop just before the return stmt
			units.insertBefore(nop, units.getFirst());
		}
		
        Iterator stmtIt = units.snapshotIterator();    	       
        while (stmtIt.hasNext()) 
        {
            Stmt s = (Stmt) stmtIt.next();
            visitor.visitStmt(thisMethod, units, s);
        }
        
    		
		if(Parameters.isMethodMain)
			Visitor.addCallMainMethodEnterInsert(thisMethod, units);
		
		if(Parameters.isMethodRunnable)
			Visitor.addCallRunMethodEnterInsert(thisMethod, units);

    	if(Parameters.isMethodSynchronized)
    	{
        	if(Parameters.isReplay)
        		thisMethod.setModifiers(thisMethod.getModifiers()&~Modifier.SYNCHRONIZED);
    		
        	Visitor.addCallMonitorEntry(body);
    	}
    	
    	if(Parameters.isMethodNonPrivate)
    	{
    		Visitor.addCallMethodEntry(thisMethod,units,false);
    	}
    	else
    	{
    		Visitor.addCallMethodEntry(thisMethod,units,true);
    	}
    	
//    	System.err.println("====after-============:");
//    	System.err.println(body);
//    	
//    	System.err.println("\n\n\n");
    	
//    	if(Parameters.isMethodNonPrivate)
//    	{
//    		Visitor.addCallMethodEntry(thisMethod,units,false);
//    	}
//    	else
//    	{
//    		Visitor.addCallMethodEntry(thisMethod,units,true);
//    	}
    	
    //	body.validate();
	}

}
