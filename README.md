WatchTask
=========

Ant task that invokes ant targets whenever particular files have been modified.  
This is great for automatically executing tasks whenever certain files are modified.
For example, compiling LESS, SASS, combining and minifying Javascript, etc.  This
task requires a minimum Java version >= 1.7.x.

Example
==========

Here is a simple example of how to execute targets when files change:

    <project name="example1" basedir='.'>
      <property name="some.dir" value="${basedir}/someDir"/>

      <taskdef name="watch"
               classname="org.apache.tools.ant.taskdefs.optional.watch.WatchTask"
               classpath="${basedir}/lib/watch-task-1.0.jar" />

      <target name="run">
          <watch>
              <when>
                  <target>
                    <echo>Something has happened.</echo>
                    <echo>A file has changed! ${watched.file}</echo>
                  </target>
                  <fileset dir="${some.dir}" includes="**/*"/>
              </when>
          </watch>
      </target>
    </project>

This is a very simple example.  You execute it with ant run.  When files from ${some.dir} are 
modified it invokes the target.  A when tag consists of two pieces of data:  what target to 
invoke when files change (ie <target>), and what files you want to watch for changes (ie <fileset>).
In the example above we have a fileset tied to ${some.dir} that we are watching.  When any file
is modified in there the <target> embedded in the <when> tag is executed.

There is a special variable is set called ${watched.file} that contains the file that was modified.
The value of this property changes on each invocation.

There are two ways to specify the target to invoke.  Targets can either be embedded or invoked by
the name of an existing target in the build file.  Here is an example of that:

    <project name="example2" basedir=".">
      <property name="some.dir" value="${basedir}/someDir"/>

      <taskdef name="watch"
               classname="org.apache.tools.ant.taskdefs.optional.watch.WatchTask"
               classpath="${basedir}/lib/watch-task-1.0.jar" />

      <target name="run">
        <watch>
            <when>
                <target name="printMessage"/>
                <fileset dir="${some.dir}" includes="**/*"/>
            </when>
        </watch>
      </target>

      <target name="printMessage">
        <echo>Something has happened.</echo>
        <echo>A file has changed! ${watched.file}</echo>
      </target>
      
    </project>

Now we see the same result of this project as the prior, but we are calling a target by name 
instead of inlining the target inside the when tag.

Lastly, the watch task can be used to watch multiple sets of files with different targets
invoked for each set.  You can specify multiple <when> tags to separate sets of files that
need different targets being invoked.

    <project name="example3" basedir=".">
      <property name="less.dir" value="${basedir}/less"/>
      <property name="js.dir" value="${basedir}/js/>

      <taskdef name="watch"
               classname="org.apache.tools.ant.taskdefs.optional.watch.WatchTask"
               classpath="${basedir}/lib/watch-task-1.0.jar" />

      <target name="run">
        <watch>
            <when>
                <target name="less"/>
                <fileset dir="${less.dir}" includes="**/*"/>
            </when>
            <when>
                <target name="combine"/>
                <fileset dir="${js.dir}" includes="**/*"/>
            </when>
        </watch>
      </target>

      <target name="less">
         <lesscss ..../>
      </target>
      
      <target name="combine">
         <concat ...>
         </concat>
      </target>
      
    </project>

