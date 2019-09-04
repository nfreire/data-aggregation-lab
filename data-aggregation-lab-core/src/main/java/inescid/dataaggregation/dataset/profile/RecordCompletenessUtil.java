/*
 * Copyright 2005-2009 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package inescid.dataaggregation.dataset.profile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

/**
 * Ranking records by completeness.
 * 
 * It gives rank from 0 to 10 for a record, that consists of two parts: up to 5
 * points for tags with values (potentially) coming from controlled
 * vocabularies, and up to 5 points for free-text fields.
 * 
 * These two parts reflect on metadata precision and its attractedness to human
 * users.
 * 
 * Records with one of the title, description, or thumbnail missing get rank 0.
 * 
 * Then we count unique different words that are longer than 3 characters.
 * 
 * First, we count words in several metadata tags: place, time, creator, etc.
 * Each unique word counts as 1 point. Maximal 5 points for tags.
 * 
 * Second, we count words in text fields: title and description. Each 5 unique
 * words, that were not seen before in this record, count as 1 point. Maximal 5
 * points for text fields.
 * 
 * If the record gets less than 2 points for fields, or less than 1 point for
 * texts, then it gets rank 0.
 * 
 * @author Borys Omelayenko
 */

public class RecordCompletenessUtil {

	private static final int MIN_WORD_LENGTH = 3;
	private static final int WORDS_IN_TAGS_PER_POINT = 1;
	private static final int WORDS_IN_TEXTS_PER_POINT = 5;

	
//	/**
//	 * Checking completeness at ingestion to store in solr index.
//	 */
//	public static int rankRecordCompleteness(SolrInputDocument document) {
//
//		List<String> tags = new ArrayList<String>();
//		tags.add(objectAsString(document.getFieldValue("proxy_dc_coverage")));
//		tags.add(objectAsString(document.getFieldValue("proxy_dc_contributor")));
//		tags.add(objectAsString(document.getFieldValue("proxy_dc_creator")));
//		tags.add(objectAsString(document.getFieldValue("proxy_dc_date")));
//		tags.add(objectAsString(document.getFieldValue("proxy_dc_format")));
//		tags.add(objectAsString(document.getFieldValue("proxy_dc_identifier")));
//		tags.add(objectAsString(document.getFieldValue("proxy_dc_language")));
//		tags.add(objectAsString(document.getFieldValue("proxy_dc_publisher")));
//		tags.add(objectAsString(document.getFieldValue("proxy_dc_relation")));
//		tags.add(objectAsString(document.getFieldValue("proxy_dc_rights")));
//		tags.add(objectAsString(document.getFieldValue("proxy_dc_source")));
//		tags.add(objectAsString(document.getFieldValue("proxy_dc_subject")));
//
//		tags.add(objectAsString(document
//				.getFieldValue("proxy_dcterms_alternative")));
//		tags.add(objectAsString(document.getFieldValue("proxy_dcterms_created")));
//		tags.add(objectAsString(document
//				.getFieldValue("proxy_dcterms_conformsTo")));
//		tags.add(objectAsString(document.getFieldValue("proxy_dcterms_extent")));
//		tags.add(objectAsString(document
//				.getFieldValue("proxy_dcterms_hasFormat")));
//		tags.add(objectAsString(document.getFieldValue("proxy_dcterms_hasPart")));
//		tags.add(objectAsString(document
//				.getFieldValue("proxy_dcterms_hasVersion")));
//		tags.add(objectAsString(document
//				.getFieldValue("proxy_dcterms_isFormatOf")));
//		tags.add(objectAsString(document
//				.getFieldValue("proxy_dcterms_isPartOf")));
//		tags.add(objectAsString(document
//				.getFieldValue("proxy_dcterms_isReferencedBy")));
//		tags.add(objectAsString(document
//				.getFieldValue("proxy_dcterms_isReplacedBy")));
//		tags.add(objectAsString(document
//				.getFieldValue("proxy_dcterms_isRequiredBy")));
//		tags.add(objectAsString(document.getFieldValue("proxy_dcterms_issued")));
//		tags.add(objectAsString(document
//				.getFieldValue("proxy_dcterms_isVersionOf")));
//		tags.add(objectAsString(document.getFieldValue("proxy_dcterms_medium")));
//		tags.add(objectAsString(document
//				.getFieldValue("proxy_dcterms_provenance")));
//		tags.add(objectAsString(document
//				.getFieldValue("proxy_dcterms_references")));
//		tags.add(objectAsString(document
//				.getFieldValue("proxy_dcterms_replaces")));
//		tags.add(objectAsString(document
//				.getFieldValue("proxy_dcterms_requires")));
//		tags.add(objectAsString(document.getFieldValue("proxy_dcterms_spatial")));
//		tags.add(objectAsString(document
//				.getFieldValue("proxy_dcterms_tableOfContents")));
//		tags.add(objectAsString(document
//				.getFieldValue("proxy_dcterms_temporal")));
//
//		String thumbnailUrl = objectAsString(document
//				.getFieldValue("provider_aggregation_edm_object"));
//		String title = objectAsString(document.getFieldValue("proxy_dc_title"));
//		String description = objectAsString(StringUtils.join(
//				document.getFieldValues("proxy_dc_description"), "."));
//
//		return rankRecordCompleteness(thumbnailUrl, title, description, tags);
//	}

	/**
	 * Here actual computation happens.
	 */
	public static int rankRecordCompleteness(String thumbnailUrl, String title,
			String description, List<String> tags) {

		if (StringUtils.isEmpty(thumbnailUrl) || StringUtils.isEmpty(title)
				&& StringUtils.isEmpty(description)) {
			return 0;
		}

		Set<String> words = new HashSet<String>();
		List<String> text = toList(title, description);

		int pointsForTags = computePoints(words, tags, WORDS_IN_TAGS_PER_POINT);
		int pointsForText = computePoints(words, text, WORDS_IN_TEXTS_PER_POINT);

		return pointsForText + pointsForTags;
	}

	/**
	 * Utility function: count new words and compute points for them.
	 */
	static int computePoints(Set<String> words, Collection<String> fields,
			int wordsPerPoint) {

		int wordsBefore = words.size();
		words.addAll(extractWordsFromFields(fields));
		return capPoints((words.size() - wordsBefore) / wordsPerPoint);
	}

	/**
	 * Utility function: extract words from field values.
	 */
	static Collection<String> extractWordsFromFields(Collection<String> fields) {

		List<String> words = new ArrayList<String>();
		for (String field : fields) {
			if (!StringUtils.isEmpty(field)) {
				words.addAll(extractWordsFromField(field));
			}
		}
		return words;
	}

	/**
	 * Utility function: extract words from a single field value.
	 */
	static List<String> extractWordsFromField(String field) {
		List<String> words = new ArrayList<String>();
		for (String word : field.split("\\W")) {
			if (!StringUtils.isEmpty(word) && word.length() >= MIN_WORD_LENGTH) {
				words.add(word.toLowerCase());
			}
		}
		return words;
	}

	/**
	 * Utility function: cap points.
	 */
	private static int capPoints(int points) {
		if (points > 5) {
			points = 5;
		}
		return points;
	}

	public static List<String> toList(String... strings) {
		List<String> text = new ArrayList<String>();
		for (String string : strings) {
			text.add(string);
		}
		return text;
	}

	private static String objectAsString(Object object) {
		if (object == null) {
			return null;
		}
		return object.toString();
	}

}
