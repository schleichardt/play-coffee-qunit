set -x

export xsbt="$(pwd)/sbt -Dsbt.log.noformat=true"
chmod a+x sbt sbtwrapper/sbt-launch.jar
cd sbt-plugin && $xsbt publish-local && cd ../plugin && $xsbt publish-local && cd ../example && $xsbt test