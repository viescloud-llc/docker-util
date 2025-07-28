package com.viescloud.llc.docker_util.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DockerPullRequest {
    private String dockerHubUrl;
    private String dockerRegistryUrl;
}
