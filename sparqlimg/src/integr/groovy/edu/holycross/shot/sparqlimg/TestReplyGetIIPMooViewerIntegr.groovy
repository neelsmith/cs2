package edu.holycross.shot.sparqlimg

import static org.junit.Assert.*
import org.junit.Test
import org.custommonkey.xmlunit.*

import edu.holycross.shot.sparqlimg.CiteImage
import edu.harvard.chs.cite.Cite2Urn
import edu.harvard.chs.cite.CtsUrn
import edu.holycross.shot.prestochango.*


class TestReplyGetIIPMooViewerIntegr extends GroovyTestCase {

  String baseUrl = "http://localhost:8080/fuseki/img/query"
  String iipserv = "http://beta.hpcc.uh.edu/fcgi-bin/iipsrv.fcgi"
	String serviceUrl = "http://localhost:8080/sparqlimg/api?"


	@Test
	void testGetIIPMooViewerUnversioned(){
    // set up XMLUnit
		XMLUnit.setNormalizeWhitespace(true)
		//XMLUnit.setIgnoreWhitespace(true)

		Cite2Urn urn = new Cite2Urn("urn:cite2:hmt:vaimg:VA327RN_0497")
		Sparql sparql = new Sparql(baseUrl)
		CiteImage cimg = new CiteImage(sparql,iipserv,serviceUrl)
		String replyString = cimg.getIIPMooViewerReply(urn)

    String expectedXml = """
<GetIIPMooViewer  xmlns='http://chs.harvard.edu/xmlns/citeimg'>
<request>
<urn>urn:cite2:hmt:vaimg:VA327RN_0497</urn>
<resolvedUrn>urn:cite2:hmt:vaimg.v1:VA327RN_0497</resolvedUrn>
<baseUrn>urn:cite2:hmt:vaimg.v1:VA327RN_0497</baseUrn>
<roi>null</roi>
</request>
<reply><serverUrl val='http://beta.hpcc.uh.edu/fcgi-bin/iipsrv.fcgi'/>
<imgPath val='/project/homer/pyramidal/VenA/VA327RN_0497.tif'/>
<roi val='null'/>
<label>Venetus A: Marcianus Graecus Z. 454 (= 822), folio 327, recto. This image was derived from an original ©2007, Biblioteca Nazionale Marciana, Venezie, Italia. The derivative image is ©2010, Center for Hellenic Studies. Original and derivative are licensed under the Creative Commons Attribution-Noncommercial-Share Alike 3.0 License. The CHS/Marciana Imaging Project was directed by David Jacobs of the British Library.</label>
</reply>
</GetIIPMooViewer>
"""

		System.err.println(replyString)
		System.err.println("<----replyString | expectedXML ----->")
		System.err.println(expectedXml)

		  Diff xmlDiff = new Diff(expectedXml, replyString)
		  assert xmlDiff.identical()

	}

	@Test
	void testGetIIPMooViewerVersioned(){
    // set up XMLUnit
		XMLUnit.setNormalizeWhitespace(true)
		//XMLUnit.setIgnoreWhitespace(true)

		Cite2Urn urn = new Cite2Urn("urn:cite2:hmt:vaimg.v1:VA327RN_0497")
		Sparql sparql = new Sparql(baseUrl)
		CiteImage cimg = new CiteImage(sparql,iipserv,serviceUrl)
		String replyString = cimg.getIIPMooViewerReply(urn)

    String expectedXml = """
<GetIIPMooViewer  xmlns='http://chs.harvard.edu/xmlns/citeimg'>
<request>
<urn>urn:cite2:hmt:vaimg.v1:VA327RN_0497</urn>
<resolvedUrn>urn:cite2:hmt:vaimg.v1:VA327RN_0497</resolvedUrn>
<baseUrn>urn:cite2:hmt:vaimg.v1:VA327RN_0497</baseUrn>
<roi>null</roi>
</request>
<reply><serverUrl val='http://beta.hpcc.uh.edu/fcgi-bin/iipsrv.fcgi'/>
<imgPath val='/project/homer/pyramidal/VenA/VA327RN_0497.tif'/>
<roi val='null'/>
<label>Venetus A: Marcianus Graecus Z. 454 (= 822), folio 327, recto. This image was derived from an original ©2007, Biblioteca Nazionale Marciana, Venezie, Italia. The derivative image is ©2010, Center for Hellenic Studies. Original and derivative are licensed under the Creative Commons Attribution-Noncommercial-Share Alike 3.0 License. The CHS/Marciana Imaging Project was directed by David Jacobs of the British Library.</label>
</reply>
</GetIIPMooViewer>
"""

		System.err.println(replyString)
		System.err.println("<----replyString | expectedXML ----->")
		System.err.println(expectedXml)

		  Diff xmlDiff = new Diff(expectedXml, replyString)
		  assert xmlDiff.identical()

	}

	@Test
	void testGetIIPMooViewerVersionedROI(){
    // set up XMLUnit
		XMLUnit.setNormalizeWhitespace(true)
		//XMLUnit.setIgnoreWhitespace(true)

		Cite2Urn urn = new Cite2Urn("urn:cite2:hmt:vaimg.v1:VA327RN_0497@0.25,0.25,0.25,0.25")
		Sparql sparql = new Sparql(baseUrl)
		CiteImage cimg = new CiteImage(sparql,iipserv,serviceUrl)
		String replyString = cimg.getIIPMooViewerReply(urn)

    String expectedXml = """
<GetIIPMooViewer  xmlns='http://chs.harvard.edu/xmlns/citeimg'>
<request>
<urn>urn:cite2:hmt:vaimg.v1:VA327RN_0497@0.25,0.25,0.25,0.25</urn>
<resolvedUrn>urn:cite2:hmt:vaimg.v1:VA327RN_0497@0.25,0.25,0.25,0.25</resolvedUrn>
<baseUrn>urn:cite2:hmt:vaimg.v1:VA327RN_0497</baseUrn>
<roi>0.25,0.25,0.25,0.25</roi>
</request>
<reply><serverUrl val='http://beta.hpcc.uh.edu/fcgi-bin/iipsrv.fcgi'/>
<imgPath val='/project/homer/pyramidal/VenA/VA327RN_0497.tif'/>
<roi val='0.25,0.25,0.25,0.25'/>
<label>Venetus A: Marcianus Graecus Z. 454 (= 822), folio 327, recto. This image was derived from an original ©2007, Biblioteca Nazionale Marciana, Venezie, Italia. The derivative image is ©2010, Center for Hellenic Studies. Original and derivative are licensed under the Creative Commons Attribution-Noncommercial-Share Alike 3.0 License. The CHS/Marciana Imaging Project was directed by David Jacobs of the British Library.</label>
</reply>
</GetIIPMooViewer>
"""

		System.err.println(replyString)
		System.err.println("<----replyString | expectedXML ----->")
		System.err.println(expectedXml)

		  Diff xmlDiff = new Diff(expectedXml, replyString)
		  assert xmlDiff.identical()

	}

}
