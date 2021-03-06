package data.RDF.rule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RDFRuleReader {
	/**
	 * 直前に"}"がある";"と、直前に"."がある";"の後ろの位置にマッチ. ただし直前(直後)の判定の際，空白文字は無視する.
	 * 位置にマッチなので置換や分割をしても，"}"も";"も残る.
	 */
	private static final Pattern SPLIT_RULES_PATTERN = 
			Pattern.compile("(?<=(?<=\\});)|(?<=(?<=\\.)\\s*;)", Pattern.CASE_INSENSITIVE);

	/**
	 * "IF(name){...}THEN{...}"の形式で書かれたルールにマッチ. 大文字小文字を問わない. ルールの名前"(name)"は無くても良い.
	 * (){}の前後に空白文字が入っても良い.
	 */
	private static final Pattern WHOLE_IFTHEN_PATTERN = Pattern.compile(
			"\\A\\s*IF\\s*(\\((.*?)\\))??\\s*\\{(.+)\\}\\s*THEN\\s*\\{(.+)\\};\\s*\\z", 
			//0           1   2                 3                     4
			Pattern.CASE_INSENSITIVE
			);
	/**
	 * {@link Matcher#group(int)}で取り出す際の定数.
	 * {@code WHOLE_IFTHEN_PATTERN}の括弧()の変更に合わせること.
	 */
	private static final int IFTHEN_NAME_NUM = 2, IFTHEN_IF_NUM = 3, IFTHEN_THEN_NUM = 4;
	
	/** "...->...;"の形式で書かれたルールにマッチ. 名前は付けられない. */
	private static final Pattern WHOLE_ARROW_PATTERN = Pattern.compile(
			"\\A(.+)->(.+);\\s*\\z", Pattern.CASE_INSENSITIVE);
			//0 1     2
	/**
	 * {@link Matcher#group(int)}で取り出す際の定数.
	 * {@code WHOLE_ARROW_PATTERN}の括弧()の変更に合わせること.
	 */
	private static final int ARROW_IF_NUM = 1, ARROW_THEN_NUM = 2;

	/** コメントアウトの"#"以降にマッチ. "#"から行末まで削除するために使う. */
	private static final Pattern COMMENT_PATTERN = Pattern.compile("#.*$");
	/** 1文字以上の空白文字にマッチ. 行揃えのために使われている余分な空白文字を半角スペース1文字に置換する. */
	private static final Pattern SPACE_CHARACTERS_PATTERN = Pattern.compile("\\s+");

	
	public static RDFRulesSet readRDFRulesSet(Path rulesDir) {
		try (Stream<Path> files = Files.walk(rulesDir)) {
			return files.filter(Files::isRegularFile)
					.filter(p -> p.toString().toLowerCase().endsWith(".txt"))
					.map(RDFRuleReader::readRDFRules)
					.collect(Collectors.toCollection(RDFRulesSet::new));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * RDFルールを記述したファイルを読み込む. コメントアウトとBOMを削除
	 * @param rulesFile
	 * @return RDFルールのセット
	 */
	public static RDFRules readRDFRules(Path rulesFile) {
		String rulesString;
		try (Stream<String> lines = Files.lines(rulesFile)) {
			rulesString = cleanupRuleString(lines.collect(Collectors.toList()));
			rulesString = removeBOM(rulesString);
		} catch (IOException e) {
			rulesString = "";
			e.printStackTrace();
		}
		return createRDFRules(rulesString);
	}

	private static String cleanupRuleString(List<String> rulesString) {
		return rulesString.stream()
			.map(COMMENT_PATTERN::matcher)			// コメントアウトを検知
			.map(m -> m.replaceAll(""))				// #から行末まで削除
			.map(SPACE_CHARACTERS_PATTERN::matcher)	// 1文字以上連続した空白文字を検知
			.map(m -> m.replaceAll(" "))			// 半角スペース1文字に置換
			.collect(Collectors.joining());	
	}
	/** BOM削除. */
	private static String removeBOM(String s) {
		return s.startsWith("\uFEFF") ? s.substring(1) : s;
	}

	/**
	 * 文字列から一連のRDFルールを生成.
	 * @param rulesString
	 * @return RDFルールセット
	 */
	public static RDFRules createRDFRules(String rulesString) {
		return new RDFRules(SPLIT_RULES_PATTERN.splitAsStream(rulesString)
				.map(RDFRuleReader::createRDFRule)
				.collect(Collectors.toCollection(LinkedHashSet::new)));
	}

	/**
	 * 文字列から1つのSPARQL対応RDFルールを生成.
	 * @param ruleString
	 * @return SPARQLルール
	 */
	public static RDFRule createRDFRule(String ruleString) {
		Matcher matcherIFTHEN = WHOLE_IFTHEN_PATTERN.matcher(ruleString);
		Matcher matcherArrow = WHOLE_ARROW_PATTERN.matcher(ruleString);
		return matcherIFTHEN.matches()
				? new RDFRule(matcherIFTHEN.group(IFTHEN_NAME_NUM), matcherIFTHEN.group(IFTHEN_IF_NUM),
						matcherIFTHEN.group(IFTHEN_THEN_NUM))
				: matcherArrow.matches()
						? new RDFRule("arrowRule", matcherIFTHEN.group(ARROW_IF_NUM),
								matcherIFTHEN.group(ARROW_THEN_NUM))
						: RDFRule.EMPTY_RULE;
	}
}
