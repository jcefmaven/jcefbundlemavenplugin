package org.cef.jcefbundlemavenplugin;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilder;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilderException;
import org.apache.maven.shared.dependency.graph.DependencyNode;
import org.apache.maven.shared.transfer.artifact.DefaultArtifactCoordinate;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResolver;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResolverException;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResult;
import org.cef.jcefbundlemavenplugin.exporter.ExportType;
import org.cef.jcefbundlemavenplugin.exporter.Exporter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * This class represents bundle goals for all platforms.
 *
 * @author Fritz Windisch
 */
public abstract class BundleNativesMojo extends AbstractMojo {
    private final NativeType nativeType;
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;
    @Component
    private ArtifactResolver artifactResolver;
    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    private MavenSession session;
    @Component(hint = "default")
    private DependencyGraphBuilder dependencyGraphBuilder;
    @Parameter(defaultValue = "${project.build.directory}", readonly = true, required = true)
    private String projectBuildDir;

    /**
     * The export type to use. You can either export your project as "plain" (simple directory structure),
     * "zip" or "targz". We recommend using zip for Windows (users can extract it easier) and tar.gz for MacOSX,
     * as files within the bundle require to be marked as executable. The Linux version marks files as executable
     * automatically, so the type is not important.
     */
    @Parameter(defaultValue = "plain")
    private String exportType;

    public BundleNativesMojo(NativeType nativeType) {
        this.nativeType = nativeType;
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("Building bundle for " + nativeType.name());
        //Determine native version
        String nativeVersion = getNativeVersion(findJCefApiArtifact(fetchDependencies(project)));
        //Fetch native version if not present
        File natives = resolveArtifact("org.cef", "jcef-natives-" + nativeType.getNativeName(), nativeVersion);
        //Invoke bundle build
        try {
            this.bundle(natives, new File(projectBuildDir, "jcef-bundle-" + nativeType.getNativeName()));
        } catch (IOException e) {
            throw new MojoExecutionException("An error occurred while creating the bundle", e);
        }
    }

    /**
     * This method generates a platform dependent bundle in "targetDir", using the natives provided in file "natives".
     * Implementations differ depending on the target platform.
     *
     * @param natives   The native maven dependency file
     * @param targetDir The directory to create the bundle in
     * @throws IOException            When a file operation failed
     * @throws MojoFailureException   When a configuration error is detected
     * @throws MojoExecutionException When an unforeseen exception occurs
     */
    public abstract void bundle(File natives, File targetDir) throws IOException, MojoFailureException, MojoExecutionException;

    /**
     * Generates a request used to fetch meta information on the target project.
     *
     * @param project The target project
     * @return The request
     */
    public ProjectBuildingRequest buildingRequest(MavenProject project) {
        final ProjectBuildingRequest buildingRequest = new DefaultProjectBuildingRequest(session.getProjectBuildingRequest());
        buildingRequest.setRemoteRepositories(new ArrayList<>(project.getRemoteArtifactRepositories()));
        buildingRequest.setProject(project);
        return buildingRequest;
    }

    /**
     * Retrieves a list of dependencies without downloading them.
     *
     * @param project The project to get the dependencies for
     * @return A list of all dependencies
     * @throws MojoExecutionException If the action can not be completed
     */
    public List<Artifact> fetchDependencies(MavenProject project) throws MojoExecutionException {
        DependencyNode node;
        try {
            node = dependencyGraphBuilder
                    .buildDependencyGraph(buildingRequest(project), a -> true);
        } catch (DependencyGraphBuilderException e) {
            throw new MojoExecutionException("Failed to build dependency tree", e);
        }
        Queue<DependencyNode> queue = new ConcurrentLinkedQueue<>();
        queue.add(node);
        List<Artifact> artifacts = new LinkedList<>();
        while (!queue.isEmpty()) {
            node = queue.poll();
            artifacts.add(node.getArtifact());
            queue.addAll(node.getChildren());
        }
        return artifacts;
    }

    /**
     * Locates the jcef-api artifact in a list of artifacts.
     *
     * @param artifacts The list to search in
     * @return The jcef-api artifact, when present
     * @throws MojoFailureException If the artifact list did not contain jcef-api
     */
    public Artifact findJCefApiArtifact(List<Artifact> artifacts) throws MojoFailureException {
        for (Artifact artifact : artifacts) {
            if (artifact.getGroupId().equals("org.cef") && artifact.getArtifactId().equals("jcef-api")) {
                return artifact;
            }
        }
        throw new MojoFailureException("Your project does not contain a org.cef:jcef-api artifact. Please include it to deploy with natives!");
    }

    /**
     * Determines the native version of a jcef artifact
     *
     * @param artifact The jcef artifact
     * @return The native version
     */
    public String getNativeVersion(Artifact artifact) {
        return artifact.getVersion().split("-build-")[0];
    }

    /**
     * Resolves (downloads) a required artifact and returns the artifact's location on disk
     *
     * @param groupId    The groupId of the required artifact
     * @param artifactId The artifactId of the required artifact
     * @param version    The version of the required artifact
     * @return Location on disk, where artifact is stored
     * @throws MojoExecutionException If the artifact could not be resolved
     */
    public File resolveArtifact(String groupId, String artifactId, String version) throws MojoExecutionException {
        DefaultArtifactCoordinate coordinate = new DefaultArtifactCoordinate();
        coordinate.setGroupId(groupId);
        coordinate.setArtifactId(artifactId);
        coordinate.setVersion(version);
        try {
            ArtifactResult result = artifactResolver.resolveArtifact(buildingRequest(project), coordinate);
            Artifact artifact = result.getArtifact();
            return artifact.getFile();
        } catch (ArtifactResolverException e) {
            throw new MojoExecutionException("Could not resolve " + groupId + ":" + artifactId + ":" + version, e);
        }
    }

    public NativeType getNativeType() {
        return nativeType;
    }

    public String getProjectBuildDir() {
        return projectBuildDir;
    }

    public Exporter getExporter() throws MojoFailureException {
        return ExportType.getExporterByName(exportType);
    }
}
