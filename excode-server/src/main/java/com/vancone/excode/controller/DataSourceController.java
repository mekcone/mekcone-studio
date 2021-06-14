package com.vancone.excode.controller;

import com.vancone.cloud.common.model.Response;
import com.vancone.excode.entity.DTO.data.source.DataSource;
import com.vancone.excode.service.basic.DataSourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author Tenton Lien
 * @date 6/13/2021
 */
@RestController
@RequestMapping("/api/excode/data-source")
public class DataSourceController {

    @Autowired
    private DataSourceService dataSourceService;

    @PostMapping
    public Response create(@RequestBody DataSource dataSource) {
        dataSourceService.save(dataSource);
        return Response.success();
    }

    @GetMapping("{dataSourceId}")
    public Response query(@PathVariable String dataSourceId) {
        return Response.success(dataSourceService.query(dataSourceId));
    }

    @GetMapping
    public Response queryList(@RequestParam(defaultValue = "0") int pageNo,
                              @RequestParam(defaultValue = "5") int pageSize,
                              @RequestParam(defaultValue = "") String search) {
        return Response.success(dataSourceService.queryPage(pageNo, pageSize, search));
    }

    @GetMapping("test/{dataSourceId}")
    public Response testConnection(@PathVariable String dataSourceId) {
        dataSourceService.testConnection(dataSourceId);
        return Response.success("Test data source connection success", null);
    }
}
