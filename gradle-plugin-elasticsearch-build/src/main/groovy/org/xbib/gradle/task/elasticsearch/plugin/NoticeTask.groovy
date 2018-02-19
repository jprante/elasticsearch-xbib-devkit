package org.xbib.gradle.task.elasticsearch.plugin

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

/**
 * A task to create a notice file which includes dependencies' notices.
 */
class NoticeTask extends DefaultTask {

    @InputFile
    File inputFile = project.rootProject.file('NOTICE.txt')

    @OutputFile
    File outputFile = new File(project.buildDir, "notices/${name}/NOTICE.txt")

    /** Directories to include notices from */
    private List<File> licensesDirs = new ArrayList<>()

    NoticeTask() {
        description = 'Create a notice file from dependencies'
        // Default licenses directory is ${projectDir}/licenses (if it exists)
        File licensesDir = new File(project.projectDir, 'licenses')
        if (licensesDir.exists()) {
            licensesDirs.add(licensesDir)
        }
    }

    /** Add notices from the specified directory. */
    void licensesDir(File licensesDir) {
        licensesDirs.add(licensesDir)
    }

    @TaskAction
    void generateNotice() {
        StringBuilder output = new StringBuilder()
        output.append(inputFile.getText('UTF-8'))
        output.append('\n\n')
        // This is a map rather than a set so that the sort order is the 3rd
        // party component names, unaffected by the full path to the various files
        Map<String, File> seen = new TreeMap<>()
        for (File licensesDir : licensesDirs) {
            licensesDir.eachFileMatch({ it ==~ /.*-NOTICE\.txt/ }) { File file ->
                String name = file.name.substring(0, file.name.length() - '-NOTICE.txt'.length())
                if (seen.containsKey(name)) {
                    File prevFile = seen.get(name)
                    if (prevFile.text != file.text) {
                        throw new RuntimeException("Two different notices exist for dependency '" +
                                name + "': " + prevFile + " and " + file)
                    }
                } else {
                    seen.put(name, file)
                }
            }
        }
        for (Map.Entry<String, File> entry : seen.entrySet()) {
            String name = entry.getKey()
            File file = entry.getValue()
            appendFile(file, name, 'NOTICE', output)
            appendFile(new File(file.parentFile, "${name}-LICENSE.txt"), name, 'LICENSE', output)
        }
        outputFile.setText(output.toString(), 'UTF-8')
    }

    static void appendFile(File file, String name, String type, StringBuilder output) {
        String text = file.getText('UTF-8')
        if (text.trim().isEmpty()) {
            return
        }
        output.append('================================================================================\n')
        output.append("${name} ${type}\n")
        output.append('================================================================================\n')
        output.append(text)
        output.append('\n\n')
    }
}
