package edu.holycross.shot.sparqlimg

import edu.harvard.chs.cite.CtsUrn
import edu.harvard.chs.cite.Cite2Urn

import static org.junit.Assert.*
import org.junit.Test

class TestSetup extends GroovyTestCase {
    // use default fuseki settings to test
    String serverUrl = "http://localhost:3030/ds/"
    String iipsrv = "http://beta.hpcc.uh.edu/fcgi-bin/iipsrv.fcgi"

    void testTest() {
		String urnString1 = "urn:cite2:hmt:test:1"
		String urnString2 = "urn:cite2:hmt:test:1"
		String urnString3 = "urn:cts:greekLit:tlgl0012.tlg001:1.1"

		Cite2Urn urn1 = new Cite2Urn(urnString1)
		Cite2Urn urn2 = new Cite2Urn(urnString2)
		CtsUrn urn3 = new CtsUrn(urnString3)

		assert urn1.toString() == urnString1
		assert urn2.toString() == urnString2
		assert urn3.toString() == urnString3
    }


}
