package se.intem.epub;

import com.google.common.base.Strings;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class NcxTask extends DefaultTask {

  /** Logger for this class */
  private static final Logger log = (Logger) LoggerFactory.getLogger(NcxTask.class);
  private static final String DEFAULT_EPUB_NAV_XHTML = "EPUB/nav.xhtml";

  private File sourceDirectory;

  private String navFile;

  private String ncxFile = "EPUB/toc.ncx";

  private static XPathFactory xPathFactory = XPathFactory.newInstance();
  private static DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

  @InputDirectory
  public File getSourceDirectory() {
    return sourceDirectory;
  }

  public void setSourceDirectory(File sourceDirectory) {
    this.sourceDirectory = sourceDirectory;
  }

  @TaskAction
  public void generate() {
    log.lifecycle("Generating ncx...");
    File nav = findNavFile();

    if (!nav.exists()) {
      throw new GradleException("Could not find nav file " + nav);
    }

    File ncx = new File(sourceDirectory, this.ncxFile);

    log.debug("Generating ncx from source nav {} to {}", nav, ncx);

    try {

      Transformer transformer = createXslTransformer();

      StreamSource source = new StreamSource(nav);
      StreamResult result = new StreamResult(ncx);
      transformer.transform(source, result);

      log.lifecycle("Created " + this.ncxFile);

    } catch (TransformerException e) {
      throw new GradleException("Failed to create ncx", e);
    }
  }

  private File findNavFile() {

    if (!Strings.isNullOrEmpty(this.navFile)) {
      File nav = new File(sourceDirectory, this.navFile);
      log.lifecycle("Using configured nav document: {}", nav);
      return nav;
    }

    Optional<File> fromOpf = findNavFileFromPackageDocument();

    if (fromOpf.isPresent()) {
      log.lifecycle("Using nav document from package file: {}", fromOpf.get());
      return fromOpf.get();
    }

    File nav = new File(sourceDirectory, DEFAULT_EPUB_NAV_XHTML);
    log.lifecycle("Using default nav document: {}", nav);
    return nav;
  }

  private Optional<File> findNavFileFromPackageDocument() {
    Optional<String> packageDocumentPath = findPackageDocumentPath();

    if (packageDocumentPath.isPresent()) {
      String expr = "/opf:package/opf:manifest/opf:item[@properties='nav']/@href";
      String packageDocument = packageDocumentPath.get();
      Optional<String> href = evaluateXpathForFile(packageDocument, expr);

      if (href.isPresent()) {
        File packageDocumentDirectory = new File(sourceDirectory, packageDocument).getParentFile();
        File navFile = new File(packageDocumentDirectory, href.get());
        return Optional.of(navFile);
      }

    }

    return Optional.empty();

  }

  private Optional<String> findPackageDocumentPath() {
    String filename = "META-INF/container.xml";
    String expr = "/c:container/c:rootfiles/c:rootfile[1]/@full-path";

    return evaluateXpathForFile(filename, expr);
  }

  private Optional<String> evaluateXpathForFile(String filename, String expr) {
    try {

      File containerXml = new File(sourceDirectory, filename);

      if (!containerXml.exists()) {
        log.warn("Could not find file {}", filename);
        return Optional.empty();
      }

      DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
      Document document = documentBuilder.parse(containerXml);

      XPath xPath = xPathFactory.newXPath();
      xPath.setNamespaceContext(new EpubNamespaceContext());
      XPathExpression expression = xPath.compile(expr);

      String evaluated = (String) expression.evaluate(document, XPathConstants.STRING);

      return Optional.ofNullable(Strings.emptyToNull(evaluated));

    } catch (XPathExpressionException | ParserConfigurationException | IOException | SAXException e) {
      log.warn("Failed to parse " + filename, e);
    }

    return Optional.empty();
  }

  private Transformer createXslTransformer() throws TransformerConfigurationException {
    InputStream xsl = NcxTask.class.getResourceAsStream("/navdoc2ncx.xsl");
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    StreamSource xslSource = new StreamSource(xsl);
    Transformer transformer = transformerFactory.newTransformer(xslSource);
    transformer.setParameter("cwd", sourceDirectory.getAbsolutePath() + "/");
    return transformer;
  }

  public String getNavFile() {
    return navFile;
  }

  @Input
  public void setNavFile(String navFile) {
    this.navFile = navFile;
  }

  public String getNcxFile() {
    return ncxFile;
  }

  @Input
  public void setNcxFile(String ncxFile) {
    this.ncxFile = ncxFile;
  }
}
