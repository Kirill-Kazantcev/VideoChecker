plugins {
	id("java")
}

group = "com.autotest"
version = "1.0-SNAPSHOT"

java {
	sourceCompatibility = JavaVersion.VERSION_21
	targetCompatibility = JavaVersion.VERSION_21
}

repositories {
	mavenCentral()
}

dependencies {
	testImplementation("org.seleniumhq.selenium:selenium-java:4.20.0")
	testImplementation("io.github.bonigarcia:webdrivermanager:5.9.2")
	testImplementation("org.testng:testng:7.10.2")
	testImplementation("commons-io:commons-io:2.16.1")
	testImplementation("org.slf4j:slf4j-simple:2.0.13")
}

// Настройка кодировки для компиляции
tasks.withType<JavaCompile> {
	options.encoding = "UTF-8"
	options.release.set(21)
}

// Настройка кодировки для тестов
tasks.withType<Test> {
	systemProperty("file.encoding", "UTF-8")
}

// Настройка кодировки для Java процессов
tasks.withType<JavaExec> {
	systemProperty("file.encoding", "UTF-8")
}

tasks.test {
	useTestNG()
	testLogging {
		events("PASSED", "SKIPPED", "FAILED")
		exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
		showStandardStreams = true
	}
	systemProperty("file.encoding", "UTF-8")
}

tasks.register("testVkScenario1", Test::class) {
	useTestNG()
	filter { includeTestsMatching("com.vk.tests.VkScenario1Test") }
	systemProperty("file.encoding", "UTF-8")
	doFirst { println("VK - Сценарий 1") }
}

tasks.register("testVkScenario2", Test::class) {
	useTestNG()
	filter { includeTestsMatching("com.vk.tests.VkScenario2Test") }
	systemProperty("CI", "true")
	systemProperty("file.encoding", "UTF-8")
	doFirst { println("VK - Сценарий 2 (CI)") }
}