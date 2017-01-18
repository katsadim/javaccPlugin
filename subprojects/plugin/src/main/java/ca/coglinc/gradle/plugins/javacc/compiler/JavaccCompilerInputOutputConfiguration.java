package ca.coglinc.gradle.plugins.javacc.compiler;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.gradle.api.file.FileTree;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.TaskCollection;
import org.gradle.api.tasks.compile.JavaCompile;

public class JavaccCompilerInputOutputConfiguration implements CompilerInputOutputConfiguration {
    private final File inputDirectory;
    private final File outputDirectory;
    private final FileTree source;
    private final Set<JavaCompile> javaCompileTasks;

    public JavaccCompilerInputOutputConfiguration(File inputDirectory, File outputDirectory, FileTree source, TaskCollection<JavaCompile> javaCompileTasks) {
        this.inputDirectory = inputDirectory;
        this.outputDirectory = outputDirectory;
        this.source = source;
        this.javaCompileTasks = new HashSet<JavaCompile>();

        if (!CollectionUtils.isEmpty(javaCompileTasks)) {
            this.javaCompileTasks.addAll(javaCompileTasks);
        }
    }

    @Override
    public File getOutputDirectory() {
        return outputDirectory;
    }

    @Override
    public File getInputDirectory() {
        return inputDirectory;
    }

    @Override
    public File getTempOutputDirectory() {
        return new File(getOutputDirectory(), "tmp");
    }

    @Override
    public FileTree getSource() {
        return source;
    }

    @Override
    public FileTree getCompleteSourceTree() {
        FileTree javaccTaskSourceTree = getSource();
        FileTree javaTasksSourceTree = getJavaSourceTree();
        FileTree completeSourceTree;

        if (javaTasksSourceTree == null) {
            completeSourceTree = javaccTaskSourceTree;
        } else {
            completeSourceTree = javaccTaskSourceTree.plus(javaTasksSourceTree);
        }

        return excludeOutputDirectory(completeSourceTree);
    }

    @Override
    public FileTree getJavaSourceTree() {
        FileTree javaSourceTree = null;

        for (JavaCompile task : javaCompileTasks) {
            if (javaSourceTree == null) {
                javaSourceTree = task.getSource();
            } else {
                javaSourceTree = javaSourceTree.plus(task.getSource());
            }
        }

        return excludeOutputDirectory(javaSourceTree);
    }

    private FileTree excludeOutputDirectory(FileTree sourceTree) {
        if (sourceTree == null) {
            return null;
        }

        Spec<File> outputDirectoryFilter = new Spec<File>() {

            @Override
            public boolean isSatisfiedBy(File file) {
                return file.getAbsolutePath().contains(getOutputDirectory().getAbsolutePath());
            }
        };

        FileTree fileTree = sourceTree.minus(sourceTree.filter(outputDirectoryFilter)).getAsFileTree();
        return fileTree;
    }
}
