import org.gradle.plugins.ide.idea.model.IdeaLanguageLevel

plugins {
    idea
}

allprojects {
    group = "com.github.wakingrufus"
    version = "0.0.2-alpha"
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
