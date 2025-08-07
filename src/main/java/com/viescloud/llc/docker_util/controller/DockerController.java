package com.viescloud.llc.docker_util.controller;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.viescloud.eco.viesspringutils.exception.HttpResponseThrowers;
import com.viescloud.llc.docker_util.model.docker.DockerPullRequest;
import com.viescloud.llc.docker_util.service.DockerService;

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
        if(ObjectUtils.isEmpty(dockerPullRequest.getImage())) {
            return HttpResponseThrowers.getExceptionResponse(HttpStatus.BAD_REQUEST, "Image name is required");
        }

        if(ObjectUtils.isEmpty(dockerPullRequest.getTag())) {
            dockerPullRequest.setTag("latest");
        }

        if(ObjectUtils.isEmpty(dockerPullRequest.getDockerHub())) {
            dockerPullRequest.setDockerHub(DockerPullRequest.DOCKER_HUB);
        }

        var file = this.dockerService.pullImageAndPackTar(dockerPullRequest);

        if(file.isPresent()) {
            return ResponseEntity.ok().header("Content-Type", "application/x-tar").body(file.get());
        }
        else {
            return HttpResponseThrowers.getExceptionResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to pull image");
        }
    }
}
