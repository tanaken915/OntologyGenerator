package data.original;

import java.net.URI;
import java.net.URISyntaxException;

public enum Namespace {
	EXAMPLE("ex", "http://www.uec.ac.jp/example#"),
	EMPTY("_", ""),
	RDF("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#"),
	RDFS("rdfs", "http://www.w3.org/2000/01/rdf-schema#"),
	OWL("owl", "http://www.w3.org/2002/07/owl#"),
	DC("dc","http://purl.org/dc/elements/1.1/"),
	DCTERMS("dcterms", "http://purl.org/dc/terms/"),
	SCHEMA("schema", "http://schema.org/"),
	
	JASS("jass", "http://www.uec.ac.jp/k-lab/k-tanabe/jass/"),
	
	GOO("goo", "http://dictionary.goo.ne.jp/jn#"),
	LITERAL("", ""),
	;
	
	private final String prefix;
	private URI uri;

	/***********************************/
	/**********  Constructor  **********/
	/***********************************/
	private Namespace(String prefix, String uri) {
		this.prefix = prefix;
		try {
			this.uri = new URI(uri);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	/***********************************/
	/**********  MemberMethod **********/
	/***********************************/
	public static String getURIofPrefix(String prefix) {
		for (Namespace ns : values())
			if (ns.prefix.equals(prefix))
				return ns.uri.toString();
		return null;
	}
	public String toQueryPrefixDefinition() {
		return "PREFIX "+ prefix +": <"+ uri +">";
	}
	
	/**********************************/
	/**********    Getter    **********/
	/**********************************/
	public String getPrefix() {
		return prefix;
	}
	public URI getURI() {
		return uri;
	}
	
	@Override
	public String toString() {
		return prefix;
	}
}