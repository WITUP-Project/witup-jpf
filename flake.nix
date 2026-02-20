{
  description = "witup-jpf: JPF extension for finding exception conditions via symbolic execution";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-24.05";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs = { self, nixpkgs, flake-utils }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = nixpkgs.legacyPackages.${system};

        # Java 8 required for JPF (sun.misc.SharedSecrets removed in Java 9+)
        jdk8 = pkgs.jdk8;

        # Maven configured to use Java 8
        maven = pkgs.maven.override { jdk = jdk8; };
      in
      {
        devShells.default = pkgs.mkShell {
          buildInputs = [
            jdk8
            maven
            pkgs.ant
          ];

          # Ensure Java 8 is used (for JPF and Maven)
          JAVA_HOME = "${jdk8}";

          shellHook = ''
            echo "witup-jpf dev shell"
            echo "  Java: $(java -version 2>&1 | head -1)"
            echo "  Maven: $(mvn -v | head -1)"
            echo ""
            echo "Setup: ./tools/copy-jpf.sh  (requires jpf-core, jpf-symbc in ../)"
            echo "Run:   ./tools/run-jpf.sh AccountTestSymbolic.jpf"
          '';
        };
      }
    );
}
