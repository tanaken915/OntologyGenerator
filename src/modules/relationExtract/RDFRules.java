package modules.relationExtract;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;

public class RDFRules {

	Set<RDFRule> rules;
	
	/***********************************/
	/**********  Constructor  **********/
	/***********************************/
	public RDFRules(Collection<? extends RDFRule> c) {
		this.rules = new HashSet<>(c);
	}
	
	
	/***********************************/
	/**********  MemberMethod **********/
	/***********************************/
	public List<Statement> solve(Model targetModel) {
		return rules.stream().flatMap(r -> r.solve(targetModel).stream()).collect(Collectors.toList());
	}
}