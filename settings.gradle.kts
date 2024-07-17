pluginManagement {
	repositories {
		google {
			content {
				includeGroupByRegex("com\\.android.*")
				includeGroupByRegex("com\\.google.*")
				includeGroupByRegex("androidx.*")
			}
		}
		mavenCentral()
		gradlePluginPortal()
	}
}
dependencyResolutionManagement {
	repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
	repositories {
		// JitPack仓库
		maven { url = uri("https://jitpack.io") }
		// 阿里云仓库
		maven { url = uri("https://maven.aliyun.com/repository/releases") }
		maven { url = uri("https://maven.aliyun.com/repository/google") }
		maven { url = uri("https://maven.aliyun.com/repository/central") }
		maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
		maven { url = uri("https://maven.aliyun.com/repository/public") }
		//官方仓库
		google()
		mavenCentral()
	}
}

rootProject.name = "Video"
include(":app")
 