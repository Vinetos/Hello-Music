NAME="HelloMusic-$VERSION_MAJOR.$VERSION_MINOR.$VERSION_PATCH-build.$VERSION_BUILD-$VERSION_NUMBER"

zipalign -v -p 4 "$NAME.apk" "$NAME-aligned.apk"

apksigner sign --ks HelloMusic_Keystore.jks --out "$NAME-release.apk" "$NAME-aligned.apk"

apksigner verify "$NAME-release.apk"