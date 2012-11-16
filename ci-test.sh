set -x

export xsbt="$(pwd)/sbt"
chmod a+x sbt sbtwrapper/sbt-launch.jar
cd sbt-plugin && $xsbt publish-local && cd ../plugin && $xsbt publish-local && cd ../example && $xsbt test