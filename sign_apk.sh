NAME="HelloMusic-$VERSION_MAJOR.$VERSION_MINOR.$VERSION_PATCH-build.$VERSION_BUILD-$VERSION_NUMBER"

cd ./apk/

${ANDROID_HOME}/build-tools/26.0.1/zipalign -v -p 4 "$NAME.apk" "$NAME-aligned.apk"

${ANDROID_HOME}/build-tools/26.0.1/apksigner sign --ks HelloMusic_Keystore.jks --ks-pass "pass:$storepass" --key-pass "pass:$keypass" --out "$NAME-release.apk" "$NAME-aligned.apk"

${ANDROID_HOME}/build-tools/26.0.1/apksigner verify "$NAME-release.apk"