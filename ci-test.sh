set -x

export xsbt="$(pwd)/sbt -Dsbt.log.noformat=true"
chmod a+x sbt sbtwrapper/sbt-launch.jar
cd sbt-plugin && $xsbt clean test publish-local && cd ../plugin && $xsbt clean test publish-local && cd ../testProject && $xsbt test