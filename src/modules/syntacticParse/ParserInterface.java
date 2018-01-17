package modules.syntacticParse;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import grammar.Concept;
import grammar.Morpheme;
import grammar.NaturalLanguage;
import grammar.Sentence;
import grammar.clause.Clause;
import grammar.word.Word;

public interface ParserInterface {
	/********************************/
	/***** 解析器・階層化呼び出し部 *****/
	/********************************/
	/** 入力が自然言語文1文のみ */
	Sentence text2sentence(NaturalLanguage nlText);
	/** 入力が自然言語文のList */
	List<Sentence> texts2sentences(List<NaturalLanguage> nlTextList);
	/** 入力が自然言語文の配列 */
	Sentence[] texts2sentences(NaturalLanguage[] nlTexts);
	/** 入力がテキストファイル */
	List<Sentence> texts2sentences(Path nlTextFilePath);
	
	
	/********************************/
	/********** 解析器実行部 **********/
	/********************************/

	/*** 自然言語文をParserに通し，出力結果をListに保管 ***/
	/** 入力: Path or NaturalLanguage or List<NaturalLanguage> **/
	/** 出力: List<String> **/	// Listの1要素=出力の1行
	/** 入力が自然言語文1文のみ */
	List<String> parse(NaturalLanguage nlText);
	/** 入力が自然言語文のList */
	List<String> parse(List<NaturalLanguage> nlTextList);
	/** 入力が自然言語文の配列 */
	List<String> parse(NaturalLanguage[] nlTexts);
	/** 入力がテキストファイル */
	List<String> parse(Path nlTextFilePath);

	/** 入力されたList<NL>が空だった場合の処理 */
	default List<String> emptyInput() {
		System.err.println("Input List is Empty!!!");
		return new ArrayList<String>();
	}


	/***********************************/
	/********** 解析結果階層化部 **********/
	/***********************************/
	/*** 出力(List<String>)を分解 ***/
	List<Sentence> decodeProcessOutput(List<String> parseResult4all);

	/** Sentence1(Clause1(Word1,Word2,..),Clause2(Word,Word,..),Clause3(..),...)の構成に変換 **/
	Sentence decode2Sentence(List<String> parseResult4sentence);
	Clause decode2Clause(List<String> parseResult4clause);
	Word decode2Word(List<List<String>> parseResult4word);			// 複数の形態素からなる場合を考慮
	Concept decode2Concept(List<List<String>> parseResult4concept);	// Wordに同じ
	Morpheme decode2Morpheme(List<String> parseResult4morpheme);		// 形態素の情報が複数行の場合を考慮
}