package com.viescloud.llc.docker_util.controller;

import java.util.Map;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.viescloud.eco.viesspringutils.exception.HttpResponseThrowers;
import com.viescloud.eco.viesspringutils.util.FSUtils;
import com.viescloud.llc.docker_util.model.docker.DockerPullRequest;
import com.viescloud.llc.docker_util.service.DockerService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/dockers")
@RequiredArgsConstructor
public class DockerController {

    private final DockerService dockerService;

    @GetMapping("engine/ready")
    public ResponseEntity<?> engineReady() {
        return ResponseEntity.ok().body(Map.of("ready", dockerService.isDockerRunning()));
    }

    @GetMapping("image")
    public ResponseEntity<?> getImages(
        @RequestParam(required = false) String filePath
    ) {
        if(ObjectUtils.isEmpty(filePath)) {
            return HttpResponseThrowers.getExceptionResponse(HttpStatus.BAD_REQUEST, "File path is required");
        }

        var fileOpt = FSUtils.readFileAsBytes(filePath);
        var fileName = filePath.substring(filePath.lastIndexOf("/") + 1);

        if(fileOpt.isPresent()) {
            return ResponseEntity.ok()
                                 .contentType(MediaType.APPLICATION_OCTET_STREAM)
                                 .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + ".tar\"")
                                 .contentLength(fileOpt.get().length)
                                 .body(fileOpt.get());
        }
        else {
            return HttpResponseThrowers.getExceptionResponse(HttpStatus.NOT_FOUND, "File not found");
        }   
    }

    @PutMapping("pull")
    public ResponseEntity<?> pullAndGetImage(
        @RequestBody DockerPullRequest dockerPullRequest
    ) {
        if(!this.dockerService.isDockerRunning()) {
            return HttpResponseThrowers.getExceptionResponse(HttpStatus.CONFLICT, "Docker engine in backend is not running");
        }

        if(ObjectUtils.isEmpty(dockerPullRequest.getImage())) {
            return HttpResponseThrowers.getExceptionResponse(HttpStatus.BAD_REQUEST, "Image name is required");
        }

        if(ObjectUtils.isEmpty(dockerPullRequest.getTag())) {
            dockerPullRequest.setTag("latest");
        }

        if(ObjectUtils.isEmpty(dockerPullRequest.getDockerHub())) {
            dockerPullRequest.setDockerHub(DockerPullRequest.DOCKER_HUB);
        }

        var filePath = this.dockerService.pullImageAndPackTar(dockerPullRequest);

        if(filePath.isPresent()) {
            return ResponseEntity.ok().body(Map.of("filePath", filePath.get()));
        }
        else {
            return HttpResponseThrowers.getExceptionResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to pull image");
        }
    }
}
