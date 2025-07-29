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
    private String image;
    private String tag;
    private String username;
    private String password;

    @Builder.Default
    private String dockerHub = "docker.io";
}
