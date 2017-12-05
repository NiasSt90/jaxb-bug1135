import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.junit.Test;
import org.xmlunit.matchers.CompareMatcher;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Method;


public class DemoTest {

	private String determineBuildInfo(JAXBContext context) {
		try {
			Method getBuildId = context.getClass().getMethod("getBuildId");
			Object res = getBuildId.invoke(context);
			return (String) res;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return "UNKNOWN";
	}

	/**
	 * xsi:type information is lost. marshaled <element>B</element> instead of <element xsi:type=...>B</element>
	 */
	@Test
	public void testMarshal() throws Exception {
		JAXBContext jc = JAXBContext.newInstance(Mapping.class);
		System.out.println("****** VALIDATE marshal with JAXB-BuildId " + determineBuildInfo(jc) + " ******");
		Marshaller marshaller = jc.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		StringWriter resultWriter = new StringWriter();

		Mapping mapping = new Mapping();
		mapping.element1 = new B("B1");
		mapping.element2 = new C("C1");
		marshaller.marshal(mapping, resultWriter);

		String expectedXml1 = "<root>\n"
									 + "		<list/>\n"
									 + "    <element1 xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"ClassType1\">B1</element1>\n"
									 + "    <element2 xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"ClassType2\">C1</element2>\n"
									 + "</root>";
		MatcherAssert.assertThat(resultWriter.toString(), CompareMatcher.isIdenticalTo(expectedXml1).ignoreWhitespace());


		resultWriter = new StringWriter();
		mapping.list.add(new B("B"));
		mapping.list.add(new C("C"));
		String expectedXml2 = "<root>\n"
									+ "    <list>\n"
									+ "        <element xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"ClassType1\">B</element>\n"
									+ "        <element xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"ClassType2\">C</element>\n"
									+ "    </list>\n"
									+ "    <element1 xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"ClassType1\">B1</element1>\n"
									+ "    <element2 xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"ClassType2\">C1</element2>\n"
									+ "</root>";

		marshaller.marshal(mapping, resultWriter);
		MatcherAssert.assertThat(resultWriter.toString(), CompareMatcher.isIdenticalTo(expectedXml2).ignoreWhitespace());
	}

	@Test
	public void testUnmarshal() throws Exception {

		JAXBContext jc = JAXBContext.newInstance(Mapping.class);
		System.out.println("****** VALIDATE Unmarshal with JAXB-BuildId " + determineBuildInfo(jc) + " ******");
		Unmarshaller unmarshaller = jc.createUnmarshaller();

		//works without list..
		String sourceXml1 = "<root>\n"
									+ "    <element1 xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"ClassType1\">B1</element1>\n"
									+ "    <element2 xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"ClassType2\">C1</element2>\n"
									+ "</root>";
		JAXBElement<Mapping> element = unmarshaller.unmarshal(new StreamSource(new StringReader(sourceXml1)), Mapping.class);
		Assert.assertNotNull(element.getValue());
		Assert.assertEquals(B.class, element.getValue().element1.getClass());
		Assert.assertEquals(C.class, element.getValue().element2.getClass());

		//don't work -> try to instantiate the abstract class
		String sourceXml2 = "<root>\n"
									+ "    <list>\n"
									+ "        <element xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"ClassType1\">B</element>\n"
									+ "        <element xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"ClassType2\">C</element>\n"
									+ "    </list>\n"
									+ "    <element1 xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"ClassType1\">B1</element1>\n"
									+ "    <element2 xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"ClassType2\">C1</element2>\n"
									+ "</root>";

		try {
			element = unmarshaller.unmarshal(new StreamSource(new StringReader(sourceXml2)), Mapping.class);
			Assert.assertEquals(2, element.getValue().list.size());
		}
		catch (Throwable e) {
			Assert.fail();
		}
	}


}
