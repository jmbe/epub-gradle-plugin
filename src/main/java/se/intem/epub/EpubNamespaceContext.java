package se.intem.epub;

import java.util.Iterator;
import javax.xml.namespace.NamespaceContext;

class EpubNamespaceContext implements NamespaceContext {
  @Override
  public String getNamespaceURI(String prefix) {

    switch (prefix) {
      case "c":
        return "urn:oasis:names:tc:opendocument:xmlns:container";
      case "opf":
        return "http://www.idpf.org/2007/opf";
      default:
        return null;
    }
  }

  @Override
  public String getPrefix(String namespaceURI) {
    return null;
  }

  @Override
  public Iterator getPrefixes(String namespaceURI) {
    return null;
  }
}
