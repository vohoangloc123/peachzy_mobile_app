
plugins {
    alias(libs.plugins.androidApplication)
    id("com.google.gms.google-services") version "4.3.8"
}

android {
    namespace = "com.example.peachzyapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.peachzyapp"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    packagingOptions {
        exclude("META-INF/LICENSE.md")
        exclude("META-INF/NOTICE.md")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
//    implementation("com.google.guava:guava:20.0") {
//        exclude(group = "com.google.guava", module = "listenablefuture")
//    }
//        implementation("com.amazonaws:aws-android-sdk-core:2.75.0") {
//        exclude(module = "aws-java-sdk-core")
//    }
    implementation("com.google.guava:guava:30.1-jre")
    //Firebase Core SDK cung cấp các công cụ cần thiết để khởi tạo và
    // quản lý kết nối giữa ứng dụng của bạn và các dịch vụ Firebase khác.
    implementation("com.google.firebase:firebase-core:19.0.1")
    implementation("com.google.firebase:firebase-auth:21.0.1")
    //để gửi email
    implementation("com.sun.mail:android-mail:1.6.7")
    implementation("com.sun.mail:android-activation:1.6.7")
    //web socket
    implementation("com.squareup.okhttp3:okhttp:4.9.1")

    //dynamoDB
    implementation("com.amazonaws:aws-android-sdk-core:2.75.0")
    implementation("com.amazonaws:aws-android-sdk-ddb:2.75.0")
    implementation("com.amazonaws:aws-android-sdk-ddb-document:2.4.5")
    implementation("com.amazonaws:aws-android-sdk-s3:2.75.0")
    //load ảnh
    implementation("com.squareup.picasso:picasso:2.71828")
    //s3
    implementation("com.amazonaws:aws-android-sdk-s3:2.75.0")
    //socket.io
    implementation("io.socket:socket.io-client:1.0.0")

    //
    //crop avatar
    implementation ("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.12.0")
    
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}