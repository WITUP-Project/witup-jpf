#!/bin/bash
# Copies minimal JPF jars from jpf-core and jpf-symbc into tools/jpf/lib/.
# Run from project root: ./tools/copy-jpf.sh
# Prerequisites: jpf-core and jpf-symbc built (ant) in sibling directories.

set -e
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
LIB_DIR="$PROJECT_ROOT/tools/jpf/lib"
JPF_CORE="${JPF_CORE:-$PROJECT_ROOT/../jpf-core}"
JPF_SYMBC="${JPF_SYMBC:-$PROJECT_ROOT/../jpf-symbc}"

mkdir -p "$LIB_DIR"

echo "Copying JPF jars from $JPF_CORE and $JPF_SYMBC to $LIB_DIR"

# jpf-core (RunJPF is the launcher; jpf.jar has the VM)
cp -v "$JPF_CORE/build/RunJPF.jar" "$LIB_DIR/"
cp -v "$JPF_CORE/build/jpf.jar" "$LIB_DIR/"
cp -v "$JPF_CORE/build/jpf-annotations.jar" "$LIB_DIR/"

# jpf-symbc
cp -v "$JPF_SYMBC/build/jpf-symbc.jar" "$LIB_DIR/"

# Minimal solver libs (numeric + string/Choco)
for jar in choco-1_2_04.jar choco-solver-2.1.1-20100709.142532-2.jar automaton.jar string.jar solver.jar commons-lang-2.4.jar commons-math-1.2.jar; do
  cp -v "$JPF_SYMBC/lib/$jar" "$LIB_DIR/"
done

echo "Done. Jars are in $LIB_DIR"
