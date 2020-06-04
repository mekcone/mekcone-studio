package com.mekcone.excrud.codegen;

import com.mekcone.excrud.codegen.constant.ApplicationParameter;
import com.mekcone.excrud.codegen.controller.ProjectLoader;
import com.mekcone.excrud.codegen.controller.packager.SpringBootPackager;
import com.mekcone.excrud.codegen.model.project.Project;
import com.mekcone.excrud.codegen.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.File;

@Slf4j
public class PackageTest {

    private final String examplePath = ApplicationParameter.EXCRUD_HOME + "examples" + File.separator;

    @Test
    void packageSpringBootModuleOfMall() {

        String mallPath = examplePath + "mall" + File.separator + "excrud.xml";
        String projectContent = FileUtil.read(mallPath);
        if (projectContent != null) {
            ProjectLoader projectLoader = new ProjectLoader();
            projectLoader.load(projectContent);
            packageSpringBootModule(projectLoader.getProject());
        } else {
            log.error("Failed to package mall project");
        }
    }

    void packageSpringBootModule(Project project) {
        String groupId = project.getGroupId();
        String artifactId = project.getArtifactId();
        String version = project.getVersion();
        SpringBootPackager springBootPackager = new SpringBootPackager(groupId, artifactId, version);
        springBootPackager.build();
    }
}
