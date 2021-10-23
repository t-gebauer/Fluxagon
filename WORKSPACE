load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive", "http_jar")

RULES_JVM_EXTERNAL_TAG = "4.0"

http_archive(
    name = "rules_jvm_external",
    sha256 = "31701ad93dbfe544d597dbe62c9a1fdd76d81d8a9150c2bf1ecf928ecdf97169",
    strip_prefix = "rules_jvm_external-%s" % RULES_JVM_EXTERNAL_TAG,
    url = "https://github.com/bazelbuild/rules_jvm_external/archive/%s.zip" % RULES_JVM_EXTERNAL_TAG,
)

load("@rules_jvm_external//:defs.bzl", "maven_install")

maven_install(
    artifacts = [
        "org.lwjgl.lwjgl:lwjgl:2.9.3",
        "org.lwjgl.lwjgl:lwjgl_util:2.9.3",
    ],
    repositories = [
        "https://repo1.maven.org/maven2",
    ],
)

http_jar(
    name = "slick_util",
    sha256 = "c85f3d80b30487e11531df2032579c7ffa3fc3c0c045e9918f24153422fa6baf",
    url = "http://slick.ninjacave.com/slick-util.jar",
)
