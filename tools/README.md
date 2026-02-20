# JPF Tools

Bundled JPF (jpf-core + jpf-symbc) for self-contained symbolic execution.

## Requirements

- **Java 8** (JPF uses `sun.misc.SharedSecrets`, removed in Java 9+).
  - **Nix**: `nix develop` provides Java 8, Maven, and Ant (recommended).
  - **SDKMAN**: `.sdkmanrc` and `run-jpf.sh` use `8.0.422-amzn`. Run `sdk env` in the project root.

## Setup

1. **Build jpf-core and jpf-symbc** (in sibling directories):

   ```bash
   cd ../jpf-core && ant && cd ../jpf-symbc && ant
   ```

2. **Copy jars** into `tools/jpf/lib/`:

   ```bash
   ./tools/copy-jpf.sh
   ```

   Or via Maven (runs copy before compile):

   ```bash
   mvn compile -Pjpf-bundle
   ```

3. **Compile the project**:

   ```bash
   mvn compile
   ```

## Run JPF

```bash
./tools/run-jpf.sh AccountTestSymbolic.jpf
./tools/run-jpf.sh MathTestSymbolic.jpf
./tools/run-jpf.sh StringUtilsLengthTestSymbolic.jpf
```

## Bundled Jars (minimal set)

- **jpf-core**: RunJPF.jar, jpf.jar, jpf-annotations.jar
- **jpf-symbc**: jpf-symbc.jar
- **String solver** (Choco + automata): choco-1_2_04.jar, choco-solver-2.1.1-20100709.142532-2.jar, automaton.jar, string.jar, solver.jar, commons-lang-2.4.jar, commons-math-1.2.jar

## Nix (reproducible environment)

```bash
nix develop
# then: ./tools/copy-jpf.sh && mvn compile && ./tools/run-jpf.sh AccountTestSymbolic.jpf
```

Works on macOS and Linux. The flake provides Java 8, Maven, and Ant.

## Optional: Native libs

For string symbolic execution, native libs may be needed. The run script adds `../jpf-symbc/lib` to `DYLD_LIBRARY_PATH` (macOS) if present.
