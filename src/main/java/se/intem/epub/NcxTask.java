package se.intem.epub;

import java.io.File;
import java.io.InputStream;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.slf4j.LoggerFactory;

public class NcxTask extends DefaultTask {

  /** Logger for this class */
  private static final Logger log = (Logger) LoggerFactory.getLogger(NcxTask.class);

  private File sourceDirectory;

  private String navFile = "EPUB/nav.xhtml";

  private String ncxFile = "EPUB/toc.ncx";

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
    File nav = new File(sourceDirectory, this.navFile);

    if (!nav.exists()) {
      throw new GradleException("Could not find nav file " + this.navFile);
    }
    return nav;
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
