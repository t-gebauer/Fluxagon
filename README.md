# Fluxagon

Dies ist der Prototyp eines kleinen 2D Puzzle-Action Games. Erstellt mit der Lightweight Java Game Library (lwjgl.org).

Enstanden im Kurs Mediengestaltung 1 im Studiengang Media Systems an der Hochschule für Angewandte Wissenschaften Hamburg im Sommersemester 2014.

## Menu Grafiken

Ich habe keine Ahnung wo die originalen Grafiken (Icons und Bilder) sind. Daher habe ich ein Platzhalter Bild erstellt, damit die Anwendung zumindest läuft bis ich sie wiederfinde.

## Native Dependencies

- LWJGL 2.x.x (http://legacy.lwjgl.org/download.php.html)

Unter Linux wird zusätzlich noch `libXxf86vm` benötigt und eventuell eine neuere
Version von `libopenal` (siehe `shell.nix`).

Unter Linux müssen die Dependencies unter `$LD_LIBRARY_PATH` vorhanden sein.

Auf Windows Systemen muss möglicherweise der Pfad zu den nativen LWJGL Libraries (`lwjgl/native/windows/`) als Java System Property über `java -Dorg.lwjgl.librarypath="..." -jar Fluxagon.jar` angegeben werden.

## Development

This project can be build with Bazel (https://docs.bazel.build/).

**Building:**  
    `bazel build`

**Running:**  
    `bazel run`  
    (alternatively `bazel-bin/Fluxagon`)  

**Deploying (Fat JAR):**  
    `bazel build //:Fluxagon_deploy.jar`  
    It is then possible to run the JAR file directly:  
    `java -jar bazel-bin/Fluxagon_deploy.jar`
