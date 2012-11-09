package com.jb;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

public class FolderMonitor {

    public FolderMonitor() {
    }

    @SuppressWarnings({ "unused", "rawtypes" })
    public void start(final String[] sources, String destinationDir) throws IOException, InterruptedException {
        for (final String arg : sources) {
            try {
                File dir = new File(arg);
                File[] files = dir.listFiles();
                TagCleanup.cleanupFolder(dir);
                for (int i = 0; i < files.length; i++) {
                    try {
                        FileUtils.moveFileToDirectory(files[i], new File(destinationDir), true);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }

        final FileSystem fileSystem = FileSystems.getDefault();
        try (final WatchService watchService = fileSystem.newWatchService()) {
            final Map<WatchKey, Path> keyMap = new HashMap<WatchKey, Path>();
            for (final String arg : sources.length > 0 ? sources : new String[] { "." }) {
                final Path path = Paths.get(arg);
                // keyMap.put(path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
                // StandardWatchEventKinds.ENTRY_DELETE), path);
                keyMap.put(path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE), path);
                System.out.println(path + ": begin monitoring");
            }
            WatchKey watchKey;
            do {
                watchKey = watchService.take();
                final Path eventDir = keyMap.get(watchKey);
                for (final WatchEvent<?> event : watchKey.pollEvents()) {
                    final WatchEvent.Kind kind = event.kind();
                    final Path eventPath = (Path) event.context();
                    System.out.println(eventDir + ": " + event.kind() + ": " + event.context());

                    // print it out
                    File sourceFile = new File(eventDir + "" + File.separatorChar + event.context());
                    if (!sourceFile.exists()) {
                        System.out.println(sourceFile.getAbsolutePath() + " does not exist");
                        continue;
                    }
                    TagCleanup.cleanupFile(sourceFile);
                    FileUtils.moveFileToDirectory(sourceFile, new File(destinationDir), true);
                }
            } while (watchKey.reset());
        }
    }

    /**
     * Main program.
     * @param args Command line arguments - dirs to watch.
     * @throws IOException in case of I/O problems.
     * @throws InterruptedException in case the thread was interrupted during watchService.take().
     */
    public static void main(final String[] args) throws IOException, InterruptedException {
        if (args.length < 3 || !StringUtils.equalsIgnoreCase(args[0], "-d")) {
            System.out.println("Usage:  FolderMonitor -d <destination> <dir to monitor 1> [<dir to monitor 2> ...]");
            System.exit(-1);
        }

        FolderMonitor dirWatch = new FolderMonitor();
        dirWatch.start(Arrays.copyOfRange(args, 2, args.length), args[1]);
    }
}
