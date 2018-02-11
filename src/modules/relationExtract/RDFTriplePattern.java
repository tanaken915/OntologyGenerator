package modules.relationExtract;

import java.util.Map;
import java.util.StringJoiner;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

public class RDFTriplePattern {

	private String subjectURI;
	private String predicateURI;
	private String objectURI;


	/***********************************/
	/**********  Constructor  **********/
	/***********************************/
	public RDFTriplePattern(String subjectURI, String predicateURI, String objectURI) {
		this.subjectURI = subjectURI;
		this.predicateURI = predicateURI;
		this.objectURI = objectURI;
	}


	/***********************************/
	/**********  MemberMethod **********/
	/***********************************/
	public String[] toArray() {
		return new String[]{subjectURI, predicateURI, objectURI};
	}
	public String join(CharSequence delimiter, CharSequence prefix, CharSequence suffix) {
		return new StringJoiner(delimiter, prefix, suffix)
				.add(subjectURI).add(predicateURI).add(objectURI).toString();
	}
	
	public Statement fillStatement(Model targetModel, Map<String, String> varURIMap) {
		/*
		System.out.println("filling");
		System.out.println("  "+subjectURI +", " + predicateURI + ", " + objectURI);
		//System.out.println("varmap");
		//varURIMap.entrySet().forEach(e -> System.out.println("| "+e.getKey() + "\t : " + e.getValue()));	//TODO
		 */
		Resource subject = targetModel.getResource(varURIMap.get(subjectURI));
		Property predicate = targetModel.getProperty(varURIMap.get(predicateURI));
		RDFNode object = targetModel.getResource(varURIMap.get(objectURI));

		/*
		System.out.println("filled");
		System.out.println("  "+subject.getURI() +", "+ predicate.getURI() +", "+ object.toString());	//TODO
		 */
		
		return targetModel.createStatement(subject, predicate, object);
	}


	/**********************************/
	/********** Objectメソッド **********/
	/**********************************/
	@Override
	public String toString() {
		return join(" ", "", ".");
	}
}