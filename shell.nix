{ pkgs ? import <nixpkgs> { } }:

let
  # http://legacy.lwjgl.org/download.php.html
  lwjgl = pkgs.fetchzip {
    sha256 = "08jb8fg7jhmblgv9c92d0yx7hv6czmqy5yij9f52a34rzf1p5c47";
    url = "https://kumisystems.dl.sourceforge.net/project/java-game-lib/Official%20Releases/LWJGL%202.9.3/lwjgl-2.9.3.zip";
  };
  libraries = pkgs.runCommandLocal "fluxagon-deps" { } ''
    mkdir $out
    cp -t $out ${pkgs.xorg.libXxf86vm}/lib/libXxf86vm.so.1
    cp -t $out ${lwjgl}/native/linux/*.so
    # replace OpenAL with newer version
    rm $out/libopenal*.so
    cp -t $out ${pkgs.openal}/lib/libopenal*
  '';
in
pkgs.mkShell {
  buildInputs = with pkgs; [
    bazel
    bazel-buildtools
    openjdk8
  ];

  LD_LIBRARY_PATH = "${libraries}";
}

