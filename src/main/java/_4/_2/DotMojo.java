package io.github.patrickdoc;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;

import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Mojo( name = "dot", requiresDependencyResolution = ResolutionScope.TEST )
@Execute(phase = LifecyclePhase.COMPILE)
public class DotMojo extends AbstractMojo {

    @Parameter( defaultValue = "${project}" )
    private MavenProject project;

    @Parameter( defaultValue = "${project.groupId}", property = "basePackages", required = true )
    private String[] basePackages;

    @Parameter( defaultValue = "cluster.dot", property = "outputFile", required = true )
    private String outputFile;

    public void execute() throws MojoFailureException {
        try {
            ClassGraph graph = new ClassGraph();
            graph.enableAllInfo();
            graph.acceptPackages(basePackages);
            graph.enableInterClassDependencies();
            graph.overrideClasspath(project.getRuntimeClasspathElements().toArray(new String[0]));

            ScanResult result = graph.scan();
            ClassInfoList infoList = result.getAllClasses()
                .filter(classInfo -> !classInfo.isInnerClass());

            String graphDot = infoList.generateGraphVizDotFileFromInterClassDependencies();

            Path path = Paths.get(outputFile);
            byte[] strToBytes = graphDot.getBytes();

            Files.write(path, strToBytes);
        } catch (Exception e) {
            throw new MojoFailureException("Error generating dot file", e);
        }
    }

    public void removeDotFile() throws IOException {
        Files.delete(Paths.get(outputFile));
    }

    public void setProject(MavenProject project) {
        this.project = project;
    }

    public void setBasePackages(String[] basePackages) {
        this.basePackages = basePackages;
    }

    public void setOutputFile(String outputFile) {
        this.outputFile = outputFile;
    }
}
