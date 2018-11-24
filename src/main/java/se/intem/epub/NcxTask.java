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
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class NcxTask extends DefaultTask {

  /** Logger for this class */
  private static final Logger log = (Logger) LoggerFactory.getLogger(NcxTask.class);
  private static final String DEFAULT_EPUB_NAV_XHTML = "EPUB/nav.xhtml";
  public static final String DEFAULT_EPUB_TOC_NCX = "EPUB/toc.ncx";

  private File sourceDirectory;

  private File navFile;

  private File ncxFile;

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

    File ncx = findNcxFile();

    log.debug("Generating ncx from source nav {} to {}", nav, ncx);

    try {

      Transformer transformer = createXslTransformer();

      StreamSource source = new StreamSource(nav);
      StreamResult result = new StreamResult(ncx);
      transformer.transform(source, result);

      log.lifecycle("Created " + ncx);

    } catch (TransformerException e) {
      throw new GradleException("Failed to create ncx", e);
    }
  }

  private File findNcxFile() {

    if (this.ncxFile != null) {
      log.lifecycle("Using configured ncx file: {}", this.ncxFile);
      return this.ncxFile;
    }

    Optional<File> fromOpf = findNcxFileFromPackageDocument();

    if (fromOpf.isPresent()) {
      log.lifecycle("Using ncx file from package file: {}", fromOpf.get());
      return fromOpf.get();
    }

    File ncx = getProject().file(DEFAULT_EPUB_TOC_NCX);
    log.lifecycle("Using default ncx file: {}", ncx);
    return ncx;
  }

  private File findNavFile() {

    if (this.navFile != null) {
      log.lifecycle("Using configured nav document: {}", this.navFile);
      return this.navFile;
    }

    Optional<File> fromOpf = findNavFileFromPackageDocument();

    if (fromOpf.isPresent()) {
      log.lifecycle("Using nav document from package file: {}", fromOpf.get());
      return fromOpf.get();
    }

    File nav = getProject().file(DEFAULT_EPUB_NAV_XHTML);
    log.lifecycle("Using default nav document: {}", nav);
    return nav;
  }

  private Optional<File> findNavFileFromPackageDocument() {
    String expr = "/opf:package/opf:manifest/opf:item[@properties='nav']/@href";
    return findPathInPackageDocument(expr);
  }

  private Optional<File> findNcxFileFromPackageDocument() {
    String expr = "/opf:package/opf:manifest/opf:item[@media-type='application/x-dtbncx+xml']/@href";
    return findPathInPackageDocument(expr);
  }

  private Optional<File> findPathInPackageDocument(String xpathExpression) {
    Optional<String> packageDocumentPath = findPackageDocumentPath();

    if (packageDocumentPath.isPresent()) {

      String packageDocument = packageDocumentPath.get();
      Optional<String> href = evaluateXpathForFile(packageDocument, xpathExpression);

      if (href.isPresent()) {
        File packageDocumentDirectory = getProject().file(packageDocument).getParentFile();
        File navFile = new File(packageDocumentDirectory, href.get());
        return Optional.of(navFile);
      }

    }

    return Optional.empty();
  }

  private Optional<String> findPackageDocumentPath() {
    String filename = "META-INF/container.xml".replace("/", File.separator);
    String expr = "/c:container/c:rootfiles/c:rootfile[1]/@full-path";

    return evaluateXpathForFile(filename, expr);
  }

  private Optional<String> evaluateXpathForFile(String filename, String expr) {
    try {

      File containerXml = getProject().file(filename);

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

    String cwd = sourceDirectory.getAbsolutePath() + File.separator;
    // log.lifecycle("Using cwd {}", cwd);
    transformer.setParameter("cwd", cwd);
    return transformer;
  }

  @org.gradle.api.tasks.Optional
  @InputFile
  public File getNavFile() {
    return navFile;
  }

  public void setNavFile(File navFile) {
    this.navFile = navFile;
  }

  @org.gradle.api.tasks.Optional
  @OutputFile
  public File getNcxFile() {
    return ncxFile;
  }

  public void setNcxFile(File ncxFile) {
    this.ncxFile = ncxFile;
  }
}
