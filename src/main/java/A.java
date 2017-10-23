import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlValue;


@XmlSeeAlso({B.class, C.class})
abstract class A {

	//marshal/unmarshal of list elements containing elements of type = A will work without @XmlValue annotation...
	@XmlValue
	protected String value;

}
