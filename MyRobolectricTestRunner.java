import org.apache.maven.artifact.ant.DependenciesTask;
import org.apache.maven.artifact.ant.RemoteRepository;
import org.apache.maven.model.Dependency;
import org.apache.tools.ant.Project;
import org.junit.runners.model.InitializationError;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.internal.dependency.DependencyJar;
import org.robolectric.internal.dependency.DependencyResolver;
import org.robolectric.util.Util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;

/**
 * @see <a href="http://stackoverflow.com/questions/39358697/how-to-override-the-robolectric-runtime-dependency-repository-url">How to override the Robolectric runtime dependency repository URL?</a>
 */

public final class MyRobolectricTestRunner extends RobolectricTestRunner {
    public MyRobolectricTestRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
    }

    @Override
    protected DependencyResolver getJarResolver() {
        return new CustomDependencyResolver();
    }

    static final class CustomDependencyResolver implements DependencyResolver {
        private final Project project = new Project();

        @Override
        public URL[] getLocalArtifactUrls(DependencyJar... dependencies) {
            DependenciesTask dependenciesTask = new DependenciesTask();
            RemoteRepository repository = new RemoteRepository();
            repository.setUrl("http://maven.aliyun.com/nexus/content/groups/public");
            repository.setId("my-nexus");
            dependenciesTask.addConfiguredRemoteRepository(repository);
            dependenciesTask.setProject(project);
            for (DependencyJar dependencyJar : dependencies) {
                Dependency dependency = new Dependency();
                dependency.setArtifactId(dependencyJar.getArtifactId());
                dependency.setGroupId(dependencyJar.getGroupId());
                dependency.setType(dependencyJar.getType());
                dependency.setVersion(dependencyJar.getVersion());
                if (dependencyJar.getClassifier() != null) {
                    dependency.setClassifier(dependencyJar.getClassifier());
                }
                dependenciesTask.addDependency(dependency);
            }
            dependenciesTask.execute();

            @SuppressWarnings("unchecked")
            Hashtable<String, String> artifacts = project.getProperties();
            URL[] urls = new URL[dependencies.length];
            for (int i = 0; i < urls.length; i++) {
                try {
                    urls[i] = Util.url(artifacts.get(key(dependencies[i])));
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            }
            return urls;
        }

        @Override
        public URL getLocalArtifactUrl(DependencyJar dependency) {
            URL[] urls = getLocalArtifactUrls(dependency);
            if (urls.length > 0) {
                return urls[0];
            }
            return null;
        }

        private String key(DependencyJar dependency) {
            String key =
                    dependency.getGroupId() + ":" + dependency.getArtifactId() + ":" + dependency.getType();
            if (dependency.getClassifier() != null) {
                key += ":" + dependency.getClassifier();
            }
            return key;
        }
    }
}
