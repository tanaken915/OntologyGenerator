package grammar.word;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;

import data.RDF.vocabulary.JASS;
import grammar.Constituent;
import grammar.SyntacticChild;
import grammar.SyntacticParent;
import grammar.morpheme.Morpheme;
import grammar.morpheme.MorphemeFactory;
import language.pos.CabochaPoSInterface;
import language.pos.CabochaTags;
import language.pos.Concatable;

public class Word extends SyntacticParent<Morpheme>
		implements SyntacticChild, CabochaPoSInterface, 
		Concatable<Word>, Constituent {
	private static int SUM = 0;

	private final int id;

	/* ================================================== */
	/* ================== Constructor =================== */
	/* ================================================== */
	public Word(List<Morpheme> morphemes) {
		super(morphemes);
		this.id = SUM++;
	}
	public Word(Morpheme... morphemes) {
		this(Arrays.asList(morphemes));
	}
	public Word(String name, CabochaTags tags) {
		this(MorphemeFactory.getInstance().getMorpheme(name, tags));
	}
	private Word(Word word) {
		this(new ArrayList<>(word.children));
	}

	/* ================================================== */
	/* ================== Static Method ================= */
	/* ================================================== */
	
	/* ================================================== */
	/* ================== Member Method ================= */
	/* ================================================== */
	public Morpheme coreMorpheme() {
		ListIterator<Morpheme> li = children.listIterator(children.size());
		while (li.hasPrevious())
			if (!li.previous().contains("接尾"))
				return children.get(li.previousIndex()+1);
		return tail();
	}

	/* ================================================== */
	/* ================ Interface Method ================ */ 
	/* ================================================== */
	@Override
	public int id() { return id; }
	@Override
	public String name() { return children.stream().map(Morpheme::name).collect(Collectors.joining()); }
	@Override
	public String mainPoS() { return coreMorpheme().mainPoS(); }
	@Override
	public String subPoS1() { return coreMorpheme().subPoS1(); }
	@Override
	public String subPoS2() { return coreMorpheme().subPoS2(); }
	@Override
	public String subPoS3() { return coreMorpheme().subPoS3(); }
	@Override
	public String inflection() { return coreMorpheme().inflection(); }
	@Override
	public String conjugation() { return coreMorpheme().conjugation(); }
	@Override
	public String infinitive() { return children.stream().map(m -> m.infinitive()).collect(Collectors.joining()); }
	@Override
	public String yomi() { return children.stream().map(m -> m.yomi()).collect(Collectors.joining()); }
	@Override
	public String pronunciation() { return children.stream().map(m -> m.pronunciation()).collect(Collectors.joining()); }
	
	@Override
	public boolean contains(String pos) { return children.stream().anyMatch(m -> m.contains(pos)); }
	@Override
	public boolean containsAll(Collection<String> poss) { return children.stream().anyMatch(m -> m.containsAll(poss)); }
	
	@Override
	public Word clone() { return new Word(this); }
	
	@Override
	public Resource toJASS(Model model) {
		Resource morphemeList = 
				model.createList(children.stream().map(m -> m.toJASS(model)).iterator());
				//.addProperty(RDF.type, JASS.MorphemeList);
		Resource wordResource = model.createResource(getJassURI())
				.addProperty(RDF.type, JASS.Word)
				.addProperty(JASS.morphemes, morphemeList)
				.addLiteral(JASS.name, name())
				.addLiteral(JASS.mainPoS, mainPoS())
				.addLiteral(JASS.subPoS1, subPoS1())
				.addLiteral(JASS.subPoS2, subPoS2())
				.addLiteral(JASS.subPoS3, subPoS3())
				.addLiteral(JASS.conjugation, conjugation())
				.addLiteral(JASS.inflection, inflection())
				.addLiteral(JASS.infinitive, infinitive())
				.addLiteral(JASS.yomi, yomi())
				.addLiteral(JASS.pronunciation, pronunciation());
		return wordResource;
	}
	
	@Override
	public Word concat(Word other) {
		this.children.addAll(other.children);
		return this;
	}
	
	/* ================================================== */
	/* ================== Object Method ================= */ 
	/* ================================================== */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hashCode(children);
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof Word))
			return false; 
		Word other = (Word) obj;
		return Objects.equals(this.id, other.id) &&	//TODO
				Objects.equals(this.children, other.children);
	}
	
	@Override
	public String toString() {
		return children.stream().map(m -> m.toString()).collect(Collectors.joining());
	}
	

}