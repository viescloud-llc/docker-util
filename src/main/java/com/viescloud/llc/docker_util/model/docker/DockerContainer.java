package com.viescloud.llc.docker_util.model.docker;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DockerContainer {
    private String id;
    private String name;
    private String image;
    private String command;
    private String status;
    private String created;
}
