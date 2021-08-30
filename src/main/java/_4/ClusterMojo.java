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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Mojo( name = "cluster", requiresDependencyResolution = ResolutionScope.TEST )
@Execute(phase = LifecyclePhase.COMPILE)
public class ClusterMojo extends AbstractMojo {

    @Parameter( defaultValue = "${project}" )
    private MavenProject project;

    @Parameter( defaultValue = "${project.groupId}", property = "basePackages", required = true )
    private String[] basePackages;

    @Parameter( defaultValue = "cluster.dot", property = "outputFile", required = true )
    private String outputFile;

    @Parameter( defaultValue = "true", property = "dryRun", required = true )
    private boolean dryRun;

    public void execute() throws MojoFailureException {
        try {
            DotMojo dot = new DotMojo();
            dot.setProject(project);
            dot.setBasePackages(basePackages);
            dot.setOutputFile(outputFile);
            dot.execute();
            DependencyMatrix deps = DependencyMatrix.fromDotFile(outputFile);
            DistanceMatrix dists = DistanceMatrix.fromDependencyMatrix(deps);
            Hclust hClust = Hclust.fromDistanceMatrix(dists);
            FileCluster fileClust = FileCluster.fromHclust(hClust);
            fileClust.writeFiles(dryRun);
            dot.removeDotFile();
        } catch (Exception e) {
            throw new MojoFailureException("Error in clustering", e);
        }
    }
}
