set -x

export xsbt="$(pwd)/sbt -Dsbt.log.noformat=true"
chmod a+x sbt sbtwrapper/sbt-launch.jar
cd sbt-plugin && $xsbt clean publish-local && cd ../plugin && $xsbt clean publish-local && cd ../testProject && $xsbt test