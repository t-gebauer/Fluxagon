java_binary(
    name = "Fluxagon",
    srcs = glob([
        "src/**/*.java",
    ]),
    main_class = "fluxagon.Fluxagon",
    resources = glob([
        "sounds/*.wav",
        "gfx/**/*.png",
    ]),
    deps = [
        "@maven//:org_lwjgl_lwjgl_lwjgl",
        "@maven//:org_lwjgl_lwjgl_lwjgl_util",
        "@slick_util//jar",
    ],
)

