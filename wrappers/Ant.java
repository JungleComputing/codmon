import java.io.File;
import org.apache.tools.ant.ProjectHelper;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;

public class Ant{
	File buildFile;
	Project project;
	ProjectHelper projectHelper;
	String dir;

	public Ant(String dir){
		this.dir= dir;
		buildFile = new File(dir+"/build.xml");
		project = new Project();
		projectHelper = ProjectHelper.getProjectHelper();
	}

	public void init(){;
		project.setUserProperty("antFile",buildFile.getAbsolutePath());
                project.init();
                project.addReference("ant.projectHelper",projectHelper);
                projectHelper.parse(project,buildFile);
	}

	public void run(){
		this.project.executeTarget(this.project.getDefaultTarget());
	}

}
