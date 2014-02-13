package org.apache.tools.ant.taskdefs.optional.watch;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.nio.file.StandardWatchEventKinds.*;

public class WatchTask extends Task {

    List<WatchedTarget> targets;

    WatchService watcher;

    private class ShutdownTask implements Runnable {
        Thread shutdownThread;
        Thread antThread;

        private ShutdownTask(Thread antThread) {
            this.antThread = antThread;
            this.shutdownThread = new Thread(this);
            Runtime.getRuntime().addShutdownHook( shutdownThread );
        }

        public void run() {
            try {
                log("Shut down received...");
                antThread.interrupt();
                synchronized ( this ) {
                    this.wait();
                }
                log("done");
            } catch (InterruptedException e) {
                log("Shutting down because interrupted");
            }
        }
    }

    @Override
    public void execute() throws BuildException {
        final ShutdownTask shutdown = new ShutdownTask(Thread.currentThread());
        try {
            watcher = FileSystems.getDefault().newWatchService();

            for( WatchedTarget watch : targets ) {
                watch.startWatching( getProject(), watcher );
            }

            while(true) {
                WatchKey key = watcher.take();
                for( WatchEvent<?> event : key.pollEvents() ) {
                    WatchEvent.Kind kind = event.kind();
                    if( kind != OVERFLOW ) {
                        for( WatchedTarget target : targets ) {
                            if( target.watching( key ) ) {
                                Path pathToEvent = target.resolve( key, (Path)event.context() );
                                if( Files.isDirectory(pathToEvent, LinkOption.NOFOLLOW_LINKS)) {
                                    if( event.kind() == ENTRY_CREATE ) {
                                        log( "Start watching: " + pathToEvent );
                                        target.addWatch( pathToEvent, watcher );
                                    } else if( event.kind() == ENTRY_DELETE ) {
                                        log( "Stop watching: " + pathToEvent );
                                        target.removeWatch( key );
                                    }
                                }
                                target.execute( getProject(), (WatchEvent<Path>)event );
                            }
                        }
                    }
                }
                if( !key.reset() ) {
                    log( key.toString() + " could not be reset!");
                }
            }
        } catch (IOException e) {
            throw new BuildException("IO Exception", e);
        } catch( InterruptedException e ) {
            // todo shutting down
        } finally {
            for( WatchedTarget watch : targets ) {
                watch.stopWatching(watcher);
            }
            synchronized (shutdown) {
                shutdown.notifyAll();
            }
        }
    }

    public void addWhen( WatchedTarget watching ) {
        if( targets == null ) targets = new ArrayList<WatchedTarget>();
        targets.add( watching );
    }
}
