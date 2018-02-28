package se.intem.epub;

import com.adobe.epubcheck.api.EpubCheck;
import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.util.Archive;
import com.adobe.epubcheck.util.DefaultReportImpl;
import java.io.File;
import org.gradle.api.DefaultTask;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.slf4j.LoggerFactory;

public class EpubTask extends DefaultTask {

  /** Logger for this class */
  private static final Logger log = (Logger) LoggerFactory.getLogger(EpubTask.class);

  private File sourceDirectory;
  private File outputDirectory = new File("target");
  private boolean validate = true;

  @InputDirectory
  public File getSourceDirectory() {
    return sourceDirectory;
  }

  public void setSourceDirectory(File sourceDirectory) {
    this.sourceDirectory = sourceDirectory;
  }

  @OutputDirectory
  public File getOutputDirectory() {
    return outputDirectory;
  }

  public void setOutputDirectory(File outputDirectory) {
    this.outputDirectory = outputDirectory;
  }

  @Input
  public boolean isValidate() {
    return validate;
  }

  public void setValidate(boolean validate) {
    this.validate = validate;
  }

  @TaskAction
  void generate() {
    log.lifecycle("Assembling epub for {}...", sourceDirectory);

    Archive epub = new Archive(sourceDirectory.getAbsolutePath(), false);
    epub.createArchive();
    String name = epub.getEpubName();

    if (validate) {
      Report report = new DefaultReportImpl(epub.getEpubName());
      EpubCheck check = new EpubCheck(epub.getEpubFile(), report);
      int validationResult = check.doValidate();
      switch (validationResult) {
        case 0:
          log.info("Validation OK");
          break;
        case 1:
          log.warn("Validation detected warnings");
          break;
        default:
          log.warn("Validation detected errors");
          break;
      }

    }

    File target = new File(outputDirectory, name);
    epub.getEpubFile().renameTo(target);
    log.lifecycle("Created {}", target);
  }
}
