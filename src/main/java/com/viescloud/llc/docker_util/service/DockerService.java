package com.viescloud.llc.docker_util.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import com.viescloud.eco.viesspringutils.util.FSUtils;
import com.viescloud.eco.viesspringutils.util.Streams;
import com.viescloud.eco.viesspringutils.util.TerminalUtils;
import com.viescloud.llc.docker_util.model.docker.DockerContainer;
import com.viescloud.llc.docker_util.model.docker.DockerPullRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DockerService {
    private boolean isDockerRunning = false;
    private boolean checkIsDockerRunning = false;
    private static final String WORKDIR = "/tmp/docker_image";

    public List<DockerContainer> getContainers() {
        var response = TerminalUtils.executeCommand("docker container list -a --no-trunc --format \"{{.ID}}|{{.Names}}|{{.Image}}|{{.Command}}|{{.Status}}|{{.CreatedAt}}\"");
        
        if(response == null || response.trim().isEmpty()) {
            return new ArrayList<>();
        }

        var lines = response.split("\n");

        return Arrays.stream(lines).map(line -> {
            var splits = line.split("\\|");
            return new DockerContainer(splits[0].trim(), splits[1].trim(), splits[2].trim(), splits[3].trim(), splits[4].trim(), splits[5].trim());
        }).toList();
    }

    public boolean isDockerRunning() {
        if(!this.checkIsDockerRunning) {
            int count = 0;

            while(!isDockerRunning && count < 10) {
                var response = TerminalUtils.executeCommand("docker info | grep version");
                this.isDockerRunning = response.contains("runc version") || response.contains("init version") || response.contains("containerd version");
                count++;

                if(isDockerRunning)
                    break;
                else {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        log.error(ex.getMessage(), ex);
                    }
                }
            }

            this.checkIsDockerRunning = true;
        }

        return this.isDockerRunning;
    }

    public String pullImage(String imageName) {
        return TerminalUtils.executeCommand("docker pull " + imageName);
    }

    public Optional<DockerContainer> getContainerById(String containerId) {
        var id = containerId.trim();
        return Streams.stream(this.getContainers()).filter(e -> e.getId().equals(id)).findFirst();
    }

    public Optional<DockerContainer> getContainerByName(String containerName) {
        return Streams.stream(this.getContainers()).filter(e -> e.getName().equals(containerName)).findFirst();
    }

    public Optional<DockerContainer> getContainer(String idOrName) {
        return this.getContainerById(idOrName).or(() -> this.getContainerByName(idOrName));
    }

    public boolean isContainerRunningById(String containerId) {
        var container = this.getContainerById(containerId);
        return container.isPresent() && container.get().getStatus().contains("Up");
    }

    public boolean isContainerRunningByName(String containerName) {
        var container = this.getContainerByName(containerName);
        return container.isPresent() && container.get().getStatus().contains("Up");
    }

    public boolean isContainerRunning(String idOrName) {
        return this.isContainerRunningById(idOrName) || this.isContainerRunningByName(idOrName);
    }

    public String startContainerById(String containerId) {
        return TerminalUtils.executeCommand("docker start " + containerId);
    }

    public String stopContainerById(String containerId) {
        return TerminalUtils.executeCommand("docker stop " + containerId);
    }

    public String killContainerById(String containerId) {
        return TerminalUtils.executeCommand("docker kill " + containerId);
    }

    public String removeContainerById(String containerId) {
        StringBuilder sb = new StringBuilder();
        sb.append(this.killContainerById(containerId)).append("\n");
        sb.append(TerminalUtils.executeCommand("docker rm " + containerId));
        return sb.toString();
    }

    public String removeContainerByName(String containerName) {
        var container = this.getContainerByName(containerName);
        if(container.isPresent())
            return this.removeContainerById(container.get().getId());
        else
            return "Container not found";
    }

    public String getExecuteCommand(String dockerContainerId, String command) {
        StringBuilder sb = new StringBuilder();
        sb.append("docker exec ").append(dockerContainerId).append(" ").append(command);
        return sb.toString();
    }

    public String executeCommand(String dockerContainerId, String command) {
        return TerminalUtils.executeCommand(this.getExecuteCommand(dockerContainerId, command));
    }

    public String getExecuteCommandWithBash(String dockerContainerId, String command) {
        return this.getExecuteCommand(dockerContainerId, String.format("bash -c \"%s\"", command));
    }

    public String executeCommandWithBash(String dockerContainerId, String command) {
        return executeCommand(dockerContainerId, String.format("bash -c \"%s\"", command));
    }

    public String getExecuteCommandWithSh(String dockerContainerId, String command) {
        return this.getExecuteCommand(dockerContainerId, String.format("sh -c \"%s\"", command));
    }

    public String executeCommandWithSh(String dockerContainerId, String command) {
        return executeCommand(dockerContainerId, String.format("sh -c \"%s\"", command));
    }

    public Optional<String> getExecuteCommandByNameWithBash(String containerName, String command) {
        var container = this.getContainerByName(containerName);
        if(container.isPresent() && this.isContainerRunningById(container.get().getId()))
            return Optional.of(this.getExecuteCommandWithBash(container.get().getId(), command));
        else
            return Optional.empty();
    }

    public Optional<String> executeCommandByNameWithBash(String containerName, String command) {
        var container = this.getContainerByName(containerName);
        if(container.isPresent() && this.isContainerRunningById(container.get().getId()))
            return Optional.of(this.executeCommandWithBash(container.get().getId(), command));
        else
            return Optional.empty();
    }

    public Optional<String> getExecuteCommandByNameWithSh(String containerName, String command) {
        var container = this.getContainerByName(containerName);
        if(container.isPresent() && this.isContainerRunningById(container.get().getId()))
            return Optional.of(this.getExecuteCommandWithSh(container.get().getId(), command));
        else
            return Optional.empty();
    }

    public Optional<String> executeCommandByNameWithSh(String containerName, String command) {
        var container = this.getContainerByName(containerName);
        if(container.isPresent() && this.isContainerRunningById(container.get().getId()))
            return Optional.of(this.executeCommandWithSh(container.get().getId(), command));
        else
            return Optional.empty();
    }

    public Optional<byte[]> pullImageAndPackTar(DockerPullRequest dockerPullRequest) {
        while(dockerPullRequest.getImage().startsWith("/") && dockerPullRequest.getImage().length() > 1) {
            dockerPullRequest.setImage(dockerPullRequest.getImage().substring(1));
        }

        while(dockerPullRequest.getDockerHub().endsWith("/") && dockerPullRequest.getDockerHub().length() > 1) {
            dockerPullRequest.setDockerHub(dockerPullRequest.getDockerHub().substring(0, dockerPullRequest.getDockerHub().length() - 1));
        }

        TerminalUtils.executeCommand("mkdir", "-p", WORKDIR);

        var fullImagePath = String.format("%s/%s:%s", dockerPullRequest.getDockerHub(), dockerPullRequest.getImage(), dockerPullRequest.getTag());
        var filePath = String.format("%s/%s", WORKDIR, fullImagePath.replaceAll("/+", "_").replaceAll(":+", "-"));
        filePath += ".tar";

        if(FSUtils.isFileExist(filePath)) {
            return FSUtils.readFileAsBytes(filePath);
        }

        var liveTerminal = TerminalUtils.newShLiveTerminal();

        if(ObjectUtils.isNotEmpty(dockerPullRequest.getUsername()) && ObjectUtils.isNotEmpty(dockerPullRequest.getPassword())) {
            liveTerminal.runCommandAndWait(Duration.ofMinutes(1), new String[][] {
                {String.format("docker login \"%s\" -u \"%s\"", dockerPullRequest.getDockerHub(), dockerPullRequest.getUsername())},
                {dockerPullRequest.getPassword()}
            });
        }

        liveTerminal.runCommandAndWait(Duration.ofDays(1), new String[][] {
            {String.format("docker pull %s", fullImagePath)},
            {String.format("docker save -o \"%s\" \"%s\"", filePath, fullImagePath)}
        });

        return FSUtils.readFileAsBytes(filePath);
    }
}
