# JPF Tools

Bundled JPF (jpf-core + jpf-symbc) for self-contained symbolic execution.

## Requirements

- **Java 8** (JPF uses `sun.misc.SharedSecrets`, removed in Java 9+).
  - **Nix**: `nix develop` provides Java 8, Maven, and Ant (recommended).
  - **SDKMAN**: `.sdkmanrc` and `run-jpf.sh` use `8.0.422-amzn`. Run `sdk env` in the project root.

## Setup

The JPF jars are pre-bundled in `tools/jpf/lib/`; no need to build jpf-core or jpf-symbc.

1. **Compile the project** (includes test classes for JPF drivers):

   ```bash
   mvn test-compile
   ```

2. **Optional** – refresh bundled jars from a local jpf-core/jpf-symbc build:

   ```bash
   cd ../jpf-core && ant && cd ../jpf-symbc && ant
   ./tools/copy-jpf.sh
   ```

   Or via Maven: `mvn compile -Pjpf-bundle` (runs copy before compile).

## Run JPF

Configs live in `src/test/resources/jpf/`. Use `run_jpf.py` for a friendly interface:

```bash
python run_jpf.py -h              # List available configs with descriptions
python run_jpf.py AccountTestSymbolic.jpf
```

Or run directly: `./tools/run-jpf.sh AccountTestSymbolic.jpf`

For **EXCEPTION CONDITIONS** (symbolic path conditions like `value > this.balance`), use the `*Symbolic.jpf` configs. `AccountTest.jpf` (concrete) explores many paths but does not produce symbolic conditions.

## Bundled Jars (minimal set)

- **jpf-core**: RunJPF.jar, jpf.jar, jpf-annotations.jar
- **jpf-symbc**: jpf-symbc.jar
- **String solver** (Choco + automata): choco-1_2_04.jar, choco-solver-2.1.1-20100709.142532-2.jar, automaton.jar, string.jar, solver.jar, commons-lang-2.4.jar, commons-math-1.2.jar

## Nix (reproducible environment)

```bash
nix develop
# then: mvn test-compile && ./tools/run-jpf.sh AccountTestSymbolic.jpf
```

Works on macOS and Linux. The flake provides Java 8, Maven, and Ant.

## Optional: Native libs

For string symbolic execution, native libs may be needed. The run script adds `../jpf-symbc/lib` to `DYLD_LIBRARY_PATH` (macOS) if present.
