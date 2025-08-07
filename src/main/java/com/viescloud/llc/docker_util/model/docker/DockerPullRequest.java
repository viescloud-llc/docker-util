package com.viescloud.llc.docker_util.model.docker;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DockerPullRequest {
    public static final String DOCKER_HUB = "docker.io";

    private String image;
    private String tag;
    private String username;
    private String password;

    @Builder.Default
    private String dockerHub = DOCKER_HUB;
}
