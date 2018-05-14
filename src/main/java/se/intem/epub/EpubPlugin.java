package se.intem.epub;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EpubPlugin implements Plugin<Project> {

  /** Logger for this class */
  private static final Logger log = LoggerFactory.getLogger(EpubPlugin.class);

  @Override
  public void apply(Project project) {
    log.trace("Applying epub plugin for {}", project.getName());

    project.getTasks().create("epub", EpubTask.class, (EpubTask task) -> {
      log.debug("Configuring epub task for {}", project.getProjectDir());

      /* It is assumed that the whole directory is the book (i.e. no src directory) */
      task.setSourceDirectory(project.getProjectDir());

      /* Output to root project, since writing to subdirectories would trigger build again */
      task.setOutputDirectory(project.getRootProject().getBuildDir());

      task.setEpubName(project.getName());
    });

  }
}
