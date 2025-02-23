package edu.holycross.shot.sparqlcts

import edu.harvard.chs.cite.CtsUrn

class QueryBuilder {


  static String prefixPhrase  = "PREFIX hmt:        <http://www.homermultitext.org/hmt/rdf/>\nPREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\nPREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\nPREFIX dcterms: <http://purl.org/dc/terms/>\nPREFIX cts:        <http://www.homermultitext.org/cts/rdf/>\nPREFIX cite:        <http://www.homermultitext.org/cite/rdf/>\n"

	/** Builds SPARQL query string to retrieve all
	 * relevant data about a citable leaf node.
	 */
	static String getLeafNodeQuery(CtsUrn urn) {
		return getLeafNodeQuery(urn, 0)
	}

	static String getLeafNodeQuery(CtsUrn urn, Integer context) {
		return prefixPhrase + """


		SELECT ?psg ?txt ?anc ?xpt ?xmlns ?xmlnsabbr ?nxt WHERE {
			?psg cts:isPassageOf <${urn.getUrnWithoutPassage()}> .
				?psg cts:hasTextContent ?txt .
				?psg cts:hasSequence ?s .
				optional {
					?psg cts:xmlnsabbr ?xmlnsabbr .
						?psg cts:xmlns ?xmlns .
						?psg hmt:xpTemplate ?xpt .
						?psg hmt:xmlOpen ?anc  .
						?psg cts:next ?nxt  .
				}

			{
				{ SELECT (xsd:int(?seq) + ${context} AS ?max)
					WHERE {
						<${urn}> cts:hasSequence ?seq .
					}
				}

				{ SELECT (xsd:int(?seq) - ${context} AS ?min)
					WHERE {
						<${urn}> cts:hasSequence ?seq .
					}
				}
			}
			FILTER (?s <= ?max) .
				FILTER (?s >= ?min) .
		}
		ORDER BY ?s

			"""
	}



	/** Builds SPARQL query string to retrieve a
	 * rdf label fo a URN.
	 */
	static String getRdfLabel(CtsUrn urn) {
		return prefixPhrase + """

		SELECT ?label WHERE {
			<${urn}> rdf:label ?label .
		}
		"""
	}


	/** Builds SPARQL query string to find
	 * URN of leaf node preceding the
	 * requested leaf node.
	 * @param urn URN at leaf node level.
	 * @returns A complete SPARQL query string.
	 */
	static String getPrevUrnQuery(CtsUrn urn) {
		String workUrnStr = urn.getUrnWithoutPassage()
			return prefixPhrase + """

		SELECT ?prevUrn ?prevSeq WHERE {
			<${urn}> cts:prev ?prevUrn .
				?prevUrn cts:hasSequence ?prevSeq .
		}
		"""
	}




	/** Builds SPARQL query string to find
	 * URN of leaf node following the
	 * requested leaf node.
	 * @param urn URN at leaf node level.
	 * @returns A complete SPARQL query string.
	 */
	static String getNextUrnQuery(CtsUrn urn) {
		String workUrnStr = urn.getUrnWithoutPassage()
			return prefixPhrase + """

		SELECT ?nextUrn ?nextSeq WHERE {
			<${urn}> cts:next ?nextUrn .
				?nextUrn cts:hasSequence ?nextSeq .
		}
		"""
	}



	/** Builds SPARQL query string to retrieve a
	 * strcutured description for a version-level CTS URN.
	 */
	static String getVersionDescrQuery(CtsUrn psgUrn) {
		def vers = "${psgUrn.getUrnWithoutPassage()}"
			return prefixPhrase + """


		SELECT ?gname ?title ?lab WHERE {
			<${vers}> dcterms:title ?lab .
				<${vers}> cts:belongsTo ?wk .
				?wk dcterms:title ?title .
				?wk cts:belongsTo ?tg .
				?tg dcterms:title ?gname .
		}
		"""
	}


	/** Builds SPARQL query string to determine if a
	 * CTS URN refers to a leaf citation node.
	 * @param urn The urn to test.
	 * @returns A complete SPARQL query string.
	 */
	static String getIsLeafQuery(CtsUrn urn) {
		return prefixPhrase + """

		ASK
			WHERE {
				<${urn}> cts:hasTextContent ?txt .
			}
		"""
	}


	/** Builds SPARQL query string to retrieve
	 * version mappings for a work-level CTS URN.
	 * @param urn The work-level urn to find versions for.
	 * @returns A complete SPARQL query string.
	 */
	static String getVersionQuery(CtsUrn workLevelUrn) {
		return prefixPhrase + """

		SELECT   ?vers ?wk ?type
			WHERE {
				?vers cts:belongsTo  <${workLevelUrn.getUrnWithoutPassage()}>  .
					?vers rdf:type ?type .
					BIND (<${workLevelUrn.getUrnWithoutPassage()}> as ?wk)
			}
		"""
	}


	/** Builds SPARQL query string to retrieve
	 * text content of a leaf citation node.
	 * @param urn The urn of the passage to retrieve.
	 * @returns A complete SPARQL query string.
	 */
	static String getLeafNodeTextQuery(CtsUrn urn) {
		return getLeafNodeQuery(urn, 0)
	}


	/** Builds SPARQL query string to retrieve
	 * text content of a leaf citation node witn n units
	 * of context.
	 * @param urn The urn of the passage to retrieve.
	 * @context Number of units of context to include.
	 * @returns A complete SPARQL query string.
	 */
	static String getLeafNodeTextQuery(CtsUrn urn, Integer context) {
		return prefixPhrase + """


		SELECT ?psg ?txt ?anc ?xpt ?nxt ?xmlns ?xmlnsabbr WHERE {
			?psg cts:isPassageOf <${urn.getUrnWithoutPassage()}> .
				?psg cts:hasTextContent ?txt .
				?psg cts:hasSequence ?s .
				optional {
					?psg hmt:xpTemplate ?xpt .
						?psg hmt:xmlOpen ?anc  .
						?psg cts:xmlns ?xmlns  .
						?psg cts:xmlnsabbr ?xmlnsabbr  .
						?psg cts:next ?nxt  .
				}

			{
				{ SELECT (xsd:int(?seq) + ${context} AS ?max)
					WHERE {
						<${urn}> cts:hasSequence ?seq .
					}
				}

				{ SELECT (xsd:int(?seq) - ${context} AS ?min)
					WHERE {
						<${urn}> cts:hasSequence ?seq .
					}
				}
			}
			FILTER (?s <= ?max) .
				FILTER (?s >= ?min) .
		}
		ORDER BY ?s
			"""
	}


	/** Builds SPARQL query string to retrieve
	 * the sequence number for a leaf-node URN
	 * based on document order
	 * @param urn The urn of the passage whose sequence we want to find.
	 * @returns A complete SPARQL query string.
	 */
	static String getSeqQuery(CtsUrn urn) {
		return prefixPhrase + """

		SELECT ?urn ?seq
			WHERE  {
				<${urn}> cts:hasSequence ?seq .
					BIND( <${urn}> as ?urn )
			}
		"""
	}

	/** Builds SPARQL query string to find the URN and
	 * sequence number of the first citable node contained in
	 * a given URN.  If the URN is a leaf node, returns the
	 * the query to identify URN and sequence of that node.
	 * @param urn The urn to test.
	 * @returns A complete SPARQL query string.
	 */
	static String getFirstContainedQuery(CtsUrn containingUrn) {
		return prefixPhrase + """

		SELECT   ?urn ?seq
			WHERE {
				{
     				?urn cts:containedBy  <${containingUrn}>  .
					?urn cts:hasSequence ?seq .
				} union {
     				?urn  cts:isPassageOf  <${containingUrn}>  .
					?urn cts:hasSequence ?seq .
				} union {
					<${containingUrn}> cts:hasSequence ?seq .
					bind( <${containingUrn}> as ?urn) .
  }
			}
		ORDER BY ?seq
			LIMIT 1
			"""
	}

	/** Builds SPARQL query string to find URN at a given sequence
	 * @param seq The sequence number.
	 * @param versionUrnStr a version-level URN.
	 * @returns A complete SPARQL query string.
	 */

	static String urnForSequence(Integer seq, String versionUrnStr){

		return prefixPhrase + """


		SELECT ?urn WHERE {
			?urn cts:isPassageOf <${versionUrnStr}> .
				?urn cts:hasSequence ${seq} .
		}
		"""
	}


	/** Builds SPARQL query string to find the URN and
	 * sequence number of the last citable node contained in
	 * a given URN.  If the URN is a leaf node, returns the
	 * the query to identify URN and sequence of that node.
	 * @param urn The urn to test.
	 * @returns A complete SPARQL query string.
	 */
	static String getLastContainedQuery(CtsUrn containingUrn) {

		return prefixPhrase + """

		SELECT   ?urn ?seq
			WHERE {
				?urn  cts:containedBy*  <${containingUrn}>  .
					?urn cts:hasSequence ?seq .
			}
		ORDER BY DESC(?seq)
			LIMIT 1
			"""
	}

	/** Builds SPARQL query string to find the URN
	 * of the last citable node of a work.
	 * @param urn The urn to test.
	 * @returns A complete SPARQL query string.
	 */
	static String getLastUrnQuery(CtsUrn versionUrn) {

		return prefixPhrase + """

		SELECT   ?urn ?seq
			WHERE {
				?urn  cts:isPassageOf  <${versionUrn}>  .
					?urn cts:hasSequence ?seq .
			}
		ORDER BY DESC(?seq)
			LIMIT 1
			"""
	}

	/* Forms SPARQL query to find all URNs
	 *  between two leaf-nodes, identified by their sequence numbers
	 *  (@startCount, @endCount). Requires a version-level URN.
	 */
	static String getRangeUrnsQuery(Integer startCount, Integer endCount, String versionUrn) {
		return prefixPhrase + """

		SELECT distinct ?ref
			WHERE {
					?ref cts:isPassageOf <${versionUrn}> .
					?ref cts:hasSequence ?s .
					?ref cts:citationDepth ?d .
					?ref cts:hasTextContent ?t .

					FILTER (?s >= "${startCount}"^^xsd:integer) .
					FILTER (?s <= "${endCount}"^^xsd:integer) .
			}
		ORDER BY ?s
			"""
	}

	/* Forms SPARQL query to find all URNs, and their accompanying text
	 *  between two leaf-nodes, identified by their sequence numbers
	 *  (@startCount, @endCount). Requires a version-level URN.
	 */
	static String getRangeNodesQuery(Integer startCount, Integer endCount, String versionUrn) {
		return prefixPhrase + """

		SELECT ?ref ?t ?anc ?xpt ?nxt ?xmlns ?xmlnsabbr
			WHERE {
				?ref cts:isPassageOf <${versionUrn}> .
					?ref cts:hasSequence ?s .
					?ref cts:citationDepth ?d .
					?ref cts:hasTextContent ?t .
					optional {
						?ref hmt:xmlOpen ?anc .
							?ref hmt:xpTemplate ?xpt .
							?ref cts:xmlnsabbr ?xmlnsabbr .
							?ref cts:xmlns ?xmlns .
					}
					optional {
						?ref cts:next ?nxt .
					}

				FILTER (?s >= "${startCount}"^^xsd:integer) .
				FILTER (?s <= "${endCount}"^^xsd:integer) .
			}
		ORDER BY ?s
			"""
	}

	/* Forms SPARQL query to find the type of an version-level URN (Translation or Edition);
	 * if the URN points to an exemplar, returns the type of the parent Version.
	 * @returns a Sparql query as String
	 **/
	static String getVersionTypeQuery(String urn) {
		return prefixPhrase + """

		SELECT ?t
			WHERE {

				bind (<${urn}> as ?s)
				{
					?s rdf:type ?t .
				} union {
					?s cts:belongsTo ?v .
						?v rdf:type ?t .
				}

			} """
	}


	/* Forms SPARQL query to find the language of an Edition, Translation, or
	 * derived Exemplar.
	 * @returns a Sparql query as String
	 **/
	static String getLangQuery(String urn) {
		return prefixPhrase + """

		SELECT ?l
			WHERE {

				bind (<${urn}> as ?s)
				{
					?s cts:belongsTo ?v .
						?s rdf:type <http://www.homermultitext.org/cts/rdf/Edition> .
						?v cts:lang ?l .
				} union {
					?s cts:belongsTo ?v .
						?v rdf:type <http://www.homermultitext.org/cts/rdf/Edition> .
						?v cts:belongsTo ?w .
						?w cts:lang ?l .
				} union {
					?s cts:belongsTo ?v .
						?v rdf:type <http://www.homermultitext.org/cts/rdf/Translation> .
						?v cts:lang ?l .
				} union {
					?s rdf:type <http://www.homermultitext.org/cts/rdf/Translation> .
						?s cts:lang ?l .
				}
			}"""
	}

	/* Forms SPARQL query to find all urns at a given depth
	 *  given a version- or exemplar-level URN and an integer (depth) as input
	 */
	static String getWorkGVRQuery(CtsUrn urn, Integer level) {
		return prefixPhrase + """

		SELECT ?ref (MIN(?s) AS ?st)
			WHERE {
			?leaf cts:isPassageOf <${urn.toString()}> .
					?leaf cts:containedBy* ?ref .
					?leaf cts:hasSequence ?s .
  					?ref cts:citationDepth ${level} .			}
		GROUP BY ?ref
			ORDER BY ?st
			"""
	}


	/* Forms SPARQL query to find all URNs at a given depth (@level)
	 *  between two leaf-nodes, identified by their sequence numbers
	 *  (@startCount, @endCount). Requires a word-level URN. Note
	 *  'distinct', which is necessary when the level requested is lower
	 *  than the maximum citation-depth.
	 */
	static String getFillGVRQuery(Integer startCount, Integer endCount, Integer level, String workUrn) {
		return prefixPhrase + """

		SELECT distinct ?ref WHERE {
			  ?urn cts:isPassageOf <${workUrn}> .
			  ?urn cts:hasSequence ?s .
			  {
			  	?urn cts:citationDepth ${level} .
			  	bind (?urn as ?ref).
			  } union {
			  	?container cts:contains ?urn.
				?container cts:citationDepth ${level} .
			  	bind (?container as ?ref).
			  } union {
				 ?urn cts:hasTextContent ?throwaway .
					bind (?urn as ?ref).
				 }

			  FILTER (?s >= "${startCount}"^^xsd:integer) .
			  FILTER (?s <= "${endCount}"^^xsd:integer) .

			} 		ORDER BY ?s

			"""
	}

	static String getGVRNodeQuery(CtsUrn urn, Integer level) {
		return prefixPhrase + """

		SELECT ?ref (MIN(?s) AS ?startSeq)
			WHERE {
				<${urn}> cts:contains ?leaf .
			  ?leaf cts:containedBy* ?ref .
			  ?ref cts:citationDepth  ${level} .
			  ?leaf cts:hasSequence ?s .	}
		GROUP BY ?ref
			ORDER BY ?startSeq
			"""
	}

/** Forms SPARQL query to find greatest citation depth
* anywhere in a work. Input is a work-level URN.
*/
	static String getLeafDepthForWorkQuery(CtsUrn urn) {
		return prefixPhrase + """


		SELECT (MAX(?d) AS ?deepest) WHERE {
			{
				?old cts:isPassageOf <${urn.toString()}> .
					?old cts:citationDepth ?d .
			}
			UNION
			{
				?old cts:isPassageOf <${urn.toString()}> .
					?c cts:containedBy+  ?old .
					?leaf cts:containedBy* ?c .
					?leaf cts:citationDepth ?d .
			}
		}
		"""
	}

/** Forms SPARQL query to find the greatest citation depth
* anywhere in a work. Input is citation-level URN.
*/
	static String getLeafDepthQuery(CtsUrn urn) {
		String workUrnStr = urn.getUrnWithoutPassage()
			return prefixPhrase + """

		SELECT (MAX(?d) AS ?deepest)
			WHERE {
				?c cts:isPassageOf <${workUrnStr}> .
					?c cts:citationDepth ?d .
			}
		"""
	}


}
