import org.gradle.plugins.ide.idea.model.IdeaLanguageLevel

plugins {
    idea
}

allprojects {
    group = "com.github.wakingrufus"
}

tasks.getByName<Wrapper>("wrapper") {
    gradleVersion = "5.4.1"
    distributionType = Wrapper.DistributionType.ALL
}

idea {
    project {
        languageLevel = IdeaLanguageLevel("1.8")
    }
}