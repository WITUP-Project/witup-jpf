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
            # Colors (use ''$ to escape dollar for Nix)
            _cyan='\033[0;36m'
            _green='\033[0;32m'
            _dim='\033[0;2m'
            _reset='\033[0m'

            echo ""
            echo -e "''${_cyan}╭─────────────────────────────────────────────────────────╮''${_reset}"
            echo -e "''${_cyan}│''${_reset}  ''${_green}witup-jpf''${_reset}  JPF symbolic execution (exception conditions)  ''${_cyan}│''${_reset}"
            echo -e "''${_cyan}╰─────────────────────────────────────────────────────────╯''${_reset}"
            echo ""
            echo -e "  ''${_dim}Java:''${_reset}  $(java -version 2>&1 | head -1)"
            echo -e "  ''${_dim}Maven:''${_reset} $(mvn -v 2>/dev/null | head -1 | sed 's/^  //')"
            echo ""
            echo -e "  ''${_dim}Setup:''${_reset}   mvn compile  (jars pre-bundled in tools/jpf/lib/)"
            echo -e "  ''${_dim}Run:''${_reset}    ./tools/run-jpf.sh AccountTestSymbolic.jpf   # for EXCEPTION CONDITIONS"
            echo -e "  ''${_dim}Alias:''${_reset}  jpf <config>  →  ./tools/run-jpf.sh <config>"
            echo ""

            # Prompt prefix to show we're in the dev shell
            export PS1="\[''${_cyan}\][witup-jpf]\[''${_reset}\] $PS1"

            # Convenience alias
            alias jpf='./tools/run-jpf.sh'
          '';
        };
      }
    );
}
