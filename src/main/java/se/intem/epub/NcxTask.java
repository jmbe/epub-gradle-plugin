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
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.slf4j.LoggerFactory;

public class NcxTask extends DefaultTask {

  /** Logger for this class */
  private static final Logger log = (Logger) LoggerFactory.getLogger(NcxTask.class);

  private File sourceDirectory;

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

    String ncxName = "EPUB/toc.ncx";
    File ncx = new File(sourceDirectory, ncxName);

    log.debug("Generating ncx from source nav {} to {}", nav, ncx);

    try {

      Transformer transformer = createXslTransformer();

      StreamSource source = new StreamSource(nav);
      StreamResult result = new StreamResult(ncx);
      transformer.transform(source, result);

      log.lifecycle("Created " + ncxName);

    } catch (TransformerException e) {
      throw new GradleException("Failed to create ncx", e);
    }
  }

  private File findNavFile() {
    String navName = "EPUB/nav.xhtml";
    File nav = new File(sourceDirectory, navName);

    if (!nav.exists()) {
      throw new GradleException("Could not find nav file " + navName);
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
}
