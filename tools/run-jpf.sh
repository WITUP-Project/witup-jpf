#!/bin/bash
# Runs JPF using bundled jars in tools/jpf/lib/.
# Usage: ./tools/run-jpf.sh <config.jpf> [JPF options...]
# Examples (configs in src/test/resources/jpf/):
#   ./tools/run-jpf.sh AccountTestSymbolic.jpf   # symbolic → EXCEPTION CONDITIONS
#   ./tools/run-jpf.sh AccountTest.jpf          # concrete  → no path conditions
#   ./tools/run-jpf.sh --verbose AccountTestSymbolic.jpf   # full JPF output

set -e
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
LIB_DIR="$PROJECT_ROOT/tools/jpf/lib"

if [ ! -f "$LIB_DIR/jpf.jar" ]; then
  echo "JPF jars not found. Run: ./tools/copy-jpf.sh"
  exit 1
fi

# Build classpath: RunJPF launcher + jpf + symbc + solver libs + project classes + gson
CP="$LIB_DIR/RunJPF.jar"
CP="$CP:$LIB_DIR/jpf.jar"
CP="$CP:$LIB_DIR/jpf-annotations.jar"
CP="$CP:$LIB_DIR/jpf-symbc.jar"
CP="$CP:$LIB_DIR/choco-1_2_04.jar"
CP="$CP:$LIB_DIR/choco-solver-2.1.1-20100709.142532-2.jar"
CP="$CP:$LIB_DIR/automaton.jar"
CP="$CP:$LIB_DIR/string.jar"
CP="$CP:$LIB_DIR/solver.jar"
CP="$CP:$LIB_DIR/commons-lang-2.4.jar"
CP="$CP:$LIB_DIR/commons-math-1.2.jar"
CP="$CP:$PROJECT_ROOT/target/classes"
CP="$CP:$PROJECT_ROOT/target/test-classes"
CP="$CP:${HOME}/.m2/repository/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar"

# Use Java 8 (JPF requires it). Prefer JAVA_HOME from nix develop; else SDKMAN if available
if [ -z "$JAVA_HOME" ]; then
  SDK_JAVA8="$HOME/.sdkman/candidates/java/8.0.422-amzn"
  if [ -d "$SDK_JAVA8" ]; then
    export JAVA_HOME="$SDK_JAVA8"
    export PATH="$JAVA_HOME/bin:$PATH"
  fi
fi

# Native libs for Choco (macOS)
if [ -d "$PROJECT_ROOT/../jpf-symbc/lib" ]; then
  export DYLD_LIBRARY_PATH="${DYLD_LIBRARY_PATH:+$DYLD_LIBRARY_PATH:}$PROJECT_ROOT/../jpf-symbc/lib"
  export LD_LIBRARY_PATH="${LD_LIBRARY_PATH:+$LD_LIBRARY_PATH:}$PROJECT_ROOT/../jpf-symbc/lib"
fi

cd "$PROJECT_ROOT"

# Strip --verbose / -V first (show full output; default is quiet = EXCEPTION CONDITIONS only)
VERBOSE=0
NEW_ARGS=()
for a in "$@"; do
  case "$a" in
    --verbose|-V) VERBOSE=1 ;;
    *) NEW_ARGS+=("$a") ;;
  esac
done
set -- "${NEW_ARGS[@]}"

# Resolve config path: bare filename → src/test/resources/jpf/<name>
CONFIG="$1"
if [ -n "$CONFIG" ] && [ "${CONFIG#*/*}" = "$CONFIG" ]; then
  JPF_DIR="src/test/resources/jpf"
  if [ -f "$JPF_DIR/$CONFIG" ]; then
    set -- "$JPF_DIR/$CONFIG" "${@:2}"
  fi
fi

# Run JPF; filter output to EXCEPTION CONDITIONS block unless --verbose
if [ "$VERBOSE" = 1 ]; then
  exec java -Xmx1024m -cp "$CP" gov.nasa.jpf.tool.RunJPF "$@"
else
  java -Xmx1024m -cp "$CP" gov.nasa.jpf.tool.RunJPF "$@" 2>&1 | awk '
    /^=+$/ { if (inblock) { print; inblock=0 }; prev=$0; next }
    /EXCEPTION CONDITIONS \(symbolic path conditions\)/ { inblock=1; print prev; print; next }
    inblock { print }
    { prev=$0 }
  '
  exit "${PIPESTATUS[0]}"
fi
