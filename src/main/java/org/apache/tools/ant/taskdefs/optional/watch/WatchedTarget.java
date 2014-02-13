package org.apache.tools.ant.taskdefs.optional.watch;

import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.types.FileSet;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.nio.file.StandardWatchEventKinds.*;

public class WatchedTarget {
    private List<FileSet> filesets;
    private Target target;

    private Map<WatchKey,Path> watchedPaths;

    public void addFileset(FileSet fs) {
        if( filesets == null ) filesets = new ArrayList<FileSet>();
        filesets.add(fs);
    }

    public void addTarget(Target target) {
        this.target = target;
    }

    public void execute(Project project, WatchEvent<Path> event) {
        if( target.getName() != null ) {
            project.setInheritedProperty("watched.file", event.context().toFile().getAbsolutePath() );
            project.executeTarget( target.getName() );
        } else {
            project.setInheritedProperty("watched.file", event.context().toFile().getAbsolutePath() );
            target.execute();
        }
    }

    public void startWatching(Project project, WatchService watchService) throws IOException {
        watchedPaths = new HashMap<WatchKey, Path>();

        for( FileSet fileset : filesets ) {
            File dir = fileset.getDir();
            DirectoryScanner scanner = fileset.getDirectoryScanner(project);
            String[] files = scanner.getIncludedDirectories();
            for(String d : files ) {
                System.out.println("Watching: " + d);
                Path path = Paths.get( dir.getAbsolutePath(), d );
                watchedPaths.put( path.register(watchService, ENTRY_DELETE, ENTRY_MODIFY, ENTRY_CREATE ), path );
            }

            if( dir != null ) {
                Path path = Paths.get(dir.getAbsolutePath());
                watchedPaths.put( path.register(watchService, ENTRY_DELETE, ENTRY_MODIFY, ENTRY_CREATE ), path );
            }
        }
    }

    public boolean watching( WatchKey key ) {
        return watchedPaths.containsKey(key);
    }

    public void stopWatching(WatchService watcher) {
        for( WatchKey key : watchedPaths.keySet() ) {
            key.cancel();
        }
    }

    public void addWatch( Path path, WatchService watchService ) throws IOException {
        watchedPaths.put( path.register(watchService, ENTRY_DELETE, ENTRY_MODIFY, ENTRY_CREATE ), path );
    }

    public void removeWatch( WatchKey key ) {
        watchedPaths.remove( key );
    }

    public Path resolve(WatchKey key, Path context) {
        return watchedPaths.containsKey(key) ? watchedPaths.get( key ).resolve( context ) : null;
    }
}
