package com.vancone.devops.controller;

import com.vancone.cloud.common.model.Response;
import com.vancone.devops.constant.DataType;
import com.vancone.devops.entity.DTO.Project;
import com.vancone.devops.service.basic.ProjectService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Tenton Lien
 */
@Slf4j
@RestController
@RequestMapping("/api/devops/project")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @PostMapping
    public Response create(@RequestBody Project project) {
        projectService.save(project);
        return Response.success();
    }

    @GetMapping("{projectId}")
    public Response findById(@PathVariable String projectId) {
        Project project = projectService.findById(projectId);
        log.info("Retrieve project: {}", project.toString());
        return Response.success(project);
    }

    @GetMapping
    public Response query(@RequestParam(defaultValue = "0") int pageNo,
                            @RequestParam(defaultValue = "5") int pageSize,
                            @RequestParam(defaultValue = "") String search) {
        return Response.success(projectService.query(pageNo, pageSize, search));
    }

    @DeleteMapping("{projectId}")
    public Response delete(@PathVariable String projectId) {
        projectService.delete(projectId);
        return Response.success();
    }

    @PostMapping("import")
    public Response importProject(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Response.fail(1, "File is empty");
        }

        try {
            /*XmlMapper xmlMapper = new XmlMapper();
            xmlMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            Project project = xmlMapper.readValue(new String(file.getBytes()), Project.class);
            log.info("Save imported project: {}", project.toString());
            projectService.saveProject(project);*/
            return Response.success();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.fail(2, "Import failed");
        }
    }

    @GetMapping("download/{fileType}/{projectId}")
    public void downloadFile(HttpServletResponse response, @PathVariable String fileType, @PathVariable String projectId) {
        projectService.export(response, fileType, projectId);
    }

    @GetMapping("dbTypeList")
    public Response retrieveDatabaseTypeList() throws IllegalAccessException {
        // Todo: The list should be cached in Redis to optimize performance
        List<String> dbTypes = new ArrayList<>();
        Field[] fields = DataType.class.getFields();
        for (Field field: fields) {
            if (field.getName().matches("SQL_.*")) {
                dbTypes.add(field.get(DataType.class).toString());
            }
        }
        return Response.success(dbTypes);
    }
}
