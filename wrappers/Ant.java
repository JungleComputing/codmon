import java.io.File;
import org.apache.tools.ant.ProjectHelper;
import org.apache.tools.ant.Project;

public class Ant{
	File buildFile;
	Project project;
	ProjectHelper projectHelper;

	public Ant(){
		buildFile = new File("build.xml");
		project = new Project();
		project.setUserProperty("antFile",buildFile.getAbsolutePath());
		project.init();

		projectHelper =ProjectHelper.getProjectHelper();
		project.addReference("ant.projectHelper",projectHelper);
		projectHelper.parse(project,buildFile);
	}

	public void run(){
		this.executeTarget(this.getDefaultTarget());
	}

}
