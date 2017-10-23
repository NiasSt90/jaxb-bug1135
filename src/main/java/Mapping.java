import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collection;


@XmlRootElement(name="root")
@XmlAccessorType(XmlAccessType.FIELD)
class Mapping{

	@XmlElementWrapper(name = "list")
	@XmlElement(name="element")
	Collection<A> list = new ArrayList<A>();

	A element1;

	A element2;

}