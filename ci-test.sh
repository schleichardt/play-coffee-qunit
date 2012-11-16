set -x

alias sbt="$(pwd)/sbt"
cd sbt-plugin && sbt publish-local && cd ../plugin && sbt publish-local && cd ../example && sbt test