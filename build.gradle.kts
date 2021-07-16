import org.gradle.plugins.ide.idea.model.IdeaLanguageLevel

plugins {
    idea
}

allprojects {
    group = "com.github.wakingrufus"
    version = if (System.getenv("res_kalibrate_release_isGitTag") == "true")
        System.getenv("res_kalibrate_release_gitTagName") else "0.0.3-SNAPSHOT"
}

tasks.getByName<Wrapper>("wrapper") {
    gradleVersion = "6.8.3"
    distributionType = Wrapper.DistributionType.ALL
}

idea {
    project {
        languageLevel = IdeaLanguageLevel("1.8")
    }
}
