﻿package main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.jena.rdf.model.Model;

import data.RDF.Ontology;
import data.id.ModelIDMap;
import data.id.SentenceIDMap;
import data.id.StatementIDMap;
import grammar.naturalLanguage.NaturalLanguage;
import grammar.naturalLanguage.NaturalParagraph;
import grammar.sentence.Sentence;
import modules.OutputManager;
import modules.relationExtract.RelationExtractor;
import modules.syntacticParse.Cabocha;
import modules.syntacticParse.SyntacticParser;
import modules.textRevision.SentenceReviser;
import util.StringListUtil;

public class Generator {
	private static final Path EXTENSION_RULE_PATH = Paths.get("../OntologyGenerator/resource/rule/extensionRules.txt"); 
	private static final Path ONTOLOGY_RULE_PATH = Paths.get("../OntologyGenerator/resource/rule/ontologyRules.txt");
	private static final String JASS_MODEL_URL = "../OntologyGenerator/resource/ontology/SyntaxOntology.owl";
	private static final Path INPUT_FILE_PATH = Paths.get("../OntologyGenerator/tmp/parserIO/CaboChaInput.txt");
	private static final Path OUTPUT_FILE_PATH = Paths.get("../OntologyGenerator/tmp/parserIO/CaboChaOutput.txt");

	
	
	/****************************************/
	/**********    Main  Method    **********/
	/****************************************/
	public static void main(String[] args) {
		init();
		new Generator().execute(args.length == 1? args[0] : "");
	}
	
	/****************************************/
	/**********     Constructor    **********/
	/****************************************/
	public Generator() {}


	/****************************************/
	/**********   Member  Method   **********/
	/****************************************/
	/**
	 * ジェネレータの実行. 
	 * ぶっちゃけテスト用に色々書くために仲介させているだけ.
	 */
	private void execute(String textFileName) {
		//textFileName = "./resource/input/goo/text/gooText生物-動物名-あ.txt";
		textFileName = "../OntologyGenerator/resource/input/test/literalText.txt";
		Path textFilePath = Paths.get(textFileName);

		String[] texts = {
				/*
				"クジラは小魚を食べる。", 
				"クジラは哺乳類である。",
				"カニの味噌汁は美味しいぞ",
				"アイアイはアイアイ科の原始的な猿",
				//*/
				"馬は体長1メートルほど。",
				"藍鮫はアイザメ科の海水魚の総称だ。"
				//*/
		};
		List<NaturalLanguage> nlLists = Arrays.asList(NaturalLanguage.toNaturalLanguageArray(texts));

		Generator generator = new Generator();
		generator.generate(nlLists);
		//generator.generate(textFilePath);
	}
	
	/**
	 * ジェネレータ本体. 
	 * @param textFilePath 入力するテキストファイルのパス
	 * @return
	 */
	public Ontology generate(Path textFilePath) {
		return generateParagraphs(loadTextFile(textFilePath));
	}

	public Ontology generateParagraphs(List<NaturalParagraph> naturalLanguageParagraphs) {
		// 段落を処理に使う予定はまだないので，文のリストに均して，
		// List<Sentence>を引数として受け取る#generateに渡す
		List<NaturalLanguage> naturalLanguages = naturalLanguageParagraphs.stream()
				.map(NaturalParagraph::getTexts)
				.flatMap(List<NaturalLanguage>::stream)
				.collect(Collectors.toList());
		return generate(naturalLanguages);
	}
	/**
	 * オントロジー構築器の実行
	 * @param naturalLanguageParagraphs 自然言語文の段落のリスト
	 */
	public Ontology generate(List<NaturalLanguage> naturalLanguages) {
		System.out.println("Start.");
		/*************************************/
		/********** 構文解析モジュール **********/
		/*************************************/
		List<Sentence> sentenceList = new SyntacticParser().parseSentences(naturalLanguages);
		SentenceIDMap sentenceMap = SentenceIDMap.createFromList(sentenceList);
		sentenceMap.setLongSentence();
		
		
		/*************************************/
		/********** 文章整形モジュール **********/
		/*************************************/
		SentenceReviser sr = new SentenceReviser();
		/** Step1: 単語結合 **/
		sr.connectWord(sentenceMap);
		/** Step2: 長文分割 **/
		/* 長文を分割し複数の短文に分ける */
		sr.divideEachSentence(sentenceMap);
		sentenceMap.setShortSentence();
		sentenceMap.forEachKey(System.out::println);	//TODO
		/*************************************/
		/********** 関係抽出モジュール **********/
		/*************************************/
		RelationExtractor re = new RelationExtractor(EXTENSION_RULE_PATH, ONTOLOGY_RULE_PATH, JASS_MODEL_URL);
		ModelIDMap JASSMap = re.convertMap_Sentence2JASSModel(sentenceMap);
		ModelIDMap modelMap = re.convertMap_JASSModel2RDFModel(JASSMap);
		StatementIDMap statementMap = re.convertMap_Model2Statements(modelMap);
		
		Model unionModel = modelMap.uniteModels().difference(re.defaultJASSModel);
		Ontology ontology = new Ontology(re.convertModel_Jena2TripleList(unionModel));
		
		// ログや生成物の出力
		OutputManager opm = new OutputManager();
		opm.outputDividedSentences(sentenceMap);
		opm.outputJASSGraph(JASSMap);
		opm.outputIDAsCSV(statementMap.createIDRelation());
		opm.outputOntology(unionModel);
		opm.outputRDFRules(re.getOntologyRules());
				
		System.out.println("Finished.");
		System.out.println("Sentences: " + naturalLanguages.size() + "\t->dividedSentences: " + sentenceMap.size());
		System.out.println("ontology size: " + ontology.getTriples().size() + "\n");

		return ontology;
	}


	/**
	 * テキストファイル読み込み. テキストは1行一文. 空行を段落の境界とみなす.
	 * @param textFile テキストファイルのパス
	 * @return 段落のリスト(段落はNaturalLanguageのリストからなる)
	 */
	private List<NaturalParagraph> loadTextFile(Path textFile) {
		List<String> texts;
		try {
			texts = Files.readAllLines(textFile);
		} catch (IOException e) {
			e.printStackTrace();
			texts = new ArrayList<>(Arrays.asList("ジェネレータはファイルからテキストを読み込めませんでした。"));
		}
		// 空行を見つけたら段落の境界とする
		return StringListUtil.split("", texts).stream()
				.map(NaturalLanguage::toNaturalLanguageList)
				.map(NaturalParagraph::new)
				.collect(Collectors.toList());
	}

	private static void init() {
		Cabocha.setINPUT_TXTFILE_PATH(INPUT_FILE_PATH);
		Cabocha.setOUTPUT_TXTFILE_PATH(OUTPUT_FILE_PATH);
	}
}