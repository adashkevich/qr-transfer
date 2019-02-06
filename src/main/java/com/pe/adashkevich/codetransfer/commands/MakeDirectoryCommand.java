package com.pe.adashkevich.codetransfer.commands;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.StringTokenizer;

public class MakeDirectoryCommand implements Command {

    private String folderPath;

    private MakeDirectoryCommand() {

    }

    public MakeDirectoryCommand(String qrCodeData) {
        String[] commandCfg = qrCodeData.split("|");
        folderPath = commandCfg[2];
    }

    private void createFolder(Path entryPoint) {
        Path newPath = Paths.get(entryPoint.toString(), folderPath);
        newPath.toFile().mkdir();
    }

    @Override
    public String toString() {
        return "#cmd|MD|" + folderPath;
    }

    @Override
    public void exec() {

    }

    public static final class Builder {
        private String folderPath;

        private Builder() {
        }

        public static Builder getBuilder() {
            return new Builder();
        }

        public Builder folderPath(String folderPath) {
            this.folderPath = folderPath;
            return this;
        }

        public MakeDirectoryCommand build() {
            MakeDirectoryCommand makeDirectoryCommand = new MakeDirectoryCommand();
            makeDirectoryCommand.folderPath = this.folderPath;
            return makeDirectoryCommand;
        }
    }
}
