package grammar.paragraph;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;

import data.RDF.vocabulary.JASS;
import grammar.SyntacticChild;
import grammar.SyntacticParent;
import grammar.sentence.Sentence;

public class Paragraph extends SyntacticParent<Sentence> implements SyntacticChild {
	private static int SUM = 0;

	private final int id;

	/* ================================================== */
	/* ==========          Constructor         ========== */
	/* ================================================== */
	public Paragraph(List<Sentence> sentences) {
		super(sentences);
		this.id = SUM++;
	}

	/* ================================================== */
	/* ==========        Member  Method        ========== */
	/* ================================================== */

	/* ================================================== */
	/* ==========       Interface Method       ========== */
	/* ================================================== */
	@Override
	public int id() {return id;}
	@Override
	public String name() {
		return getChildren().stream().map(s -> s.name()).collect(Collectors.joining());
	}
	@Override
	public String getJassURI() {
		return JASS.uri+getClass().getSimpleName()+id();
	}
	@Override
	public Resource toJASS(Model model) {
		Resource sentenceList = 
				model.createList(getChildren().stream().map(m -> m.toJASS(model)).iterator())
				.addProperty(RDF.type, JASS.SentenceList);

		Resource paragraphResource = model.createResource(getJassURI())
				.addProperty(RDF.type, JASS.Paragraph)
				.addProperty(JASS.sentences, sentenceList);
		return paragraphResource;
	}

	/* ================================================== */
	/* ==========        Object  Method        ========== */
	/* ================================================== */
	@Override
	public String toString() {
		return children.stream().map(s -> s.toString()).collect(Collectors.joining("\n"));
	}

}