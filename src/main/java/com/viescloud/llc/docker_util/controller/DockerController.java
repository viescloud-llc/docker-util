package com.viescloud.llc.docker_util.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.viescloud.llc.docker_util.model.docker.DockerPullRequest;
import com.viescloud.llc.docker_util.service.DockerService;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/dockers")
@RequiredArgsConstructor
public class DockerController {

    private final DockerService dockerService;

    @PutMapping
    public ResponseEntity<?> pullAndGetImage(
        @RequestBody DockerPullRequest dockerPullRequest
    ) {
        var file = this.dockerService.pullImageAndPackTar(dockerPullRequest);
        return ResponseEntity.ok().header("Content-Type", "application/x-tar").body(file);
    }
}
