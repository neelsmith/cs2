package edu.holycross.shot.graph

import edu.holycross.shot.sparqlcts.CtsGraph
import edu.holycross.shot.sparqlcts.Sparql

import edu.harvard.chs.cite.CiteUrn
import edu.harvard.chs.cite.CtsUrn
import groovy.json.JsonSlurper

/** A class interacting with a SPARQL endpoint to
 * to resolve SPARQL replies into objects in the abstract data
 * model of CITE Collection objects.
 */
class IndexGraph {

  /** SPARQL endpoint object from citeservlet lib. */
  Sparql sparql

  /** We're going to be doing so much CTS work, let's just re-use CtsGraph */
  CtsGraph ctsgraph

	/** Constructor with required SPARQL endpoint object */
	IndexGraph(Sparql endPoint) {
		sparql = endPoint
		ctsgraph = new CtsGraph(sparql)
	}

  /** Overloaded method. Find all nodes at one degree of
   * relation to the object identified by
   * a CITE urn.
   * @param urn CITE Object to find in the graph.
   * @returns ArrayList of Triple objects.
   */
	ArrayList findAdjacent(CiteUrn urn) {
		ArrayList al = []
		al << "dogs"
		al << "cats"
	}

  /** Overloaded method. Find all nodes at one degree of
   * relation to the object identified by
   * a CITE urn.
   * @param urn CTS Object to find in the graph.
   * @returns ArrayList of Triple objects.
   */
	ArrayList findAdjacent(CtsUrn urn) {
		ArrayList al = []
		CtsUrn testUrn = new CtsUrn(urn.reduceToNode())
		String workLevel = testUrn.getWorkLevel()

		switch (workLevel){
			case "GROUP":
				al = getForGroup(urn)
				break;
			case "WORK":
				if (urn.passageComponent == null){
					al = getForWorkWithoutPassage(urn)
				} else {
					try {
						if (urn.isRange()){
							al = getForWorkRange(urn)
						} else {
							if (ctsgraph.isLeafNode(urn)){
								al = getForWorkLeaf(testUrn)
							} else {
								al = getForWorkContainerk(testUrn)
							}
						}
					} catch (Exception e) {
						al = getOneOffCtsUrn(urn)
					}
				}
				break;
			case "VERSION":
				if (urn.passageComponent == null){
					al = getForWorkWithoutPassage(urn)
				} else {
					try {
						/* Can be range or not */
						if (urn.isRange()){
							al = getForVersionRange(urn)
						} else {
							/* Can be leaf-node */
							if (ctsgraph.isLeafNode(urn)){
								al = getForVersionLeaf(urn)
							} else {
								/* is non-leaf-node citation */
								al = getForVersionContainer(urn)
							}
						}
					} catch (Exception e) {
						al = getOneOffCtsUrn(urn)
					}
				}
				break;
			case "EXEMPLAR":
				if (urn.passageComponent == null){
					al = getForWorkWithoutPassage(urn)
				} else {
					try {
						if (urn.isRange()){
							al = getForExemplarRange(urn)
						} else {
							if (ctsgraph.isLeafNode(urn)){
								al = getForExemplarLeaf(urn)
							} else {
								al = getForExemplarContainer(urn)
							}
						}
					} catch (Exception e) {
						al = getOneOffCtsUrn(urn)
					}
				}
				break;
			default:
				al << "error ${workLevel}"
		}

	    return al
	}

/* -------------------------------------------------------------------------------- */
/* Methods for Identifying Components */
/* -------------------------------------------------------------------------------- */

/** A quick check to see if a URN exists in our data.
 *   @param urn The URN to test.
 *   @returns Boolean
 **/
Boolean existsInGraph(CtsUrn urn){
	Boolean response = false
		ArrayList replyArray = []
		ArrayList workingArray = []
		String replyText = ""
		String testString
		String textgroupQuery = QueryBuilder.existsInGraph(urn.encodeSubref())
		String reply = sparql.getSparqlReply("application/json", textgroupQuery)

		JsonSlurper slurper = new groovy.json.JsonSlurper()
		def parsedReply = slurper.parseText(reply)
		workingArray = parsedJsonToTriples(parsedReply)
		replyArray = uniqueTriples(workingArray)
		replyArray.each { ttt ->
			testString = URLDecoder.decode(ttt.subj.toString(),"UTF-8")
				if (urn.toString() == testString){
					response = true
				}
		}

	return response
}

/** A quick check to see if a URN exists in our data.
 *   @param urn The URN to test.
 *   @returns Boolean
 **/
Boolean existsInGraph(CiteUrn urn){
		return false
}

/** Given a work-level URN with a citation
 * return an ArrayList of version-level URNs.
 * @param urn CTS Object to find in the graph.
 * @returns ArrayList of Triple objects.
 */
ArrayList getVersionsForWork(urn){
	CtsUrn requestUrn
		requestUrn = new CtsUrn(urn.getUrnWithoutPassage())
		ArrayList versionArray = []

		String versionQuery = QueryBuilder.getVersionsForWork(requestUrn.toString())
		String reply = sparql.getSparqlReply("application/json", versionQuery)
		JsonSlurper versionSlurper = new groovy.json.JsonSlurper()
		def versionParsedReply = versionSlurper.parseText(reply)
		versionParsedReply.results.bindings.each{ jo ->
			if (jo.version){
				versionArray << jo.version?.value // work to be done here!
			}
		}
	return versionArray
}

/** Given a version-level URN, returns all exemplar-level URNs
 * @param urn CTS-URN
 * @returns ArrayList of CTS-URNs
 */
ArrayList getExemplarsForVersion(CtsUrn urn){
	ArrayList replyArray = []
		String replyText = ""
		String exemplarQuery = QueryBuilder.getQueryExemplarsForVersion(urn.getUrnWithoutPassage())
		String reply = sparql.getSparqlReply("application/json", exemplarQuery)
		if ("${urn.getWorkLevel()}" == "VERSION"){

			JsonSlurper slurper = new groovy.json.JsonSlurper()
				def parsedReply = slurper.parseText(reply)
				parsedReply.results.bindings.each{ jo ->
					replyArray << jo.o?.value
				}
		} else {
			replyArray << "ERROR: URN must point to a version-level URN"
		}
	return replyArray
}

/** Given a work-level URN, returns all version and exemplar-level URNs
 * @param urn CTS-URN
 * @returns ArrayList of CTS-URNs
 */
ArrayList getVersionsAndExemplarForWork(CtsUrn urn){
	ArrayList replyArray = []
		replyArray << "Not implemented"
		return replyArray
}


/* -------------------------------------------------------------------------------- */
/* Methods for finding adjacents for CTS URNs
/* -------------------------------------------------------------------------------- */


/** For CTS URNs that are present in the graph, but not as part of CTS texts
 *   do a very simple, stupid search.
 *   @param urn The URN to test.
 *   @returns ArrayList of Triple objects.
 **/
ArrayList getOneOffCtsUrn(CtsUrn urn){
	String workLevel = urn.getWorkLevel()
	ArrayList replyArray = []
	ArrayList workingArray = []
	ArrayList versionArray = []
	ArrayList exemplarArray = []
	ArrayList tempArray = []
	String replytext = ""
	String oneOffQuery = ""
	String reply = ""
	JsonSlurper slurper = null
	def parsedReply = null

	switch (workLevel){
		case "WORK":
			versionArray = getVersionsForWork(urn)
			versionArray.each { u ->
				tempArray = getOneOffCtsUrn(new CtsUrn("${u}${urn.passageComponent}"))
					tempArray.each { ttt ->
						workingArray << ttt
					}
			}
		break;
		case "VERSION":
			exemplarArray = getExemplarsForVersion(urn)
			exemplarArray.each { u ->
				tempArray = getOneOffCtsUrn(new CtsUrn("${u}${urn.passageComponent}"))
					tempArray.each { ttt ->
						workingArray << ttt
					}
				}
		}

		oneOffQuery = QueryBuilder.getSimpleCtsQuery(urn.encodeSubref())
		reply = sparql.getSparqlReply("application/json", oneOffQuery)
		slurper = new groovy.json.JsonSlurper()
		parsedReply = slurper.parseText(reply)
		parsedJsonToTriples(parsedReply).each{
			workingArray << it

		}
		replyArray = uniqueTriples(workingArray)

		return replyArray
}

/** Finds data adjacent to a TextGroup-level URN.
 * @param urn The URN to test.
 * @returns ArrayList of Triple objects.
 */
ArrayList getForGroup(CtsUrn urn){
		ArrayList replyArray = []
		ArrayList workingArray = []
		String replyText = ""
		String textgroupQuery = QueryBuilder.getTextGroupAdjacentQuery(urn.encodeSubref())
		String reply = sparql.getSparqlReply("application/json", textgroupQuery)

		JsonSlurper slurper = new groovy.json.JsonSlurper()
		def parsedReply = slurper.parseText(reply)
		workingArray = parsedJsonToTriples(parsedReply)
		replyArray = uniqueTriples(workingArray)
		return replyArray
}

/** Gets Adjacents for a work-level CTS URN without a passage component.
* @param urn The URN to test.
* @returns ArrayList of Triple objects.
*/
ArrayList getForWorkWithoutPassage(CtsUrn urn){
		ArrayList replyArray = []
		ArrayList workingArray = []
		String replyText = ""
		String textgroupQuery = QueryBuilder.getWorkVersionExemplarWithoutPassage(urn.encodeSubref())
		String reply = sparql.getSparqlReply("application/json", textgroupQuery)

		JsonSlurper slurper = new groovy.json.JsonSlurper()
		def parsedReply = slurper.parseText(reply)
		workingArray = parsedJsonToTriples(parsedReply)
		replyArray = uniqueTriples(workingArray)
		return replyArray
}


/** Finds  data adjacent to a work-level containing (non-leaf-node) URN
 * @param urn The URN to test.
 * @returns ArrayList of Triple objects.
 */
ArrayList getForWorkContainer(CtsUrn urn){
	ArrayList versionArray = []
		ArrayList workingArray = []
		ArrayList replyArray = []
		ArrayList tempArray = []
		versionArray = getVersionsForWork(urn)

		String replyText = ""
		String generalQuery = QueryBuilder.generalQuery(urn)
		String reply = sparql.getSparqlReply("application/json", generalQuery)

		JsonSlurper slurper = new groovy.json.JsonSlurper()
		def parsedReply = slurper.parseText(reply)
		workingArray = parsedJsonToTriples(parsedReply)

		String tempQuery = ""

		versionArray.each { u ->
			tempArray = getForVersionContainer(new CtsUrn("${u}${urn.passageComponent}"))
				tempArray.each { ttt ->
					workingArray << ttt
				}
		}

	replyArray = uniqueTriples(workingArray)

		return replyArray
}

/** Given a work-level URN with a citation
 * return adjacent nodes for all versions (editions and translatin), and all derived exemplars
 * @param urn CTS Object to find in the graph.
 * @returns ArrayList of Triple objects.
 */
ArrayList getForWorkLeaf(CtsUrn urn) {
		ArrayList versionArray = []
		ArrayList replyArray = []
		ArrayList workingArray = []
		CtsUrn tempUrn
		String thisPassage = urn.passageComponent

		// Get all versions
		versionArray = getVersionsForWork(urn)


		// For each version, get adjacents
		versionArray.each { vers ->
				tempUrn = new CtsUrn("${vers}${thisPassage}")
				getForVersionLeaf(tempUrn).each { replyArray << it }
		}

	return uniqueTriples(replyArray)

}

/** Find all nodes at one degree of
 * relation to the object identified by
 * a CTS work-level urn with a range citation.
 * @param urn CTS Object to find in the graph.
 * @returns ArrayList of Triple objects.
 */
ArrayList getForWorkRange(CtsUrn urn){

	ArrayList workingArray = []
	ArrayList versionArray = []
	ArrayList uniquedArray = []
	CtsUrn tempUrn

	// See if there is anything mapped to this explicit range
	getOneOffCtsUrn(urn).each{ workingArray << it }

	// Get Versions
	versionArray = getVersionsForWork(urn)
	// Construct URN for each version & recurse
	versionArray.each{ versionItem ->
		tempUrn = new CtsUrn("${versionItem}${urn.passageComponent}")
		findAdjacent(tempUrn).each{
			workingArray << it
		}
	}

	uniquedArray = uniqueTriples(workingArray)

	return uniquedArray

}

/** Finds  data adjacent to a version-level containing (non-leaf-node) URN
 * We want anything indexed to the citation itself, all contained leaf-node citations,
 * and the same for any exemplars.
 * @param urn The URN to test.
 * @returns ArrayList of Triple objects.
 */
ArrayList getForVersionContainer(CtsUrn urn){
	ArrayList exemplarArray = []
		ArrayList replyArray = []
		ArrayList workingArray = []

		// Get Exemplars
		String versionUrnString = urn.getUrnWithoutPassage()
		String passageString = urn.getPassageNode()

		CtsUrn bareVersionUrn = new CtsUrn(versionUrnString)
		exemplarArray = getExemplarsForVersion(bareVersionUrn)

		// Get adjacents for this version

		String replyText = ""
		String containerQuery = QueryBuilder.getQueryVersionLevelContaining(urn.encodeSubref())
		String reply = sparql.getSparqlReply("application/json", containerQuery)

		JsonSlurper slurper = new groovy.json.JsonSlurper()
		def parsedReply = slurper.parseText(reply)

		parsedJsonToTriples(parsedReply).each { workingArray << it }

	// Get adjacents for exemplars

	exemplarArray.each{ exemInstance ->
		CtsUrn exemplarUrn = new CtsUrn("${exemInstance}${passageString}")
			replyText = ""
			containerQuery = QueryBuilder.getQueryVersionLevelContaining(exemplarUrn.encodeSubref())
			reply = sparql.getSparqlReply("application/json", containerQuery)

			slurper = new groovy.json.JsonSlurper()
			parsedReply = slurper.parseText(reply)

			parsedJsonToTriples(parsedReply).each { workingArray << it }
	}

	// Assemble

	replyArray = uniqueTriples(workingArray)
		return replyArray

}

/** Find all nodes at one degree of
 * relation to the object identified by
 * a CTS leaf-node, version- or exemplar-level urn.
 * @param urn CTS Object to find in the graph.
 * @returns ArrayList of Triple objects.
 */
ArrayList getForVersionLeaf(CtsUrn urn){
	CtsUrn requestUrn
		ArrayList exemplarArray = []
		ArrayList replyArray = []
		ArrayList workingArray = []

		if (urn.hasSubref()){
			requestUrn = new CtsUrn(urn.reduceToNode())
		} else {
			requestUrn = urn
		}

// Get Exemplars
	String versionUrnString = requestUrn.getUrnWithoutPassage()
		String passageString = requestUrn.getPassageNode()

		CtsUrn bareVersionUrn = new CtsUrn(versionUrnString)
		exemplarArray = getExemplarsForVersion(bareVersionUrn)

		// Get adjacents for this version, minus any subref

		String replyText = ""
		String leafQuery = QueryBuilder.getSingleLeafNodeQuery(requestUrn.encodeSubref())
		String reply = sparql.getSparqlReply("application/json", leafQuery)
		JsonSlurper slurper = new groovy.json.JsonSlurper()
		def parsedReply = slurper.parseText(reply)
		workingArray = parsedJsonToTriples(parsedReply)

		// Get adjacents for this version, with subref

		replyText = ""
		leafQuery = QueryBuilder.getSingleLeafNodeQuery(requestUrn.encodeSubref())
		reply = sparql.getSparqlReply("application/json", leafQuery)
		slurper = new groovy.json.JsonSlurper()
		parsedReply = slurper.parseText(reply)
		if (parsedReply.results.size() > 0 ){
			parsedJsonToTriples(parsedReply).each { workingArray << it }
		}


	// Get adjacents for exemplars

	exemplarArray.each{ exemInstance ->
		CtsUrn exemplarUrn = new CtsUrn("${exemInstance}${passageString}")

			getForExemplarLeaf(exemplarUrn).each { workingArray << it }
	}

		replyArray = uniqueTriples(workingArray)
		return replyArray

}

/** Find all nodes at one degree of
 * relation to the object identified by
 * a CTS version-level urn with a range citation.
 * @param urn CTS Object to find in the graph.
 * @returns ArrayList of Triple objects.
 */
ArrayList getForVersionRange(CtsUrn urn){

	ArrayList workingArray = []
	ArrayList exemplarArray = []
	ArrayList uniquedArray = []
	ArrayList leafArray = []
	CtsUrn tempUrn

	// See if there is anything mapped to this explicit range
	getOneOffCtsUrn(urn).each{ workingArray << it }
	println "Doing getForVersionRange ${urn}"


	// Get leaves for Version
	leafArray = ctsgraph.getUrnList(urn)
	leafArray.each{ lai ->
		findAdjacent(lai).each{
			workingArray << it
		}
	}

	// Get All Exemplars
	//     Here, we don't want a full CTS report for the Exemplars.
	//	   We really just want the URNs that belong to each version-leaf-node citation

	exemplarArray = getExemplarsForVersion(urn)
	exemplarArray.each{ exempItem ->
		leafArray.each{ lai ->
			getOneOffCtsUrn(new CtsUrn("${exempItem}${lai.getPassageNode()}")).each{
				workingArray << it
			}
		}
	}

	uniquedArray = uniqueTriples(workingArray)


	return uniquedArray

}

/** Find all nodes at one degree of
 * relation to the object identified by
 * a CTS non-leaf-node, exemplar-level urn.
 * @param urn CTS Object to find in the graph.
 * @returns ArrayList of Triple objects.
 */
ArrayList getForExemplarContainer(CtsUrn urn){

	ArrayList exemplarArray = []

	String replyText = ""
	String containerQuery = QueryBuilder.getSingleLeafNodeQuery(urn.encodeSubref())
	String reply = sparql.getSparqlReply("application/json", containerQuery)

	JsonSlurper slurper = new groovy.json.JsonSlurper()
	def parsedReply = slurper.parseText(reply)

	if (parsedReply.results.size() > 0 ){
		parsedJsonToTriples(parsedReply).each { exemplarArray << it }
	}


	return uniqueTriples(exemplarArray)

}



/** Find all nodes at one degree of
 * relation to the object identified by
 * a CTS leaf-node, exemplar-level urn.
 * @param urn CTS Object to find in the graph.
 * @returns ArrayList of Triple objects.
 */
ArrayList getForExemplarLeaf(CtsUrn urn){

	ArrayList exemplarArray = []

		String replyText = ""
		String containerQuery = QueryBuilder.getSingleLeafNodeQuery(urn.encodeSubref())
		String reply = sparql.getSparqlReply("application/json", containerQuery)

		JsonSlurper slurper = new groovy.json.JsonSlurper()
		def parsedReply = slurper.parseText(reply)

		if (parsedReply.results.size() > 0 ){
			parsedJsonToTriples(parsedReply).each { exemplarArray << it }
		}

	return uniqueTriples(exemplarArray)

}


/** Find all nodes at one degree of
 * relation to _all_ leaf-node elements in a
 * a range specified by an exemplar-level CTS leaf-node.
 * Also, incidentally, anything mapped to that particular range, itself.
 * @param urn CTS Object to find in the graph.
 * @returns ArrayList of Triple objects.
 */
ArrayList getForExemplarRange(CtsUrn urn){

	println "getForExemplarRange ${urn}"
	ArrayList workingArray = []
	ArrayList uniquedArray = []
	ArrayList leafArray = []
	CtsUrn tempUrn

	// See if there is anything mapped to this explicit range
	getOneOffCtsUrn(urn).each{ workingArray << it }

	// Get
	leafArray = ctsgraph.getUrnList(urn)
	println "leafArray:"
	println leafArray
	println "---------------------"
	leafArray.each{ lai ->
		findAdjacent(lai).each{
			workingArray << it
		}
	}
	uniquedArray = uniqueTriples(workingArray)

	return uniquedArray
}


/* -------------------------------------------------------------------------------- */
/* Methods for finding adjacents for CITE URNs
/* -------------------------------------------------------------------------------- */

/* -------------------------------------------------------------------------------- */
/* Misc. Utility Methods
/* -------------------------------------------------------------------------------- */


 /** Given a JSON reply from SPARQL
   * construct a ListArray of Triple objects,
   * including the work of sorting out object-types, and
   * catching labels on URI objects, and making them into Triples, too.
   * @param parsedReply Object, the parsed JSON text
   * @returns ArrayList of Triple objects.
   */
ArrayList parsedJsonToTriples(Object parsedReply){
	ArrayList replyArray = []

		URI tempSubject
		URI tempVerb
		Object tempObject
		String tempString = ""

		if (parsedReply?.results && (parsedReply?.results.bindings.size() > 0)){
			parsedReply.results.bindings.each{ jo ->
				tempString = URLDecoder.decode(jo.s.value,"UTF-8")
					tempSubject = new URI(URLEncoder.encode(tempString, "UTF-8")) // decode the URL encoding from Fuseki
					tempVerb = new URI(URLEncoder.encode(jo.v.value, "UTF-8")) // re-encode as URI

					switch (jo.o.type){
						case "uri":
							tempString = URLDecoder.decode(jo.o.value,"UTF-8") // decode the URL encoding from Fuseki
								tempObject = new URI(URLEncoder.encode(tempString, "UTF-8")) // Encode as URI
								break;
						case "literal":
							tempObject = jo.o.value
								break;
						case "typed-literal":
							tempObject = jo.o.value.toInteger()
								break;
					}

				Triple tempTriple = new Triple(tempSubject, tempVerb, tempObject)
					replyArray << tempTriple
					// We also want rdf:labels for all URI objects, to be nice
					if (jo.label){
						tempVerb = new URI("rdf:label")
							tempSubject = tempObject
							tempTriple = new Triple(tempSubject,tempVerb,jo.label?.value)
							replyArray << tempTriple
					}
				// We also want cts:hasSequence for all URI objects, to be nice
				if (jo.ctsSeq){
					tempVerb = new URI("http://www.homermultitext.org/cts/rdf/hasSequence")
						tempSubject = tempObject
						tempTriple = new Triple(tempSubject,tempVerb,jo.ctsSeq?.value)
						replyArray << tempTriple
				}
				// We also want olo:item sequencing for all URI objects, to be nice
				if (jo.objSeq){
					tempVerb = new URI("http://purl.org/ontology/olo/core#item")
						tempSubject = jo.v.value
						tempTriple = new Triple(tempSubject,tempVerb,jo.objSeq?.value)
						replyArray << tempTriple
				}
				// And we want triples with verbs and their labels
				if (jo.verbLabel){
					tempVerb = new URI("http://www.w3.org/1999/02/22-rdf-syntax-ns#label")
					tempSubject = new URI(jo.v.value)
						tempTriple = new Triple(tempSubject,tempVerb,jo.verbLabel?.value)
						replyArray << tempTriple
				}
				if (jo.ctsSeqLabel){
					tempVerb = new URI("http://www.w3.org/1999/02/22-rdf-syntax-ns#label")
					tempSubject = new URI("http://www.homermultitext.org/cts/rdf/hasSequence")
						tempTriple = new Triple(tempSubject,tempVerb,jo.ctsSeqLabel?.value)
						replyArray << tempTriple
				}
				if (jo.objSeqLabel){
					tempVerb = new URI("http://www.w3.org/1999/02/22-rdf-syntax-ns#label")
					tempSubject = new URI("http://purl.org/ontology/olo/core#item")
						tempTriple = new Triple(tempSubject,tempVerb,jo.objSeqLabel?.value)
						replyArray << tempTriple
				}
			}
		}

	return replyArray
}

/** Given an ArrayList of Triple objects
 * eliminate duplicates and return a new ArrayList
 * @param al ArrayList of Triple object
 * @returns ArrayList of Triple objects.
 */
ArrayList uniqueTriples(ArrayList al){
	def tripleComparator = [
		equals: { delegate.equals(it) },
		compare: { first, second ->
			first.toString() <=> second.toString()
		}
	] as Comparator
		def tsub = al.unique(tripleComparator)
		return tsub
}


}
